package com.service.model;

import com.service.constants.enums.UserRole;
import lombok.Data;

import java.util.List;

@Data
public class UserCredentials {
    private String confirmedPassword;
    private String mobile;
    private String name;
    private String password;
    private String token;
    private List<UserRole> roles;
    private String uid;

}
