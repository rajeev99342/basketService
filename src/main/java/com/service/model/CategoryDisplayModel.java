package com.service.model;

import lombok.Builder;
import lombok.Data;

@Data
public class CategoryDisplayModel {
    private Long id;
    private String categoryName;
    private String categoryType;
    private Object base64;
}
