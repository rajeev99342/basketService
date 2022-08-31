package com.service.service;

import com.service.entities.*;
import com.service.jwt.JwtTokenUtility;
import com.service.model.*;
import com.service.repos.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    CartDetailsRepo cartDetailsRepo;

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

    @Autowired
    QuantityRepo quantityRepo;

    public GlobalResponse addToCart(CartProductMappingModel cartProductMappingModel) {
        Cart cart = cartRepo.findCartByUser(userRepo.findUserByPhone(cartProductMappingModel.getUserPhone()));
        if (null == cart) {
            return null;
        }

        List<CartDetails> cartDetailsListByCartOrUser = cartDetailsRepo.findCartDetailsByCart(cart);
        Product product = null;
        CartDetails cartDetails = new CartDetails();
        for (CartDetails cartDetails1 : cartDetailsListByCartOrUser) {
            if (cartDetails1.getProduct().getId().equals(cartProductMappingModel.getProductId())) {
                cartDetails = cartDetails1;
               return new GlobalResponse("Product already present",HttpStatus.INTERNAL_SERVER_ERROR.value(),false,null);
            }
        }
        if (null == cartDetails.getId()) {
            product = productRepo.getById(cartProductMappingModel.getProductId());
        }
        cartDetails.setCart(cart);
        cartDetails.setProduct(product);
        cartDetails.setSelectedSize(cartProductMappingModel.getSelectedProductSize());
        cartDetails.setSelectedWeight(cartProductMappingModel.getSelectedProductWeight());
        cartDetails.setSelectedCount(cartProductMappingModel.getSelectedProductCount());
        if (null == cartDetails.getId()) {
            cartDetails.setCreatedAt(new Date(System.currentTimeMillis()));
        }
        cartDetails.setQuantity(quantityRepo.getById(cartProductMappingModel.getQuantityId()));
        cartDetails.setUpdatedAt(new Date(System.currentTimeMillis()));
        cartDetailsRepo.save(cartDetails);
        long count = cartDetailsRepo.getCountOfProductByCart(cart.getId());
        return new GlobalResponse("Added into cart", HttpStatus.CREATED.value(), true, count);
    }

    public List<DisplayCartProduct> getProductByCartDetails(String token) {
        List<DisplayCartProduct> displayCartProducts = new ArrayList<>();

        try {
            String phone = jwtTokenUtility.getUsernameFromToken(token);
            Cart cart = cartRepo.findCartByUser(userRepo.findUserByPhone(phone));
            List<CartDetails> cartDetails = cartDetailsRepo.findCartDetailsByCart(cart);

            for (CartDetails cartDetails1 : cartDetails) {
                DisplayCartProduct displayCartProduct = new DisplayCartProduct();
                Product product = productRepo.getById(cartDetails1.getProduct().getId());
                List<Image> images = imageRepository.findImageByProduct(product);
                List<Object> imageLinkList = new ArrayList<>();
                for (Image image : images) {
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
                if(cartDetails1.getQuantity() != null){
                    displayCartProduct.setQuantityModel(new QuantityModel(cartDetails1.getQuantity().getId(),
                            cartDetails1.getQuantity().getUnit(),
                            cartDetails1.getQuantity().getPrice(),
                            cartDetails1.getQuantity().getQuantity(),
                            true));
                }

                displayCartProducts.add(displayCartProduct);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
    public GlobalResponse deleteCartItem(CartDeleteModel cartDeleteModel) {
        User user = userRepo.findUserByPhone(cartDeleteModel.getUserPhone());
        Cart cart = cartRepo.findCartByUser(user);
        Product product = productRepo.getById(cartDeleteModel.getProductId());
        CartDetails cartDetails = cartDetailsRepo.findCartDetailsByCartAndProduct(cart, product);
        cartDetailsRepo.delete(cartDetails);
        Integer count = cartDetailsRepo.getCountOfProductByCart(cart.getId());
        return new GlobalResponse("Deleted cart item", HttpStatus.OK.value(), true, count);
    }

    public GlobalResponse getItemCount(String token) {
        String phone = jwtTokenUtility.getUsernameFromToken(token);
        Cart cart = cartRepo.findCartByUser(userRepo.findUserByPhone(phone));

        Integer count = cartDetailsRepo.getCountOfProductByCart(cart.getId());
        return new GlobalResponse("Cart item count fetched", HttpStatus.OK.value(), true, count);
    }

//    public List<DisplayCartProduct> isProductAlreadyPresent(String jwt, Long productId) {
//        User user = userRepo.findUserByPhone(jwtTokenUtility.getUsernameFromToken(jwt));
//        Cart cart = cartRepo.findCartByUser(user);
//        CartDetails
//    }
}
