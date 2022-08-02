package com.service.controller;

import com.service.constants.enums.OrderStatus;
import com.service.model.*;
import com.service.service.CartService;
import com.service.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin(value = "*")
@RestController
public class OrderController {

    @Autowired
    OrderService orderService;

    @CrossOrigin(value = "*")
    @PostMapping("/place-order")
    List<ProductWiseOrder> placeOrder(@RequestBody OrderModel orderModel) {
        try {
            return orderService.placeOrder(orderModel);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    @CrossOrigin(value = "*")
    @PostMapping("/get-order-by-user")
    List<ProductWiseOrder> getOrder(@RequestParam("token") String token) {
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
        // for admin
    List<OrderWiseProduct> getOrderAllOrderByStatus(@RequestParam("token") String token, String status) {
        List<OrderWiseProduct> orderWiseProducts = new ArrayList<>();
        try {
            if (OrderStatus.PLACED.name().equals(status)) {
                orderService.fetchAllOrderByDate(token, OrderStatus.PLACED);
            } else if (OrderStatus.ON_THE_WAY.name().equals(status)) {
                orderService.fetchAllOrderByDate(token, OrderStatus.ON_THE_WAY);
            } else if (OrderStatus.PACKING.name().equals(status)) {
                orderService.fetchAllOrderByDate(token, OrderStatus.PACKING);
            } else if (OrderStatus.DISPATCHED.name().equals(status)) {
                orderService.fetchAllOrderByDate(token, OrderStatus.DISPATCHED);
            } else  if(OrderStatus.DELIVERED.name().equals(status)){
                orderService.fetchAllOrderByDate(token,OrderStatus.DELIVERED);
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
