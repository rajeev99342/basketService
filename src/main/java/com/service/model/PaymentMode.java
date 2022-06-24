package com.service.model;

import lombok.Data;

@Data
public class PaymentMode {
    private String method ;
    private String message ;
    private String icon;
    private Boolean isSelected;
    private Boolean available;
}
