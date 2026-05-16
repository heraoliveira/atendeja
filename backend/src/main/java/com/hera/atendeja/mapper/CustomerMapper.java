package com.hera.atendeja.mapper;

import com.hera.atendeja.dto.customer.CustomerCreateRequest;
import com.hera.atendeja.dto.customer.CustomerResponse;
import com.hera.atendeja.dto.customer.CustomerUpdateRequest;
import com.hera.atendeja.entity.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public Customer toEntity(CustomerCreateRequest request) {
        Customer customer = new Customer();
        customer.setActive(true);
        customer.setName(TextNormalizer.required(request.name()));
        customer.setPhone(TextNormalizer.required(request.phone()));
        customer.setEmail(TextNormalizer.optional(request.email()));
        customer.setDocument(TextNormalizer.optional(request.document()));
        return customer;
    }

    public void updateEntity(Customer customer, CustomerUpdateRequest request) {
        customer.setName(TextNormalizer.required(request.name()));
        customer.setPhone(TextNormalizer.required(request.phone()));
        customer.setEmail(TextNormalizer.optional(request.email()));
        customer.setDocument(TextNormalizer.optional(request.document()));
        customer.setActive(request.active());
    }

    public CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getPhone(),
                customer.getEmail(),
                customer.getDocument(),
                customer.isActive(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }
}
