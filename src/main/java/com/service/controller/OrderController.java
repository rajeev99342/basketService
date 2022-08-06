package com.service.controller;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.service.constants.enums.OrderStatus;
import com.service.model.*;
import com.service.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@CrossOrigin(value = "*")
@RestController
public class OrderController {

    @Autowired
    OrderService orderService;

    @CrossOrigin(value = "*")
    @PostMapping("/place-order")
    List<DeliveryProductDetails> placeOrder(@RequestBody OrderModel orderModel) {
        try {
            return orderService.placeOrder(orderModel);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    @CrossOrigin(value = "*")
    @PostMapping("/get-order-by-user")
    List<DeliveryProductDetails> getOrder(@RequestParam("token") String token) {
        try {
            return orderService.getOrderListByUser(token);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    @CrossOrigin(value = "*")
    @PostMapping("/cancel-order")
    Boolean cancelOrder(@RequestBody Long id) {
        try {
            return orderService.cancelOrder(id);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    @CrossOrigin(value = "*")
    @PostMapping("/get-order-by-status")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @Transactional

        // for admin
    List<OrderDetailsModel> getOrderAllOrderByStatus(@RequestParam("token") String token, String status) {
        List<OrderDetailsModel> orderWiseProducts = new ArrayList<>();
        try {
            if (OrderStatus.PLACED.name().equals(status)) {
               return orderService.fetchAllOrderByStatus(token, OrderStatus.PLACED);
            } else if (OrderStatus.ON_THE_WAY.name().equals(status)) {
                return  orderService.fetchAllOrderByStatus(token, OrderStatus.ON_THE_WAY);
            } else if (OrderStatus.PACKING.name().equals(status)) {
                return  orderService.fetchAllOrderByStatus(token, OrderStatus.PACKING);
            } else if (OrderStatus.DISPATCHED.name().equals(status)) {
                return  orderService.fetchAllOrderByStatus(token, OrderStatus.DISPATCHED);
            } else  if(OrderStatus.DELIVERED.name().equals(status)){
                return  orderService.fetchAllOrderByStatus(token,OrderStatus.DELIVERED);
            }else{
                System.out.println("No order");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return orderWiseProducts;
    }


}
