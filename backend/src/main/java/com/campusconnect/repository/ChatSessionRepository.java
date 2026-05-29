package com.campusconnect.repository;

import com.campusconnect.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    List<ChatSession> findByUserIdOrderByLastActivityAtDesc(Long userId);

    // used by the scheduled cleanup to find conversations that have gone stale
    List<ChatSession> findByLastActivityAtBefore(LocalDateTime cutoff);
}
