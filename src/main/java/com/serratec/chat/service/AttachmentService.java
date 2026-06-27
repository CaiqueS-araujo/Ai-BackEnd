package com.serratec.chat.service;

import com.serratec.chat.domain.Attachment;
import com.serratec.chat.domain.Conversation;
import com.serratec.chat.dto.response.AttachmentResponse;
import com.serratec.chat.exception.UnsupportedFileTypeException;
import com.serratec.chat.repository.AttachmentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class AttachmentService {

    private static final Set<String> ALLOWED_TYPES = Set.of("text/plain", "application/pdf");
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".txt", ".pdf");

    private final AttachmentRepository attachmentRepository;
    private final ConversationService conversationService;

    @Value("${app.storage.enabled:true}")
    private boolean storageEnabled;

    @Value("${app.storage.dir:./uploads}")
    private String storageDir;

    @Value("${app.storage.max-file-size:10485760}")
    private long maxFileSize;

    public AttachmentService(AttachmentRepository attachmentRepository,
                             ConversationService conversationService) {
        this.attachmentRepository = attachmentRepository;
        this.conversationService = conversationService;
    }

    @Transactional
    public AttachmentResponse upload(UUID conversationId, MultipartFile file) {
        Conversation conversation = conversationService.getOrThrow(conversationId);

        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException("Nome do arquivo inválido");
        }

        String contentType = file.getContentType();
        String extension = extractExtension(filename);

        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new UnsupportedFileTypeException(
                    "Tipo de arquivo não suportado: " + contentType);
        }
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new UnsupportedFileTypeException(
                    "Extensão não suportada: " + extension);
        }
        if (file.getSize() > maxFileSize) {
            throw new UnsupportedFileTypeException(
                    "Arquivo excede o tamanho máximo de " + (maxFileSize / 1024 / 1024) + "MB");
        }

        Attachment attachment = new Attachment(conversation, filename, contentType, file.getSize());

        if (storageEnabled) {
            try {
                Path uploadDir = Path.of(storageDir);
                Files.createDirectories(uploadDir);
                String storedName = UUID.randomUUID() + extension;
                Path targetPath = uploadDir.resolve(storedName);
                file.transferTo(targetPath.toFile());
                attachment.setStoragePath(targetPath.toString());
            } catch (IOException e) {
                throw new RuntimeException("Falha ao salvar arquivo", e);
            }
        }

        attachment = attachmentRepository.save(attachment);

        return new AttachmentResponse(
                attachment.getId(),
                attachment.getConversation().getId(),
                attachment.getFilename(),
                attachment.getContentType(),
                attachment.getSizeBytes(),
                attachment.getCreatedAt());
    }

    private String extractExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0) {
            return "";
        }
        return filename.substring(dot).toLowerCase();
    }
}
