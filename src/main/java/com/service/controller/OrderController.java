package com.service.controller;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.service.constants.enums.OrderStatus;
import com.service.entities.User;
import com.service.jwt.JwtTokenUtility;
import com.service.model.*;
import com.service.service.OrderService;
import com.service.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin(value = "*")
@Slf4j
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
            log.info("+++ Order placed by : {}",orderModel.getUserPhone());
            return orderService.placeOrder(orderModel);
        } catch (Exception e) {
           log.error(">>> Failed to place order Ex : {}",e.getLocalizedMessage());
            return null;
        }
    }


    @CrossOrigin(value = "*")
    @GetMapping("/get-order-by-user")
    GlobalResponse getOrder(@RequestParam("status") List<OrderStatus> status, @RequestParam("days") String days, @RequestParam("token") String token, @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size) {

        return orderService.getOrderDetails(token, status, days, page, size);

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
    @PutMapping("/update-packing-order") // accept order
    void packingOrder(@RequestBody UpdateOrderRs updateOrderRs) {
        orderService.packingOrder(updateOrderRs);
    }


    @CrossOrigin(value = "*")
    @PutMapping("/marked-delivered")
    void markedDelivered(@RequestBody UpdateOrderRs updateOrder) {
             orderService.markedDelivered(updateOrder.getOrderId());
    }

    @CrossOrigin(value = "*")
    @PutMapping("/update-on-the-way-order") // after packing marked on the way
    void updateOnTheWay(@RequestBody UpdateOrderRs order) {
         orderService.updateOnTheWay(order.getOrderId());
    }

    @CrossOrigin(value = "*")
    @PutMapping("/do-refund")
    GlobalResponse refund(@RequestBody RefundOrder refundOrder) {
        return orderService.doRefund(refundOrder);
    }


    @GetMapping("/get-order-by-status")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @Transactional
    GlobalResponse getOrderAllOrderByStatus(@RequestParam("token") String token, String status, @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        List<OrderRS> orderWiseProducts = new ArrayList<>();
        try {
            Pageable pageable =
                    PageRequest.of(page, size);

            log.info(">>>> fetch Order By {} Status",status);
            if (OrderStatus.PLACED.name().equals(status)) {
                return orderService.fetchAllOrderByStatus(token, OrderStatus.PLACED,pageable);
            } else if (OrderStatus.ON_THE_WAY.name().equals(status)) {
                return orderService.fetchAllOrderByStatus(token, OrderStatus.ON_THE_WAY,pageable);
            } else if (OrderStatus.ACCEPTED.name().equals(status)) {
                return orderService.fetchAllOrderByStatus(token, OrderStatus.ACCEPTED,pageable);
            } else if (OrderStatus.DISPATCHED.name().equals(status)) {
                return orderService.fetchAllOrderByStatus(token, OrderStatus.DISPATCHED,pageable);
            } else if (OrderStatus.DELIVERED.name().equals(status)) {
                return orderService.fetchAllOrderByStatus(token, OrderStatus.DELIVERED,pageable);
            } else if (OrderStatus.RETURN_INITIATED.name().equals(status)) {
                return orderService.fetchAllOrderByStatus(token, OrderStatus.RETURN_INITIATED,pageable);
            } else {
                System.out.println("No order");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
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
