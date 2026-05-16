package com.hera.atendeja.service;

import com.hera.atendeja.dto.service.ServiceCreateRequest;
import com.hera.atendeja.dto.service.ServiceResponse;
import com.hera.atendeja.dto.service.ServiceUpdateRequest;
import com.hera.atendeja.entity.ServiceCatalog;
import com.hera.atendeja.exception.ResourceNotFoundException;
import com.hera.atendeja.mapper.ServiceCatalogMapper;
import com.hera.atendeja.repository.ServiceCatalogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServiceCatalogService {

    private final ServiceCatalogRepository serviceCatalogRepository;
    private final ServiceCatalogMapper serviceCatalogMapper;

    public ServiceCatalogService(
            ServiceCatalogRepository serviceCatalogRepository,
            ServiceCatalogMapper serviceCatalogMapper
    ) {
        this.serviceCatalogRepository = serviceCatalogRepository;
        this.serviceCatalogMapper = serviceCatalogMapper;
    }

    @Transactional(readOnly = true)
    public Page<ServiceResponse> findAll(String search, Boolean active, Pageable pageable) {
        return serviceCatalogRepository.search(SearchNormalizer.toLikePattern(search), active, pageable)
                .map(serviceCatalogMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ServiceResponse findById(Long id) {
        return serviceCatalogMapper.toResponse(getById(id));
    }

    @Transactional
    public ServiceResponse create(ServiceCreateRequest request) {
        ServiceCatalog service = serviceCatalogMapper.toEntity(request);
        return serviceCatalogMapper.toResponse(serviceCatalogRepository.save(service));
    }

    @Transactional
    public ServiceResponse update(Long id, ServiceUpdateRequest request) {
        ServiceCatalog service = getById(id);
        serviceCatalogMapper.updateEntity(service, request);
        return serviceCatalogMapper.toResponse(service);
    }

    @Transactional
    public void deactivate(Long id) {
        ServiceCatalog service = getById(id);
        service.setActive(false);
    }

    private ServiceCatalog getById(Long id) {
        return serviceCatalogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço", id));
    }
}
