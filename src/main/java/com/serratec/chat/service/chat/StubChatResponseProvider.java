package com.serratec.chat.service.chat;

import com.serratec.chat.domain.Conversation;
import org.springframework.stereotype.Service;

@Service
public class StubChatResponseProvider implements ChatResponseProvider {

    @Override
    public String generateReply(String userContent, Conversation context) {
        return "Recebi sua mensagem: \"" + userContent + "\". [resposta de teste]";
    }
}
