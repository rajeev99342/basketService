package com.service.model;

import com.service.entities.Category;
import lombok.Data;

import java.util.List;

@Data
public class HomePageData {
    private Category category;
    private List<DisplayProductModel> displayProductModelList;
}
