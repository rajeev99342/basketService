package com.service.model;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties("properties")
public class Properties {
    @Value("${deliveryHr}")
    private List<Integer> deliveryHrs;
}
