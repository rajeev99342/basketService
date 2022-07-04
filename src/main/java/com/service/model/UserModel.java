package com.service.model;

import lombok.Data;

@Data
public class UserModel {
    private Long id;
    private String name;
    private String userType;
    private String phone;
    private String password;
    private String jwt;
}
