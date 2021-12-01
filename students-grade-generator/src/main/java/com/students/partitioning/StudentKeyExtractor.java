package com.students.partitioning;

import org.springframework.cloud.stream.binder.PartitionKeyExtractorStrategy;
import org.springframework.messaging.Message;

import com.students.model.StudentGrade;
import com.students.serde.ProtoStudentGradeDeserializer;

public class StudentKeyExtractor implements PartitionKeyExtractorStrategy {

	private ProtoStudentGradeDeserializer deserializer = new ProtoStudentGradeDeserializer();
	
	public Object extractKey(Message<?> message) {
		byte[] payload = (byte[]) message.getPayload();
		StudentGrade student = deserializer.deserialize("", payload); 
		return student.getId(); 
	}

}
