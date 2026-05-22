package com.hera.atendeja.controller;

import com.hera.atendeja.dto.common.PageResponse;
import com.hera.atendeja.dto.professional.ProfessionalCreateRequest;
import com.hera.atendeja.dto.professional.ProfessionalResponse;
import com.hera.atendeja.dto.professional.ProfessionalUpdateRequest;
import com.hera.atendeja.service.ProfessionalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
@RequestMapping("/api/v1/professionals")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Professionals", description = "Cadastro de profissionais")
public class ProfessionalController {

    private final ProfessionalService professionalService;

    public ProfessionalController(ProfessionalService professionalService) {
        this.professionalService = professionalService;
    }

    @GetMapping
    @Operation(summary = "Lista profissionais com paginação e filtros")
    public PageResponse<ProfessionalResponse> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        return PageResponse.from(professionalService.findAll(search, active, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um profissional por id")
    public ProfessionalResponse findById(@PathVariable Long id) {
        return professionalService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Cria um profissional")
    public ResponseEntity<ProfessionalResponse> create(@Valid @RequestBody ProfessionalCreateRequest request) {
        ProfessionalResponse response = professionalService.create(request);
        return ResponseEntity
                .created(URI.create("/api/v1/professionals/" + response.id()))
                .body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um profissional")
    public ProfessionalResponse update(
            @PathVariable Long id,
            @Valid @RequestBody ProfessionalUpdateRequest request
    ) {
        return professionalService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Inativa um profissional sem apagar o histórico")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        professionalService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
