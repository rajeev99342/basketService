package com.service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.constants.enums.Status;
import com.service.entities.Address;
import com.service.entities.User;
import com.service.model.AddressModel;
import com.service.model.DisplayProductModel;
import com.service.model.GlobalResponse;
import com.service.model.UserCredentials;
import com.service.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
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
            User user = userService.loginUser(loginDetails);
            globalResponse.setMessage("Login successfully");
            globalResponse.setStatus(true);
            globalResponse.setHttpStatusCode(HttpStatus.OK.value());
            globalResponse.setBody(user);
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



    @CrossOrigin(origins = "*")
    @PostMapping("/save-address")
    public GlobalResponse saveAddress(@RequestBody AddressModel address){
        GlobalResponse globalResponse = new GlobalResponse();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Address recentDefaultAddress = userService.saveUserAddress(address);
            globalResponse.setStatus(true);
            globalResponse.setBody(recentDefaultAddress);
            globalResponse.setMessage(Status.SUCCESS.toString());
        }catch (Exception e){
            e.printStackTrace();
            globalResponse.setMessage("Failed");
        }

        return globalResponse;
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/get-address")
    public List<Address> getUserAddress(@RequestParam("userPhone") String userPhone){
        try{
            return userService.getAddressByUser(userPhone);
        }catch (Exception e){
            log.error("Failed to get user address due to "+e);
            return  null;
        }

    }

}
