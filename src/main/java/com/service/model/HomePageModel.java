package com.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HomePageModel {
    Map<String, List<DisplayProductModel>> categoryWiseProduct = new HashMap<>();
    List<CategoryDisplayModel> categories = new ArrayList<>();
}
