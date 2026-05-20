package com.hera.atendeja.dto.appointment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

@Schema(name = "AppointmentRescheduleRequest")
public record AppointmentRescheduleRequest(
        @NotNull(message = "Novo horário de início é obrigatório.")
        @Future(message = "Novo horário de início deve estar no futuro.")
        Instant startAt
) {
}
