package com.campusconnect.service;

import com.campusconnect.dto.ResourceDto;
import com.campusconnect.entity.Resource;
import com.campusconnect.exception.ResourceNotFoundException;
import com.campusconnect.repository.ResourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class ResourceService {

    private static final Logger log = LoggerFactory.getLogger(ResourceService.class);

    private final ResourceRepository resourceRepository;

    public ResourceService(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    public List<ResourceDto> list(String category, String subject) {
        // blank filters are treated as "no filter"
        String categoryFilter = StringUtils.hasText(category) ? category : null;
        String subjectFilter = StringUtils.hasText(subject) ? subject : null;
        return resourceRepository.search(categoryFilter, subjectFilter).stream()
                .map(this::toDto)
                .toList();
    }

    public ResourceDto getById(Long id) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + id));
        return toDto(resource);
    }

    public ResourceDto create(ResourceDto dto, Long uploadedBy) {
        Resource resource = new Resource();
        applyChanges(resource, dto);
        resource.setUploadedBy(uploadedBy);
        Resource saved = resourceRepository.save(resource);
        log.info("Created resource '{}' (id {})", saved.getTitle(), saved.getId());
        return toDto(saved);
    }

    public ResourceDto update(Long id, ResourceDto dto) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + id));
        applyChanges(resource, dto);
        Resource saved = resourceRepository.save(resource);
        log.info("Updated resource id {}", id);
        return toDto(saved);
    }

    public void delete(Long id) {
        if (!resourceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Resource not found with id: " + id);
        }
        resourceRepository.deleteById(id);
        log.info("Deleted resource id {}", id);
    }

    private void applyChanges(Resource resource, ResourceDto dto) {
        resource.setTitle(dto.getTitle());
        resource.setDescription(dto.getDescription());
        resource.setCategory(dto.getCategory());
        resource.setSubject(dto.getSubject());
        resource.setResourceUrl(dto.getResourceUrl());
        resource.setType(dto.getType());
    }

    private ResourceDto toDto(Resource resource) {
        ResourceDto dto = new ResourceDto();
        dto.setId(resource.getId());
        dto.setTitle(resource.getTitle());
        dto.setDescription(resource.getDescription());
        dto.setCategory(resource.getCategory());
        dto.setSubject(resource.getSubject());
        dto.setResourceUrl(resource.getResourceUrl());
        dto.setType(resource.getType());
        dto.setUploadedBy(resource.getUploadedBy());
        dto.setCreatedAt(resource.getCreatedAt());
        return dto;
    }
}
