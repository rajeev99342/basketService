package com.service.controller;

import com.github.javafaker.Faker;
import com.google.gson.Gson;
import com.service.model.DisplayProductModel;
import com.service.model.GlobalResponse;
import com.service.model.ProductModel;
import com.service.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public GlobalResponse saveProduct(@RequestParam("product") String product, @RequestParam("document") List<MultipartFile> images) {
        GlobalResponse globalResponse = new GlobalResponse();
        try {
            String productString = product;
            Gson gson = new Gson();
            ProductModel productModel = gson.fromJson(productString, ProductModel.class);
            return productService.addProduct(productModel, images);
        } catch (Exception e) {
            e.printStackTrace();
            globalResponse.setMessage("Failed");
        }

        return globalResponse;
    }


    @CrossOrigin(origins = "*")
    @PostMapping("/delete-product-quantity")
    public GlobalResponse deleteProductQuantity(@RequestParam("id") Long id, @RequestParam("token") String token) {
        GlobalResponse globalResponse = new GlobalResponse();
        try {
            return productService.deleteProductQuantity(id, token);
        } catch (Exception e) {
            e.printStackTrace();
            globalResponse.setMessage("Failed");
        }

        return globalResponse;
    }


    @CrossOrigin(origins = "*")
    @PostMapping("/delete-product")
    public GlobalResponse deleteProduct(@RequestParam("id") Long id) {
        GlobalResponse globalResponse = new GlobalResponse();
        try {
            return productService.deleteProduct(id);
        } catch (Exception e) {
            e.printStackTrace();
            globalResponse.setMessage("Failed");
        }

        return globalResponse;
    }


    @CrossOrigin(origins = "*")
    @GetMapping("/fetch-all-product")
    public List<DisplayProductModel> fetchAllProduct(@RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "10") int size) {
        List<DisplayProductModel> list = new ArrayList<>();
        try {
            list = productService.fetchAllProducts(page,size);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
    @CrossOrigin(origins = "*")
    @GetMapping("/product-count")
    public Long count() {
        return productService.getCount();
    }


    @CrossOrigin(origins = "*")
    @GetMapping("/fetch-all-product-by-category")
    public List<DisplayProductModel> fetchAllProduct(@RequestParam("catId") Long catId,@RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "10") int size) {

        List<DisplayProductModel> list = new ArrayList<>();
        try {
            list = productService.getProductsByCatId(catId,page,size);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }


    @CrossOrigin(origins = "*")
    @GetMapping("/get-product-details")
    public DisplayProductModel getProductDetails(@RequestParam("id") Long id) {
        DisplayProductModel productModel = null;
        try {
            productModel = productService.getProductDetails(id);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return productModel;
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/search-product")
    public List<DisplayProductModel> search(@RequestParam(required = false) String searchTerm,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        return productService.searchProduct(searchTerm, page, size);

    }


    @CrossOrigin(origins = "*")
    @GetMapping("/random-add-product")
    public int add() {
        try{
            return productService.addRandom();
        }catch (Exception e){

        }
        return 0;
    }

}
