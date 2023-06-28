FROM openjdk:11-jre-slim

WORKDIR /app

COPY target/service-0.0.1-SNAPSHOT.jar /app/service-0.0.1-SNAPSHOT.jar

EXPOSE 8081

CMD java -jar service-0.0.1-SNAPSHOT.jar
