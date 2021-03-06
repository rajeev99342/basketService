package com.service.controller;
import com.google.gson.Gson;
import com.service.entities.Category;
import com.service.entities.ImageDetails;
import com.service.entities.Product;
import com.service.model.*;
import com.service.service.CategoryService;
import com.service.service.ImageService;
import com.service.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
public class ProductController {

    @Autowired
    ProductService productService;

    @CrossOrigin(origins = "*")
    @PostMapping("/add-product")
    public GlobalResponse saveProduct(@RequestParam("product") String product,@RequestParam("document") List<MultipartFile> images){
        GlobalResponse globalResponse = new GlobalResponse();
        try {
            String productString = product;
            Gson gson = new Gson();
            ProductModel productModel = gson.fromJson(productString, ProductModel.class);
            productService.addProduct(productModel,images);
            System.out.println(productModel);
        }catch (Exception e){
            e.printStackTrace();
            globalResponse.setMessage("Failed");
        }

        return globalResponse;
    }


    @CrossOrigin(origins = "*")
    @GetMapping("/fetch-all-product")
    public List<DisplayProductModel> fetchAllProduct(){

        List<DisplayProductModel> list = new ArrayList<>();
        try {
            list  = productService.fetchAllProducts();
        }catch (Exception e){
            e.printStackTrace();
        }

        return list;
    }



    @CrossOrigin(origins = "*")
    @GetMapping("/get-product-details")
    public DisplayProductModel getProductDetails(@RequestParam("id") Long id){

        DisplayProductModel productModel = null;
        try {
            productModel  = productService.getProductDetails(id);
        }catch (Exception e){
            e.printStackTrace();
        }

        return productModel;
    }

}
