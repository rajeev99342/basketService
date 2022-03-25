package com.service.controller;
import com.google.gson.Gson;
import com.service.entities.Category;
import com.service.entities.ImageDetails;
import com.service.model.CategoryDisplayModel;
import com.service.model.CategoryModel;
import com.service.model.GlobalResponse;
import com.service.model.ProductModel;
import com.service.service.CategoryService;
import com.service.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
public class ProductController {


    @CrossOrigin(origins = "*")
    @PostMapping("/add-product")
    public GlobalResponse saveProduct(@RequestParam("product") String product,@RequestParam("file") MultipartFile image){
        GlobalResponse globalResponse = new GlobalResponse();
        try {
            String productString = product;
            Gson gson = new Gson();
            ProductModel categoryModel = gson.fromJson(productString, ProductModel.class);
            System.out.println(categoryModel);
        }catch (Exception e){
            e.printStackTrace();
            globalResponse.setMessage("Failed");
        }

        return globalResponse;
    }

}
