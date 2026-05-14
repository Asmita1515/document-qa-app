package com.docqa.repository;

import com.docqa.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByUserIdAndFileIdOrderByCreatedAtAsc(Long userId, Long fileId);
    List<ChatMessage> findByUserId(Long userId);
}
