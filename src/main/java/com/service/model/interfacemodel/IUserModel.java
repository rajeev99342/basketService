package com.service.model.interfacemodel;

import com.service.constants.enums.UserRole;

import java.util.List;

public interface IUserModel {
    public String getPhone();
    public String getName();
    public Long getId();
    public String getAddress();
    public String getLat();
    public String getLon();

    public List<String> getRoles();
}
