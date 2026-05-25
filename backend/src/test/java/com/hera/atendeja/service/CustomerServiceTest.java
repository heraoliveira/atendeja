package com.hera.atendeja.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hera.atendeja.dto.customer.CustomerCreateRequest;
import com.hera.atendeja.dto.customer.CustomerUpdateRequest;
import com.hera.atendeja.entity.Customer;
import com.hera.atendeja.exception.ResourceNotFoundException;
import com.hera.atendeja.mapper.CustomerMapper;
import com.hera.atendeja.repository.CustomerRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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
    void shouldUpdateExistingCustomer() {
        Customer customer = new Customer();
        customer.setName("Nome antigo");
        customer.setPhone("11000000000");
        customer.setEmail("antigo@example.com");
        customer.setDocument("00000000000");
        customer.setActive(true);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        customerService.update(1L, new CustomerUpdateRequest(
                "  Nome novo  ",
                " 11988887777 ",
                "  novo@example.com ",
                " 12345678900 ",
                false
        ));

        assertThat(customer.getName()).isEqualTo("Nome novo");
        assertThat(customer.getPhone()).isEqualTo("11988887777");
        assertThat(customer.getEmail()).isEqualTo("novo@example.com");
        assertThat(customer.getDocument()).isEqualTo("12345678900");
        assertThat(customer.isActive()).isFalse();
    }

    @Test
    void shouldDeactivateExistingCustomer() {
        Customer customer = new Customer();
        customer.setActive(true);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        customerService.deactivate(1L);

        assertThat(customer.isActive()).isFalse();
    }

    @Test
    void shouldFilterCustomersBySearchAndActiveStatus() {
        Customer customer = new Customer();
        customer.setName("Maria Souza");
        customer.setPhone("11999999999");
        customer.setEmail("maria@example.com");
        customer.setDocument("12345678900");
        var pageable = PageRequest.of(0, 10);
        when(customerRepository.search("%maria%", null, true, pageable))
                .thenReturn(new PageImpl<>(List.of(customer), pageable, 1));

        var result = customerService.findAll(" Maria ", true, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("Maria Souza");
    }

    @Test
    void shouldSearchOnlyActiveCustomersForAutocompleteWithLimitedPageSize() {
        Customer customer = new Customer();
        customer.setName("Maria Souza");
        customer.setPhone("11999999999");
        customer.setEmail("maria@example.com");
        Pageable requestedPageable = PageRequest.of(0, 100, Sort.by("name"));
        when(customerRepository.searchActive(eq("%maria%"), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(customer), PageRequest.of(0, 20, Sort.by("name")), 1));

        var result = customerService.searchActive(" Maria ", requestedPageable);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(customerRepository).searchActive(eq("%maria%"), isNull(), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(20);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("Maria Souza");
    }

    @Test
    void shouldSearchActiveCustomersWithNormalizedPhoneDigits() {
        Customer customer = new Customer();
        customer.setName("Maria Souza");
        customer.setPhone("(67) 2643-1365");
        Pageable pageable = PageRequest.of(0, 10);
        when(customerRepository.searchActive(eq("%672643%"), eq("%672643%"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(customer), pageable, 1));

        var result = customerService.searchActive("672643", pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).phone()).isEqualTo("(67) 2643-1365");
    }

    @Test
    void shouldNotSearchAutocompleteWhenQueryHasLessThanTwoCharacters() {
        var pageable = PageRequest.of(0, 10);

        var result = customerService.searchActive("m", pageable);

        assertThat(result.getContent()).isEmpty();
        verify(customerRepository, never()).searchActive(any(), any(), any(Pageable.class));
    }

    @Test
    void shouldThrowWhenCustomerDoesNotExist() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Cliente");
    }
}
