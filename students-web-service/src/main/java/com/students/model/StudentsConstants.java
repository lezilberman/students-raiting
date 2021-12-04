package com.students.model;

public abstract class StudentsConstants {
	// ConsumerRunner ////////////////////////////
	// config data, duplicated in docker-compose.yml
	public static String KAFKA_OUTER_BROKERS = "localhost:9092";
	public static String KAFKA_INNER_BROKERS = "kafka:9093";
	public static String STUDENTS_TOPIC = "students";
	public static String LOCAL_HOST = "localhost";
	//
	public static String INNER_TOPIC = "inner_topic";
	
	// ConsumerRunner & InteractiveQueryServer ///
	public static String STORE_NAME = "StudentAverageGrade";
	public static String APP_MODE = "application";
	public static String DOCKER_MODE = "docker";
	
    // InteractiveQueryServer ////////////////////
    public static String STORE_PARAM = ":store";
    public static String KEY_PARAM = ":key";
    public static String STORES_NOT_ACCESSIBLE = "{===> Stores not ready for service, probably re-balancing}";
        
}
