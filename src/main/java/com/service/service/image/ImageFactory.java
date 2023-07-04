package com.service.service.image;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ImageFactory {
    @Autowired
    LocalStorageService localStorageService;
    @Autowired
    S3storageService s3storageService;

    public  StorageService getStorageType(String env){
            if(env.equals("prod")){
                return s3storageService;
            }
            return localStorageService;
    }
}
