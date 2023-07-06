# Melaa | Grocery 

How to start service:
1. start netflix service registry
   1. $ cd melaa/melaa-service-registry
   2. $ java -jar target/service-registry-0.0.1-SNAPSHOT.jar

2. Start main melaa service
   1. cd melaa/basketService
   2. java -jar target/basket-service-0.0.1-SNAPSHOT.jar


2. Start load balancer service
   1. cd melaa/melaa-lb
   2. java -jar target/melaa-lb-0.0.1-SNAPSHOT.jar
   




# Component:
   1. Mysql DB
      1. Database name is - melaa
   2. Firebase notification
   3. Websocket (not in use)
   4. Redis cache
   4. Spring boot 2.7




