package com.students.service;

import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.integration.annotation.InboundChannelAdapter;

import com.students.model.Student;
import com.students.serde.ProtoStudentSerializer;
import com.students.utils.StudentsUtils;

@EnableBinding(Source.class)
public class StudentsGenerator {

	private ProtoStudentSerializer serializer = new ProtoStudentSerializer();
	private StudentsUtils utils = new StudentsUtils();
	
	@InboundChannelAdapter(Source.OUTPUT)
	byte[] sendStudentData() {
		int id = utils.getRandomId();
		String name = utils.getRandomName(id);
		int grade = StudentsUtils.getRandomGrade();
		Student student = new Student(id, name, grade);
		
		byte[] protobufData = null;
		try {
			protobufData = serializer.serialize("", student);
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return protobufData;		
	}
}
