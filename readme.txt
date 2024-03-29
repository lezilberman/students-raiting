1. Brief description of the project
===================================
It is required to build a scalable fault tolerant service that calculates the average grade for 
each student from the stream of student's grades, from the beginning of studies. Application should
run as Docker containers. The results per student Id must be available through the REST API endpoint.

2. Project structure
====================
The Kafka Streams students-raiting maven project consists of two modules:

1) students-grade-generator - a data producer that randomly generates a stream of student's grades (class 
       StudentGrade, see "Model description" section below) in the topic "students". Implemented as Spring Boot Kafka 
    client, uses Spring Cloud Stream API. 
       The duration of the generator in milliseconds is defined by the thread.duration configuration parameter in the 
    application-docker.properties file. The default duration defined in the com.students.mock.DataGenerator class.
	The range of student Id values is determined using the student.minId and student.maxId parameters in a similar way.  
    
2) students-web-service - consumer with three partitions. Implemented as Apache kafka streams client that summarizes 
    the input stream into the table of average grades for each student and supports Interactive REST Queries with REST  
    API endpoint: http://localhost:8087/StudentAverageGrade/:Id.
    
    The input stream model and average grade table are described in the next section 3) "Model Description".
       
    Each instance of the service get 2 arguments:
    1. port of the application web service, required argument - integer. In this case, we chose the values 8087, 8088 ...
    2. application launch mode - applicative or in a docker container, optional argument - string "application" or "docker".
       By default, it is assumed that the application is launched in a docker container.
       Application mode can be used for debugging purposes. 
    
    The service configuration described in the section 4) "Service Configuration".
        
3. Model description
====================
  1) The input stream model: class com.students.model.StudentGrade - generated student grade;
  
	  Integer id     - student id;
	  String name    - student name;
	  Integer grade  - student grade;
 
  2) The input stream aggregated into table with students grades sum per student id: class com.students.model.GradeStore

	  int studentId       - student id;
	  String studentName  - student name;
	  long gradeSum       - the sum of all grades received by the student;
	  long gradeCount     - the count of all grades received by the student;

4. Service configuration
========================
   The scalability of the service is provided by several partitions, each of which is a running instance 
   with its own arguments. Service configuration is determined by the following files.
   
   1) Dockerfiles of Partitions. Located in the root directory of the service ${PROJECT_DIR}/students-web-service. 
	  Uses a multi-stage build of a Docker image. The first stage is used to build the code. The second stage 
	  wraps the built jar and a JRE into a Docker image. Passes the web service port to the image as an argument.
      
   2) Partition configuration file partitions.yml located in the root directory of the service.
      Describes the correspondence between web port and service name for each partition.
      The project uses the Apache Commons Configuration software library to read and parse the producer 
      and consumer configuration files.
      
   3) The docker-compose.yml file, located in the root directory of the project, determines the configuration 
      of the project as a whole, including the number and configuration of partitions. Uses 'build' configuration option.

5. Add(remove) new partition with index N
=========================================
    
    1. Add a partition dockerfile named DockerfileN (project name convention)
       to the root directory of the service.
       
    2. Make the following changes in the docker-compose.yml:
       a) increment partitions count in the section 
          kafka: 
            environment: 
              KAFKA_CREATE_TOPICS: "students:4:1"
              
       b) add a partition service section named gradestoreN similar 
          to the previous ones for DockerfileN;

    3. Add the appropriate entry to the partitions.yml config file.
       Something like this: "8090": gradestore3;
       
    4. For debug in application mode please update --partitions field in 
       the create_students_topic.bat/sh located in the resources directory 
       ${PROJECT_DIR}/students-web-service/src/main/resources.
   
6. Testing guidelines
=====================
1) Download project from GitHub and import it into IDE. The project is written in Java 8.
2) Run Docker Desktop. 
3) Open the console (e.g. Git Bash) in the project root directory ${PROJECT_DIR}.
   Run command 'docker-compose up'.
4) When you run the application for the first time, wait a bit while the data generator and 
   three partition images are built and run.
   An indication that the necessary data has been built is the following output to the console:

   ||| STUDENT |||  id =  498   avg.grade = 15.00
   ||| STUDENT |||  id =  321   avg.grade = 21.00

5) When testing an application, you need to take into account 2 points:
    1. The generator runs for a limited time (600 seconds by default).
    2. The range of Id generation can be so large (1,000,000 e.g.),
       that at any given time, most of the Id values do not yet exist.
    So student Id values for testing you can find in the application console log.

6) Open browser and go to REST API endpoint: http://localhost:8087/StudentAverageGrade/:Id
   or open POSTMAN and use STUDENTS-RAITING.postman_collection.json located in the service
   root directory.

