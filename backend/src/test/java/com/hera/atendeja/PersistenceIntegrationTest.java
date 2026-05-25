package com.hera.atendeja;

import static org.assertj.core.api.Assertions.assertThat;

import com.hera.atendeja.entity.Appointment;
import com.hera.atendeja.entity.AppointmentStatus;
import com.hera.atendeja.entity.AvailabilityRule;
import com.hera.atendeja.entity.Customer;
import com.hera.atendeja.entity.Professional;
import com.hera.atendeja.entity.ScheduleException;
import com.hera.atendeja.entity.ScheduleExceptionType;
import com.hera.atendeja.entity.ServiceCatalog;
import com.hera.atendeja.repository.AppointmentRepository;
import com.hera.atendeja.repository.AvailabilityRuleRepository;
import com.hera.atendeja.repository.CustomerRepository;
import com.hera.atendeja.repository.ProfessionalRepository;
import com.hera.atendeja.repository.ScheduleExceptionRepository;
import com.hera.atendeja.repository.ServiceCatalogRepository;
import com.hera.atendeja.service.DashboardService;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
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
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest
@Testcontainers
class PersistenceIntegrationTest {

    @Container
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine");

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProfessionalRepository professionalRepository;

    @Autowired
    private ServiceCatalogRepository serviceCatalogRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private AvailabilityRuleRepository availabilityRuleRepository;

