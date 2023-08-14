//package com.service.config;
//
//import org.apache.spark.SparkConf;
//import org.apache.spark.api.java.JavaSparkContext;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class SparkConfig {
//
//    @Bean
//    public SparkConf sparkConf() {
//        return new SparkConf().setAppName("SparkApp").setMaster("local[*]");
//    }
//
//
//    @Bean
//    public JavaSparkContext javaSparkContext() {
//        return new JavaSparkContext(sparkConf());
//    }
//}