package com.service.model;

import com.service.constants.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserModel {
    private Long id;
    private String name;
    private String userType;
    private String phone;
    private String password;
    private String jwt;
    private AddressModel address;
    private List<String> roles;
    private UserRole loggedInAs;
    private LocationCord locationCord;
}
