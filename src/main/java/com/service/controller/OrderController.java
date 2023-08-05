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
    GlobalResponse placeOrder(@RequestBody OrderModel orderModel) throws Exception {
        return orderService.placeOrder(orderModel);
    }


    @CrossOrigin(value = "*")
    @GetMapping("/get-order-by-user")
    GlobalResponse getOrder(@RequestParam("token") String token, @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size) {
        return orderService.getOrderDetails(token, page, size);
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
    @PutMapping("/updateOrderStatus")
    GlobalResponse packingOrder(@RequestBody UpdateOrderRs updateOrderRs) {
        if (updateOrderRs.getStatus().equals(OrderStatus.PLACED)) {
            return orderService.packingOrder(updateOrderRs);
        } else if (updateOrderRs.getStatus().equals(OrderStatus.SEND_TO_SELLER)) {
            return orderService.sendToShop(updateOrderRs);
        } else if (updateOrderRs.getStatus().equals(OrderStatus.CONFIRMED_FROM_SELLER) || updateOrderRs.getStatus().equals(OrderStatus.NOT_AVAILABLE)) {
            return orderService.updateOrderBySeller(updateOrderRs);
        } else if (updateOrderRs.getStatus().equals(OrderStatus.ON_THE_WAY)) {
            return orderService.markedOrderOnTheWay(updateOrderRs);
        }else if (updateOrderRs.getStatus().equals(OrderStatus.ACCEPTED)) {
            return orderService.assignToDelivery(updateOrderRs);
        }
        else if (updateOrderRs.getStatus().equals(OrderStatus.DELIVERED)) {
            return orderService.markedDeliveryDoneStatus(updateOrderRs);
        }
        return null;
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
                    PageRequest.of(page, size, Sort.by("orderDate").descending());

            log.info(">>>>>>>>>>>>>> >>>> fetch Order By {} Status", status);
            if (OrderStatus.PLACED.name().equals(status)) {
                return orderService.fetchAllOrderByStatus(token, OrderStatus.PLACED, pageable);
            } else if (OrderStatus.ON_THE_WAY.name().equals(status)) {
                return orderService.fetchAllOrderByStatus(token, OrderStatus.ON_THE_WAY, pageable);
            } else if (OrderStatus.ACCEPTED.name().equals(status)) {
                return orderService.fetchAllOrderByStatus(token, OrderStatus.ACCEPTED, pageable);
            } else if (OrderStatus.DISPATCHED.name().equals(status)) {
                return orderService.fetchAllOrderByStatus(token, OrderStatus.DISPATCHED, pageable);
            } else if (OrderStatus.DELIVERED.name().equals(status)) {
                return orderService.fetchAllOrderByStatus(token, OrderStatus.DELIVERED, pageable);
            } else if (OrderStatus.RETURN_INITIATED.name().equals(status)) {
                return orderService.fetchAllOrderByStatus(token, OrderStatus.RETURN_INITIATED, pageable);
            } else if (OrderStatus.CANCELED.name().equals(status)) {
                return orderService.fetchAllOrderByStatus(token, OrderStatus.CANCELED, pageable);
            } else {
                System.out.println("No order");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }


    @GetMapping("/getByStatuses")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @Transactional
    GlobalResponse getOrderAllOrderByStatuses(@RequestParam("token") String token, @RequestParam("status") List<OrderStatus> statuses, @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "10") int size) {
        List<OrderRS> orderWiseProducts = new ArrayList<>();
        try {
            Pageable pageable = PageRequest.of(page, size);
            log.info(">>>>>>>>>>>>>>  fetch Order By {} Status", statuses);
            return orderService.fetchAllOrderByStatus(token, statuses, pageable);
        } catch (Exception e) {
            log.error("Failed to fetch order list by status due to : {} ", e.getLocalizedMessage());
            return null;
        }
    }

    @GetMapping("/getOrderForSeller")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @Transactional
    GlobalResponse getAllOrderFromShops(@RequestParam("status") OrderStatus status, @RequestParam("sellerPhone") String sellerPhone, @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "10") int size) {
        List<OrderRS> orderWiseProducts = new ArrayList<>();
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("modified_date").descending());
            log.info(">>>>>>>>>>>>>> >>>> fetch Order from shops");
            return orderService.getAllOrderFromShops(status, sellerPhone, pageable);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    @GetMapping("/getOrderFromShopsForAdmin")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @Transactional
    GlobalResponse getAllOrderFromShops(@RequestParam("status") OrderStatus status, @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "10") int size) {
        List<OrderRS> orderWiseProducts = new ArrayList<>();
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("modified_date").descending());
            log.info(">>>>>>>>>>>>>> >>>> fetch Order from shops");
            return orderService.getAllOrderFromShopsForAdmin(status, pageable);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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

    @CrossOrigin(value = "*")
    @GetMapping("/getOrderForDelivery")
    GlobalResponse getOrderDetailsToBeDeliver(@RequestParam("status") OrderStatus status, @RequestParam("deliveryPhone") String deliveryPhone
            , @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return orderService.getOrderListToDeliver(status, deliveryPhone, page, size);
    }


}
