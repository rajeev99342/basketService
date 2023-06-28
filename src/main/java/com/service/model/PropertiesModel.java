package com.service.model;

import lombok.Data;

import java.util.List;


@Data
public class PropertiesModel {
    private List<Integer> deliveryHrs;
    private Double orderMoreThan;
    private Double shippingCharge;
}
