package com.hera.atendeja.service;

import com.hera.atendeja.dto.customer.CustomerCreateRequest;
import com.hera.atendeja.dto.customer.CustomerResponse;
import com.hera.atendeja.dto.customer.CustomerUpdateRequest;
import com.hera.atendeja.entity.Customer;
import com.hera.atendeja.exception.ResourceNotFoundException;
import com.hera.atendeja.mapper.CustomerMapper;
import com.hera.atendeja.repository.CustomerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {

    private static final int MIN_AUTOCOMPLETE_QUERY_LENGTH = 2;
    private static final int MAX_AUTOCOMPLETE_SIZE = 20;

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
    public Page<CustomerResponse> searchActive(String query, Pageable pageable) {
        Pageable limitedPageable = limitAutocompletePageable(pageable);
        if (query == null || query.trim().length() < MIN_AUTOCOMPLETE_QUERY_LENGTH) {
            return Page.empty(limitedPageable);
        }
        return customerRepository.searchActive(SearchNormalizer.toLikePattern(query), limitedPageable)
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

    private Pageable limitAutocompletePageable(Pageable pageable) {
        int requestedSize = pageable.isPaged() ? pageable.getPageSize() : MAX_AUTOCOMPLETE_SIZE;
        int pageSize = Math.min(requestedSize, MAX_AUTOCOMPLETE_SIZE);
        int pageNumber = pageable.isPaged() ? pageable.getPageNumber() : 0;
        Sort sort = pageable.getSortOr(Sort.by("name").ascending());
        return PageRequest.of(pageNumber, pageSize, sort);
    }
}
