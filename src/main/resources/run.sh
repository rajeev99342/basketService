#!/bin/bash

# Start Eureka server
java -jar /path/to/eureka-server.jar > /path/to/eureka-server.log 2>&1 &

# Start load balancer
java -jar /path/to/load-balancer.jar > /path/to/load-balancer.log 2>&1 &

# Start first instance
java -jar /path/to/your/application.jar > /path/to/log/file1.log 2>&1 &

# Start second instance
java -jar /path/to/your/application.jar > /path/to/log/file2.log 2>&1 &

echo "Eureka server, load balancer, and two instances of the Spring Boot application have been started."
