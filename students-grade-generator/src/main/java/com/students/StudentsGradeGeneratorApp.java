package com.students;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.students.mock.DataGenerator;

@SpringBootApplication
public class StudentsGradeGeneratorApp {

	public static void main(String[] args) {
	    ConfigurableApplicationContext ctx = SpringApplication.run(StudentsGradeGeneratorApp.class, args);

        long duration = DataGenerator.getThreadDuration();
        System.out.println("##### Thread Duration = " + duration);
        
		try {
			Thread.sleep(duration);   
			ctx.close();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	    
	}

}
