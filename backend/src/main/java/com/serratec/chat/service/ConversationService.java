package com.serratec.chat.service;

import com.serratec.chat.domain.Conversation;
import com.serratec.chat.dto.response.ConversationSummaryResponse;
import com.serratec.chat.exception.ResourceNotFoundException;
import com.serratec.chat.repository.ConversationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;

    public ConversationService(ConversationRepository conversationRepository) {
        this.conversationRepository = conversationRepository;
    }

    @Transactional
    public Conversation create(String title) {
        if (title == null || title.isBlank()) {
            title = "Nova conversa";
        }
        Conversation conversation = new Conversation(title);
        return conversationRepository.save(conversation);
    }

    @Transactional(readOnly = true)
    public List<ConversationSummaryResponse> listSummaries() {
        return conversationRepository.findAllByOrderByUpdatedAtDesc()
                .stream()
                .map(c -> {
                    var messages = c.getMessages();
                    int count = messages.size();
                    String preview = messages.isEmpty()
                            ? null
                            : messages.get(messages.size() - 1).getContent();
                    String trimmed = (preview != null && preview.length() > 100)
                            ? preview.substring(0, 100)
                            : preview;
                    return new ConversationSummaryResponse(
                            c.getId(), c.getTitle(),
                            c.getCreatedAt(), c.getUpdatedAt(),
                            count, trimmed);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public Conversation getOrThrow(UUID id) {
        return conversationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Conversa não encontrada: " + id));
    }
}
