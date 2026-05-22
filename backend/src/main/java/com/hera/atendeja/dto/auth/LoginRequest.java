package com.hera.atendeja.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "LoginRequest")
public record LoginRequest(
        @NotBlank(message = "E-mail é obrigatório.")
        @Email(message = "E-mail deve ter formato válido.")
        String email,

        @NotBlank(message = "Senha é obrigatória.")
        String password
) {
}
