package com.service.model;

import lombok.Data;

@Data
public class ProductModel {
 private  Long id;
   private String name ;
    private Double pricePerUnit;
    private String unit;
    private Double quantity;
    private String desc;
    private String brand;
    private Double sellingPrice;
    private Double discount ;
    private Integer inStock;
    private CategoryModel category;
}
