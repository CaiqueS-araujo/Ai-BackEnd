package com.serratec.chat.repository;

import com.serratec.chat.domain.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    List<Conversation> findAllByOrderByUpdatedAtDesc();
}