    @Autowired
    private ScheduleExceptionRepository scheduleExceptionRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DashboardService dashboardService;

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @BeforeEach
    void cleanDatabase() {
        appointmentRepository.deleteAll();
        scheduleExceptionRepository.deleteAll();
        availabilityRuleRepository.deleteAll();
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
        assertThat(appliedMigrations).isEqualTo(6);
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

        var result = customerRepository.search("%beatriz%", null, true, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Beatriz Silva");
    }

    @Test
    void shouldSearchOnlyActiveCustomersForAutocompleteByNamePhoneAndEmail() {
        Customer nameMatch = new Customer();
        nameMatch.setName("Cliente Mariana");
        nameMatch.setPhone("11900000001");
        nameMatch.setEmail("mariana@example.com");

        Customer phoneMatch = new Customer();
        phoneMatch.setName("Cliente Telefone");
        phoneMatch.setPhone("(67) 2643-1365");
        phoneMatch.setEmail("telefone@example.com");

        Customer emailMatch = new Customer();
        emailMatch.setName("Cliente Email");
        emailMatch.setPhone("11900000003");
        emailMatch.setEmail("agenda.email@example.com");

        Customer inactiveMatch = new Customer();
        inactiveMatch.setName("Cliente Mariana Inativo");
        inactiveMatch.setPhone("21987654322");
        inactiveMatch.setEmail("agenda.email.inativo@example.com");
        inactiveMatch.setActive(false);

        customerRepository.saveAll(Set.of(nameMatch, phoneMatch, emailMatch, inactiveMatch));

        var byName = customerRepository.searchActive("%mariana%", null, PageRequest.of(0, 10));
        var byPhoneWithAreaCode = customerRepository.searchActive("%672643%", "%672643%", PageRequest.of(0, 10));
        var byPhoneWithoutAreaCode = customerRepository.searchActive("%26431365%", "%26431365%", PageRequest.of(0, 10));
        var byFullPhoneDigits = customerRepository.searchActive("%6726431365%", "%6726431365%", PageRequest.of(0, 10));
        var byEmail = customerRepository.searchActive("%agenda.email@example.com%", null, PageRequest.of(0, 10));
        var limited = customerRepository.searchActive("%cliente%", null, PageRequest.of(0, 2));

        assertThat(byName.getContent()).extracting(Customer::getName).containsExactly("Cliente Mariana");
        assertThat(byPhoneWithAreaCode.getContent()).extracting(Customer::getName).containsExactly("Cliente Telefone");
        assertThat(byPhoneWithoutAreaCode.getContent()).extracting(Customer::getName).containsExactly("Cliente Telefone");
        assertThat(byFullPhoneDigits.getContent()).extracting(Customer::getName).containsExactly("Cliente Telefone");
        assertThat(byEmail.getContent()).extracting(Customer::getName).containsExactly("Cliente Email");
        assertThat(limited.getContent()).hasSize(2);
        assertThat(limited.getTotalElements()).isEqualTo(3);
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

    @Test
    void shouldDetectAppointmentConflictUsingHalfOpenIntervals() {
        Customer customer = new Customer();
        customer.setName("Cliente Agenda");
        customer.setPhone("21999990000");
        customerRepository.save(customer);

        Professional professional = new Professional();
        professional.setName("Profissional Agenda");
        professional.setPhone("21999990001");
        professionalRepository.save(professional);

        ServiceCatalog service = new ServiceCatalog();
        service.setName("Consulta agenda");
        service.setDurationMinutes(45);
        service.setBufferMinutes(15);
        service.setPrice(new BigDecimal("150.00"));
        serviceCatalogRepository.save(service);

        Appointment appointment = new Appointment();
        appointment.setCustomer(customer);
        appointment.setProfessional(professional);
        appointment.setService(service);
        appointment.setStartAt(Instant.parse("2030-01-20T10:00:00Z"));
        appointment.setEndAt(Instant.parse("2030-01-20T11:00:00Z"));
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointmentRepository.saveAndFlush(appointment);

        List<String> nonBlockingStatuses = List.of(AppointmentStatus.CANCELED.name(), AppointmentStatus.NO_SHOW.name());

        boolean overlappingConflict = appointmentRepository.existsScheduleConflict(
                professional.getId(),
                Instant.parse("2030-01-20T10:30:00Z"),
                Instant.parse("2030-01-20T11:30:00Z"),
                null,
                nonBlockingStatuses
        );
        boolean exactBoundaryConflict = appointmentRepository.existsScheduleConflict(
                professional.getId(),
                Instant.parse("2030-01-20T11:00:00Z"),
                Instant.parse("2030-01-20T12:00:00Z"),
                null,
                nonBlockingStatuses
        );

        assertThat(overlappingConflict).isTrue();
        assertThat(exactBoundaryConflict).isFalse();

        appointment.setStatus(AppointmentStatus.CANCELED);
        appointmentRepository.saveAndFlush(appointment);
        boolean canceledAppointmentConflict = appointmentRepository.existsScheduleConflict(
                professional.getId(),
                Instant.parse("2030-01-20T10:30:00Z"),
                Instant.parse("2030-01-20T11:30:00Z"),
                null,
                nonBlockingStatuses
        );

        appointment.setStatus(AppointmentStatus.NO_SHOW);
        appointmentRepository.saveAndFlush(appointment);
        boolean noShowAppointmentConflict = appointmentRepository.existsScheduleConflict(
                professional.getId(),
                Instant.parse("2030-01-20T10:30:00Z"),
                Instant.parse("2030-01-20T11:30:00Z"),
                null,
                nonBlockingStatuses
        );

        assertThat(canceledAppointmentConflict).isFalse();
        assertThat(noShowAppointmentConflict).isFalse();
    }

    @Test
    void shouldUseCompletedAtAndServiceBufferWhenDetectingAppointmentConflict() {
        Customer customer = new Customer();
        customer.setName("Cliente Conclusao");
        customer.setPhone("21999990030");
        customerRepository.save(customer);

        Professional professional = new Professional();
        professional.setName("Profissional Conclusao");
        professional.setPhone("21999990031");
        professionalRepository.save(professional);

        ServiceCatalog service = new ServiceCatalog();
        service.setName("Consulta com intervalo");
        service.setDurationMinutes(40);
        service.setBufferMinutes(5);
        service.setPrice(new BigDecimal("120.00"));
        serviceCatalogRepository.save(service);

        Appointment appointment = new Appointment();
        appointment.setCustomer(customer);
        appointment.setProfessional(professional);
        appointment.setService(service);
        appointment.setStartAt(Instant.parse("2030-01-20T20:04:00Z"));
        appointment.setEndAt(Instant.parse("2030-01-20T20:49:00Z"));
        appointment.setStatus(AppointmentStatus.CHECKED_IN);
        appointmentRepository.saveAndFlush(appointment);

        List<String> nonBlockingStatuses = List.of(AppointmentStatus.CANCELED.name(), AppointmentStatus.NO_SHOW.name());

        boolean inProgressConflict = appointmentRepository.existsScheduleConflict(
                professional.getId(),
                Instant.parse("2030-01-20T20:10:00Z"),
                Instant.parse("2030-01-20T20:30:00Z"),
                null,
                nonBlockingStatuses
        );

        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointment.setCompletedAt(Instant.parse("2030-01-20T20:05:00Z"));
        appointmentRepository.saveAndFlush(appointment);

        boolean insideBufferConflict = appointmentRepository.existsScheduleConflict(
                professional.getId(),
                Instant.parse("2030-01-20T20:09:00Z"),
                Instant.parse("2030-01-20T20:30:00Z"),
                null,
                nonBlockingStatuses
        );
        boolean exactBufferBoundaryConflict = appointmentRepository.existsScheduleConflict(
                professional.getId(),
                Instant.parse("2030-01-20T20:10:00Z"),
                Instant.parse("2030-01-20T20:30:00Z"),
                null,
                nonBlockingStatuses
        );
        boolean afterBufferConflict = appointmentRepository.existsScheduleConflict(
                professional.getId(),
                Instant.parse("2030-01-20T20:11:00Z"),
                Instant.parse("2030-01-20T20:30:00Z"),
                null,
                nonBlockingStatuses
        );

        assertThat(inProgressConflict).isTrue();
        assertThat(insideBufferConflict).isTrue();
        assertThat(exactBufferBoundaryConflict).isFalse();
        assertThat(afterBufferConflict).isFalse();
    }

    @Test
    void shouldFilterAppointmentsWithPaginationAndOperationalFilters() {
        Customer customer = new Customer();
        customer.setName("Cliente Filtro");
        customer.setPhone("21999990010");
        customerRepository.save(customer);

        Professional professional = new Professional();
        professional.setName("Profissional Filtro");
        professional.setPhone("21999990011");
        professionalRepository.save(professional);

        ServiceCatalog service = new ServiceCatalog();
        service.setName("Consulta filtro");
        service.setDurationMinutes(45);
        service.setBufferMinutes(15);
        service.setPrice(new BigDecimal("150.00"));
        serviceCatalogRepository.save(service);

        Appointment scheduled = new Appointment();
        scheduled.setCustomer(customer);
        scheduled.setProfessional(professional);
        scheduled.setService(service);
        scheduled.setStartAt(Instant.parse("2030-01-21T12:00:00Z"));
        scheduled.setEndAt(Instant.parse("2030-01-21T13:00:00Z"));
        scheduled.setStatus(AppointmentStatus.SCHEDULED);
        appointmentRepository.save(scheduled);

        Appointment completed = new Appointment();
        completed.setCustomer(customer);
        completed.setProfessional(professional);
        completed.setService(service);
        completed.setStartAt(Instant.parse("2030-01-21T14:00:00Z"));
        completed.setEndAt(Instant.parse("2030-01-21T15:00:00Z"));
        completed.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.save(completed);

        var page = appointmentRepository.search(
                professional.getId(),
                customer.getId(),
                service.getId(),
                AppointmentStatus.SCHEDULED,
                Instant.parse("2030-01-21T03:00:00Z"),
                Instant.parse("2030-01-22T03:00:00Z"),
                PageRequest.of(0, 10)
        );

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
    }

    @Test
    void shouldPersistProfessionalCalendarAndEnforcePeriodConstraints() {
        Professional professional = new Professional();
        professional.setName("Profissional Calendario");
        professional.setPhone("21999990002");
        professionalRepository.save(professional);

        AvailabilityRule availabilityRule = new AvailabilityRule();
        availabilityRule.setProfessional(professional);
        availabilityRule.setDayOfWeek(DayOfWeek.MONDAY);
        availabilityRule.setStartTime(LocalTime.of(8, 0));
        availabilityRule.setEndTime(LocalTime.of(12, 0));
        availabilityRuleRepository.saveAndFlush(availabilityRule);

        ScheduleException scheduleException = new ScheduleException();
        scheduleException.setProfessional(professional);
        scheduleException.setDate(LocalDate.of(2030, 1, 20));
        scheduleException.setStartTime(LocalTime.of(13, 0));
        scheduleException.setEndTime(LocalTime.of(15, 0));
        scheduleException.setType(ScheduleExceptionType.AVAILABLE);
        scheduleException.setReason("Atendimento extra");
        scheduleExceptionRepository.saveAndFlush(scheduleException);

        AvailabilityRule invalidRule = new AvailabilityRule();
        invalidRule.setProfessional(professional);
        invalidRule.setDayOfWeek(DayOfWeek.TUESDAY);
        invalidRule.setStartTime(LocalTime.of(18, 0));
        invalidRule.setEndTime(LocalTime.of(18, 0));

        Assertions.assertThrows(
                DataIntegrityViolationException.class,
                () -> availabilityRuleRepository.saveAndFlush(invalidRule)
        );

        assertThat(availabilityRuleRepository.count()).isEqualTo(1);
        assertThat(scheduleExceptionRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldCalculateDailyDashboardUsingPostgreSQLData() {
        LocalDate dashboardDate = LocalDate.of(2030, 1, 21);

        Customer customer = new Customer();
        customer.setName("Cliente Dashboard");
        customer.setPhone("21999990020");
        customerRepository.save(customer);

        Professional professional = new Professional();
        professional.setName("Profissional Dashboard");
        professional.setPhone("21999990021");
        professionalRepository.save(professional);

        ServiceCatalog service = new ServiceCatalog();
        service.setName("Consulta dashboard");
        service.setDurationMinutes(45);
        service.setBufferMinutes(15);
        service.setPrice(new BigDecimal("150.00"));
        serviceCatalogRepository.save(service);

        AvailabilityRule morning = new AvailabilityRule();
        morning.setProfessional(professional);
        morning.setDayOfWeek(dashboardDate.getDayOfWeek());
        morning.setStartTime(LocalTime.of(8, 0));
        morning.setEndTime(LocalTime.of(12, 0));
        availabilityRuleRepository.save(morning);

        AvailabilityRule overlapping = new AvailabilityRule();
        overlapping.setProfessional(professional);
        overlapping.setDayOfWeek(dashboardDate.getDayOfWeek());
        overlapping.setStartTime(LocalTime.of(10, 0));
        overlapping.setEndTime(LocalTime.of(14, 0));
        availabilityRuleRepository.save(overlapping);

        ScheduleException blocked = new ScheduleException();
        blocked.setProfessional(professional);
        blocked.setDate(dashboardDate);
        blocked.setStartTime(LocalTime.of(11, 0));
        blocked.setEndTime(LocalTime.of(12, 0));
        blocked.setType(ScheduleExceptionType.BLOCKED);
        scheduleExceptionRepository.save(blocked);

        ScheduleException available = new ScheduleException();
        available.setProfessional(professional);
        available.setDate(dashboardDate);
        available.setStartTime(LocalTime.of(13, 0));
        available.setEndTime(LocalTime.of(15, 0));
        available.setType(ScheduleExceptionType.AVAILABLE);
        scheduleExceptionRepository.save(available);

        Appointment scheduled = new Appointment();
        scheduled.setCustomer(customer);
        scheduled.setProfessional(professional);
        scheduled.setService(service);
        scheduled.setStartAt(Instant.parse("2030-01-21T12:00:00Z"));
        scheduled.setEndAt(Instant.parse("2030-01-21T13:00:00Z"));
        scheduled.setStatus(AppointmentStatus.SCHEDULED);
        appointmentRepository.save(scheduled);

        Appointment canceled = new Appointment();
        canceled.setCustomer(customer);
        canceled.setProfessional(professional);
        canceled.setService(service);
        canceled.setStartAt(Instant.parse("2030-01-21T14:00:00Z"));
        canceled.setEndAt(Instant.parse("2030-01-21T15:00:00Z"));
        canceled.setStatus(AppointmentStatus.CANCELED);
        appointmentRepository.save(canceled);

        var response = dashboardService.getDailyDashboard(dashboardDate, professional.getId());

        assertThat(response.totalAppointments()).isEqualTo(2);
        assertThat(response.cancellations()).isEqualTo(1);
        assertThat(response.noShows()).isZero();
        assertThat(response.availableMinutes()).isEqualTo(360);
        assertThat(response.occupiedMinutes()).isEqualTo(60);
        assertThat(response.occupancyPercentage()).isEqualByComparingTo(new BigDecimal("16.67"));
    }
}
