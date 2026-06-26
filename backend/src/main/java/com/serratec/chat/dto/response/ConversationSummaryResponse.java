package com.serratec.chat.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ConversationSummaryResponse(
        UUID id,
        String title,
        Instant createdAt,
        Instant updatedAt,
        int messageCount,
        String lastMessagePreview
) {
}
