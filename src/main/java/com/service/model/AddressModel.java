package com.service.model;

import lombok.Data;

@Data
public class AddressModel {
    private Long id;
    private String userPhone;
    private String city;
    private String mobile;
    private String landmark;
    private String area;
    private Long pincode;
    private String addressOne;
    private Boolean isDefault;
}
