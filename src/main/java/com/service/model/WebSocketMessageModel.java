package com.service.model;

import lombok.*;


@NoArgsConstructor
public class WebSocketMessageModel {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
