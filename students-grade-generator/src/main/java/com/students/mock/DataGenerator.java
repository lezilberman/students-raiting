package com.students.mock;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

import com.github.javafaker.Faker;
import com.students.model.StudentGrade;

public class DataGenerator {
// use org.apache.commons.configuration2.Configuration
	
    static Configurations configs;      
    static Configuration config;
    static String configPath = "./src/main/resources/application-docker.properties";
    
    static final int DEFAULT_THREAD_DURATION = 600000;
    static final int DEFAULT_MAX_ID = 1000;
    static final int DEFAULT_MIN_ID = 1;
    
    static int threadDuration;
    static int maxId;
    static int minId;
    
	static final int minGrade = 1;
	static final int maxGrade = 100;
	static Map<Integer, String> mapNames = new HashMap<>();
	static Faker faker = new Faker();
	
	static {
		try {
			configs = new Configurations (); 
	        config = configs.properties (configPath);
	        threadDuration = config.get(Integer.class, "thread.duration", DEFAULT_THREAD_DURATION);
	        maxId = config.get(Integer.class, "student.maxId", DEFAULT_MAX_ID);
	        minId = config.get(Integer.class, "student.minId", DEFAULT_MIN_ID);
	
		} catch (ConfigurationException e) {
			config = null;
			threadDuration = DEFAULT_THREAD_DURATION;
			maxId = DEFAULT_MAX_ID;
			minId = DEFAULT_MIN_ID;			
		}		
	}
	
    public static int getThreadDuration() {    	
    	return threadDuration;
	}
	
    public static StudentGrade getRandomStudentGrade() {
    	
		int id = getRandomStudentId();
		String name = getRandomName(id);
		int grade = getRandomGrade();
		return new StudentGrade(id, name, grade);    	
    }

	private static int getRandomGrade() {
		return (int)(minGrade + Math.random()*(maxGrade-minGrade+1));
	}
	private static int getRandomStudentId() { // static
		return (int)(minId + Math.random()*(maxId-minId+1));
	}
	private static String getRandomName(int id) {
		String name = mapNames.getOrDefault((Integer)id, null);
		if(name == null) {
			name = faker.name().name();
			mapNames.put(id, name);
			return name;
		}
		return name;
	}	
}
