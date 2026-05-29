package com.campusconnect.service;

import com.campusconnect.entity.ChatSession;
import com.campusconnect.repository.ChatMessageRepository;
import com.campusconnect.repository.ChatSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatCleanupServiceTest {

    @Mock
    private ChatSessionRepository sessionRepository;
    @Mock
    private ChatMessageRepository messageRepository;

    @InjectMocks
    private ChatCleanupService chatCleanupService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(chatCleanupService, "retentionDays", 7);
    }

    @Test
    void deletesStaleSessionsAndTheirMessages() {
        ChatSession stale = new ChatSession(1L, "old chat");
        stale.setId(42L);
        stale.setLastActivityAt(LocalDateTime.now().minusDays(30));
        when(sessionRepository.findByLastActivityAtBefore(org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of(stale));

        chatCleanupService.flushOldConversations();

        verify(messageRepository).deleteBySessionIdIn(List.of(42L));
        verify(sessionRepository).deleteAll(List.of(stale));
    }

    @Test
    void doesNothingWhenNoStaleSessions() {
        when(sessionRepository.findByLastActivityAtBefore(org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of());

        chatCleanupService.flushOldConversations();

        verify(messageRepository, never()).deleteBySessionIdIn(anyCollection());
        verify(sessionRepository, never()).deleteAll(org.mockito.ArgumentMatchers.anyList());
    }
}
