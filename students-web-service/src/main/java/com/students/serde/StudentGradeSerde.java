package com.students.serde;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import com.students.model.StudentGrade;

public class StudentGradeSerde implements Serde<StudentGrade> {
	
    private Serializer<StudentGrade> serializer;
    private Deserializer<StudentGrade> deserializer;

    private StudentGradeSerde() {
    	this.serializer = new ProtoStudentGradeSerializer();
    	this.deserializer = new ProtoStudentGradeDeserializer();
    }
    
	public Deserializer<StudentGrade> deserializer() {
		return deserializer;
	}
	public Serializer<StudentGrade> serializer() {
		return serializer;
	}

	public static Serde<StudentGrade> StudentsSerde(){
		return new StudentGradeSerde();
	}
}
