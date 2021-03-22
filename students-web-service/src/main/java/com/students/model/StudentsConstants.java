package com.students.model;

public interface StudentsConstants {
	// ConsumerRunner
	public static String KAFKA_OUTER_BROKERS = "localhost:9092";
	public static String KAFKA_INNER_BROKERS = "kafka:9093";
	public static String STUDENTS_TOPIC = "students";
	public static String INNER_TOPIC = "inner_topic";
	public static String STORE_NAME = "StudentAverageGrade";
	public static Integer PARTITION_COUNT = 3;
	public static Integer TOP_SIZE = 5;
	
	public static String APP_MODE = "application";
	public static String DOCKER_MODE = "docker";
	
    // InteractiveQueryServer
    public static String STORE_PARAM = ":store";
    public static String KEY_PARAM = ":key";
    public static String STORES_NOT_ACCESSIBLE = "{===> Stores not ready for service, probably re-balancing}";
	public static String HOST = "localhost";
	public static Integer PORT = 8087;
	
	public static String PARTITION_0_HOST = "gradestore0";
	public static Integer PARTITION_0_PORT = 8087;
	
	public static String PARTITION_1_HOST = "gradestore1";
	public static Integer PARTITION_1_PORT = 8088;
	
	public static String PARTITION_2_HOST = "gradestore2";
	public static Integer PARTITION_2_PORT = 8089;		
}
