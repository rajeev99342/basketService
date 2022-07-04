package com.service.model;

import lombok.Builder;
import lombok.Getter;

@Builder
public class WebSocketMessageModel {
    @Getter
    private String from;
    @Getter
    private String text;
}
