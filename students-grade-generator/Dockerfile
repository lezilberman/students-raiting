FROM openjdk:8
ADD target/gradegenerator.jar gradegenerator.jar
ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-jar", "gradegenerator.jar"]
