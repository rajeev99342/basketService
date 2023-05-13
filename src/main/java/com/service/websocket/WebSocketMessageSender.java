package com.service.websocket;

import com.google.gson.Gson;
import com.service.model.WebSocketMessageModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class WebSocketMessageSender {

    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public WebSocketMessageSender(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void notifyNewPlacedOrder(String dest, WebSocketMessageModel messageModel) {
        String json = new Gson().toJson(messageModel);
        messagingTemplate.convertAndSend(dest, json);
    }

    public void notifyUpdateOrderToUser(String phone,String dest, WebSocketMessageModel messageModel) {
        String json = new Gson().toJson(messageModel);
        dest = dest+phone;
        messagingTemplate.convertAndSend(dest, json);

//        messagingTemplate.convertAndSendToUser(phone,dest, json);
    }
}
