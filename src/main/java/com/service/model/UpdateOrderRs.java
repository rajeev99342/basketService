package com.service.model;

import com.service.constants.enums.OrderStatus;
import lombok.Data;

@Data
public class UpdateOrderRs {
    private Integer time;
    private String agent;
    private Long orderId;
    private OrderStatus status;
}
