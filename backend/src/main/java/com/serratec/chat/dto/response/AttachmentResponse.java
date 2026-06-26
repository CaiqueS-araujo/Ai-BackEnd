package com.serratec.chat.dto.response;

import java.time.Instant;
import java.util.UUID;

public record AttachmentResponse(
        UUID id,
        UUID conversationId,
        String filename,
        String contentType,
        long sizeBytes,
        Instant createdAt
) {
}
