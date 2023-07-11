package com.service.config;

import com.service.model.CategoryDisplayModel;
import com.service.model.DisplayProductModel;
import lombok.Data;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Data
public class StaticMapConfig {
   public static Map<String, List<DisplayProductModel>> HOME_PAGE_CATEGORY_PRODUCTS = new HashMap<>();
   public static List<CategoryDisplayModel> DISPLAY_CATEGORY = new ArrayList<>();

}
