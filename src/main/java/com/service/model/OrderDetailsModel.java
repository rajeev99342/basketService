package com.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.service.constants.enums.OrderStatus;
import com.service.constants.enums.PaymentModeEnum;
import com.service.entities.User;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class OrderDetailsModel {

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User user;

    private OrderStatus orderStatus;

    private Double totalCost;

    private PaymentModeEnum paymentMode;

    private Date orderDate;

    private String completeAddress;
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private List<ProductOrderDetails> deliveryProducts;
    private AddressModel addressModel;
    private Date expectedDeliveryDate ;
    private Long orderId;
    private String displayOrderStatus;
    private Boolean  isNew = true;
}
