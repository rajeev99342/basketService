package com.service.model;

import lombok.Data;

import javax.persistence.Column;

@Data
public class AddressModel {
    private Long id;
    private String userPhone;
    private String completeAddress;
    private String addressLine;
    private String latitude;
    private String longitude;
}
