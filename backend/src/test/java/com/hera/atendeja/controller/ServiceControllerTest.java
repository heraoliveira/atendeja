package com.hera.atendeja.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hera.atendeja.dto.service.ServiceResponse;
import com.hera.atendeja.exception.ResourceNotFoundException;
import com.hera.atendeja.service.ServiceCatalogService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ServiceController.class)
class ServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ServiceCatalogService serviceCatalogService;

    @Test
    void shouldListServicesWithFilters() throws Exception {
        ServiceResponse response = serviceResponse();
        when(serviceCatalogService.findAll(eq("consulta"), eq(true), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/v1/services")
                        .param("search", "consulta")
                        .param("active", "true")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Consulta inicial"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void shouldCreateService() throws Exception {
        when(serviceCatalogService.create(any())).thenReturn(serviceResponse());

        mockMvc.perform(post("/api/v1/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Consulta inicial",
                                  "description": "Atendimento de avaliacao",
                                  "durationMinutes": 45,
                                  "bufferMinutes": 15,
                                  "price": 150.00
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, "/api/v1/services/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Consulta inicial"));
    }

    @Test
    void shouldUpdateService() throws Exception {
        when(serviceCatalogService.update(eq(1L), any())).thenReturn(serviceResponse());

        mockMvc.perform(put("/api/v1/services/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Consulta inicial",
                                  "description": "Atendimento de avaliacao",
                                  "durationMinutes": 45,
                                  "bufferMinutes": 15,
                                  "price": 150.00,
                                  "active": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.durationMinutes").value(45));
    }

    @Test
    void shouldDeactivateService() throws Exception {
        mockMvc.perform(delete("/api/v1/services/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnNotFoundWhenServiceDoesNotExist() throws Exception {
        when(serviceCatalogService.findById(99L))
                .thenThrow(new ResourceNotFoundException("Servico", 99L));

        mockMvc.perform(get("/api/v1/services/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void shouldReturnValidationErrorWhenServicePayloadIsInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "durationMinutes": 0,
                                  "bufferMinutes": -1,
                                  "price": -1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void shouldReturnConflictWhenServiceUniqueDataIsDuplicated() throws Exception {
        when(serviceCatalogService.create(any()))
                .thenThrow(new DataIntegrityViolationException("duplicate service"));

        mockMvc.perform(post("/api/v1/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Consulta inicial",
                                  "description": "Atendimento de avaliacao",
                                  "durationMinutes": 45,
                                  "bufferMinutes": 15,
                                  "price": 150.00
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DATA_INTEGRITY_VIOLATION"));
    }

    private ServiceResponse serviceResponse() {
        return new ServiceResponse(
                1L,
                "Consulta inicial",
                "Atendimento de avaliacao",
                45,
                15,
                new BigDecimal("150.00"),
                true,
                Instant.parse("2026-05-15T12:00:00Z"),
                Instant.parse("2026-05-15T12:00:00Z")
        );
    }
}
