package com.service.controller;

import com.service.config.StaticMapConfig;
import com.service.model.GlobalResponse;
import com.service.service.homepage.HomePageHandler;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/home")
public class HomePageController {


    private final HomePageHandler homePageHandler;

    public HomePageController(HomePageHandler homePageHandler) {
        this.homePageHandler = homePageHandler;
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/getHomePage")
    public GlobalResponse getCompleteOrder(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        StaticMapConfig staticMapConfig = new StaticMapConfig();
        return GlobalResponse.getSuccess(homePageHandler.getHomePageDataV2(page,size));
    }

}
