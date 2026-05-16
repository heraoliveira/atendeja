package com.hera.atendeja.controller;

import com.hera.atendeja.dto.customer.CustomerCreateRequest;
import com.hera.atendeja.dto.customer.CustomerResponse;
import com.hera.atendeja.dto.customer.CustomerUpdateRequest;
import com.hera.atendeja.dto.common.PageResponse;
import com.hera.atendeja.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/v1/customers")
@Tag(name = "Customers", description = "Cadastro de clientes")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    @Operation(summary = "Lista clientes com paginação e filtros")
    public PageResponse<CustomerResponse> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        return PageResponse.from(customerService.findAll(search, active, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um cliente por id")
    public CustomerResponse findById(@PathVariable Long id) {
        return customerService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Cria um cliente")
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CustomerCreateRequest request) {
        CustomerResponse response = customerService.create(request);
        return ResponseEntity
                .created(URI.create("/api/v1/customers/" + response.id()))
                .body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um cliente")
    public CustomerResponse update(
            @PathVariable Long id,
            @Valid @RequestBody CustomerUpdateRequest request
    ) {
        return customerService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Inativa um cliente sem apagar o histórico")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        customerService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
