package com.service.controller;

import com.service.entities.CartDetails;
import com.service.model.CartProductMappingModel;
import com.service.model.DisplayCartProduct;
import com.service.model.GlobalResponse;
import com.service.model.UserCredentials;
import com.service.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin(value = "*")
@RestController
public class CartController {
    @Autowired
    CartService cartService;

    @CrossOrigin(value = "*")
    @PostMapping("/add-to-cart")
    GlobalResponse addToCart(@RequestBody CartProductMappingModel cartProductMappingModel){
        try{
            cartService.addToCart(cartProductMappingModel);
            return new GlobalResponse("Success",200);
        }catch (Exception e){
            e.printStackTrace();
            return new GlobalResponse("Failed "+e,500);
        }
    }

    @CrossOrigin(value = "*")
    @PostMapping("/fetch-product-by-cart")
    List<DisplayCartProduct> getProducts(@RequestBody UserCredentials userCredentials){
        try{
            return cartService.getProductByCartDetails(userCredentials);
        }catch (Exception e){
            e.printStackTrace();
            return  null;
        }
    }
}
