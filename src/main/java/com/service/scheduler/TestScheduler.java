package com.service.scheduler;


import com.google.gson.Gson;
import com.service.model.WebSocketMessageModel;
import com.service.websocket.WebSocketMessageSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class TestScheduler {
    @Autowired
    WebSocketMessageSender webSockerSender;

    @Scheduled(cron = "*/20 * * * * *")
    public void sendToWebSocket() {
//        WebSocketMessageModel webSocketMessageModel = new WebSocketMessageModel();
//        webSocketMessageModel.setName(message);
//        String topic = "/topic/new-order";
//        webSockerSender.notifyNewPlacedOrder(topic,webSocketMessageModel);
    }
}
