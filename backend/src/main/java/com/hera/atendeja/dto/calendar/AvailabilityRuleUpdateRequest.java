package com.hera.atendeja.dto.calendar;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalTime;

@Schema(name = "AvailabilityRuleUpdateRequest")
public record AvailabilityRuleUpdateRequest(
        @NotNull(message = "Dia da semana é obrigatório.")
        DayOfWeek dayOfWeek,

        @NotNull(message = "Horário inicial é obrigatório.")
        LocalTime startTime,

        @NotNull(message = "Horário final é obrigatório.")
        LocalTime endTime,

        @NotNull(message = "Situação da regra é obrigatória.")
        Boolean active
) {
}
