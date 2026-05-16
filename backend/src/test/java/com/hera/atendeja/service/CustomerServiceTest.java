package com.hera.atendeja.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hera.atendeja.dto.customer.CustomerCreateRequest;
import com.hera.atendeja.entity.Customer;
import com.hera.atendeja.exception.ResourceNotFoundException;
import com.hera.atendeja.mapper.CustomerMapper;
import com.hera.atendeja.repository.CustomerRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        customerService = new CustomerService(customerRepository, new CustomerMapper());
    }

    @Test
    void shouldCreateCustomerWithNormalizedOptionalFields() {
        CustomerCreateRequest request = new CustomerCreateRequest(
                "  Maria Souza  ",
                " 11999999999 ",
                "  maria@example.com ",
                " 12345678900 "
        );
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        customerService.create(request);

        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(captor.capture());
        Customer savedCustomer = captor.getValue();
        assertThat(savedCustomer.getName()).isEqualTo("Maria Souza");
        assertThat(savedCustomer.getPhone()).isEqualTo("11999999999");
        assertThat(savedCustomer.getEmail()).isEqualTo("maria@example.com");
        assertThat(savedCustomer.getDocument()).isEqualTo("12345678900");
        assertThat(savedCustomer.isActive()).isTrue();
    }

    @Test
    void shouldThrowWhenCustomerDoesNotExist() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Cliente não encontrado");
    }
}
