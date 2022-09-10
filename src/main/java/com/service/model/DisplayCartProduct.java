package com.service.model;

import lombok.Data;

import java.util.List;

@Data
public class DisplayCartProduct {
    private Long id;
    private ProductModel model;
    private List<Object> images;
    private Double selectedWeight;
    private Integer selectedSize;
    private Integer selectedCount;
    private Double eachProductPrice;
    private Double totalSumPriceOfThisProduct;
    private Integer inStock;
    private QuantityModel quantityModel;
    private Long cartDetailsId;

}
