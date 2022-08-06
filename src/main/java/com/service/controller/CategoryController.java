package com.service.controller;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.firebase.database.annotations.NotNull;
import com.google.gson.Gson;
import com.service.entities.Category;
import com.service.entities.ImageDetails;
import com.service.model.CategoryDisplayModel;
import com.service.model.CategoryModel;
import com.service.model.GlobalResponse;
import com.service.service.CategoryService;
import com.service.service.ImageService;
import com.service.utilites.ImageUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
public class CategoryController {
    @Autowired
    ImageUtility imageUtility;
    @Autowired
    ImageService imageService;
    @Autowired
    CategoryService categoryService;


    @CrossOrigin(origins = "*")
    @PostMapping("/add-category")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    public GlobalResponse saveCategory(@RequestParam("file") MultipartFile file,@RequestParam("category") String category) {
        GlobalResponse globalResponse = new GlobalResponse();
        try {
            String categoryString = category;
            Gson gson = new Gson();
            CategoryModel categoryModel = gson.fromJson(categoryString, CategoryModel.class);
            String imageReference = imageUtility.getImageName("category", categoryModel.getCategoryName());
            GlobalResponse imageResponse = imageService.saveImage(file, imageReference);
            if (imageResponse.getHttpStatusCode() == HttpStatus.OK.value()) {

                globalResponse = categoryService.addCategory(categoryModel, (ImageDetails) imageResponse.getBody());
            }
        } catch (Exception e) {
            e.printStackTrace();
            globalResponse.setMessage("Failed");
        }

        return globalResponse;
    }




    @CrossOrigin(origins = "*")
    @PostMapping("/delete-category")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    public GlobalResponse deleteCategory(@RequestParam("id") Long id) {
        GlobalResponse globalResponse = new GlobalResponse();
        try {
            categoryService.deleteCategory(id);
        } catch (Exception e) {
            e.printStackTrace();
            globalResponse.setMessage("Failed");
        }

        return globalResponse;
    }


    @CrossOrigin(origins = "*")
    @GetMapping("/fetch-all-category")
    public List<CategoryDisplayModel> test() {
        return categoryService.getAllCategory();
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/fetch-all-category-name")
    public List<CategoryModel> fetchAllCategoryName() {
        return categoryService.fetchAllCategoryName();
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/get-category")
    public CategoryDisplayModel getCategoryById(@NotNull @RequestParam("id") Long id) {
        try {
            return categoryService.getCategoryById(id);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}
