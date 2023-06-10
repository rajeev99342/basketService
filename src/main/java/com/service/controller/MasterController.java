package com.service.controller;

import com.service.constants.enums.UserRole;
import com.service.model.GlobalResponse;
import com.service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/master")
public class MasterController {
    @Autowired
    UserService userService;

    @CrossOrigin(origins = "*")
    @GetMapping("/getUserCompleteDetails")
    public GlobalResponse getCompleteOrder(
            @RequestParam("role") String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return userService.userCompleteDetails(role,page, size);
    }
}
