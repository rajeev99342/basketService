package com.service.scheduler;


import com.fasterxml.jackson.databind.util.JSONPObject;
import com.google.gson.Gson;
import com.service.model.OutputMessage;
import com.service.model.WebSocketMessageModel;
import com.service.websocket.WebSocketMessageSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class TestScheduler {
    @Autowired
    WebSocketMessageSender webSockerSender;

    @Scheduled(cron = "*/20 * * * * *")
    public void sendToWebSocket() {
        System.out.println("helo");
        OutputMessage outputMessage = new OutputMessage();
        outputMessage.setFrom("Rajeev");
        outputMessage.setText("My name is rajeev");
        outputMessage.setTime(new Date().toString());
        WebSocketMessageModel webSocketMessageModel = new WebSocketMessageModel();
        webSocketMessageModel.setName("ee");
        webSockerSender.sendMessageToHelloDestination("/topic/new-order", new Gson().toJson(webSocketMessageModel));
    }
}
