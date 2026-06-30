package com.serratec.chat.mapper;

import com.serratec.chat.domain.Message;
import com.serratec.chat.dto.response.MessageResponse;

public class MessageMapper {

    public static MessageResponse toResponse(Message m) {
        return new MessageResponse(
                m.getId(),
                m.getConversation().getId(),
                m.getRole(),
                m.getContent(),
                m.getCreatedAt());
    }
}
