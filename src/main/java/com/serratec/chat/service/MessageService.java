package com.serratec.chat.service;

import com.serratec.chat.domain.Message;
import com.serratec.chat.domain.MessageRole;
import com.serratec.chat.dto.response.SendMessageResponse;
import com.serratec.chat.mapper.MessageMapper;
import com.serratec.chat.repository.MessageRepository;
import com.serratec.chat.service.chat.ChatResponseProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationService conversationService;
    private final ChatResponseProvider chatResponseProvider;

    public MessageService(MessageRepository messageRepository,
                          ConversationService conversationService,
                          ChatResponseProvider chatResponseProvider) {
        this.messageRepository = messageRepository;
        this.conversationService = conversationService;
        this.chatResponseProvider = chatResponseProvider;
    }

    @Transactional(readOnly = true)
    public List<Message> history(UUID conversationId) {
        conversationService.getOrThrow(conversationId);
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
    }

    @Transactional
    public SendMessageResponse send(UUID conversationId, String content) {
        var conversation = conversationService.getOrThrow(conversationId);

        Message userMessage = messageRepository.save(
                new Message(conversation, MessageRole.USER, content));

        String reply = chatResponseProvider.generateReply(content, conversation);
        Message assistantMessage = messageRepository.save(
                new Message(conversation, MessageRole.ASSISTANT, reply));

        return new SendMessageResponse(
                MessageMapper.toResponse(userMessage),
                MessageMapper.toResponse(assistantMessage));
    }
}
