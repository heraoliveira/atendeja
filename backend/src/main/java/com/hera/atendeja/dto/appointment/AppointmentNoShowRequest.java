package com.hera.atendeja.dto.appointment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(name = "AppointmentNoShowRequest")
public record AppointmentNoShowRequest(
        @Size(max = 500, message = "Motivo da falta deve ter no máximo 500 caracteres.")
        String noShowReason
) {
}
