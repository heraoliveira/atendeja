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

import com.hera.atendeja.dto.calendar.AvailabilityRuleResponse;
import com.hera.atendeja.dto.calendar.ScheduleExceptionResponse;
import com.hera.atendeja.entity.ScheduleExceptionType;
import com.hera.atendeja.service.ProfessionalCalendarService;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProfessionalCalendarController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProfessionalCalendarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProfessionalCalendarService professionalCalendarService;

    @Test
    void shouldListAvailabilityRules() throws Exception {
        when(professionalCalendarService.findAvailabilityRules(1L)).thenReturn(List.of(availabilityRuleResponse()));

        mockMvc.perform(get("/api/v1/professionals/1/availability-rules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].dayOfWeek").value("MONDAY"));
    }

    @Test
    void shouldCreateAvailabilityRule() throws Exception {
        when(professionalCalendarService.createAvailabilityRule(eq(1L), any()))
                .thenReturn(availabilityRuleResponse());

        mockMvc.perform(post("/api/v1/professionals/1/availability-rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "dayOfWeek": "MONDAY",
                                  "startTime": "08:00:00",
                                  "endTime": "12:00:00"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION,
                        "/api/v1/professionals/1/availability-rules/10"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldUpdateAndDeactivateAvailabilityRule() throws Exception {
        when(professionalCalendarService.updateAvailabilityRule(eq(1L), eq(10L), any()))
                .thenReturn(availabilityRuleResponse());

        mockMvc.perform(put("/api/v1/professionals/1/availability-rules/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "dayOfWeek": "MONDAY",
                                  "startTime": "08:00:00",
                                  "endTime": "12:00:00",
                                  "active": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));

        mockMvc.perform(delete("/api/v1/professionals/1/availability-rules/10"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldRejectInvalidAvailabilityPayload() throws Exception {
        mockMvc.perform(post("/api/v1/professionals/1/availability-rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "dayOfWeek": "MONDAY"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void shouldListCreateUpdateAndDeleteScheduleExceptions() throws Exception {
        ScheduleExceptionResponse response = scheduleExceptionResponse();
        when(professionalCalendarService.findScheduleExceptions(
                1L,
                LocalDate.of(2030, 1, 20),
                LocalDate.of(2030, 1, 21)
        )).thenReturn(List.of(response));
        when(professionalCalendarService.createScheduleException(eq(1L), any())).thenReturn(response);
        when(professionalCalendarService.updateScheduleException(eq(1L), eq(20L), any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/professionals/1/schedule-exceptions")
                        .param("from", "2030-01-20")
                        .param("to", "2030-01-21"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("BLOCKED"));

        mockMvc.perform(post("/api/v1/professionals/1/schedule-exceptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(scheduleExceptionPayload()))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION,
                        "/api/v1/professionals/1/schedule-exceptions/20"));

        mockMvc.perform(put("/api/v1/professionals/1/schedule-exceptions/20")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(scheduleExceptionPayload()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(20));

        mockMvc.perform(delete("/api/v1/professionals/1/schedule-exceptions/20"))
                .andExpect(status().isNoContent());
    }

    private AvailabilityRuleResponse availabilityRuleResponse() {
        return new AvailabilityRuleResponse(
                10L,
                1L,
                DayOfWeek.MONDAY,
                LocalTime.of(8, 0),
                LocalTime.of(12, 0),
                true,
                Instant.parse("2030-01-01T10:00:00Z"),
                Instant.parse("2030-01-01T10:00:00Z")
        );
    }

    private ScheduleExceptionResponse scheduleExceptionResponse() {
        return new ScheduleExceptionResponse(
                20L,
                1L,
                LocalDate.of(2030, 1, 20),
                LocalTime.of(8, 0),
                LocalTime.of(12, 0),
                ScheduleExceptionType.BLOCKED,
                "Folga",
                Instant.parse("2030-01-01T10:00:00Z"),
                Instant.parse("2030-01-01T10:00:00Z")
        );
    }

    private String scheduleExceptionPayload() {
        return """
                {
                  "date": "2030-01-20",
                  "startTime": "08:00:00",
                  "endTime": "12:00:00",
                  "type": "BLOCKED",
                  "reason": "Folga"
                }
                """;
    }
}
