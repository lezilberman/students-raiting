package com.students.serde;

import java.io.IOException;

import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.students.model.GradeStore;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.protobuf.ProtobufMapper;
import com.fasterxml.jackson.dataformat.protobuf.schema.ProtobufSchema;
import com.fasterxml.jackson.dataformat.protobuf.schema.ProtobufSchemaLoader;

public class ProtoGradeStoreSerializer implements Serializer<GradeStore> {
    private static final Logger LOG = LoggerFactory.getLogger(ProtoGradeStoreSerializer.class);	

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
	    	LOG.error("%%%%% ProtoGradeStoreSerializer " + e.getMessage());
	        throw new RuntimeException(e);
	    }
	}
    static ObjectWriter writer = mapper.writer(schema);
	
    @Override
	public byte[] serialize(String topic, GradeStore data) {
	    byte[] protobufData=null;
	    
		try {
			protobufData = writer.forType(GradeStore.class).writeValueAsBytes(data);
		} catch (IOException e) {
			LOG.error("##### ProtoGradeStoreSerializer " + e.getMessage());
			e.printStackTrace();
		}

		return protobufData;
	}

}
