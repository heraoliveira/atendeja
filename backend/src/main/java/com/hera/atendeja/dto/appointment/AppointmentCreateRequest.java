package com.hera.atendeja.dto.appointment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

@Schema(name = "AppointmentCreateRequest")
public record AppointmentCreateRequest(
        @NotNull(message = "Cliente é obrigatório.")
        Long customerId,

        @NotNull(message = "Profissional é obrigatório.")
        Long professionalId,

        @NotNull(message = "Serviço é obrigatório.")
        Long serviceId,

        @NotNull(message = "Horário de início é obrigatório.")
        @Future(message = "Horário de início deve estar no futuro.")
        Instant startAt
) {
}
