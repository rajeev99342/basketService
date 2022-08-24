package com.service.model;

import com.service.constants.enums.OrderStatus;
import lombok.Data;

import java.util.List;

@Data
public class DeliveryProductDetails {
    private Long productId;
    private String productName;
    private Integer totalProductCount ;
    private OrderStatus orderStatus;
    private Double price;
    private String completeAddress;
    private String deliveryAgentDetails;
    private List<Object> image;
    private String orderDate;
    private String deliveryDate;
    private Long productDeliveryId;
    private Long orderId;

}
