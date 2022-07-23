package com.service.utilites;

import org.springframework.stereotype.Component;

@Component
public class ImageUtility {
    public String getImageName(String type , String name){
        return type+"-"+name + "-"+ System.currentTimeMillis();
    }
}
