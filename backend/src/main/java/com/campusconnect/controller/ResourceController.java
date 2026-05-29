package com.campusconnect.controller;

import com.campusconnect.dto.ResourceDto;
import com.campusconnect.entity.User;
import com.campusconnect.repository.UserRepository;
import com.campusconnect.service.ResourceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    private final ResourceService resourceService;
    private final UserRepository userRepository;

    public ResourceController(ResourceService resourceService, UserRepository userRepository) {
        this.resourceService = resourceService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<ResourceDto>> list(@RequestParam(required = false) String category,
                                                  @RequestParam(required = false) String subject) {
        return ResponseEntity.ok(resourceService.list(category, subject));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResourceDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(resourceService.getById(id));
    }

    @PostMapping
    public ResponseEntity<ResourceDto> create(@Valid @RequestBody ResourceDto dto,
                                              @AuthenticationPrincipal UserDetails principal) {
        Long uploaderId = currentUserId(principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(resourceService.create(dto, uploaderId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResourceDto> update(@PathVariable Long id, @Valid @RequestBody ResourceDto dto) {
        return ResponseEntity.ok(resourceService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        resourceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private Long currentUserId(UserDetails principal) {
        return userRepository.findByEmail(principal.getUsername())
                .map(User::getId)
                .orElse(null);
    }
}
