package com.hera.atendeja.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hera.atendeja.dto.appointment.AppointmentResponse;
import com.hera.atendeja.entity.AppointmentStatus;
import com.hera.atendeja.exception.AppointmentConflictException;
import com.hera.atendeja.exception.ResourceNotFoundException;
import com.hera.atendeja.service.AppointmentService;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AppointmentController.class)
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AppointmentService appointmentService;

    @Test
    void shouldListAppointmentsWithFilters() throws Exception {
        AppointmentResponse response = appointmentResponse(AppointmentStatus.SCHEDULED);
        when(appointmentService.findAll(
                eq(LocalDate.of(2030, 1, 20)),
                eq(2L),
                isNull(),
                isNull(),
                eq(AppointmentStatus.SCHEDULED),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/v1/appointments")
                        .param("date", "2030-01-20")
                        .param("professionalId", "2")
                        .param("status", "SCHEDULED")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(10))
                .andExpect(jsonPath("$.content[0].professionalName").value("Ana Profissional"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void shouldCreateAppointment() throws Exception {
        when(appointmentService.create(any())).thenReturn(appointmentResponse(AppointmentStatus.SCHEDULED));

        mockMvc.perform(post("/api/v1/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": 1,
                                  "professionalId": 2,
                                  "serviceId": 3,
                                  "startAt": "2030-01-20T10:00:00Z"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, "/api/v1/appointments/10"))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.endAt").value("2030-01-20T11:00:00Z"));
    }

    @Test
    void shouldFindAppointmentById() throws Exception {
        when(appointmentService.findById(10L)).thenReturn(appointmentResponse(AppointmentStatus.SCHEDULED));

        mockMvc.perform(get("/api/v1/appointments/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }

    @Test
    void shouldRescheduleAppointment() throws Exception {
        when(appointmentService.reschedule(eq(10L), any())).thenReturn(appointmentResponse(AppointmentStatus.SCHEDULED));

        mockMvc.perform(patch("/api/v1/appointments/10/reschedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "startAt": "2030-01-21T14:00:00Z"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void shouldCancelAppointment() throws Exception {
        when(appointmentService.cancel(eq(10L), any())).thenReturn(appointmentResponse(AppointmentStatus.CANCELED));

        mockMvc.perform(patch("/api/v1/appointments/10/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cancelReason": "Cliente solicitou cancelamento"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));
    }

    @Test
    void shouldReturnConflictWhenAppointmentOverlaps() throws Exception {
        when(appointmentService.create(any())).thenThrow(new AppointmentConflictException());

        mockMvc.perform(post("/api/v1/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": 1,
                                  "professionalId": 2,
                                  "serviceId": 3,
                                  "startAt": "2030-01-20T10:30:00Z"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("APPOINTMENT_TIME_CONFLICT"));
    }

    @Test
    void shouldReturnConflictWhenRescheduleOverlaps() throws Exception {
        when(appointmentService.reschedule(eq(10L), any())).thenThrow(new AppointmentConflictException());

        mockMvc.perform(patch("/api/v1/appointments/10/reschedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "startAt": "2030-01-20T10:30:00Z"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("APPOINTMENT_TIME_CONFLICT"));
    }

    @Test
    void shouldReturnNotFoundWhenCustomerDoesNotExist() throws Exception {
        when(appointmentService.create(any())).thenThrow(new ResourceNotFoundException("Cliente", 99L));

        mockMvc.perform(post("/api/v1/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": 99,
                                  "professionalId": 2,
                                  "serviceId": 3,
                                  "startAt": "2030-01-20T10:00:00Z"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void shouldReturnValidationErrorWhenAppointmentPayloadIsInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": 1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    private AppointmentResponse appointmentResponse(AppointmentStatus status) {
        return new AppointmentResponse(
                10L,
                1L,
                "Maria Souza",
                2L,
                "Ana Profissional",
                3L,
                "Consulta inicial",
                Instant.parse("2030-01-20T10:00:00Z"),
                Instant.parse("2030-01-20T11:00:00Z"),
                status,
                status == AppointmentStatus.CANCELED ? "Cliente solicitou cancelamento" : null,
                null,
                null,
                Instant.parse("2030-01-01T12:00:00Z"),
                Instant.parse("2030-01-01T12:00:00Z")
        );
    }
}
