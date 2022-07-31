package com.service.controller;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.service.model.GlobalResponse;
import com.service.service.FirebasePushNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin(origins = "*")
@RestController
public class FirebaseController {

    @Autowired
    FirebasePushNotificationService firebasePushNotificationService;
    @CrossOrigin(origins = "*")
    @GetMapping("/send-notification-to-user")
    public void sendNotification() {
        try{
            firebasePushNotificationService.sendChuckQuotes();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
