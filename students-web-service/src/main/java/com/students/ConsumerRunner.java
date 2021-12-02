package com.students;

import org.springframework.boot.CommandLineRunner;

import com.students.exception.DeserializerErrorHandler;
import com.students.model.GradeStore;
import com.students.model.StudentGrade;
import com.students.partitioning.StudentGradePartitioner;
import com.students.restore.LoggingStateRestoreListener;
import com.students.serde.GradeStoreSerde;
import com.students.serde.StudentGradeSerde;
import com.students.utils.ServiceUtils;
import com.students.web.InteractiveQueryServer;

import org.apache.commons.configuration2.YAMLConfiguration;
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

import java.io.FileInputStream;
import java.util.Properties;

public class ConsumerRunner implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(ConsumerRunner.class);
    
	public void run(String... args) throws Exception {

// PASS ARGS TO THE START POINT & CONFIG APP. ///////////////////////////
		
		// get partitions config to: count partitions, validate ports,  
		YAMLConfiguration config = new YAMLConfiguration();
		config.read(new FileInputStream("./partitions.yml"));
		
		final int PARTITION_COUNT = config.size();		
		HostInfo hostInfo = validateArgs(config, args);
		String mode = args.length > 1 ? args[1] : DOCKER_MODE;
		
		// configuring consumer and queryServer
		Properties properties = initProperties(mode);
		properties.put(StreamsConfig.APPLICATION_SERVER_CONFIG, hostInfo.host() + ":" + hostInfo.port());		
		
        // prepare objects for source stream
        Serde<Integer> integerSerde = Serdes.Integer();
        Serde<StudentGrade> studentSerde = StudentGradeSerde.StudentsSerde();
        
        StreamsBuilder streamBuilder = new StreamsBuilder();

// CREATE AND PROCESS DATA STREAM //////////////////////////////////////////////////   
        
        // source stream with record key
        KStream<Integer, StudentGrade> studentsStream = streamBuilder.stream(STUDENTS_TOPIC, Consumed.with(integerSerde, studentSerde));
        
        KeyValueMapper<Integer, StudentGrade, Integer> mapper = (key, student) -> student.getId();
        
        KStream<Integer, StudentGrade> studentsKeyStream = studentsStream.selectKey(mapper);
//        		.peek((k, v) -> System.out.printf("=== KEY STUDENT === key = %4d, grade = %4d\n", k, v.getGrade()));

        // preparing objects for re-partitioning of source key-stream
        StudentGradePartitioner studentPartitioner = new StudentGradePartitioner();        
        
		Repartitioned<Integer, StudentGrade> repartitioned = Repartitioned
		.with(integerSerde, studentSerde).withNumberOfPartitions(PARTITION_COUNT) 
		.withName(INNER_TOPIC).withStreamPartitioner(studentPartitioner);
        
		// create partitioned stream
		KStream<Integer, StudentGrade> partStudentStream = studentsKeyStream.repartition(repartitioned);
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
		InteractiveQueryServer queryServer = new InteractiveQueryServer(kafkaStreams, hostInfo, mode, config); 
		
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

    private HostInfo validateArgs(YAMLConfiguration config, String... args) {
		// port for Application WebServer: 
		// validation
		if(args.length == 0) {
            LOG.error("Port number must be passed as an argument");
            System.exit(1);			
		}
		else if(!ServiceUtils.tryParseInt(args[0])) {
            LOG.error("Port value must be a number");
            System.exit(1);
        }
		if (!config.containsKey(args[0])) {
            LOG.error("Port value out of bounds defined by configuration");
            System.exit(1);			
		}
		String strPort = args[0]; // used below as map key
		int port = Integer.parseInt(strPort);

        // app.mode: "application" or "docker"(default value)
		String mode = args.length > 1 ? args[1] : DOCKER_MODE;
		if(!mode.equals(APP_MODE) && !mode.equals(DOCKER_MODE) ) {
            LOG.error("Application mode must be defined as {} or {} ", APP_MODE, DOCKER_MODE);
            System.exit(1);			
		}
		
		// host/HostInfo for Application WebServer config 
		// in APP_MODE, intended for debugging, we use only localhost
		String host = mode.equals(APP_MODE)? LOCAL_HOST : config.get(String.class, strPort);
		HostInfo hostInfo = new HostInfo(host, port);
		LOG.info("### Application Started with HOST: {} PORT: {} MODE: {}", host, port, mode); 
    	
    	return hostInfo;
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
	
}
