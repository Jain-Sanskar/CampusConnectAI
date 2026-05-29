package com.campusconnect.service;

import com.campusconnect.entity.ChatSession;
import com.campusconnect.repository.ChatMessageRepository;
import com.campusconnect.repository.ChatSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Periodically removes chat conversations that haven't been touched for a while,
 * so we only keep the last few days of history per the retention policy.
 */
@Service
public class ChatCleanupService {

    private static final Logger log = LoggerFactory.getLogger(ChatCleanupService.class);

    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;

    @Value("${chat.retention-days:7}")
    private int retentionDays;

    public ChatCleanupService(ChatSessionRepository sessionRepository,
                              ChatMessageRepository messageRepository) {
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
    }

    @Scheduled(cron = "${chat.cleanup-cron:0 0 3 * * *}")
    @Transactional
    public void flushOldConversations() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);
        List<ChatSession> stale = sessionRepository.findByLastActivityAtBefore(cutoff);

        if (stale.isEmpty()) {
            return;
        }

        List<Long> staleIds = stale.stream().map(ChatSession::getId).toList();
        messageRepository.deleteBySessionIdIn(staleIds);
        sessionRepository.deleteAll(stale);
        log.info("Flushed {} chat session(s) inactive for more than {} day(s)", stale.size(), retentionDays);
    }
}
