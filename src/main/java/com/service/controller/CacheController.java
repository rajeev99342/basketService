package com.service.controller;

import com.service.rediscache.CacheHandler;
import com.service.model.CartProductMappingModel;
import com.service.model.GlobalResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/cache")
public class CacheController {

    @Autowired
    CacheHandler cacheHandler;

    @DeleteMapping("/clearAll")
    GlobalResponse addToCart() {
        try {
            return cacheHandler.clearCache();
        } catch (Exception e) {
            e.printStackTrace();
            return new GlobalResponse("Failed " + e, 500);
        }
    }
}
