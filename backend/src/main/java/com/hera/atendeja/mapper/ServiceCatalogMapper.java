package com.hera.atendeja.mapper;

import com.hera.atendeja.dto.service.ServiceCreateRequest;
import com.hera.atendeja.dto.service.ServiceResponse;
import com.hera.atendeja.dto.service.ServiceUpdateRequest;
import com.hera.atendeja.entity.ServiceCatalog;
import org.springframework.stereotype.Component;

@Component
public class ServiceCatalogMapper {

    public ServiceCatalog toEntity(ServiceCreateRequest request) {
        ServiceCatalog service = new ServiceCatalog();
        service.setActive(true);
        service.setName(TextNormalizer.required(request.name()));
        service.setDescription(TextNormalizer.optional(request.description()));
        service.setDurationMinutes(request.durationMinutes());
        service.setBufferMinutes(request.bufferMinutes());
        service.setPrice(request.price());
        return service;
    }

    public void updateEntity(ServiceCatalog service, ServiceUpdateRequest request) {
        service.setName(TextNormalizer.required(request.name()));
        service.setDescription(TextNormalizer.optional(request.description()));
        service.setDurationMinutes(request.durationMinutes());
        service.setBufferMinutes(request.bufferMinutes());
        service.setPrice(request.price());
        service.setActive(request.active());
    }

    public ServiceResponse toResponse(ServiceCatalog service) {
        return new ServiceResponse(
                service.getId(),
                service.getName(),
                service.getDescription(),
                service.getDurationMinutes(),
                service.getBufferMinutes(),
                service.getPrice(),
                service.isActive(),
                service.getCreatedAt(),
                service.getUpdatedAt()
        );
    }
}
