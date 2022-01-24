# Step: Install maven and copy project for compilation
FROM maven:3.8.3-openjdk-8 as build
COPY pom.xml /local/pom.xml
COPY src /local/src
WORKDIR /local/
RUN mvn clean install

# Step: final docker image
FROM openjdk:8
COPY --from=build /local/target  /prod/target
WORKDIR /prod/target
ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-jar", "gradegenerator.jar"]

