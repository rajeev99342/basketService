package com.service.constants.values;

import org.springframework.stereotype.Component;

@Component
public class IpAddress {
    public static final String RAJEEV_MOBILE_IP = "192.168.0.104";
    public String getAddress(String ip){
        return "http://"+RAJEEV_MOBILE_IP+":8100";
    }
}
