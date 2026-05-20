package com.hera.atendeja.dto.appointment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "AppointmentCancelRequest")
public record AppointmentCancelRequest(
        @NotBlank(message = "Motivo do cancelamento é obrigatório.")
        @Size(max = 500, message = "Motivo do cancelamento deve ter no máximo 500 caracteres.")
        String cancelReason
) {
}
