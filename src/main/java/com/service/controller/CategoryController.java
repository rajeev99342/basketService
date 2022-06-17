package com.service.controller;

import com.google.gson.Gson;
import com.service.entities.Category;
import com.service.entities.ImageDetails;
import com.service.model.CategoryDisplayModel;
import com.service.model.CategoryModel;
import com.service.model.GlobalResponse;
import com.service.service.CategoryService;
import com.service.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
public class CategoryController {
    @Autowired
    ImageService imageService;
    @Autowired
    CategoryService categoryService;


    @CrossOrigin(origins = "*")
    @PostMapping("/add-category")
    public GlobalResponse saveCategory(@RequestParam("category") String category,@RequestParam("file") MultipartFile image){
        GlobalResponse globalResponse = new GlobalResponse();
        try {
           GlobalResponse imageResponse =  imageService.saveImage(image);
           if(imageResponse.getHttpStatusCode() == HttpStatus.OK.value()){
               String categoryString = category;
               Gson gson = new Gson();
               CategoryModel categoryModel = gson.fromJson(categoryString, CategoryModel.class);
               globalResponse =  categoryService.addCategory(categoryModel, (ImageDetails) imageResponse.getBody());
           }
        }catch (Exception e){
            e.printStackTrace();
            globalResponse.setMessage("Failed");
        }

        return globalResponse;
    }


    @CrossOrigin(origins = "*")
    @GetMapping("/fetch-all-category")
    public List<CategoryDisplayModel> test(){
        return categoryService.getAllCategory();
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/fetch-all-category-name")
    public List<CategoryModel> fetchAllCategoryName(){
        return categoryService.fetchAllCategoryName();
    }

}
