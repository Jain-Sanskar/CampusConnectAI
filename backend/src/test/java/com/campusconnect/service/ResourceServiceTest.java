package com.campusconnect.service;

import com.campusconnect.dto.ResourceDto;
import com.campusconnect.dto.ResourceOptions;
import com.campusconnect.entity.Resource;
import com.campusconnect.exception.ResourceNotFoundException;
import com.campusconnect.repository.ResourceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceServiceTest {

    @Mock
    private ResourceRepository resourceRepository;

    @InjectMocks
    private ResourceService resourceService;

    private Resource sampleResource(Long id) {
        Resource r = new Resource();
        r.setId(id);
        r.setTitle("DBMS PYQ");
        r.setCategory("Previous Year Questions");
        r.setSubject("DBMS");
        r.setResourceUrl("https://drive.google.com/dbms");
        r.setType("PDF");
        r.setUploadedBy(5L);
        return r;
    }

    private ResourceDto sampleDto() {
        ResourceDto dto = new ResourceDto();
        dto.setTitle("DBMS PYQ");
        dto.setCategory("Previous Year Questions");
        dto.setSubject("DBMS");
        dto.setResourceUrl("https://drive.google.com/dbms");
        dto.setType("PDF");
        return dto;
    }

    @Test
    void listReturnsMappedResources() {
        when(resourceRepository.search(null, "DBMS")).thenReturn(List.of(sampleResource(1L)));

        List<ResourceDto> result = resourceService.list("", "DBMS");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSubject()).isEqualTo("DBMS");
    }

    @Test
    void getOptionsCollectsDistinctValues() {
        when(resourceRepository.findDistinctCategories()).thenReturn(List.of("Notes", "Video"));
        when(resourceRepository.findDistinctSubjects()).thenReturn(List.of("DBMS", "Operating Systems"));
        when(resourceRepository.findDistinctTypes()).thenReturn(List.of("PDF", "LINK"));

        ResourceOptions options = resourceService.getOptions();

        assertThat(options.getCategories()).containsExactly("Notes", "Video");
        assertThat(options.getSubjects()).containsExactly("DBMS", "Operating Systems");
        assertThat(options.getTypes()).containsExactly("PDF", "LINK");
    }

    @Test
    void getByIdReturnsResourceWhenFound() {
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(sampleResource(1L)));

        ResourceDto dto = resourceService.getById(1L);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getTitle()).isEqualTo("DBMS PYQ");
    }

    @Test
    void getByIdThrowsWhenMissing() {
        when(resourceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resourceService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createSavesResourceWithUploader() {
        when(resourceRepository.save(any(Resource.class))).thenAnswer(inv -> {
            Resource r = inv.getArgument(0);
            r.setId(10L);
            return r;
        });

        ResourceDto created = resourceService.create(sampleDto(), 5L);

        assertThat(created.getId()).isEqualTo(10L);
        assertThat(created.getUploadedBy()).isEqualTo(5L);
        verify(resourceRepository).save(any(Resource.class));
    }

    @Test
    void updateThrowsWhenMissing() {
        when(resourceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resourceService.update(99L, sampleDto()))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(resourceRepository, never()).save(any(Resource.class));
    }

    @Test
    void deleteThrowsWhenMissing() {
        when(resourceRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> resourceService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(resourceRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteRemovesExistingResource() {
        when(resourceRepository.existsById(1L)).thenReturn(true);

        resourceService.delete(1L);

        verify(resourceRepository).deleteById(1L);
    }
}
