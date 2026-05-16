package com.hera.atendeja.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hera.atendeja.dto.customer.CustomerResponse;
import com.hera.atendeja.service.CustomerService;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerService customerService;

    @Test
    void shouldCreateCustomer() throws Exception {
        CustomerResponse response = new CustomerResponse(
                1L,
                "Maria Souza",
                "11999999999",
                "maria@example.com",
                "12345678900",
                true,
                Instant.parse("2026-05-15T12:00:00Z"),
                Instant.parse("2026-05-15T12:00:00Z")
        );
        when(customerService.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Maria Souza",
                                  "phone": "11999999999",
                                  "email": "maria@example.com",
                                  "document": "12345678900"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, "/api/v1/customers/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Maria Souza"));
    }

    @Test
    void shouldReturnValidationErrorWhenRequiredFieldsAreMissing() throws Exception {
        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "invalid-email"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors").isArray());
    }
}
