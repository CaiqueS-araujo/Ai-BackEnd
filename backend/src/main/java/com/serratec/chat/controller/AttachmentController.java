package com.serratec.chat.controller;

import com.serratec.chat.dto.response.AttachmentResponse;
import com.serratec.chat.service.AttachmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/conversations/{conversationId}/attachments")
public class AttachmentController {

    private final AttachmentService attachmentService;

    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<AttachmentResponse> upload(
            @PathVariable UUID conversationId,
            @RequestParam("file") MultipartFile file) {
        var response = attachmentService.upload(conversationId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
