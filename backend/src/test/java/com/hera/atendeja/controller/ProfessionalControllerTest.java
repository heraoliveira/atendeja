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

import com.hera.atendeja.dto.professional.ProfessionalResponse;
import com.hera.atendeja.exception.ResourceNotFoundException;
import com.hera.atendeja.service.ProfessionalService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProfessionalController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProfessionalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProfessionalService professionalService;

    @Test
    void shouldListProfessionalsWithFilters() throws Exception {
        ProfessionalResponse response = professionalResponse();
        when(professionalService.findAll(eq("ana"), eq(true), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/v1/professionals")
                        .param("search", "ana")
                        .param("active", "true")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Ana Profissional"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void shouldCreateProfessional() throws Exception {
        when(professionalService.create(any())).thenReturn(professionalResponse());

        mockMvc.perform(post("/api/v1/professionals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Ana Profissional",
                                  "phone": "11999999999",
                                  "email": "ana@example.com"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, "/api/v1/professionals/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Ana Profissional"));
    }

    @Test
    void shouldUpdateProfessional() throws Exception {
        when(professionalService.update(eq(1L), any())).thenReturn(professionalResponse());

        mockMvc.perform(put("/api/v1/professionals/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Ana Profissional",
                                  "phone": "11999999999",
                                  "email": "ana@example.com",
                                  "active": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldDeactivateProfessional() throws Exception {
        mockMvc.perform(delete("/api/v1/professionals/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnNotFoundWhenProfessionalDoesNotExist() throws Exception {
        when(professionalService.findById(99L))
                .thenThrow(new ResourceNotFoundException("Profissional", 99L));

        mockMvc.perform(get("/api/v1/professionals/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void shouldReturnValidationErrorWhenProfessionalPayloadIsInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/professionals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "invalid-email"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void shouldReturnConflictWhenProfessionalUniqueDataIsDuplicated() throws Exception {
        when(professionalService.create(any()))
                .thenThrow(new DataIntegrityViolationException("duplicate professional"));

        mockMvc.perform(post("/api/v1/professionals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Ana Profissional",
                                  "phone": "11999999999",
                                  "email": "ana@example.com"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DATA_INTEGRITY_VIOLATION"));
    }

    private ProfessionalResponse professionalResponse() {
        return new ProfessionalResponse(
                1L,
                "Ana Profissional",
                "11999999999",
                "ana@example.com",
                true,
                Instant.parse("2026-05-15T12:00:00Z"),
                Instant.parse("2026-05-15T12:00:00Z")
        );
    }
}
