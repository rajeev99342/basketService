package com.service.model;

import lombok.Data;

@Data
public class UserCredentials {
    private String confirmedPassword;
    private String mobile;
    private String name;
    private String password;
}
