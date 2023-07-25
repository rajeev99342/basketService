package com.service.controller;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.firebase.database.annotations.NotNull;
import com.google.gson.Gson;
import com.service.entities.ImageDetails;
import com.service.model.CategoryModel;
import com.service.model.GlobalResponse;
import com.service.service.CategoryService;
import com.service.service.image.ImageServiceImpl;
import com.service.utilites.ImageUtility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@CrossOrigin(origins = "*")
@RestController
public class CategoryController {
    @Autowired
    ImageUtility imageUtility;
    @Autowired
    ImageServiceImpl imageService;
    @Autowired
    CategoryService categoryService;


    @CrossOrigin(origins = "*")
    @PostMapping("/add-category")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    public GlobalResponse saveCategory(@RequestParam("files") List<MultipartFile> files, @RequestParam("category") String category) {
        GlobalResponse globalResponse = new GlobalResponse();
        try {
            String categoryString = category;
            Gson gson = new Gson();
            CategoryModel categoryModel = gson.fromJson(categoryString, CategoryModel.class);
            String imageReference = imageUtility.getImageName("category", categoryModel.getCategoryName());
            GlobalResponse imageResponse = imageService.saveImage(files.get(0), imageReference);
            if (imageResponse.getHttpStatusCode() == HttpStatus.OK.value()) {
                globalResponse = categoryService.addCategory(categoryModel, (ImageDetails) imageResponse.getBody());
            }
        } catch (Exception e) {
            log.error("Failed to category due to {}",e.getLocalizedMessage());
            globalResponse.setMessage("Failed to write the image due to "+e.getLocalizedMessage());
        }

        return globalResponse;
    }




    @CrossOrigin(origins = "*")
    @DeleteMapping("/delete-category")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    public GlobalResponse deleteCategory(@RequestParam("id") Long id) {

          return  categoryService.deleteCategory(id);

    }


    @CrossOrigin(origins = "*")
    @GetMapping("/fetch-all-category")
    public GlobalResponse test() {
        return categoryService.getAllCategory();
    }


    @CrossOrigin(origins = "*")
    @GetMapping("/getCategoryByPage")
    public GlobalResponse getCategoryByPage(@RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        return categoryService.getAllCategoryByPage(page,size);
    }



    @CrossOrigin(origins = "*")
    @GetMapping("/fetch-all-category-name")
    public GlobalResponse fetchAllCategoryName() {
        return categoryService.fetchAllCategoryName();
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/get-category")
    public GlobalResponse getCategoryById(@NotNull @RequestParam("id") Long id) {
            return categoryService.getCategoryById(id);
    }


}
