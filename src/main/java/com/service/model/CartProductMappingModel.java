package com.service.model;

import lombok.Data;

@Data
public class CartProductMappingModel {
    private Integer selectedProductCount;
    private Double selectedProductWeight;
    private Integer selectedProductSize;
    private Long productId;
    private String userPhone;
}
