package com.serratec.chat.service.chat;

import com.serratec.chat.domain.Conversation;

public interface ChatResponseProvider {
    String generateReply(String userContent, Conversation context);
}
