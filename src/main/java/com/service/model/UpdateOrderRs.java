package com.service.model;

import com.service.constants.enums.OrderStatus;
import lombok.Data;

import java.util.List;

@Data
public class UpdateOrderRs {
    private Integer time;
    private List<String> sellerPhones;
    private Long orderId;
    private OrderStatus status;
    private String buyer;
    private String deliveryAgent;
}
