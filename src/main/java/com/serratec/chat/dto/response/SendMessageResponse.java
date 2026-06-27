package com.serratec.chat.dto.response;

public record SendMessageResponse(
        MessageResponse userMessage,
        MessageResponse assistantMessage
) {
}
