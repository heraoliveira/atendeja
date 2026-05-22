package com.hera.atendeja.controller;

import com.hera.atendeja.dto.common.PageResponse;
import com.hera.atendeja.dto.service.ServiceCreateRequest;
import com.hera.atendeja.dto.service.ServiceResponse;
import com.hera.atendeja.dto.service.ServiceUpdateRequest;
import com.hera.atendeja.service.ServiceCatalogService;
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
@RequestMapping("/api/v1/services")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Services", description = "Cadastro de serviços")
public class ServiceController {

    private final ServiceCatalogService serviceCatalogService;

    public ServiceController(ServiceCatalogService serviceCatalogService) {
        this.serviceCatalogService = serviceCatalogService;
    }

    @GetMapping
    @Operation(summary = "Lista serviços com paginação e filtros")
    public PageResponse<ServiceResponse> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        return PageResponse.from(serviceCatalogService.findAll(search, active, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um serviço por id")
    public ServiceResponse findById(@PathVariable Long id) {
        return serviceCatalogService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Cria um serviço")
    public ResponseEntity<ServiceResponse> create(@Valid @RequestBody ServiceCreateRequest request) {
        ServiceResponse response = serviceCatalogService.create(request);
        return ResponseEntity
                .created(URI.create("/api/v1/services/" + response.id()))
                .body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um serviço")
    public ServiceResponse update(
            @PathVariable Long id,
            @Valid @RequestBody ServiceUpdateRequest request
    ) {
        return serviceCatalogService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Inativa um serviço sem apagar o histórico")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        serviceCatalogService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
