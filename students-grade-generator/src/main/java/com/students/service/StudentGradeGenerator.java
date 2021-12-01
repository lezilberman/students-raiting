package com.students.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.integration.annotation.InboundChannelAdapter;

import com.students.mock.DataGenerator;
import com.students.model.StudentGrade;
import com.students.serde.ProtoStudentGradeSerializer;

@EnableBinding(Source.class)
public class StudentGradeGenerator {
	
//    private static final Logger LOG = LoggerFactory.getLogger(StudentGradeGenerator.class);

	private ProtoStudentGradeSerializer serializer = new ProtoStudentGradeSerializer();
	
	@InboundChannelAdapter(Source.OUTPUT)
	byte[] sendStudentData() {
		StudentGrade student = DataGenerator.getRandomStudentGrade();
		
		byte[] protobufData = null;
		try {
			protobufData = serializer.serialize("", student);
//			LOG.info("Data sent: {}", student);
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return protobufData;		
	}
}
