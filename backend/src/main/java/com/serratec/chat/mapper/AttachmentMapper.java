package com.serratec.chat.mapper;

import com.serratec.chat.domain.Attachment;
import com.serratec.chat.dto.response.AttachmentResponse;

public class AttachmentMapper {

    public static AttachmentResponse toResponse(Attachment a) {
        return new AttachmentResponse(
                a.getId(),
                a.getConversation().getId(),
                a.getFilename(),
                a.getContentType(),
                a.getSizeBytes(),
                a.getCreatedAt());
    }
}
