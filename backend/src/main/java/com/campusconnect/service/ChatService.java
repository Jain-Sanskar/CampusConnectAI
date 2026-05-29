package com.campusconnect.service;

import com.campusconnect.config.GeminiProperties;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
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

    public ChatService(WebClient geminiWebClient, GeminiProperties properties) {
        this.geminiWebClient = geminiWebClient;
        this.properties = properties;
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
