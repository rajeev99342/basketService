package com.service.service;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.service.messaging.FcmClient;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.unbescape.html.HtmlEscape;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
@EnableScheduling
public class FirebasePushNotificationService {

    private final RestTemplate restTemplate;
    private final FcmClient fcmClient;

    private int id = 0;

    public FirebasePushNotificationService(FcmClient fcmClient) {
        this.restTemplate = new RestTemplate();
        this.fcmClient = fcmClient;
    }


    //    @Scheduled(fixedDelay = 30000)
    public void sendChuckQuotes() throws ExecutionException, InterruptedException {
        System.out.println("Scheduled : => ");
        String message = "Hi from firebase";
        Map<String, String> data = new HashMap<>();
        data.put("id", String.valueOf(++this.id));
        data.put("text", "joke");
        data.put("order_status","ORDER PLACED");
        sendPushMessage(data);
    }


    void sendPushMessage(Map<String,String> data) throws ExecutionException, InterruptedException {
        this.fcmClient.sendJoke(data);
    }
}
