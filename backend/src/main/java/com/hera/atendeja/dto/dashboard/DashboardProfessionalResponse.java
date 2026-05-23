package com.hera.atendeja.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "DashboardProfessionalResponse")
public record DashboardProfessionalResponse(
        Long id,
        String name
) {
}
