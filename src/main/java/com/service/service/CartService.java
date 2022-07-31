package com.service.service;

import com.service.entities.*;
import com.service.jwt.JwtTokenUtility;
import com.service.model.CartDeleteModel;
import com.service.model.CartProductMappingModel;
import com.service.model.DisplayCartProduct;
import com.service.model.UserCredentials;
import com.service.repos.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class CartService {
    @Autowired
    ProductRepo productRepo;

    @Autowired
    UserRepo userRepo;


    @Autowired
    CartRepo cartRepo;

    @Autowired
    CartDetailsRepo cartDetailsRepo ;

    @Autowired
    ImageRepository imageRepository;

    @Autowired
    ProductService productService;

    @Autowired
    ImageService imageService;
    @Autowired
    InstockRepo instockRepo;
    @Autowired
    JwtTokenUtility jwtTokenUtility;

    public void addToCart(CartProductMappingModel cartProductMappingModel) {
        Cart cart = cartRepo.findCartByUser(userRepo.findUserByPhone(cartProductMappingModel.getUserPhone()));
        if(null == cart){
           return;
        }

        List<CartDetails> cartDetailsListByCartOrUser = cartDetailsRepo.findCartDetailsByCart(cart);

        CartDetails cartDetails = new CartDetails();
        for (CartDetails cartDetails1 : cartDetailsListByCartOrUser){
            Product productInCartDetails = productRepo.getById(cartProductMappingModel.getProductId());
            if(productInCartDetails.getId().equals(cartDetails1.getProduct().get(0).getId())){
                cartDetails = cartDetails1;
                break;
            }
        }
        cartDetails.setCart(cart);
        List<Product> list = new ArrayList<>();
        list.add(productRepo.getById(cartProductMappingModel.getProductId()));
        cartDetails.setProduct(list);
        cartDetails.setSelectedSize(cartProductMappingModel.getSelectedProductSize());
        cartDetails.setSelectedWeight(cartProductMappingModel.getSelectedProductWeight());
        cartDetails.setSelectedCount(cartProductMappingModel.getSelectedProductCount());
        if(null == cartDetails.getId()){
            cartDetails.setCreatedAt(new Date(System.currentTimeMillis()));
        }
        cartDetails.setUpdatedAt(new Date(System.currentTimeMillis()));
        cartDetailsRepo.save(cartDetails);
    }

    public List<DisplayCartProduct> getProductByCartDetails(String token){
        String phone = jwtTokenUtility.getUsernameFromToken(token);
         Cart cart = cartRepo.findCartByUser(userRepo.findUserByPhone(phone));
         List<CartDetails> cartDetails = cartDetailsRepo.findCartDetailsByCart(cart);
         List<DisplayCartProduct> displayCartProducts = new ArrayList<>();

         for(CartDetails cartDetails1 : cartDetails){
             DisplayCartProduct displayCartProduct = new DisplayCartProduct();
             Product  product = productRepo.getById(cartDetails1.getProduct().get(0).getId());
             List<Image> images = imageRepository.findImageByProduct(product);
             List<Object> imageLinkList = new ArrayList<>();
             for(Image image : images){
                 Object img = imageService.getImage(image.getId());
                 imageLinkList.add(img);
             }
             displayCartProduct.setInStock(instockRepo.findStockByProduct(product).getInStock());
             displayCartProduct.setImages(imageLinkList);
             displayCartProduct.setModel(productService.getProductModelByProduct(product));
             displayCartProduct.setSelectedCount(cartDetails1.getSelectedCount());
             displayCartProduct.setSelectedSize(cartDetails1.getSelectedSize());
             displayCartProduct.setSelectedWeight(cartDetails1.getSelectedWeight());
             displayCartProduct.setId(product.getId());
             displayCartProducts.add(displayCartProduct);
         }
         return displayCartProducts;
    }

    public void createCartByUser(User user) {
        Cart cart = new Cart();
        cart.setUser(user);
        cart.setCreatedAt(new Date(System.currentTimeMillis()));
        cartRepo.save(cart);
    }
    @Transactional
    public Boolean deleteCartItem(CartDeleteModel cartDeleteModel) {
        User user = userRepo.findUserByPhone(cartDeleteModel.getUserPhone());
        Cart cart = cartRepo.findCartByUser(user);
        Product product = productRepo.getById(cartDeleteModel.getProductId());
        CartDetails cartDetails = cartDetailsRepo.findCartDetailsByCartAndProduct(cart,product);
        cartDetailsRepo.delete(cartDetails);
        return true;
    }

//    public List<DisplayCartProduct> isProductAlreadyPresent(String jwt, Long productId) {
//        User user = userRepo.findUserByPhone(jwtTokenUtility.getUsernameFromToken(jwt));
//        Cart cart = cartRepo.findCartByUser(user);
//        CartDetails
//    }
}
