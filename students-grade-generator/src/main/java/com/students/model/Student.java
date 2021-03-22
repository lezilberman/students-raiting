package com.students.model;
import lombok.*;

@Data
public class Student {

	Integer id;
	String name;
	Integer grade;
	
	public Student() {}
	public Student(int id, String name, int grade) {
		super();
		this.id = id;
		this.name = name;
		this.grade = grade;
	}
	
}
