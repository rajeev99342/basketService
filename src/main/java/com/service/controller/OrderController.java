package com.service.controller;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.service.constants.enums.OrderStatus;
import com.service.entities.User;
import com.service.jwt.JwtTokenUtility;
import com.service.model.ProductOrderDetails;
import com.service.model.OrderDetailsModel;
import com.service.model.OrderModel;
import com.service.model.RequestModel;
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
    List<ProductOrderDetails> placeOrder(@RequestBody OrderModel orderModel) {
        try {
            return orderService.placeOrder(orderModel);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    @CrossOrigin(value = "*")
    @PostMapping("/get-order-by-user")
    List<OrderDetailsModel> getOrder(@RequestBody RequestModel requestModel) {
        try {
            return orderService.getOrderDetails(requestModel.getToken(),requestModel.getOrderStatusList());
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
            return orderService.cancelOrder(requestModel.getId(),user);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @CrossOrigin(value = "*")
    @PostMapping("/update-packing-order")
    Boolean packingOrder(@RequestBody Long id) {
        try {
            return orderService.packingOrder(id);

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
                return orderService.fetchAllOrderByStatus(token, OrderStatus.ON_THE_WAY);
            } else if (OrderStatus.PACKING.name().equals(status)) {
                return orderService.fetchAllOrderByStatus(token, OrderStatus.PACKING);
            } else if (OrderStatus.DISPATCHED.name().equals(status)) {
                return orderService.fetchAllOrderByStatus(token, OrderStatus.DISPATCHED);
            } else if (OrderStatus.DELIVERED.name().equals(status)) {
                return orderService.fetchAllOrderByStatus(token, OrderStatus.DELIVERED);
            } else {
                System.out.println("No order");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return orderWiseProducts;
    }


}
