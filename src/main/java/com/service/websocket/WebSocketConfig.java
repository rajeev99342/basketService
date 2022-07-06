package com.service.websocket;

import com.service.constants.values.IpAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
@Configuration
@EnableWebSocketMessageBroker
@Controller
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    IpAddress address;

    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.enableSimpleBroker("/topic/update");
        config.enableSimpleBroker("/topic/notify-server");
//        config.setApplicationDestinationPrefixes("/app");

    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/temp").setHandshakeHandler(new UserHandshakHandler())
                .setAllowedOrigins("http://*","https://*","http://localhost:8100","http://localhost:8101").withSockJS();
    }

}
