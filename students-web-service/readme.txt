1. Brief description of the task
================================
It is required to build a scalable fault tolerant application that calculates the average grade for 
each student from the stream of student's grades, from the beginning of studies. Application should
run as Docker containers. The results per student Id must be available through the REST API endpoint.

2. Project structure
====================+
The Kafka Streams students-rating project consists of two modules:

1) students-grade-generator - data producer.
	The duration, in milliseconds, of the generator is set by the threadDuration environment variable 
	(if you use Eclipse, see Run Configurations menu). Or by default it will be 600000 milliseconds.
    The upper limit of the randomly generated student Id is set by the maxStudentId environment variable. 
    Or by default it will be 1000.	
    The root directory of the module contains the Dockerfile and the create_generator_image.sh script 
    to build the generator Docker image. To build an image, you need to run the script from the root 
    directory of the module, having previously run the command "chmod 755 create_generator_image.sh" from there.
    
2) students-web-service - consumer with three partitions.
	For scalability purposes, the service consists of three partitions, each of which is a running instance 
	with its own arguments. Since the application supports Interactive REST Queries, each instance receives 
	a port value as its first argument. In our case it is 8087, 8088, 8089. 
	The second argument is the launch method - "application" or "docker". This determines two points:
		a) the way to connect the application to the broker
		b) communication between partitions in the WEB application server.
	REST API endpoint: http://localhost:8087/StudentAverageGrade/:Id 

	In the root directory of the module, there are three Dockerfiles (for each partition) and 
	the create_service_images.sh script for building Docker images of all partitions. To build images, 
	you need to run the script from the root directory of the module, having previously run the command 
	"chmod 755 create_service_images.sh" from there. There is also docker-compose.yml for running the 
	application in a container environment. In addition to the generator and partitions images, 
	the containers wurstmeister / zookeeper: latest and wurstmeister / kafka: latest are used.

In addition, the root directory of the parent project contains the POSTMAN collection for testing 
the application and the readme.txt file.

3. Testing guidelines
=====================
When testing an application, you need to take into account 2 points:
1) The generator runs for a limited time (600 seconds by default).
2) The range of Id generation can be so large (1,000,000 e.g.),
   that at any given time, most of the Id values do not yet exist.

Therefore, there are 2 options for starting the application. In both cases it is NOT used
option -d disable output, which allows from console output select existing Id values.

||| STUDENT |||  id =  498   avg.grade = 15.00
||| STUDENT |||  id =  321   avg.grade = 21.00

1) docker-compose up - we see the general picture of a sequential start containers, 
   but the testing time is limited by the generator operation.
2) docker-compose up zookeeper kafka gradegenerator
   docker-compose up gradestore0
   docker-compose up gradestore1
   docker-compose up gradestore2
   in this case, the WEB application server remains available even after the generator has worked.


