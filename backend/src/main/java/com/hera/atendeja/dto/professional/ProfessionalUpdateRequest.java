package com.hera.atendeja.dto.professional;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(name = "ProfessionalUpdateRequest")
public record ProfessionalUpdateRequest(
        @NotBlank(message = "Nome é obrigatório.")
        @Size(max = 120, message = "Nome deve ter no máximo 120 caracteres.")
        String name,

        @NotBlank(message = "Telefone é obrigatório.")
        @Size(max = 30, message = "Telefone deve ter no máximo 30 caracteres.")
        String phone,

        @Email(message = "E-mail deve ser válido.")
        @Size(max = 180, message = "E-mail deve ter no máximo 180 caracteres.")
        String email,

        @NotNull(message = "Status ativo é obrigatório.")
        Boolean active
) {
}
