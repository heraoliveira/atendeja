package com.hera.atendeja.service;

import com.hera.atendeja.dto.customer.CustomerCreateRequest;
import com.hera.atendeja.dto.customer.CustomerResponse;
import com.hera.atendeja.dto.customer.CustomerUpdateRequest;
import com.hera.atendeja.entity.Customer;
import com.hera.atendeja.exception.ResourceNotFoundException;
import com.hera.atendeja.mapper.CustomerMapper;
import com.hera.atendeja.repository.CustomerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public CustomerService(CustomerRepository customerRepository, CustomerMapper customerMapper) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
    }

    @Transactional(readOnly = true)
    public Page<CustomerResponse> findAll(String search, Boolean active, Pageable pageable) {
        return customerRepository.search(SearchNormalizer.toLikePattern(search), active, pageable)
                .map(customerMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public CustomerResponse findById(Long id) {
        return customerMapper.toResponse(getById(id));
    }

    @Transactional
    public CustomerResponse create(CustomerCreateRequest request) {
        Customer customer = customerMapper.toEntity(request);
        return customerMapper.toResponse(customerRepository.save(customer));
    }

    @Transactional
    public CustomerResponse update(Long id, CustomerUpdateRequest request) {
        Customer customer = getById(id);
        customerMapper.updateEntity(customer, request);
        return customerMapper.toResponse(customer);
    }

    @Transactional
    public void deactivate(Long id) {
        Customer customer = getById(id);
        customer.setActive(false);
    }

    private Customer getById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", id));
    }
}
