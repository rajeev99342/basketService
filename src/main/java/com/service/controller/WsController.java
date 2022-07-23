//package com.service.controller;
//
//import com.service.model.Message;
//import com.service.model.ResponseMessage;
//import com.service.service.NotificationService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.stereotype.Service;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//
//@Service
//public class WsController {
//    @Autowired
//    private NotificationService service;
//
//    @PostMapping("/send-message")
//    public void sendMessage(@RequestBody final Message message) {
////        service.sendPrivateNotification(message.getMessageContent());
//    }
//
//    @PostMapping("/send-private-message/{id}")
//    public void sendPrivateMessage(@PathVariable final String id,
//                                   @RequestBody final Message message) {
////        service.sendPrivateNotification(message.getMessageContent());
//    }}
