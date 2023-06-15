#!/bin/bash

# Start Eureka server

java -jar "C:/Users/Dell/Desktop/store/melaa-service-registry/target/service-registry-0.0.1-SNAPSHOT.jar" --server.port=8761 &
sleep 1m
java -jar "C:/Users/Dell/Desktop/store/basketService/target/service-0.0.1-SNAPSHOT.jar" &
sleep 5m
java -jar "C:/Users/Dell/Desktop/store/basketService/target/service-0.0.1-SNAPSHOT.jar" &
sleep 5m
java -jar "C:/Users/Dell/Desktop/store/melaa-lb/target/loadBalancer-0.0.1-SNAPSHOT.jar" --server.port=9000

echo "Eureka server, load balancer, and two instances of the Spring Boot application have been started."


