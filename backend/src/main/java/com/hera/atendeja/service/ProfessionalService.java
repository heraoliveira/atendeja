package com.hera.atendeja.service;

import com.hera.atendeja.dto.professional.ProfessionalCreateRequest;
import com.hera.atendeja.dto.professional.ProfessionalResponse;
import com.hera.atendeja.dto.professional.ProfessionalUpdateRequest;
import com.hera.atendeja.entity.Professional;
import com.hera.atendeja.exception.ResourceNotFoundException;
import com.hera.atendeja.mapper.ProfessionalMapper;
import com.hera.atendeja.repository.ProfessionalRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfessionalService {

    private final ProfessionalRepository professionalRepository;
    private final ProfessionalMapper professionalMapper;

    public ProfessionalService(
            ProfessionalRepository professionalRepository,
            ProfessionalMapper professionalMapper
    ) {
        this.professionalRepository = professionalRepository;
        this.professionalMapper = professionalMapper;
    }

    @Transactional(readOnly = true)
    public Page<ProfessionalResponse> findAll(String search, Boolean active, Pageable pageable) {
        return professionalRepository.search(SearchNormalizer.toLikePattern(search), active, pageable)
                .map(professionalMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ProfessionalResponse findById(Long id) {
        return professionalMapper.toResponse(getById(id));
    }

    @Transactional
    public ProfessionalResponse create(ProfessionalCreateRequest request) {
        Professional professional = professionalMapper.toEntity(request);
        return professionalMapper.toResponse(professionalRepository.save(professional));
    }

    @Transactional
    public ProfessionalResponse update(Long id, ProfessionalUpdateRequest request) {
        Professional professional = getById(id);
        professionalMapper.updateEntity(professional, request);
        return professionalMapper.toResponse(professional);
    }

    @Transactional
    public void deactivate(Long id) {
        Professional professional = getById(id);
        professional.setActive(false);
    }

    private Professional getById(Long id) {
        return professionalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profissional", id));
    }
}
