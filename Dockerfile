#FROM openjdk:11-jre-slim
#
#WORKDIR /app
#
#COPY target/service-0.0.1-SNAPSHOT.jar /app/service-0.0.1-SNAPSHOT.jar
#
#EXPOSE 8081
#
#CMD java -jar service-0.0.1-SNAPSHOT.jar


# Use a Maven image as the base image
FROM maven:3.8.4-openjdk-11-slim AS builder

# Set the working directory
WORKDIR /app

# Copy the Maven project file
COPY pom.xml .

# Download the project dependencies
RUN mvn dependency:go-offline -B

# Copy the application source code
COPY src ./src

# Build the application package
RUN mvn clean install -DskipTests

# Use an OpenJDK image as the base image
FROM openjdk:11-jre-slim

# Set the working directory
WORKDIR /app

# Copy the JAR file from the builder stage
COPY --from=builder /app/target/service-0.0.1-SNAPSHOT.jar .

COPY src/main/resources/baba-basket-645b9-firebase-adminsdk-zyteh-7b351169bd.json .
COPY src/main/resources/prometheus.yml .
# Expose the port on which your Spring Boot app listens
EXPOSE 8080

# Define the entry point command to run your application
CMD ["java", "-jar", "service-0.0.1-SNAPSHOT.jar"]
