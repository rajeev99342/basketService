package com.service.model;

import lombok.Data;

@Data
public class MessageSenderRequest {
    private String phoneNumber;
    private String message;
}
