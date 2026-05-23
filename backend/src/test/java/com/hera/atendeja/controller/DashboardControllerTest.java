package com.hera.atendeja.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hera.atendeja.dto.dashboard.DailyDashboardResponse;
import com.hera.atendeja.dto.dashboard.DashboardProfessionalResponse;
import com.hera.atendeja.entity.AppointmentStatus;
import com.hera.atendeja.service.DashboardService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardService dashboardService;

    @Test
    void shouldReturnDailyDashboardWithoutProfessionalFilter() throws Exception {
        when(dashboardService.getDailyDashboard(eq(LocalDate.of(2030, 1, 21)), isNull()))
                .thenReturn(response(null));

        mockMvc.perform(get("/api/v1/dashboard/daily")
                        .param("date", "2030-01-21"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2030-01-21"))
                .andExpect(jsonPath("$.professional").doesNotExist())
                .andExpect(jsonPath("$.totalAppointments").value(3))
                .andExpect(jsonPath("$.appointmentsByStatus.SCHEDULED").value(1))
                .andExpect(jsonPath("$.cancellations").value(1))
                .andExpect(jsonPath("$.noShows").value(1))
                .andExpect(jsonPath("$.availableMinutes").value(480))
                .andExpect(jsonPath("$.occupiedMinutes").value(120))
                .andExpect(jsonPath("$.occupancyPercentage").value(25.00));
    }

    @Test
    void shouldReturnDailyDashboardWithProfessionalFilter() throws Exception {
        when(dashboardService.getDailyDashboard(LocalDate.of(2030, 1, 21), 2L))
                .thenReturn(response(new DashboardProfessionalResponse(2L, "Ana Profissional")));

        mockMvc.perform(get("/api/v1/dashboard/daily")
                        .param("date", "2030-01-21")
                        .param("professionalId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.professional.id").value(2))
                .andExpect(jsonPath("$.professional.name").value("Ana Profissional"));
    }

    private DailyDashboardResponse response(DashboardProfessionalResponse professional) {
        Map<AppointmentStatus, Long> statuses = new LinkedHashMap<>();
        for (AppointmentStatus status : AppointmentStatus.values()) {
            statuses.put(status, 0L);
        }
        statuses.put(AppointmentStatus.SCHEDULED, 1L);
        statuses.put(AppointmentStatus.CANCELED, 1L);
        statuses.put(AppointmentStatus.NO_SHOW, 1L);
        return new DailyDashboardResponse(
                LocalDate.of(2030, 1, 21),
                professional,
                3,
                statuses,
                1,
                1,
                480,
                120,
                new BigDecimal("25.00")
        );
    }
}
