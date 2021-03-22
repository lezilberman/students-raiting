package com.students.utils;

import java.util.HashMap;
import java.util.Map;

import com.github.javafaker.Faker;

public class StudentsUtils {

	public StudentsUtils() {
		super();
		try {
			maxId = Integer.parseInt(System.getenv("maxStudentId"));
		} catch (Exception e) {
			maxId = defaultMaxId;
		}		
	}
	
	private static String defaultThreadDuration = "600000";
	
	private int minId = 1;
	private final int defaultMaxId = 1000; 
	private int maxId;
	
	private static int minGrade = 1;
	private static int maxGrade = 100;
	
	private Map<Integer, String> mapNames = new HashMap<>();

    public static String getDefaultDuration() {
		return defaultThreadDuration;
	}
	public static int getRandomGrade() {
		return (int)(minGrade + Math.random()*(maxGrade-minGrade+1));
	}
	
    public int getRandomId() {
		return (int)(minId + Math.random()*(maxId-minId+1));
	}
	public String getRandomName(int id) {
		String name = mapNames.getOrDefault((Integer)id, null);
		if(name == null) {
			Faker faker = new Faker();
			name = faker.name().name();
			mapNames.put(id, name);
			return name;
		}
		return name;
	}	
}
