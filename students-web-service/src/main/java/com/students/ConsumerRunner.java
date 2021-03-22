package com.students;

import org.springframework.boot.CommandLineRunner;

import com.students.config.DeserializerErrorHandler;
import com.students.config.StudentPartitioner;
import com.students.model.GradeStore;
import com.students.model.Student;
import com.students.restore.LoggingStateRestoreListener;
import com.students.serde.GradeStoreSerde;
import com.students.serde.StudentSerde;
import com.students.web.InteractiveQueryServer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Repartitioned;
import org.apache.kafka.streams.state.HostInfo;
import org.apache.kafka.streams.state.KeyValueBytesStoreSupplier;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.Stores;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static com.students.model.StudentsConstants.*;

import java.util.Properties;

public class ConsumerRunner implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(ConsumerRunner.class);

	public void run(String... args) throws Exception {

// INITIALISATION PROPERTIES & APPLICATION CONFIGURATION ///////////////////////////		
		// prepare data for InteractiveQueryServer
        if(args.length > 0 && !tryParseInt(args[0])){
            LOG.error("Port value must be a number");
            System.exit(1);
        }
		int port = args.length > 0 ? Integer.parseInt(args[0]) : PORT;
		final HostInfo hostInfo = new HostInfo(HOST, port);

        // configuring app.mode: "application" or "docker"
		String mode = args.length > 1 ? args[1] : DOCKER_MODE;
		if(!mode.equals(APP_MODE) && !mode.equals(DOCKER_MODE) ) {
            LOG.error("Application mode must be defined as {} or {} ", APP_MODE, DOCKER_MODE);
            System.exit(1);			
		}
		LOG.info("### Application mode is {}", mode); 

		// configuring consumer and queryServer
		Properties properties = initProperties(mode);
		properties.put(StreamsConfig.APPLICATION_SERVER_CONFIG, HOST + ":" + port);		
		
        // prepare objects for source stream
        Serde<Integer> integerSerde = Serdes.Integer();
        Serde<Student> studentSerde = StudentSerde.StudentsSerde();
        
        StreamsBuilder streamBuilder = new StreamsBuilder();

// CREATE AND PROCESS DATA STREAM //////////////////////////////////////////////////      
        // source stream with record key
        KStream<Integer, Student> studentsStream = streamBuilder.stream(STUDENTS_TOPIC, Consumed.with(integerSerde, studentSerde));
        
        KeyValueMapper<Integer, Student, Integer> mapper = (key, student) -> student.getId();
        
        KStream<Integer, Student> studentsKeyStream = studentsStream.selectKey(mapper);
//        		.peek((k, v) -> System.out.printf("=== KEY STUDENT === key = %4d, grade = %4d\n", k, v.getGrade()));

        // preparing objects for re-partitioning of source key-stream
        StudentPartitioner studentPartitioner = new StudentPartitioner();        
        
		Repartitioned<Integer, Student> repartitioned = Repartitioned
		.with(integerSerde, studentSerde).withNumberOfPartitions(PARTITION_COUNT)
		.withName(INNER_TOPIC).withStreamPartitioner(studentPartitioner);
        
		// create partitioned stream
		KStream<Integer, Student> partStudentStream = studentsKeyStream.repartition(repartitioned);
//				.peek((k, v) -> System.out.printf("||| STUDENT ||| id = %4d   grade = %4d\n", k, v.getGrade()));
		
		// preparing objects for aggregation of the partitioned stream
		// in local inMemory KeyValueStore instead locking rocksdb store
		Serde<GradeStore> storeSerde = GradeStoreSerde.GradeStoresSerde();
		
		KeyValueBytesStoreSupplier storeSupplier = Stores.inMemoryKeyValueStore(STORE_NAME);
		
		Materialized<Integer, GradeStore, KeyValueStore<Bytes,byte[]>> materialized = Materialized.as(storeSupplier); 
 
		// aggregation
		KTable<Integer, GradeStore> gradesTable = partStudentStream
			.mapValues(student -> GradeStore.newBuilder(student).build())
			.groupByKey(Grouped.with(integerSerde, storeSerde))
			.reduce(GradeStore::sum, materialized); 
		
        // get values from console for browser testing      			
		gradesTable.toStream()
		.peek((k, v) -> System.out.printf("||| STUDENT |||  id = %4d   avg.grade = %4d\n", k, v.calcAvgGrade()));
		
// PREPARE AND START APPLICATION ///////////////////////////////////////////////////
		// prepare data for KafkaStreams creation
		Topology topology = streamBuilder.build();
		
        // create/init consumer 
		KafkaStreams kafkaStreams = new KafkaStreams(topology, properties);
        // create queryServer
		InteractiveQueryServer queryServer = new InteractiveQueryServer(kafkaStreams, hostInfo, mode); 
		
        kafkaStreams.setGlobalStateRestoreListener(new LoggingStateRestoreListener());
        kafkaStreams.setStateListener(((newState, oldState) -> {
            if (newState == KafkaStreams.State.RUNNING && oldState == KafkaStreams.State.REBALANCING) {
                LOG.info("===> Setting the query server to ready");
                queryServer.setReady(true);
            } else if (newState != KafkaStreams.State.RUNNING) {
                LOG.info("===> State not RUNNING, disabling the query server");
                queryServer.setReady(false);
            }
        }));
        kafkaStreams.setUncaughtExceptionHandler((t, e) -> {
            LOG.error("Thread {} had a fatal error {}", t, e, e);
            shutdown(kafkaStreams, queryServer);
        });
 		
		// start queryServer
        queryServer.init();
        
        // start consumer
        LOG.info("Kafks streams started");
        kafkaStreams.cleanUp();
        kafkaStreams.start(); 
		
	}

    private static void shutdown(KafkaStreams kafkaStreams, InteractiveQueryServer queryServer) {
        LOG.info("Shutting down the application and query server");
        kafkaStreams.close();
        queryServer.stop();
    }

	private static Properties initProperties(String mode) {
	    Properties props = new Properties();
		
        String broker = mode.equals(APP_MODE) ? KAFKA_OUTER_BROKERS : KAFKA_INNER_BROKERS; 
        LOG.info("### Application bootstrap server is {}", broker); 
        
        props.put (StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, broker);
	    props.put(StreamsConfig.APPLICATION_ID_CONFIG, "customer_id");	    
	    props.put(ConsumerConfig.GROUP_ID_CONFIG, "customer_groupId");
	    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");	
        props.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG, DeserializerErrorHandler.class);
	    
	    return props;
	}       
    private boolean tryParseInt(String value) {
	   try {
	       Integer.parseInt(value);
	       return true;
	   } catch (NumberFormatException ex) {
	      return false;
	   }
	}    
	
}
