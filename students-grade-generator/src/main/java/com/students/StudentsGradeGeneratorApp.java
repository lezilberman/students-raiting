package com.students;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import static com.students.utils.StudentsUtils.getDefaultDuration;

@SpringBootApplication
public class StudentsGradeGeneratorApp {

	public static void main(String[] args) {
	    ConfigurableApplicationContext ctx = SpringApplication.run(StudentsGradeGeneratorApp.class, args);

        long duration = Long.parseLong(ctx.getEnvironment().getProperty("threadDuration", getDefaultDuration()));
		try {
			Thread.sleep(duration);   
			ctx.close();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	    
	}

}
