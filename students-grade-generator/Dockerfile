FROM openjdk:8
ADD target/gradegenerator.jar gradegenerator.jar
ADD src/main/resources/application-docker.properties src/main/resources/application-docker.properties
ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-jar", "gradegenerator.jar"]
