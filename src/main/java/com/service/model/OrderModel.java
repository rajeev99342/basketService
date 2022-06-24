package com.service.model;

import lombok.Data;

import java.util.List;

@Data
public class OrderModel {
    private String userPhone;
    private List<DisplayCartProduct> cartProducts;
    private PaymentMode paymentMode;
    private final Double finalAmount;
    private final  Long addressId;
}
