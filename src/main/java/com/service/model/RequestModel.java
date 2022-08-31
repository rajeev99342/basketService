package com.service.model;

import com.service.constants.enums.OrderStatus;
import lombok.Data;

import java.util.List;

@Data
public class RequestModel {
    private Long id;
    private  String token;
    private List<OrderStatus> orderStatusList;

}
