package com.students.serde;

import java.io.IOException;

import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.protobuf.ProtobufMapper;
import com.fasterxml.jackson.dataformat.protobuf.schema.ProtobufSchema;
import com.fasterxml.jackson.dataformat.protobuf.schema.ProtobufSchemaLoader;
import com.students.model.GradeStore;

public class ProtoGradeStoreDeserializer implements Deserializer<GradeStore> {

    private static final Logger LOG = LoggerFactory.getLogger(ProtoGradeStoreDeserializer.class);	
    
    static ProtobufMapper mapper = new ProtobufMapper ();
	static String protobuf_str = "message GradeStore {\n"
            + " required int32 studentId = 1;\n"
            + " required string studentName = 2;\n"
            + " required int64 gradeSum = 3;\n"
            + " required int64 gradeCount = 4;\n"
            + "}\n";
	static ProtobufSchema schema;

	static {
	    try {
	    	schema = ProtobufSchemaLoader.std.parse(protobuf_str);
	    } catch (IOException e) {
	    	LOG.error("%%%%% ProtoGradeStoreDeserializer " + e.getMessage());
	        throw new RuntimeException(e);
	    }
	}
    static ObjectReader reader = mapper.reader(schema).forType(GradeStore.class);
    @Override
	public GradeStore deserialize(String topic, byte[] data) {

		GradeStore gradeStore = null;

		try {
			gradeStore = reader.readValue(data);  
		} catch (IOException e) {
	    	LOG.error("%%%%% ProtoGradeStoreDeserializer " + e.getMessage());
			e.printStackTrace();
		}
		
		return gradeStore;
	}

}
