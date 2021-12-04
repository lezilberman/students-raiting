package com.students.web;
import static com.students.model.StudentsConstants.*;
import spark.Spark;
import static spark.Spark.*;

import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;

import org.apache.commons.configuration2.YAMLConfiguration;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyQueryMetadata;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.HostInfo;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.students.exception.StudentGradeException;
import com.students.model.GradeStore;
import com.students.utils.ServiceUtils;
import com.google.gson.Gson;


public class InteractiveQueryServer {

    private static final Logger LOG = LoggerFactory.getLogger(InteractiveQueryServer.class);
    private final Gson gson = new Gson();
    private Client client = ClientBuilder.newClient();
    private volatile boolean ready = false;
    
    private final KafkaStreams kafkaStreams;
    private final HostInfo hostInfo;
    private final String appMode;
    private final YAMLConfiguration config;

    public InteractiveQueryServer(final KafkaStreams kafkaStreams, final HostInfo hostInfo, 
    		                      final String mode, final YAMLConfiguration config) {
    	// Wrapper fields
        this.kafkaStreams = kafkaStreams;
        this.hostInfo = hostInfo;
        this.appMode = mode;
        this.config = config;
        // Spark
        staticFiles.location("/web");
        port(hostInfo.port());
        
    }
    
    // start/stop web service ////////////////////////
    public void init() {
        LOG.info("***** Query Server started with HOST {} PORT {} MODE {}", hostInfo.host(), hostInfo.port(), appMode);
                
        // logger, key validator
        before((request, response) -> {
        	
            LOG.info("### SPARK GET {}",   request.url());
            String path = request.pathInfo();
            String key = path.substring(path.lastIndexOf('/') + 1);
            
        	if (key == null || key.trim().isEmpty() || !ServiceUtils.tryParseInt(key)) {
        		halt(400, "Bad Request: Custom 400");
        	}
        	
//breaks interpartition communication:        	
//          String store = path.substring(1, path.lastIndexOf('/'));
//          LOG.info("### SPARK STORE {}",   store);        	
//        	if (!store.equals(STORE_NAME)) {
//        		halt(400, "Bad Request: Custom 400");
//        	}
        });
        
        // end point
        get("/:store/:key", (req, res) -> ready ? fetchKeyValueStore(req.params()) : STORES_NOT_ACCESSIBLE );
        
        // access to state store. Inner api.
        get("local/:store/:key", (req, res) -> ready ? fetchLocalStore(req.params()) : STORES_NOT_ACCESSIBLE );  
        
        // exception handling	
        exception(StudentGradeException.class, (exception, request, response) -> {
        	
        	int status = exception.status;
        	String msg = (status==204)? "{\"No Content\": \"Custom 204\"}" : "{\"Bad Request\": \"Custom 400\"}";
        	LOG.info("### SPARK EXCEPTION {}", msg);
        	
        	response.type("application/json");
        	response.status(status);
        	response.body(msg);
        });  
        
    }
    public void stop() {
        Spark.stop();
        client.close();
        LOG.info("***** Shutting down the Interactive Query Web server");
    }
    
    // helper functions ////////////////////////
    public synchronized void setReady(boolean ready) {
        this.ready = ready;
    }
    private boolean dataLocal(HostInfo hostInfo) {
        return this.hostInfo.equals(hostInfo);
    }

    // local API        ////////////////////////        
	private String selectValueDataByKey(String storeName, Integer key) throws Exception {
		
    	StoreQueryParameters<ReadOnlyKeyValueStore<Integer, GradeStore>> queryParameters = 
    			StoreQueryParameters.fromNameAndType(storeName, QueryableStoreTypes.keyValueStore());
    	
    	ReadOnlyKeyValueStore<Integer, GradeStore> readOnlyStore = kafkaStreams.store(queryParameters);
    	
    	GradeStore value = readOnlyStore.get(key);
    	String log = (value == null)? ">>>>> selectValueDataByKey value == null" :
    		                          "##### selectValueDataByKey value = " + value;
		LOG.info(log);
		
        return (value == null)? "{\"No Content\": \"Custom 204\"}" : gson.toJson(value.calcAvgGrade());
    }	
	private String fetchLocalStore(Map<String, String> params) throws Exception {
		
        String store = params.get(STORE_PARAM);
        String key = params.get(KEY_PARAM);
        Integer keyInteger = Integer.parseInt(key);
        
		return selectValueDataByKey(store, keyInteger);
	}
	
    // remote API ////////////////////////
	// validates metadata and calls data fetch from local or remote store
 	private String fetchKeyValueStore(Map<String, String> params) throws Exception {
 		
        String key = params.get(KEY_PARAM).trim();
        String store = params.get(STORE_PARAM).trim();
        
    	if (!store.equals(STORE_NAME)) {
    		LOG.error(">>>>> fetchKeyValueStore: !store.equals(STORE_NAME)");
    		throw new StudentGradeException(400);
	    }
                
        Long keyLong = Long.parseLong(key);        
		KeyQueryMetadata metadata = kafkaStreams
				.queryMetadataForKey(store, keyLong, (topic, k, v, numPartitions)-> {return (int) (k % numPartitions);});
		
		if (metadata.equals(KeyQueryMetadata.NOT_AVAILABLE)) {
			throw new StudentGradeException(204);			
		}
		
		HostInfo activeHost = metadata.activeHost();
		
		LOG.info("***** fetchKeyValueStore: metadata: activeHost= {} PORT= {} PARTITION= {}", 
				                                      activeHost.host(), activeHost.port(), metadata.partition());			
		
		return (dataLocal(activeHost)) ? fetchLocalStore(params) : fetchRemoteStore(activeHost, params);
	}
    private String fetchRemoteStore(HostInfo activeHostInfo, Map<String, String> params) throws Exception {
    	
        String store = params.get(STORE_PARAM);
        String key = params.get(KEY_PARAM);
        
        // in APP_MODE, intended for debugging, we use only localhost
        String strPort = Integer.toString(activeHostInfo.port());        
        String host = appMode.equals(APP_MODE) ? LOCAL_HOST : config.get(String.class, strPort); 
        
        String url = String.format("http://%s:%d/local/%s/%s", host, activeHostInfo.port(), store, key);

        String remoteResponseValue = "";
        try {
            remoteResponseValue = client.target(url).request(MediaType.APPLICATION_JSON_TYPE).get(String.class);
        } 
        catch (Exception e) {
            LOG.error(">>>>> fetchRemoteStore: " + e.getMessage());
            throw new StudentGradeException(204);
        }
        LOG.info("***** fetchRemoteStore: remoteResponseValue= {}", remoteResponseValue);        
        return remoteResponseValue;
    }
 
}
