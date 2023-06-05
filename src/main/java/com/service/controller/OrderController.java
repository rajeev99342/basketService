package com.service.controller;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.service.constants.enums.OrderStatus;
import com.service.entities.User;
import com.service.jwt.JwtTokenUtility;
import com.service.model.*;
import com.service.service.OrderService;
import com.service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@CrossOrigin(value = "*")
@RestController
public class OrderController {


    @Autowired
    UserService userService;
    @Autowired
    OrderService orderService;

    @Autowired
    JwtTokenUtility jwtTokenUtility;

    @CrossOrigin(value = "*")
    @PostMapping("/place-order")
    GlobalResponse placeOrder(@RequestBody OrderModel orderModel) {
        try {
            return orderService.placeOrder(orderModel);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    @CrossOrigin(value = "*")
    @PostMapping("/get-order-by-user")
    List<OrderRS> getOrder(@RequestBody RequestModel requestModel,@RequestParam(defaultValue = "0") String olderDays,@RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size) {
        try {
            return orderService.getOrderDetails(requestModel.getToken(), requestModel.getOrderStatusList(),olderDays,page,size);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    @CrossOrigin(value = "*")
    @PostMapping("/cancel-order")
    Boolean cancelOrder(@RequestBody RequestModel requestModel) {
        try {
            String userPhone = jwtTokenUtility.getUsernameFromToken(requestModel.getToken());
            User user = userService.getUserByPhoneNumber(userPhone);
            return orderService.cancelOrder(requestModel.getId(), user);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @CrossOrigin(value = "*")
    @PostMapping("/request-return")
    GlobalResponse returnRequest(@RequestBody RequestModel requestModel) {
        String userPhone = jwtTokenUtility.getUsernameFromToken(requestModel.getToken());
        User user = userService.getUserByPhoneNumber(userPhone);
        return orderService.returnRequest(requestModel.getId(), user);

    }


    @CrossOrigin(value = "*")
    @PostMapping("/update-packing-order")
    Boolean packingOrder(@RequestParam("time") Integer time , @RequestBody Long id) {
        try {
            System.out.println("-------------------");
            System.out.println(time);
            return orderService.packingOrder(id,time);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    @CrossOrigin(value = "*")
    @PostMapping("/marked-delivered")
    Boolean markedDelivered(@RequestBody Long id) {
        try {
            return orderService.markedDelivered(id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @CrossOrigin(value = "*")
    @PostMapping("/update-on-the-way-order")
    Boolean updateOnTheWay(@RequestBody Long id) {
        try {
            return orderService.updateOnTheWay(id);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @CrossOrigin(value = "*")
    @PutMapping("/do-refund")
    GlobalResponse refund(@RequestBody RefundOrder refundOrder) {
        return orderService.doRefund(refundOrder);
    }


    @CrossOrigin(value = "*")
    @PostMapping("/get-order-by-status")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @Transactional

        // for admin
    List<OrderRS> getOrderAllOrderByStatus(@RequestParam("token") String token, String status) {
        List<OrderRS> orderWiseProducts = new ArrayList<>();
        try {
            if (OrderStatus.PLACED.name().equals(status)) {
                return orderService.fetchAllOrderByStatus(token, OrderStatus.PLACED);
            } else if (OrderStatus.ON_THE_WAY.name().equals(status)) {
                return orderService.fetchAllOrderByStatus(token, OrderStatus.ON_THE_WAY);
            } else if (OrderStatus.ACCEPTED.name().equals(status)) {
                return orderService.fetchAllOrderByStatus(token, OrderStatus.ACCEPTED);
            } else if (OrderStatus.DISPATCHED.name().equals(status)) {
                return orderService.fetchAllOrderByStatus(token, OrderStatus.DISPATCHED);
            } else if (OrderStatus.DELIVERED.name().equals(status)) {
                return orderService.fetchAllOrderByStatus(token, OrderStatus.DELIVERED);
            } else if (OrderStatus.RETURN_INITIATED.name().equals(status)) {
                return orderService.fetchAllOrderByStatus(token, OrderStatus.RETURN_INITIATED);
            } else {
                System.out.println("No order");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return orderWiseProducts;
    }



    @CrossOrigin(value = "*")
    @GetMapping("/order-details-by-id")
    GlobalResponse getOrderDetailsById(@RequestParam("id") Long id) {
        try {
            return orderService.getOrderById(id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}
