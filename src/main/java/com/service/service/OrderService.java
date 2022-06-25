package com.service.service;

import com.service.constants.enums.OrderStatus;
import com.service.entities.*;
import com.service.model.DisplayCartProduct;
import com.service.model.OrderModel;
import com.service.model.ProductWiseOrder;
import com.service.repos.*;
import com.service.utilites.Payment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {
    @Autowired
    CartRepo cartRepo;
    @Autowired
    CartDetailsRepo cartDetailsRepo;

    @Autowired
    InstockRepo instockRepo;
    @Autowired
    Payment payment;

    @Autowired
    UserRepo userRepo;

    @Autowired
    OrderRepo orderRepo;

    @Autowired
    ProductRepo productRepo;

    @Autowired
    AddressRepo addressRepo;

    @Autowired
    ProductDeliveryRepo productDeliveryRepo;

    @Autowired
    ImageService imageService;

    public  List<ProductWiseOrder> placeOrder(OrderModel orderModel){
        List<ProductWiseOrder> productWiseOrders = new ArrayList<>();
        User user  = userRepo.findUserByPhone(orderModel.getUserPhone());
        Order order = new Order();
        order.setOrderDate(new Date(System.currentTimeMillis()));
        order.setOrderStatus(OrderStatus.PLACED);
        order.setPaymentMode(payment.getPaymentMode(orderModel.getPaymentMode().getMethod()));
        order.setUser(user);
        order.setTotalCost(orderModel.getFinalAmount());
        orderRepo.save(order);

        for(DisplayCartProduct cartProduct : orderModel.getCartProducts()){
            ProductWiseOrder productWiseOrder = new ProductWiseOrder();

            try{
                // check for product availability --> someone could have order this

                Stock stock = instockRepo.findStockByProduct(productRepo.getById(cartProduct.getId()));
                Integer inStock = stock.getInStock();
                if(inStock >=  cartProduct.getSelectedCount() && cartProduct.getSelectedCount() != 0){
                    Integer availableStockAfterThisOrder = inStock - cartProduct.getSelectedCount();
                    stock.setInStock(availableStockAfterThisOrder);
                    instockRepo.save(stock);
                    ProductDelivery productDelivery = new ProductDelivery();
                    productDelivery.setOrderStatus(OrderStatus.PLACED);
                    productDelivery.setProduct(productRepo.getById(cartProduct.getId()));
                    productDelivery.setDeliveryDate(new Date(System.currentTimeMillis()+86400000));
                    productDelivery.setOrder(order);
                    productDelivery.setOrderedTotalCount(cartProduct.getSelectedCount());
                    productDelivery.setOrderedTotalWeight(cartProduct.getSelectedWeight());
                    Address address = addressRepo.getById(orderModel.getAddressId());
                    productDelivery.setAddress(address);
                    String completeAddress = address.getLandmark() +", "+address.getAddressOne() + ", " + address.getArea() +", " + address.getCity() + "-"+
                            +address.getPincode() + ", Mobile - " + address.getMobile();
                    productDeliveryRepo.save(productDelivery);
                    productWiseOrder.setProductId(cartProduct.getId());
                    productWiseOrder.setOrderStatus(OrderStatus.PLACED);
                    productWiseOrder.setPrice(cartProduct.getEachProductPrice());
                    productWiseOrder.setCompleteAddress(completeAddress);
                    productWiseOrder.setDeliveryAgentDetails("Rajeev Kumar, Mobile - 9878979798");
                }else{
                    productWiseOrder.setProductId(cartProduct.getId());
                    productWiseOrder.setTotalProductCount(cartProduct.getSelectedCount());
                    productWiseOrder.setOrderStatus(OrderStatus.CANCELED_DUE_TO_OUT_OF_STOCK);
                }
            }catch (Exception e){
                productWiseOrder.setProductId(cartProduct.getId());
                productWiseOrder.setOrderStatus(OrderStatus.FAILED_DUE_TO_TECHNICAL_ISSUE);
            }

            productWiseOrders.add(productWiseOrder);

        }

                // delete from the cart
            Cart cart = cartRepo.findCartByUser(userRepo.findUserByPhone(orderModel.getUserPhone()));
            List<CartDetails> cartDetails = cartDetailsRepo.findCartDetailsByCart(cart);
            List<ProductWiseOrder> successfullyOrdredProduct = productWiseOrders.stream().
                filter(productWiseOrder -> productWiseOrder.getOrderStatus().equals(OrderStatus.PLACED)).collect(Collectors.toList());

            for(ProductWiseOrder productWiseOrder : successfullyOrdredProduct){
                    CartDetails cartDetails1 = cartDetailsRepo.findCartDetailsByCartAndProduct(cart,productRepo.getById(productWiseOrder.getProductId()));
                    cartDetailsRepo.delete(cartDetails1);
            }

        return productWiseOrders;

    }

    public List<ProductWiseOrder> getOrderListByUser(String userPhone) {
        User user = userRepo.findUserByPhone(userPhone);
        List<Order> orders = this.orderRepo.findOrderByUser(user);
        List<ProductWiseOrder> productWiseOrders = new ArrayList<>();

        for(Order order : orders){
            List<ProductDelivery> productsDeliveryList = productDeliveryRepo.findProductDeliveryByOrder(order);
            for(ProductDelivery productDelivery : productsDeliveryList){
                ProductWiseOrder productWiseOrder = new ProductWiseOrder();
                SimpleDateFormat parser = new SimpleDateFormat("dd-MM-yyyy");
                try {
                    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm");
                    String deliveryDate = formatter.format(productDelivery.getDeliveryDate());
                    String orderDate = formatter.format(productDelivery.getOrder().getOrderDate());
                    productWiseOrder.setDeliveryDate(deliveryDate);
                    productWiseOrder.setOrderDate(orderDate);
                } catch (Exception e) { // won't happen here
                    System.err.println("Invalid date");
                }
                productWiseOrder.setProductDeliveryId(productDelivery.getId());
                productWiseOrder.setImage(imageService.getAllImageByProduct(productDelivery.getProduct()));
                productWiseOrder.setProductId(productDelivery.getProduct().getId());
                productWiseOrder.setProductName(productDelivery.getProduct().getName());
                productWiseOrder.setOrderStatus(productDelivery.getOrderStatus());
                productWiseOrder.setPrice(productDelivery.getOrderedTotalCount()*productDelivery.getProduct().getSellingPrice());
                productWiseOrder.setDeliveryAgentDetails("Rajeev Kumar, Mobile - 9878979798");
                productWiseOrder.setTotalProductCount(productDelivery.getOrderedTotalCount());
                Address address = productDelivery.getAddress();
                String completeAddress = address.getLandmark() +", "+address.getAddressOne() + ", " + address.getArea() +", " + address.getCity() + "-"+
                        +address.getPincode() + ", Mobile - " + address.getMobile();


                productWiseOrder.setCompleteAddress(completeAddress);
                productWiseOrders.add(productWiseOrder);
            }
        }

        return productWiseOrders;
    }

    public Boolean cancelOrder(Long id) {
        ProductDelivery productDelivery = productDeliveryRepo.getById(id);
        productDelivery.setOrderStatus(OrderStatus.CANCELED);
        productDeliveryRepo.save(productDelivery);
        updateProductInventory(productDelivery.getProduct(),productDelivery.getOrderedTotalCount());
        return true;
    }

    public void updateProductInventory(Product product,Integer restoreCount){
            Stock stock = instockRepo.findStockByProduct(product);
            Integer inStock = stock.getInStock() + restoreCount;
            stock.setInStock(inStock);
            instockRepo.save(stock);
    }
}
