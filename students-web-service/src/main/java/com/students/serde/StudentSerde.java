package com.students.serde;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import com.students.model.Student;

public class StudentSerde implements Serde<Student> {
	
    private Serializer<Student> serializer;
    private Deserializer<Student> deserializer;

    private StudentSerde() {
    	this.serializer = new ProtoStudentSerializer();
    	this.deserializer = new ProtoStudentDeserializer();
    }
    
	public Deserializer<Student> deserializer() {
		return deserializer;
	}
	public Serializer<Student> serializer() {
		return serializer;
	}

	public static Serde<Student> StudentsSerde(){
		return new StudentSerde();
	}
}
