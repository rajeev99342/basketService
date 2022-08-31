package com.service.controller;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.constants.enums.Status;
import com.service.entities.Address;
import com.service.jwt.JwtTokenUtility;
import com.service.jwt.MyUserDetailsService;
import com.service.model.AddressModel;
import com.service.model.GlobalResponse;
import com.service.model.UserCredentials;
import com.service.model.UserModel;
import com.service.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Transactional

public class UserController {
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
        GlobalResponse globalResponse = new GlobalResponse();
        try {

            authenticate(loginDetails.getMobile(), loginDetails.getPassword());

            final UserDetails userDetails = userDetailsService
                    .loadUserByUsername(loginDetails.getMobile());

            final String token = jwtTokenUtil.generateToken(userDetails);
            UserModel userModel = new UserModel();
            userModel.setName(userDetails.getUsername());
            userModel.setPhone(loginDetails.getMobile());
            userModel.setJwt(token);
            List<String> roles = new ArrayList<>();
            for(GrantedAuthority grantedAuthority : userDetails.getAuthorities()){
                roles.add(grantedAuthority.getAuthority());
            }
            userModel.setRoles(roles);
            globalResponse.setMessage("Login successfully");
            globalResponse.setStatus(true);
            globalResponse.setHttpStatusCode(HttpStatus.OK.value());
            globalResponse.setBody(userModel);
        } catch (Exception e) {
            e.printStackTrace();
            globalResponse.setMessage(e.getMessage());
        }

        return globalResponse;
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/sign-up")
    public GlobalResponse signup(@RequestBody UserCredentials userDetails) {
        GlobalResponse globalResponse = new GlobalResponse();
        try {
            userService.saveUserDetails(userDetails);
            globalResponse.setStatus(true);
            globalResponse.setMessage(Status.SUCCESS.toString());
        } catch (Exception e) {
            e.printStackTrace();
            globalResponse.setMessage(e.getMessage());

        }

        return globalResponse;
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/reset-password")
    public GlobalResponse resetPassword(@RequestBody UserCredentials userDetails) {
        GlobalResponse globalResponse = new GlobalResponse();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            userService.reset(userDetails);
            globalResponse.setStatus(true);
            globalResponse.setMessage(Status.SUCCESS.toString());
        } catch (Exception e) {
            e.printStackTrace();
            globalResponse.setMessage(e.getMessage());
        }

        return globalResponse;
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
    public Address getUserAddress(@RequestParam("userPhone") String userPhone) {
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

    private void authenticate(String username, String password) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }


    @CrossOrigin(origins = "*")
    @GetMapping("/is-user-present")
    public UserCredentials isUserPresentWithPhone(@RequestParam("userPhone") String userPhone) {
        try {
            return userService.isUserPresent(userPhone);
        } catch (Exception e) {
            log.error("Failed to get user address due to " + e);
            return null;
        }

    }



    @CrossOrigin(origins = "*")
    @PostMapping("/update-user-token")
    public GlobalResponse updateUserToken(@RequestParam("token") String token , @RequestParam("jwt") String jwt) {
        try {
            return userService.updateUserToken(token,jwt);
        } catch (Exception e) {
            log.error("Failed to get user address due to " + e);
            return null;
        }

    }


}
