package com.serratec.chat.controller;

import com.serratec.chat.dto.request.SendMessageRequest;
import com.serratec.chat.dto.response.MessageResponse;
import com.serratec.chat.dto.response.SendMessageResponse;
import com.serratec.chat.mapper.MessageMapper;
import com.serratec.chat.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/conversations/{conversationId}/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping
    public ResponseEntity<List<MessageResponse>> history(
            @PathVariable UUID conversationId) {
        var messages = messageService.history(conversationId);
        var response = messages.stream()
                .map(MessageMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<SendMessageResponse> send(
            @PathVariable UUID conversationId,
            @RequestBody @Valid SendMessageRequest request) {
        var response = messageService.send(conversationId, request.content());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
