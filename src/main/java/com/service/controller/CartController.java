package com.service.controller;

import com.service.model.*;
import com.service.service.CartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@Slf4j
public class CartController {
    @Autowired
    CartService cartService;

    @CrossOrigin(origins = "*")
    @PostMapping("/add-to-cart")
    GlobalResponse addToCart(@RequestBody CartProductMappingModel cartProductMappingModel) {
        try {
            return cartService.addToCart(cartProductMappingModel);
        } catch (Exception e) {

            return new GlobalResponse("Failed " + e, 500);
        }
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/fetch-product-by-cart")
    GlobalResponse getProducts(@RequestParam("token") String token) {
        return cartService.getProductByCartDetails(token);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/item-count")
    GlobalResponse getItemCount(@RequestParam("token") String token) {
        try {
            return cartService.getItemCount(token);
        } catch (Exception e) {
           log.error("----------->> Failed to get count toke : {}",token);
            return null;
        }
    }


    @CrossOrigin(value = "*")
    @PostMapping("/delete-cart-item")
    GlobalResponse deleteCartProduct(@RequestBody CartDeleteModel cartDeleteModel) {
        try {
            return cartService.deleteCartItem(cartDeleteModel);
        } catch (Exception e) {
            log.error("----------->> >>> Failed to delete cart item Ex : {}", e.getLocalizedMessage());
            return GlobalResponse.getFailure(e.getLocalizedMessage());
        }
    }

    @CrossOrigin(value = "*")
    @PostMapping("/update-cart-item-count")
    GlobalResponse updateCartItemCount(@RequestBody CartDetailsRequestModel cartDetailsRequestModel) {
        try {
            return cartService.updateCount(cartDetailsRequestModel);
        } catch (Exception e) {
            log.error("----------->> >>> Failed to update cart count Ex : {}", e.getLocalizedMessage());
            return null;
        }
    }


}
