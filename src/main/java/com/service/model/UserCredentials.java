package com.service.model;

import com.service.constants.enums.Role;
import lombok.Data;

import java.util.List;

@Data
public class UserCredentials {
    private String confirmedPassword;
    private String mobile;
    private String name;
    private String password;
    private List<Role> roles;
    private String uid;

}
