package com.service.model;

import lombok.Data;

@Data
public class AddressModel {
    private Long id;
    private String city;
    private String mobile;
    private String landmark;
    private String area;
    private String pincode;
    private String addressOne;
    private Boolean isDefault;
}
