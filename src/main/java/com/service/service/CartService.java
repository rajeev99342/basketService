package com.service.service;

import com.service.entities.Cart;
import com.service.entities.CartProductMapping;
import com.service.model.CartProductMappingModel;
import com.service.repos.CartProductRepo;
import com.service.repos.CartRepo;
import com.service.repos.ProductRepo;
import com.service.repos.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.crypto.Data;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class CartService {
    @Autowired
    ProductRepo productRepo;

    @Autowired
    UserRepo userRepo;


    @Autowired
    CartRepo cartRepo;

    public void addToCart(CartProductMappingModel cartProductMappingModel) {
        Cart cart = cartRepo.findCartByProductAndUser(productRepo.getById(cartProductMappingModel.getProductId()), userRepo.getById(cartProductMappingModel.getUserId()));
        if(null == cart){
            cart = new Cart();
        }
        cart.setSelectedCount(cartProductMappingModel.getSelectedProductCount());
        cart.setSelectedSize(cartProductMappingModel.getSelectedProductSize());
        cart.setSelectedWeight(cartProductMappingModel.getSelectedProductWeight());
        cart.setUser(userRepo.getById(cartProductMappingModel.getUserId()));
        cart.setCreatedAt(new Date(System.currentTimeMillis()));
        cart.setUpdatedAt(new Date(System.currentTimeMillis()));
        cart.setProduct(productRepo.getById(cartProductMappingModel.getProductId()));
        cartRepo.save(cart);
    }
}
