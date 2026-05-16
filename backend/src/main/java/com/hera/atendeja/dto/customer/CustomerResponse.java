package com.hera.atendeja.dto.customer;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(name = "CustomerResponse")
public record CustomerResponse(
        Long id,
        String name,
        String phone,
        String email,
        String document,
        Boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
