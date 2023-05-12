package com.service.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.service.messaging.FcmClient;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
@EnableScheduling
public class FirebasePushNotificationService {

    private final RestTemplate restTemplate;
    private final FcmClient fcmClient;

    private int id = 0;

    public FirebasePushNotificationService( FcmClient fcmClient) {
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
        this.fcmClient.sendNotification(data);
    }


    void sendBulkPushMessage(Map<String,String> data,List<String> tokens) throws ExecutionException, InterruptedException {
        this.fcmClient.multiCaseMessage(data,tokens);
    }



    public void notifyAllAdmin(List<String> deviceTokens, String title, String body) {
        MulticastMessage message = MulticastMessage.builder()
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .setImage("https://firebasestorage.googleapis.com/v0/b/baba-basket-645b9.appspot.com/o/order-image%2Fnew_order.jpg?alt=media&token=2dee986b-6bcc-4ae6-b4f0-90c4fdc872b8")
                        .build())
                .addAllTokens(deviceTokens)
                .build();

        FirebaseMessaging.getInstance().sendMulticastAsync(message);
    }

}
