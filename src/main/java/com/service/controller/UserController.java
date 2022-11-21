package com.service.controller;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.constants.enums.Status;
import com.service.entities.Address;
import com.service.entities.User;
import com.service.jwt.JwtTokenUtility;
import com.service.jwt.MyUserDetailsService;
import com.service.model.AddressModel;
import com.service.model.GlobalResponse;
import com.service.model.UserCredentials;
import com.service.service.TwilioMessageSenderService;
import com.service.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;

@Slf4j
@RestController
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Transactional

public class UserController {

    @Autowired
    TwilioMessageSenderService twilioMessageSenderService;
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtility jwtTokenUtil;
    @Autowired
    UserService userService;

    @Autowired
    MyUserDetailsService userDetailsService;

    @CrossOrigin(origins = "*")
    @PostMapping("/sign-in")
    public GlobalResponse login(@RequestBody UserCredentials loginDetails) {
        return userService.signIn(loginDetails);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/sign-up")
    public GlobalResponse signup(@RequestBody UserCredentials userDetails) {
        return userService.saveUserDetails(userDetails);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/reset-password")
    public GlobalResponse resetPassword(@RequestBody UserCredentials userDetails) {
       return userService.reset(userDetails);
    }


    @CrossOrigin(origins = "*")
    @PostMapping("/save-address")
    public GlobalResponse saveAddress(@RequestBody AddressModel address) {
        GlobalResponse globalResponse = new GlobalResponse();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Address recentDefaultAddress = userService.saveUserAddress(address);
            globalResponse.setStatus(true);
            globalResponse.setBody(recentDefaultAddress);
            globalResponse.setMessage(Status.SUCCESS.toString());
        } catch (Exception e) {
            e.printStackTrace();
            globalResponse.setMessage("Failed");
        }

        return globalResponse;
    }


    @CrossOrigin(origins = "*")
    @GetMapping("/get-address")
    public AddressModel getUserAddress(@RequestParam("userPhone") String userPhone) {
        try {
            return userService.getAddressByUser(userPhone);
        } catch (Exception e) {
            log.error("Failed to get user address due to " + e);
            return null;
        }

    }


    @CrossOrigin(origins = "*")
    @GetMapping("/hello")
    public String hello() {
        try {
            return "Hello from server";
        } catch (Exception e) {
            log.error("Failed to get user address due to " + e);
            return null;
        }

    }


    @CrossOrigin(origins = "*")
    @GetMapping("/is-user-present")
    public UserCredentials isUserPresentWithPhone(@RequestParam("userPhone") String userPhone) {
        return userService.isUserPresent(userPhone);
    }


    @CrossOrigin(origins = "*")
    @GetMapping("/get-user")
    public User getUser(@RequestParam("userPhone") String userPhone) {
        return userService.getUserByPhone(userPhone);
    }


    @CrossOrigin(origins = "*")
    @PostMapping("/update-user-token")
    public GlobalResponse updateUserToken(@RequestParam("token") String token, @RequestParam("jwt") String jwt) {
        return userService.updateUserToken(token, jwt);
    }


    @CrossOrigin(origins = "*")
    @PostMapping("/auto-verify-phone")
    public Integer verifyPhoneNumber(@RequestParam("phoneNumber") String phoneNumber) {
        try {
            return twilioMessageSenderService.getOtp(phoneNumber);
        } catch (Exception e) {
            log.error("Failed to get user address due to " + e);
            return null;
        }

    }
    @CrossOrigin(origins = "*")
    @PutMapping("/update-user-name")
    public AddressModel getUserAddress(@RequestParam("username") String username, Authentication authentication) {
        try {
            return userService.updateUserName(username,authentication);
        } catch (Exception e) {
            log.error("Failed to get user address due to " + e);
            return null;
        }

    }

}
