package com.hera.atendeja.dto.appointment;

import com.hera.atendeja.entity.AppointmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(name = "AppointmentResponse")
public record AppointmentResponse(
        Long id,
        Long customerId,
        String customerName,
        Long professionalId,
        String professionalName,
        Long serviceId,
        String serviceName,
        Instant startAt,
        Instant endAt,
        AppointmentStatus status,
        String cancelReason,
        String noShowReason,
        Instant checkedInAt,
        Instant completedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
