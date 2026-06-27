package com.serratec.chat.mapper;

import com.serratec.chat.domain.Conversation;
import com.serratec.chat.dto.response.ConversationResponse;

public class ConversationMapper {

    public static ConversationResponse toResponse(Conversation c) {
        return new ConversationResponse(
                c.getId(), c.getTitle(),
                c.getCreatedAt(), c.getUpdatedAt());
    }
}
