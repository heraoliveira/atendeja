package com.hera.atendeja.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hera.atendeja.dto.professional.ProfessionalCreateRequest;
import com.hera.atendeja.dto.professional.ProfessionalUpdateRequest;
import com.hera.atendeja.entity.Professional;
import com.hera.atendeja.exception.ResourceNotFoundException;
import com.hera.atendeja.mapper.ProfessionalMapper;
import com.hera.atendeja.repository.ProfessionalRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class ProfessionalServiceTest {

    @Mock
    private ProfessionalRepository professionalRepository;

    private ProfessionalService professionalService;

    @BeforeEach
    void setUp() {
        professionalService = new ProfessionalService(professionalRepository, new ProfessionalMapper());
    }

    @Test
    void shouldCreateProfessionalWithNormalizedOptionalFields() {
        ProfessionalCreateRequest request = new ProfessionalCreateRequest(
                "  Ana Profissional  ",
                " 11999999999 ",
                "  ana@example.com "
        );
        when(professionalRepository.save(any(Professional.class))).thenAnswer(invocation -> invocation.getArgument(0));

        professionalService.create(request);

        ArgumentCaptor<Professional> captor = ArgumentCaptor.forClass(Professional.class);
        verify(professionalRepository).save(captor.capture());
        Professional savedProfessional = captor.getValue();
        assertThat(savedProfessional.getName()).isEqualTo("Ana Profissional");
        assertThat(savedProfessional.getPhone()).isEqualTo("11999999999");
        assertThat(savedProfessional.getEmail()).isEqualTo("ana@example.com");
        assertThat(savedProfessional.isActive()).isTrue();
    }

    @Test
    void shouldUpdateExistingProfessional() {
        Professional professional = new Professional();
        professional.setName("Nome antigo");
        professional.setPhone("11000000000");
        professional.setActive(true);
        when(professionalRepository.findById(1L)).thenReturn(Optional.of(professional));

        professionalService.update(1L, new ProfessionalUpdateRequest(
                "  Nome novo  ",
                " 11988887777 ",
                "  novo@example.com ",
                false
        ));

        assertThat(professional.getName()).isEqualTo("Nome novo");
        assertThat(professional.getPhone()).isEqualTo("11988887777");
        assertThat(professional.getEmail()).isEqualTo("novo@example.com");
        assertThat(professional.isActive()).isFalse();
    }

    @Test
    void shouldDeactivateExistingProfessional() {
        Professional professional = new Professional();
        professional.setActive(true);
        when(professionalRepository.findById(1L)).thenReturn(Optional.of(professional));

        professionalService.deactivate(1L);

        assertThat(professional.isActive()).isFalse();
    }

    @Test
    void shouldFilterProfessionalsBySearchAndActiveStatus() {
        Professional professional = new Professional();
        professional.setName("Ana Profissional");
        professional.setPhone("11999999999");
        var pageable = PageRequest.of(0, 10);
        when(professionalRepository.search("%ana%", true, pageable))
                .thenReturn(new PageImpl<>(List.of(professional), pageable, 1));

        var result = professionalService.findAll(" Ana ", true, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("Ana Profissional");
    }

    @Test
    void shouldThrowWhenProfessionalDoesNotExist() {
        when(professionalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> professionalService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Profissional");
    }
}
