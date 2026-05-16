package com.hera.atendeja;

import static org.assertj.core.api.Assertions.assertThat;

import com.hera.atendeja.entity.Customer;
import com.hera.atendeja.entity.Professional;
import com.hera.atendeja.entity.ServiceCatalog;
import com.hera.atendeja.repository.CustomerRepository;
import com.hera.atendeja.repository.ProfessionalRepository;
import com.hera.atendeja.repository.ServiceCatalogRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
class PersistenceIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProfessionalRepository professionalRepository;

    @Autowired
    private ServiceCatalogRepository serviceCatalogRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @BeforeEach
    void cleanDatabase() {
        serviceCatalogRepository.deleteAll();
        professionalRepository.deleteAll();
        customerRepository.deleteAll();
    }

    @Test
    void shouldApplyMigrationsAndPersistInitialCrudEntities() {
        Customer customer = new Customer();
        customer.setName("Ana Cliente");
        customer.setPhone("11988887777");
        customer.setEmail("ana@example.com");
        customer.setDocument("11122233344");

        Professional professional = new Professional();
        professional.setName("Dr. Carlos");
        professional.setPhone("11977776666");
        professional.setEmail("carlos@example.com");

        ServiceCatalog service = new ServiceCatalog();
        service.setName("Consulta inicial");
        service.setDescription("Atendimento de avaliação");
        service.setDurationMinutes(45);
        service.setBufferMinutes(15);
        service.setPrice(new BigDecimal("150.00"));

        customerRepository.save(customer);
        professionalRepository.save(professional);
        serviceCatalogRepository.save(service);

        assertThat(customerRepository.count()).isEqualTo(1);
        assertThat(professionalRepository.count()).isEqualTo(1);
        assertThat(serviceCatalogRepository.count()).isEqualTo(1);

        Integer appliedMigrations = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM flyway_schema_history WHERE success = TRUE",
                Integer.class
        );
        assertThat(appliedMigrations).isEqualTo(2);
    }

    @Test
    void shouldFilterCustomersBySearchAndActiveStatus() {
        Customer activeCustomer = new Customer();
        activeCustomer.setName("Beatriz Silva");
        activeCustomer.setPhone("21999990000");
        activeCustomer.setEmail("beatriz@example.com");
        activeCustomer.setDocument("22233344455");

        Customer inactiveCustomer = new Customer();
        inactiveCustomer.setName("Cliente Antigo");
        inactiveCustomer.setPhone("21911110000");
        inactiveCustomer.setActive(false);

        customerRepository.save(activeCustomer);
        customerRepository.save(inactiveCustomer);

        var result = customerRepository.search("%beatriz%", true, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Beatriz Silva");
    }

    @Test
    void shouldEnforceUniqueAndCheckConstraintsInPostgreSQL() {
        Customer customer = new Customer();
        customer.setName("Cliente Original");
        customer.setPhone("21999990000");
        customer.setEmail("duplicado@example.com");
        customerRepository.saveAndFlush(customer);

        Customer duplicatedEmail = new Customer();
        duplicatedEmail.setName("Cliente Duplicado");
        duplicatedEmail.setPhone("21999990001");
        duplicatedEmail.setEmail("duplicado@example.com");

        Assertions.assertThrows(
                DataIntegrityViolationException.class,
                () -> customerRepository.saveAndFlush(duplicatedEmail)
        );

        ServiceCatalog invalidService = new ServiceCatalog();
        invalidService.setName("Servico invalido");
        invalidService.setDurationMinutes(0);
        invalidService.setBufferMinutes(-1);
        invalidService.setPrice(new BigDecimal("-1.00"));

        Assertions.assertThrows(
                DataIntegrityViolationException.class,
                () -> serviceCatalogRepository.saveAndFlush(invalidService)
        );
    }
}
