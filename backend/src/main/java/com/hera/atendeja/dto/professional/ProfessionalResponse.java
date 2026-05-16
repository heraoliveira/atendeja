package com.hera.atendeja.dto.professional;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(name = "ProfessionalResponse")
public record ProfessionalResponse(
        Long id,
        String name,
        String phone,
        String email,
        Boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
