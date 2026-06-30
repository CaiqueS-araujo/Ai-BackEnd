package com.serratec.chat.dto.response;

import com.serratec.chat.domain.MessageRole;

import java.time.Instant;
import java.util.UUID;

public record MessageResponse(
        UUID id,
        UUID conversationId,
        MessageRole role,
        String content,
        Instant createdAt
) {
}
