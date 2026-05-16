package com.hera.atendeja.dto.service;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Schema(name = "ServiceUpdateRequest")
public record ServiceUpdateRequest(
        @NotBlank(message = "Nome é obrigatório.")
        @Size(max = 120, message = "Nome deve ter no máximo 120 caracteres.")
        String name,

        @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres.")
        String description,

        @NotNull(message = "Duração é obrigatória.")
        @Min(value = 1, message = "Duração deve ser maior que zero.")
        Integer durationMinutes,

        @NotNull(message = "Intervalo é obrigatório.")
        @Min(value = 0, message = "Intervalo deve ser zero ou positivo.")
        Integer bufferMinutes,

        @NotNull(message = "Preço é obrigatório.")
        @DecimalMin(value = "0.00", message = "Preço deve ser zero ou positivo.")
        BigDecimal price,

        @NotNull(message = "Status ativo é obrigatório.")
        Boolean active
) {
}
