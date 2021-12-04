package com.students.serde;

import java.io.IOException;
import java.util.Map;

import org.apache.kafka.common.serialization.Serializer;

import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.protobuf.ProtobufMapper;
import com.fasterxml.jackson.dataformat.protobuf.schema.ProtobufSchema;
import com.fasterxml.jackson.dataformat.protobuf.schema.ProtobufSchemaLoader;
import com.students.model.StudentGrade;

public class ProtoStudentGradeSerializer implements Serializer<StudentGrade> {

    static ProtobufMapper mapper = new ProtobufMapper ();
	static String protobuf_str = "message StudentGrade {\n"
	            + " required int32 id = 1;\n"
			    + " required string name = 2;\n"
	            + " required int32 grade = 3;\n"
	            + "}\n";
	static ProtobufSchema schema;

	static {
	    try {
	    	schema = ProtobufSchemaLoader.std.parse(protobuf_str);
	    } catch (IOException e) {
	      throw new RuntimeException(e);
	    }
	}
    static ObjectWriter writer = mapper.writer(schema);
	
	public byte[] serialize(String topic, StudentGrade data) {
	    byte[] protobufData=null;
	    
		try {
			protobufData = writer.writeValueAsBytes(data);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return protobufData;
	}

	public void close() {
		
	}

	public void configure(Map<String, ?> arg0, boolean arg1) {
		
	}


}
