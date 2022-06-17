package com.service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.constants.enums.Status;
import com.service.model.GlobalResponse;
import com.service.model.UserCredentials;
import com.service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

    @Autowired
    UserService userService;
    @CrossOrigin(origins = "*")
    @PostMapping("/sign-in")
    public GlobalResponse login(@RequestBody UserCredentials loginDetails){
        GlobalResponse globalResponse = new GlobalResponse();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            System.out.println(objectMapper.writeValueAsString(loginDetails));
        }catch (Exception e){
            e.printStackTrace();
            globalResponse.setMessage("Failed");
        }

        return globalResponse;
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/sign-up")
    public GlobalResponse signup(@RequestBody UserCredentials userDetails){
        GlobalResponse globalResponse = new GlobalResponse();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            userService.saveUserDetails(userDetails);
            globalResponse.setStatus(true);
            globalResponse.setMessage(Status.SUCCESS.toString());
        }catch (Exception e){
            e.printStackTrace();
            globalResponse.setMessage("Failed");
        }

        return globalResponse;
    }

}
