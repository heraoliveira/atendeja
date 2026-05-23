package com.hera.atendeja.dto.calendar;

import com.hera.atendeja.entity.ScheduleExceptionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;

@Schema(name = "ScheduleExceptionRequest")
public record ScheduleExceptionRequest(
        @NotNull(message = "Data da exceção é obrigatória.")
        LocalDate date,

        @NotNull(message = "Horário inicial é obrigatório.")
        LocalTime startTime,

        @NotNull(message = "Horário final é obrigatório.")
        LocalTime endTime,

        @NotNull(message = "Tipo da exceção é obrigatório.")
        ScheduleExceptionType type,

        @Size(max = 500, message = "Motivo da exceção deve ter no máximo 500 caracteres.")
        String reason
) {
}
