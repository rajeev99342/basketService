package com.service.controller;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.service.model.AddressModel;
import com.service.model.GlobalResponse;
import com.service.model.Properties;
import com.service.model.PropertiesModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;

@CrossOrigin(origins = "*")
@Slf4j
@RestController
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Transactional
@RequestMapping("/v1/property")
public class PropertiesController {

    @Autowired
    Properties properties;
    @GetMapping("/fetch")
    public GlobalResponse getUserAddress() {
        PropertiesModel model = new PropertiesModel();
        model.setDeliveryHrs(properties.getDeliveryHrs());
        model.setOrderMoreThan(properties.getOrderMoreThan());
        model.setShippingCharge(properties.getShippingCharge());
        return GlobalResponse.getSuccess(model);
    }

}
