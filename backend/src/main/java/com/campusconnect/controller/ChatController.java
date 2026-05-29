package com.campusconnect.controller;

import com.campusconnect.dto.ChatRequest;
import com.campusconnect.dto.ChatResponse;
import com.campusconnect.entity.ChatMessage;
import com.campusconnect.entity.ChatSession;
import com.campusconnect.entity.User;
import com.campusconnect.exception.ResourceNotFoundException;
import com.campusconnect.repository.UserRepository;
import com.campusconnect.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;
    private final UserRepository userRepository;

    public ChatController(ChatService chatService, UserRepository userRepository) {
        this.chatService = chatService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request,
                                             @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(chatService.chat(currentUserId(principal), request));
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<ChatSession>> sessions(@AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(chatService.getSessions(currentUserId(principal)));
    }

    @GetMapping("/sessions/{id}")
    public ResponseEntity<List<ChatMessage>> messages(@PathVariable Long id,
                                                      @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(chatService.getMessages(currentUserId(principal), id));
    }

    private Long currentUserId(UserDetails principal) {
        return userRepository.findByEmail(principal.getUsername())
                .map(User::getId)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }
}
