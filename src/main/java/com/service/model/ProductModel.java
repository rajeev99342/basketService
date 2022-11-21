package com.service.model;

import lombok.Data;

import java.util.List;

@Data
public class ProductModel {
 private  Long id;
   private String name ;
    private Double priceForGivenUnit;
    private String unit;
    private Double quantity;
    private String desc;
    private String brand;
    private Double sellingPrice;
    private Double discount ;
    private Integer inStock;
    private Boolean isValid;
    private Long sellerId;
    private CategoryModel category;
    private List<QuantityModel> quantityModelList;
}
