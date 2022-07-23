package com.service.service;

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
    public void sendChuckQuotes() {
        System.out.println("Scheduled : => ");
        String message = "Hi from firebase";
        sendPushMessage(HtmlEscape.unescapeHtml(message));
    }



    void sendPushMessage(String joke) {
        Map<String, String> data = new HashMap<>();
        data.put("id", String.valueOf(++this.id));
        data.put("text", joke);

        // Send a message
        System.out.println("Sending chuck joke...");
        try {
            this.fcmClient.sendJoke(data);
        }
        catch (InterruptedException | ExecutionException e) {
           e.printStackTrace();
        }
    }
}
