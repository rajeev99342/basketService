package com.service.controller;

import com.service.model.CartProductMappingModel;
import com.service.model.GlobalResponse;
import com.service.model.OrderModel;
import com.service.model.ProductWiseOrder;
import com.service.service.CartService;
import com.service.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(value = "*")
@RestController
public class OrderController {

    @Autowired
    OrderService orderService;

    @CrossOrigin(value = "*")
    @PostMapping("/place-order")
    List<ProductWiseOrder> placeOrder(@RequestBody OrderModel orderModel){
        try{
           return orderService.placeOrder(orderModel);

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }


    @CrossOrigin(value = "*")
    @GetMapping("/get-order-by-user")
    List<ProductWiseOrder> getOrder(@RequestParam("userPhone") String userPhone){
        try{
            return orderService.getOrderListByUser(userPhone);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }


}
