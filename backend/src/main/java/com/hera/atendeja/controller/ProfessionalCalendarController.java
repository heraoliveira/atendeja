package com.hera.atendeja.controller;

import com.hera.atendeja.dto.calendar.AvailabilityRuleCreateRequest;
import com.hera.atendeja.dto.calendar.AvailabilityRuleResponse;
import com.hera.atendeja.dto.calendar.AvailabilityRuleUpdateRequest;
import com.hera.atendeja.dto.calendar.ScheduleExceptionRequest;
import com.hera.atendeja.dto.calendar.ScheduleExceptionResponse;
import com.hera.atendeja.service.ProfessionalCalendarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/professionals/{professionalId}")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Professional Calendar", description = "Disponibilidade e exceções de agenda")
public class ProfessionalCalendarController {

    private final ProfessionalCalendarService professionalCalendarService;

    public ProfessionalCalendarController(ProfessionalCalendarService professionalCalendarService) {
        this.professionalCalendarService = professionalCalendarService;
    }

    @GetMapping("/availability-rules")
    @Operation(summary = "Lista regras semanais de disponibilidade")
    public List<AvailabilityRuleResponse> findAvailabilityRules(@PathVariable Long professionalId) {
        return professionalCalendarService.findAvailabilityRules(professionalId);
    }

    @PostMapping("/availability-rules")
    @Operation(summary = "Cria uma regra semanal de disponibilidade")
    public ResponseEntity<AvailabilityRuleResponse> createAvailabilityRule(
            @PathVariable Long professionalId,
            @Valid @RequestBody AvailabilityRuleCreateRequest request
    ) {
        AvailabilityRuleResponse response = professionalCalendarService.createAvailabilityRule(professionalId, request);
        return ResponseEntity
                .created(URI.create("/api/v1/professionals/" + professionalId
                        + "/availability-rules/" + response.id()))
                .body(response);
    }

    @PutMapping("/availability-rules/{ruleId}")
    @Operation(summary = "Atualiza uma regra semanal de disponibilidade")
    public AvailabilityRuleResponse updateAvailabilityRule(
            @PathVariable Long professionalId,
            @PathVariable Long ruleId,
            @Valid @RequestBody AvailabilityRuleUpdateRequest request
    ) {
        return professionalCalendarService.updateAvailabilityRule(professionalId, ruleId, request);
    }

    @DeleteMapping("/availability-rules/{ruleId}")
    @Operation(summary = "Inativa uma regra semanal de disponibilidade")
    public ResponseEntity<Void> deactivateAvailabilityRule(
            @PathVariable Long professionalId,
            @PathVariable Long ruleId
    ) {
        professionalCalendarService.deactivateAvailabilityRule(professionalId, ruleId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/schedule-exceptions")
    @Operation(summary = "Lista exceções de agenda por período")
    public List<ScheduleExceptionResponse> findScheduleExceptions(
            @PathVariable Long professionalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return professionalCalendarService.findScheduleExceptions(professionalId, from, to);
    }

    @PostMapping("/schedule-exceptions")
    @Operation(summary = "Cria uma exceção de agenda")
    public ResponseEntity<ScheduleExceptionResponse> createScheduleException(
            @PathVariable Long professionalId,
            @Valid @RequestBody ScheduleExceptionRequest request
    ) {
        ScheduleExceptionResponse response = professionalCalendarService.createScheduleException(professionalId, request);
        return ResponseEntity
                .created(URI.create("/api/v1/professionals/" + professionalId
                        + "/schedule-exceptions/" + response.id()))
                .body(response);
    }

    @PutMapping("/schedule-exceptions/{exceptionId}")
    @Operation(summary = "Atualiza uma exceção de agenda")
    public ScheduleExceptionResponse updateScheduleException(
            @PathVariable Long professionalId,
            @PathVariable Long exceptionId,
            @Valid @RequestBody ScheduleExceptionRequest request
    ) {
        return professionalCalendarService.updateScheduleException(professionalId, exceptionId, request);
    }

    @DeleteMapping("/schedule-exceptions/{exceptionId}")
    @Operation(summary = "Remove uma exceção de agenda")
    public ResponseEntity<Void> deleteScheduleException(
            @PathVariable Long professionalId,
            @PathVariable Long exceptionId
    ) {
        professionalCalendarService.deleteScheduleException(professionalId, exceptionId);
        return ResponseEntity.noContent().build();
    }
}
