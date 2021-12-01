package com.students.model;
import lombok.*;

@Data
public class StudentGrade {

	Integer id;
	String name;
	Integer grade;
	
	public StudentGrade() {}
	public StudentGrade(int id, String name, int grade) {
		super();
		this.id = id;
		this.name = name;
		this.grade = grade;
	}
	
}
