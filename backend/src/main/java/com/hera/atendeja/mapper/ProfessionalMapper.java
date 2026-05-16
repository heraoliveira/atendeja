package com.hera.atendeja.mapper;

import com.hera.atendeja.dto.professional.ProfessionalCreateRequest;
import com.hera.atendeja.dto.professional.ProfessionalResponse;
import com.hera.atendeja.dto.professional.ProfessionalUpdateRequest;
import com.hera.atendeja.entity.Professional;
import org.springframework.stereotype.Component;

@Component
public class ProfessionalMapper {

    public Professional toEntity(ProfessionalCreateRequest request) {
        Professional professional = new Professional();
        professional.setActive(true);
        professional.setName(TextNormalizer.required(request.name()));
        professional.setPhone(TextNormalizer.required(request.phone()));
        professional.setEmail(TextNormalizer.optional(request.email()));
        return professional;
    }

    public void updateEntity(Professional professional, ProfessionalUpdateRequest request) {
        professional.setName(TextNormalizer.required(request.name()));
        professional.setPhone(TextNormalizer.required(request.phone()));
        professional.setEmail(TextNormalizer.optional(request.email()));
        professional.setActive(request.active());
    }

    public ProfessionalResponse toResponse(Professional professional) {
        return new ProfessionalResponse(
                professional.getId(),
                professional.getName(),
                professional.getPhone(),
                professional.getEmail(),
                professional.isActive(),
                professional.getCreatedAt(),
                professional.getUpdatedAt()
        );
    }
}
