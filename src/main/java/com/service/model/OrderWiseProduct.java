package com.service.model;

import com.service.constants.enums.OrderStatus;
import com.service.constants.enums.PaymentModeEnum;
import com.service.entities.User;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Data
public class OrderWiseProduct {

    private User user;

    private OrderStatus orderStatus;

    private Double totalCost;

    private PaymentModeEnum paymentMode;

    private Date orderDate;

    private String completeAddress;

    List<ProductWiseOrder> deliveryProducts;


}
