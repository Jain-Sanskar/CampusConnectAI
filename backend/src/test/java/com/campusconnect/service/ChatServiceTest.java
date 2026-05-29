package com.campusconnect.service;

import com.campusconnect.config.GeminiProperties;
import com.campusconnect.dto.ChatRequest;
import com.campusconnect.dto.ChatResponse;
import com.campusconnect.entity.ChatSession;
import com.campusconnect.exception.ResourceNotFoundException;
import com.campusconnect.repository.ChatMessageRepository;
import com.campusconnect.repository.ChatSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatSessionRepository sessionRepository;
    @Mock
    private ChatMessageRepository messageRepository;

    private ChatService chatService;

    @BeforeEach
    void setUp() {
        // no API key configured, so the Gemini call is skipped and the fallback path is exercised -
        // this keeps the test fast and free of any real network calls
        GeminiProperties properties = new GeminiProperties();
        WebClient unusedWebClient = WebClient.builder().build();
        chatService = new ChatService(unusedWebClient, properties, sessionRepository, messageRepository);
    }

    private ChatRequest request(String message, Long sessionId) {
        ChatRequest req = new ChatRequest();
        req.setMessage(message);
        req.setSessionId(sessionId);
        return req;
    }

    @Test
    void generateReplyFallsBackWhenKeyMissing() {
        String reply = chatService.generateReply("How do I start with DSA?");

        assertThat(reply).startsWith("Sorry, I'm unable to reach the AI mentor");
    }

    @Test
    void chatCreatesNewSessionWhenNoSessionIdGiven() {
        when(sessionRepository.save(any(ChatSession.class))).thenAnswer(inv -> {
            ChatSession s = inv.getArgument(0);
            if (s.getId() == null) {
                s.setId(1L);
            }
            return s;
        });

        ChatResponse response = chatService.chat(5L, request("Hi senior", null));

        assertThat(response.getSessionId()).isEqualTo(1L);
        assertThat(response.getReply()).isNotBlank();
        verify(messageRepository).save(any());
    }

    @Test
    void chatContinuesExistingSession() {
        ChatSession existing = new ChatSession(5L, "earlier chat");
        existing.setId(9L);
        when(sessionRepository.findById(9L)).thenReturn(Optional.of(existing));
        when(sessionRepository.save(any(ChatSession.class))).thenAnswer(inv -> inv.getArgument(0));

        ChatResponse response = chatService.chat(5L, request("follow up question", 9L));

        assertThat(response.getSessionId()).isEqualTo(9L);
        verify(messageRepository).save(any());
    }

    @Test
    void chatRejectsSessionOwnedByAnotherUser() {
        ChatSession otherUsers = new ChatSession(99L, "not yours");
        otherUsers.setId(9L);
        when(sessionRepository.findById(9L)).thenReturn(Optional.of(otherUsers));

        assertThatThrownBy(() -> chatService.chat(5L, request("hello", 9L)))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(messageRepository, never()).save(any());
    }

    @Test
    void getMessagesThrowsForNonOwnedSession() {
        when(sessionRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.getMessages(5L, 9L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
