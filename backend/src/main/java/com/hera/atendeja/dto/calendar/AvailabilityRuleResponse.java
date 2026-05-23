package com.hera.atendeja.dto.calendar;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;

public record AvailabilityRuleResponse(
        Long id,
        Long professionalId,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
