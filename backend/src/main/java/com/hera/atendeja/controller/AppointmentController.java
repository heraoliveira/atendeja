package com.hera.atendeja.controller;

import com.hera.atendeja.dto.appointment.AppointmentCancelRequest;
import com.hera.atendeja.dto.appointment.AppointmentCreateRequest;
import com.hera.atendeja.dto.appointment.AppointmentRescheduleRequest;
import com.hera.atendeja.dto.appointment.AppointmentResponse;
import com.hera.atendeja.dto.common.PageResponse;
import com.hera.atendeja.entity.AppointmentStatus;
import com.hera.atendeja.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/appointments")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Appointments", description = "Agendamentos")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping
    @Operation(summary = "Lista agendamentos com paginação e filtros")
    public PageResponse<AppointmentResponse> findAll(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long professionalId,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long serviceId,
            @RequestParam(required = false) AppointmentStatus status,
            @PageableDefault(size = 20, sort = "startAt") Pageable pageable
    ) {
        return PageResponse.from(appointmentService.findAll(
                date,
                professionalId,
                customerId,
                serviceId,
                status,
                pageable
        ));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um agendamento por id")
    public AppointmentResponse findById(@PathVariable Long id) {
        return appointmentService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Cria um agendamento")
    public ResponseEntity<AppointmentResponse> create(@Valid @RequestBody AppointmentCreateRequest request) {
        AppointmentResponse response = appointmentService.create(request);
        return ResponseEntity
                .created(URI.create("/api/v1/appointments/" + response.id()))
                .body(response);
    }

    @PatchMapping("/{id}/reschedule")
    @Operation(summary = "Remarca um agendamento")
    public AppointmentResponse reschedule(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentRescheduleRequest request
    ) {
        return appointmentService.reschedule(id, request);
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancela um agendamento")
    public AppointmentResponse cancel(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentCancelRequest request
    ) {
        return appointmentService.cancel(id, request);
    }
}
