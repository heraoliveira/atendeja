package com.hera.atendeja.controller;

import com.hera.atendeja.dto.dashboard.DailyDashboardResponse;
import com.hera.atendeja.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Dashboard", description = "Indicadores operacionais")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/daily")
    @Operation(summary = "Consulta indicadores diários de agenda")
    public DailyDashboardResponse daily(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long professionalId
    ) {
        return dashboardService.getDailyDashboard(date, professionalId);
    }
}
