package com.students.exception;

@SuppressWarnings("serial")
public class StudentGradeException extends RuntimeException {
	public int status;
	public StudentGradeException(int status) {
		super();
		this.status = status;
	}

}
