package com.students.serde;

import java.io.IOException;
import java.util.Map;

import org.apache.kafka.common.serialization.Deserializer;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.protobuf.ProtobufMapper;
import com.fasterxml.jackson.dataformat.protobuf.schema.ProtobufSchema;
import com.fasterxml.jackson.dataformat.protobuf.schema.ProtobufSchemaLoader;
import com.students.model.StudentGrade;

public class ProtoStudentGradeDeserializer implements Deserializer<StudentGrade> {

    static ProtobufMapper mapper = new ProtobufMapper ();
	static String protobuf_str = "message Student {\n"
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
    static ObjectReader reader = mapper.reader(schema).forType(StudentGrade.class);
	
	public StudentGrade deserialize(String topic, byte[] data) {
		
		StudentGrade student = null;
		
		try {
			student = reader.readValue(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return student;
	}
	
	public void close() {
		
	}

	public void configure(Map<String, ?> arg0, boolean arg1) {
		
	}

}
