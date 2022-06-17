package com.service.model;

import lombok.Data;

import java.util.List;

@Data
public class DisplayProductModel {
    private Long id;
    private ProductModel model;
    private List<Object> images;
}
