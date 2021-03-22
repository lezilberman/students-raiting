package com.students.web;
import static com.students.model.StudentsConstants.*;
import spark.Spark;
import static spark.Spark.*;

import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyQueryMetadata;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.HostInfo;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.students.model.GradeStore;
import com.google.gson.Gson;


public class InteractiveQueryServer {

    private static final Logger LOG = LoggerFactory.getLogger(InteractiveQueryServer.class);
    private final Gson gson = new Gson();
    private Client client = ClientBuilder.newClient();
    private volatile boolean ready = false;
    
    private final KafkaStreams kafkaStreams;
    private final HostInfo hostInfo;
    private final String appMode;

    private static Map<Integer, String> mapHost = new HashMap<>();
    static {
    	mapHost.put(PARTITION_0_PORT, PARTITION_0_HOST);
    	mapHost.put(PARTITION_1_PORT, PARTITION_1_HOST);
    	mapHost.put(PARTITION_2_PORT, PARTITION_2_HOST);    	
    }
    public InteractiveQueryServer(final KafkaStreams kafkaStreams, final HostInfo hostInfo, final String mode) {
        this.kafkaStreams = kafkaStreams;
        this.hostInfo = hostInfo;
        this.appMode = mode;
        // Spark
        staticFiles.location("/web");
        port(hostInfo.port());
    }
    
    public void init() {
        LOG.info("### Query Server started with HOST {} PORT {} MODE {}", hostInfo.host(), hostInfo.port(), appMode);
                
        // logger
        before((request, response) -> {
            LOG.info(String.format("@@@ SPARK GET %s",   request.url()));
        });
        
        get("/:store/:key", (req, res) -> ready ? fetchKeyValueStore(req.params()) : STORES_NOT_ACCESSIBLE );
        // access to state store. Inner api.
        get("local/:store/:key", (req, res) -> ready ? fetchLocalStore(req.params()) : STORES_NOT_ACCESSIBLE );
        // exception handling	        
        get("/throwexception", (request, response) -> {
            throw new Exception();
        });
        exception(Exception.class, (exception, request, response) -> {
        	response.status(404);
        	response.body ("{\"Custom 404\":\"Data not found\"}");
        });   
    }
    public void stop() {
        Spark.stop();
        client.close();
        LOG.info("Shutting down the Interactive Query Web server");
    }
    public synchronized void setReady(boolean ready) {
        this.ready = ready;
    }
    
    // helper functions ////////////////////////
    private boolean dataLocal(HostInfo hostInfo) {
        return this.hostInfo.equals(hostInfo);
    }
    private boolean tryParseInt(String value) {
	   try {
	       Integer.parseInt(value);
	       return true;
	   } catch (NumberFormatException ex) {
	      return false;
	   }
	}  
    // local API        ////////////////////////        
	private String selectValueDataByKey(String storeName, Integer key) throws Exception {
		
    	StoreQueryParameters<ReadOnlyKeyValueStore<Integer, GradeStore>> queryParameters = 
    			StoreQueryParameters.fromNameAndType(storeName, QueryableStoreTypes.keyValueStore());
    	
    	ReadOnlyKeyValueStore<Integer, GradeStore> readOnlyStore = kafkaStreams.store(queryParameters);
    	GradeStore value = null;
    	try {
			value = readOnlyStore.get(key);
		} catch (Exception e) {
			LOG.error("%%% selectValueDataByKey %%% key value not found");
			throw new Exception(e.getMessage());
		}
        return gson.toJson(value.calcAvgGrade());
    }	
	private String fetchLocalStore(Map<String, String> params) throws Exception {
		
        String store = params.get(STORE_PARAM);
        String key = params.get(KEY_PARAM);
        Integer keyLong = Integer.parseInt(key);
        
		return selectValueDataByKey(store, keyLong);
	}
    // remote API ////////////////////////
 	private String fetchKeyValueStore(Map<String, String> params) throws Exception {
 		
        String store = params.get(STORE_PARAM);
        String key = params.get(KEY_PARAM);
        if (!store.equals(STORE_NAME) || !tryParseInt(key)) {
        	LOG.error("%%% fetchKeyValueStore: PARAMS NOT VALID");
        	throw new Exception(" fetchKeyValueStore: PARAMS NOT VALID");
        }
        Long keyLong = Long.parseLong(key);
        
		KeyQueryMetadata metadata = kafkaStreams.queryMetadataForKey(store, keyLong, (topic, k, v, numPartitions)-> {return (int) (k % numPartitions);});
		
		if (metadata != KeyQueryMetadata.NOT_AVAILABLE) {
			LOG.info("%%% fetchKeyValueStore %%% KeyQueryMetadata: activeHost= {} PORT= {} PARTITION= {}", 
					metadata.activeHost().host(), metadata.activeHost().port(), metadata.partition());
		}
		else {
			LOG.error("%%% fetchKeyValueStore %%% KeyQueryMetadata.NOT_AVAILABLE");
			throw new Exception("fetchKeyValueStore: KeyQueryMetadata.NOT_AVAILABLE");
		}
		
		if (dataLocal(metadata.activeHost())) {
			return fetchLocalStore(params); 
		}
		return fetchRemoteStore(metadata.activeHost(), params);
	}
    private String fetchRemoteStore(HostInfo hostInfo, Map<String, String> params) throws Exception {
    	
        String store = params.get(STORE_PARAM);
        String key = params.get(KEY_PARAM);
        String host = appMode.equals(APP_MODE) ? hostInfo.host() : mapHost.get(hostInfo.port()); 
        
        String url = String.format("http://%s:%d/local/%s/%s", host, hostInfo.port(), store, key);
		LOG.info("### fetchRemoteStore: URL {}", url);

        String remoteResponseValue = "";
        try {
            remoteResponseValue = client.target(url).request(MediaType.APPLICATION_JSON_TYPE).get(String.class);
        } catch (Exception e) {
            LOG.error("### fetchRemoteStore: connection problem error " + e.getMessage());
            throw new Exception("### fetchRemoteStore: NOT FOUND");
        }
                
        return remoteResponseValue;
    }
 
}
