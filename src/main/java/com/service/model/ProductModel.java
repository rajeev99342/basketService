package com.service.model;

import lombok.Data;

@Data
public class ProductModel {
   private String name ;
    private Double price;
    private Double weight;
    private String desc;
    private String brand;
    private Double sellingPrice;
    private Double discount ;
    private Integer inStock;
    private CategoryModel category;
}
