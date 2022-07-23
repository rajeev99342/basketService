package com.service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.model.OutputMessage;
import com.service.model.WebSocketMessageModel;
import com.service.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.util.HtmlUtils;

import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.Date;
@Controller
public class WebSocketController {

    @Autowired
    NotificationService notificationService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    WebSocketController(SimpMessagingTemplate simpMessagingTemplate){

        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @MessageMapping("/message") //<---- sender will send at
    @SendTo("/topic/messages") //<---- receiver will subscribe it
    public OutputMessage send(final  WebSocketMessageModel message) throws Exception {
        String time = new SimpleDateFormat("HH:mm").format(new Date());
        return new OutputMessage(message.getFrom(), message.getText(), time);

    }

    @MessageMapping("/place-order")
    @SendTo("/topic/broadcast-order")
    public OutputMessage clientServerCommunication(final WebSocketMessageModel message) throws Exception {
        ObjectMapper mapper =new ObjectMapper();
        Long userId =Long.parseLong( message.getFrom());
        System.out.println("ORDER PLACED BY USER "+mapper.writeValueAsString(message));
        String time = new SimpleDateFormat("HH:mm").format(new Date());
//        notificationService.sendPrivateNotification(userId);
        return new OutputMessage(message.getFrom(),"Hello, " + HtmlUtils.htmlEscape(message.getFrom()) + "!", time);

    }

//    @MessageMapping("/topic/notify-server")
//    @SendToUser("/topic/notify-server")
//    public OutputMessage clientServerCommunication(@Reqtopic/notify-serveruestBody  WebSocketMessageModel message) throws Exception {
//        ObjectMapper mapper =new ObjectMapper();
//        Long userId =Long.parseLong( message.getFrom());
//        System.out.println("ORDER PLACED BY USER"+mapper.writeValueAsString(message));
//        String time = new SimpleDateFormat("HH:mm").format(new Date());
//        return new OutputMessage(message.getFrom(), message.getText(), time);
//
//    }


}
