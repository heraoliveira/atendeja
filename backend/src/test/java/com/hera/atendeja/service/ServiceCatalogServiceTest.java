package com.hera.atendeja.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hera.atendeja.dto.service.ServiceCreateRequest;
import com.hera.atendeja.dto.service.ServiceUpdateRequest;
import com.hera.atendeja.entity.ServiceCatalog;
import com.hera.atendeja.exception.ResourceNotFoundException;
import com.hera.atendeja.mapper.ServiceCatalogMapper;
import com.hera.atendeja.repository.ServiceCatalogRepository;
import java.math.BigDecimal;
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
class ServiceCatalogServiceTest {

    @Mock
    private ServiceCatalogRepository serviceCatalogRepository;

    private ServiceCatalogService serviceCatalogService;

    @BeforeEach
    void setUp() {
        serviceCatalogService = new ServiceCatalogService(serviceCatalogRepository, new ServiceCatalogMapper());
    }

    @Test
    void shouldCreateServiceWithNormalizedOptionalFields() {
        ServiceCreateRequest request = new ServiceCreateRequest(
                "  Consulta inicial  ",
                "  Atendimento de avaliacao  ",
                45,
                15,
                new BigDecimal("150.00")
        );
        when(serviceCatalogRepository.save(any(ServiceCatalog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        serviceCatalogService.create(request);

        ArgumentCaptor<ServiceCatalog> captor = ArgumentCaptor.forClass(ServiceCatalog.class);
        verify(serviceCatalogRepository).save(captor.capture());
        ServiceCatalog savedService = captor.getValue();
        assertThat(savedService.getName()).isEqualTo("Consulta inicial");
        assertThat(savedService.getDescription()).isEqualTo("Atendimento de avaliacao");
        assertThat(savedService.getDurationMinutes()).isEqualTo(45);
        assertThat(savedService.getBufferMinutes()).isEqualTo(15);
        assertThat(savedService.getPrice()).isEqualByComparingTo("150.00");
        assertThat(savedService.isActive()).isTrue();
    }

    @Test
    void shouldUpdateExistingService() {
        ServiceCatalog service = new ServiceCatalog();
        service.setName("Servico antigo");
        service.setDescription("Descricao antiga");
        service.setDurationMinutes(30);
        service.setBufferMinutes(0);
        service.setPrice(new BigDecimal("90.00"));
        service.setActive(true);
        when(serviceCatalogRepository.findById(1L)).thenReturn(Optional.of(service));

        serviceCatalogService.update(1L, new ServiceUpdateRequest(
                "  Servico novo  ",
                "  Descricao nova  ",
                60,
                10,
                new BigDecimal("200.00"),
                false
        ));

        assertThat(service.getName()).isEqualTo("Servico novo");
        assertThat(service.getDescription()).isEqualTo("Descricao nova");
        assertThat(service.getDurationMinutes()).isEqualTo(60);
        assertThat(service.getBufferMinutes()).isEqualTo(10);
        assertThat(service.getPrice()).isEqualByComparingTo("200.00");
        assertThat(service.isActive()).isFalse();
    }

    @Test
    void shouldDeactivateExistingService() {
        ServiceCatalog service = new ServiceCatalog();
        service.setActive(true);
        when(serviceCatalogRepository.findById(1L)).thenReturn(Optional.of(service));

        serviceCatalogService.deactivate(1L);

        assertThat(service.isActive()).isFalse();
    }

    @Test
    void shouldFilterServicesBySearchAndActiveStatus() {
        ServiceCatalog service = new ServiceCatalog();
        service.setName("Consulta inicial");
        service.setDurationMinutes(45);
        service.setBufferMinutes(15);
        service.setPrice(new BigDecimal("150.00"));
        var pageable = PageRequest.of(0, 10);
        when(serviceCatalogRepository.search("%consulta%", true, pageable))
                .thenReturn(new PageImpl<>(List.of(service), pageable, 1));

        var result = serviceCatalogService.findAll(" Consulta ", true, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("Consulta inicial");
    }

    @Test
    void shouldThrowWhenServiceDoesNotExist() {
        when(serviceCatalogRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serviceCatalogService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Servi");
    }
}
