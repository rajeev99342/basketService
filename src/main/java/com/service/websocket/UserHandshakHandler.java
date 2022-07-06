package com.service.websocket;

import com.sun.security.auth.UserPrincipal;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

public class UserHandshakHandler extends DefaultHandshakeHandler {
    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username ;
        if (principal instanceof UserDetails) {
             username = ((UserDetails) principal).getUsername();

        } else {
             username = principal.toString();
        }
        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++ This user opened the page ++++++++++++++++++++++++++++++++++++=>  "+username);
        return new UserPrincipal(username);
    }
}
