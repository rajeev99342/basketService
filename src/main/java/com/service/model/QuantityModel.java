package com.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuantityModel {
    private Long id;
    private String unit;
    private Double price;
    private Double quantity;
    private Boolean isChecked;
}
