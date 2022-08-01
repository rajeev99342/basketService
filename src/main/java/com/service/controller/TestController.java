package com.service.controller;

import com.service.model.GlobalResponse;
import com.service.model.UserCredentials;
import org.springframework.web.bind.annotation.*;

@RestController
public class TestController {

    @CrossOrigin(origins = "*")
    @GetMapping("/test")
    public String test() {

       return "Success";
    }
}
