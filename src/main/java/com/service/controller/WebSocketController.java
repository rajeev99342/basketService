package com.service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.model.OutputMessage;
import com.service.model.WebSocketMessageModel;
import com.service.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
@Controller
public class WebSocketController {

    @Autowired
    NotificationService notificationService;


    @MessageMapping("/message")
    @SendTo("/topic/messages")
    public OutputMessage send(@RequestBody  WebSocketMessageModel message) throws Exception {
        String time = new SimpleDateFormat("HH:mm").format(new Date());
        return new OutputMessage(message.getFrom(), message.getText(), time);
    }

    @MessageMapping("/notify-server")
    @SendToUser("/topic/notify-server")
    public OutputMessage clientServerCommunication(@RequestBody  WebSocketMessageModel message) throws Exception {
        ObjectMapper mapper =new ObjectMapper();
        Long userId =Long.parseLong( message.getFrom());
        System.out.println("ORDER PLACED BY USER"+mapper.writeValueAsString(message));
        String time = new SimpleDateFormat("HH:mm").format(new Date());
        notificationService.sendPrivateNotification(userId);
        return new OutputMessage(message.getFrom(), message.getText(), time);

    }

//    @MessageMapping("/topic/notify-server")
//    @SendToUser("/topic/notify-server")
//    public OutputMessage clientServerCommunication(@RequestBody  WebSocketMessageModel message) throws Exception {
//        ObjectMapper mapper =new ObjectMapper();
//        Long userId =Long.parseLong( message.getFrom());
//        System.out.println("ORDER PLACED BY USER"+mapper.writeValueAsString(message));
//        String time = new SimpleDateFormat("HH:mm").format(new Date());
//        return new OutputMessage(message.getFrom(), message.getText(), time);
//
//    }


}
