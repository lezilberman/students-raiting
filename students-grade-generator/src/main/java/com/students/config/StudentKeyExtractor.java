package com.students.config;

import org.springframework.cloud.stream.binder.PartitionKeyExtractorStrategy;
import org.springframework.messaging.Message;

import com.students.model.Student;
import com.students.serde.ProtoStudentDeserializer;

public class StudentKeyExtractor implements PartitionKeyExtractorStrategy {

	private ProtoStudentDeserializer deserializer = new ProtoStudentDeserializer();
	
	public Object extractKey(Message<?> message) {
		byte[] payload = (byte[]) message.getPayload();
		Student student = deserializer.deserialize("", payload); 
		return student.getId(); 
	}

}
