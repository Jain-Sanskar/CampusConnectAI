package com.campusconnect.service;

import com.campusconnect.config.GeminiProperties;
import com.campusconnect.dto.ChatRequest;
import com.campusconnect.dto.ChatResponse;
import com.campusconnect.entity.ChatMessage;
import com.campusconnect.entity.ChatSession;
import com.campusconnect.exception.ResourceNotFoundException;
import com.campusconnect.repository.ChatMessageRepository;
import com.campusconnect.repository.ChatSessionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private static final String SYSTEM_PROMPT = """
            You are CampusConnect AI, a friendly and encouraging college senior mentor.
            You help juniors and freshers with academics, tech stacks, project ideas,
            placement preparation, and core computer science concepts.
            Keep answers clear, practical and beginner-friendly, and stay positive.
            If a question is outside academics or careers, gently steer it back.
            """;

    private static final String FALLBACK_REPLY =
            "Sorry, I'm unable to reach the AI mentor right now. Please try again in a little while.";

    private final WebClient geminiWebClient;
    private final GeminiProperties properties;
    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;

    public ChatService(WebClient geminiWebClient,
                       GeminiProperties properties,
                       ChatSessionRepository sessionRepository,
                       ChatMessageRepository messageRepository) {
        this.geminiWebClient = geminiWebClient;
        this.properties = properties;
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
    }

    @Transactional
    public ChatResponse chat(Long userId, ChatRequest request) {
        ChatSession session = resolveSession(userId, request);

        String reply = generateReply(request.getMessage());

        messageRepository.save(new ChatMessage(session.getId(), request.getMessage(), reply));
        session.setLastActivityAt(LocalDateTime.now());
        sessionRepository.save(session);

        return new ChatResponse(reply, session.getId());
    }

    @Transactional(readOnly = true)
    public List<ChatSession> getSessions(Long userId) {
        return sessionRepository.findByUserIdOrderByLastActivityAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<ChatMessage> getMessages(Long userId, Long sessionId) {
        // make sure the session exists and actually belongs to the caller before returning anything
        ownedSession(userId, sessionId);
        return messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    private ChatSession resolveSession(Long userId, ChatRequest request) {
        if (request.getSessionId() != null) {
            return ownedSession(userId, request.getSessionId());
        }
        return sessionRepository.save(new ChatSession(userId, deriveTitle(request.getMessage())));
    }

    private ChatSession ownedSession(Long userId, Long sessionId) {
        return sessionRepository.findById(sessionId)
                .filter(s -> s.getUserId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found"));
    }

    private String deriveTitle(String message) {
        String trimmed = message.strip();
        return trimmed.length() > 50 ? trimmed.substring(0, 50) + "..." : trimmed;
    }

    public String generateReply(String userMessage) {
        if (!StringUtils.hasText(properties.getKey())) {
            log.warn("Gemini API key is not configured - returning fallback reply");
            return FALLBACK_REPLY;
        }

        try {
            JsonNode response = geminiWebClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/models/{model}:generateContent")
                            .queryParam("key", properties.getKey())
                            .build(properties.getModel()))
                    .bodyValue(buildRequestBody(userMessage))
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block(Duration.ofSeconds(properties.getTimeoutSeconds()));

            return extractText(response);
        } catch (Exception ex) {
            // network issue, bad key, quota, timeout - degrade gracefully instead of failing the request
            log.error("Gemini request failed: {}", ex.getMessage());
            return FALLBACK_REPLY;
        }
    }

    private Map<String, Object> buildRequestBody(String userMessage) {
        Map<String, Object> systemInstruction = Map.of(
                "parts", List.of(Map.of("text", SYSTEM_PROMPT)));
        Map<String, Object> userContent = Map.of(
                "role", "user",
                "parts", List.of(Map.of("text", userMessage)));
        return Map.of(
                "system_instruction", systemInstruction,
                "contents", List.of(userContent));
    }

    private String extractText(JsonNode response) {
        if (response == null) {
            return FALLBACK_REPLY;
        }
        JsonNode textNode = response
                .path("candidates").path(0)
                .path("content").path("parts").path(0)
                .path("text");

        if (textNode.isMissingNode() || !StringUtils.hasText(textNode.asText())) {
            log.warn("Gemini response did not contain any text content");
            return FALLBACK_REPLY;
        }
        return textNode.asText().trim();
    }
}
