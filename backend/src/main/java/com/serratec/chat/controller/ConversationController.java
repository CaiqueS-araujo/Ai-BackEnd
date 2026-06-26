package com.serratec.chat.controller;

import com.serratec.chat.dto.request.CreateConversationRequest;
import com.serratec.chat.dto.response.ConversationResponse;
import com.serratec.chat.dto.response.ConversationSummaryResponse;
import com.serratec.chat.mapper.ConversationMapper;
import com.serratec.chat.service.ConversationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @PostMapping
    public ResponseEntity<ConversationResponse> create(
            @RequestBody(required = false) CreateConversationRequest request) {
        String title = (request != null) ? request.title() : null;
        var conversation = conversationService.create(title);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ConversationMapper.toResponse(conversation));
    }

    @GetMapping
    public ResponseEntity<List<ConversationSummaryResponse>> list() {
        var summaries = conversationService.listSummaries();
        return ResponseEntity.ok(summaries);
    }
}
