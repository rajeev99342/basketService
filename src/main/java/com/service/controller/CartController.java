package com.service.controller;

import com.service.entities.CartDetails;
import com.service.model.*;
import com.service.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
public class CartController {
    @Autowired
    CartService cartService;

    @CrossOrigin(origins = "*")
    @PostMapping("/add-to-cart")
    GlobalResponse addToCart(@RequestBody CartProductMappingModel cartProductMappingModel){
        try{
            return cartService.addToCart(cartProductMappingModel);
        }catch (Exception e){
            e.printStackTrace();
            return new GlobalResponse("Failed "+e,500);
        }
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/fetch-product-by-cart")
    List<DisplayCartProduct> getProducts(@RequestParam("token") String token){
        try{
            return cartService.getProductByCartDetails(token);
        }catch (Exception e){
            e.printStackTrace();
            return  null;
        }
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/item-count")
    GlobalResponse getItemCount(@RequestParam("token") String token){
        try{
            return cartService.getItemCount(token);
        }catch (Exception e){
            e.printStackTrace();
            return  null;
        }
    }


    @CrossOrigin(value = "*")
    @PostMapping("/delete-cart-item")
    GlobalResponse deleteCartProduct(@RequestBody CartDeleteModel cartDeleteModel){
        try{
            return cartService.deleteCartItem(cartDeleteModel);
        }catch (Exception e){
            e.printStackTrace();
            return  null;
        }
    }




}
