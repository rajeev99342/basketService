package com.service.controller;

import com.service.constants.enums.ShopType;
import com.service.model.GlobalResponse;
import com.service.model.seller.SellerDetailModel;
import com.service.service.seller.SellerServiceHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/seller")
public class SellerController {
    @Value(value = "${melaa.shopType}")
    List<ShopType> shopTypes;
    @Autowired
    private SellerServiceHandler sellerServiceHandler;
    @CrossOrigin(origins = "*")
    @GetMapping("/getSeller")
    public GlobalResponse getCompleteOrder(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return sellerServiceHandler.getSellerList(page,size);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/saveSellerDetails")
    public GlobalResponse saveSellerDetails(@RequestBody SellerDetailModel sellerDetailModel) {
        return sellerServiceHandler.saveSellerDetails(sellerDetailModel);
    }


    @CrossOrigin(origins = "*")
    @GetMapping("/getShops")
    public GlobalResponse getShops() {
        return GlobalResponse.getSuccess(shopTypes);
    }
}
