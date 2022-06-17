package com.service.model;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CategoryModel {
    private Long id;
    private String categoryType;
    private String categoryName;
}
