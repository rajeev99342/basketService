package com.service.model;

import com.service.constants.enums.Role;
import lombok.Data;

import java.util.List;

@Data
public class UserModel {
    private Long id;
    private String name;
    private String userType;
    private String phone;
    private String password;
    private String jwt;
    private List<String> roles;
}
