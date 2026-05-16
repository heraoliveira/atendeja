package com.hera.atendeja.dto.service;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;

@Schema(name = "ServiceResponse")
public record ServiceResponse(
        Long id,
        String name,
        String description,
        Integer durationMinutes,
        Integer bufferMinutes,
        BigDecimal price,
        Boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
