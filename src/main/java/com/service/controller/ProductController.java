package com.service.controller;

import com.google.gson.Gson;
import com.service.model.GlobalResponse;
import com.service.model.ProductModel;
import com.service.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
public class ProductController {

    @Autowired
    ProductService productService;

    @CrossOrigin(origins = "*")
    @PostMapping("/add-product")
    public GlobalResponse saveProduct(@RequestParam("product") String product, @RequestParam("files") List<MultipartFile> files) {
        GlobalResponse globalResponse = new GlobalResponse();
        try {
            String productString = product;
            Gson gson = new Gson();
            ProductModel productModel = gson.fromJson(productString, ProductModel.class);
            return productService.addProduct(productModel, files);
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
//    @Cacheable("products")
    public GlobalResponse fetchAllProduct(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10") int size) {
        return productService.fetchAllProducts(page,size);
    }
    @CrossOrigin(origins = "*")
    @GetMapping("/product-count")
    public GlobalResponse count() {
        return productService.getCount();
    }


    @CrossOrigin(origins = "*")
    @GetMapping("/fetch-all-product-by-category")
//    @Cacheable("productsByCatId")
    public GlobalResponse fetchAllProduct(@RequestParam("catId") Long catId,@RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "10") int size) {

            return productService.getProductsByCatId(catId,page,size);

    }


    @CrossOrigin(origins = "*")
    @GetMapping("/get-product-details")
    public GlobalResponse getProductDetails(@RequestParam("id") Long id) {
            return productService.getProductDetails(id);
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/search-product")
//    @Cacheable(value = "productSearchCache", key = "#searchTerm")
    public GlobalResponse search(@RequestParam(required = false) String searchTerm,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        return productService.searchProduct(searchTerm, page, size);

    }


    @CrossOrigin(origins = "*")
    @GetMapping("/random-add-product")
    public int add(@RequestParam("catId") Long catId,@RequestParam("path") String path) {
        try{
            return productService.addRandom(catId,path);
        }catch (Exception e){

        }
        return 0;
    }



    @GetMapping("/fetchAll")
    public GlobalResponse fetchAll() {
        return productService.fetchAll2();
    }


}
