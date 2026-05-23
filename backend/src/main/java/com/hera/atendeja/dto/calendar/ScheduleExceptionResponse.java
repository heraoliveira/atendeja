package com.hera.atendeja.dto.calendar;

import com.hera.atendeja.entity.ScheduleExceptionType;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

public record ScheduleExceptionResponse(
        Long id,
        Long professionalId,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        ScheduleExceptionType type,
        String reason,
        Instant createdAt,
        Instant updatedAt
) {
}
