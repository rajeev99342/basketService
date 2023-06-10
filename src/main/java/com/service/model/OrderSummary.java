package com.service.model;

import lombok.Data;

import java.util.List;

@Data
public class OrderSummary extends UserModel{
    private Long numOfOrder;
    private List<Long> currentOrderIds;
    private Double totalAmount;
    private String addressLine;
    public void setUserModel(UserModel userModel){
        this.setToken(userModel.getToken());
        this.setId(userModel.getId());
        this.setAddress(userModel.getAddress());
        this.setLat(userModel.getLat());
        this.setLon(userModel.getLon());
        this.setRoles(userModel.getRoles());
        this.setPhone(userModel.getPhone());
        this.setName(userModel.getName());
    }
}
