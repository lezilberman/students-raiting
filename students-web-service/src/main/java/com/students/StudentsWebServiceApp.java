package com.students;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class StudentsWebServiceApp {

	@Bean
	public ConsumerRunner startRunner () {
		return new ConsumerRunner ();  
	}
	
	public static void main(String[] args) {

		SpringApplication.run(StudentsWebServiceApp.class, args);
	}

}
