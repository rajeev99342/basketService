package com.service.interfaces;

import com.service.model.MessageSenderRequest;

public interface MessageSender {
    void sendMessage(MessageSenderRequest request);
}
