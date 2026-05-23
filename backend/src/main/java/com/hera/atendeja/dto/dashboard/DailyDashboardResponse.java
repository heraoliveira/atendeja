package com.hera.atendeja.dto.dashboard;

import com.hera.atendeja.entity.AppointmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Schema(name = "DailyDashboardResponse")
public record DailyDashboardResponse(
        LocalDate date,
        DashboardProfessionalResponse professional,
        long totalAppointments,
        Map<AppointmentStatus, Long> appointmentsByStatus,
        long cancellations,
        long noShows,
        long availableMinutes,
        long occupiedMinutes,
        BigDecimal occupancyPercentage
) {
}
