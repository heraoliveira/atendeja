package com.hera.atendeja.dto.auth;

import com.hera.atendeja.entity.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(name = "LoginResponse")
public record LoginResponse(
        String accessToken,
        String tokenType,
        Instant expiresAt,
        String email,
        UserRole role
) {
}
