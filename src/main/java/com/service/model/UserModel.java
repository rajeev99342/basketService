package com.service.model;

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
    private AddressModel address;
    private List<String> roles;
}
