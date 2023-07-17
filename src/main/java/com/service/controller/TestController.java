package com.service.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @Value("${server.port}")
    private String port;


    @GetMapping("/health")
    public String test() {
        return "UP";
    }
}
