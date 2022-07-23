package com.service.websocket;

import com.sun.security.auth.UserPrincipal;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

public class UserHandshakHandler extends DefaultHandshakeHandler {
    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String uuid = UUID.randomUUID().toString();
        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++ This user opened the page ++++++++++++++++++++++++++++++++++++=>  " + uuid);
        return new UserPrincipal(uuid);
    }
}
