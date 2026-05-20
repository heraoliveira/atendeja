package com.hera.atendeja.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hera.atendeja.dto.appointment.AppointmentCancelRequest;
import com.hera.atendeja.dto.appointment.AppointmentCreateRequest;
import com.hera.atendeja.dto.appointment.AppointmentRescheduleRequest;
import com.hera.atendeja.entity.Appointment;
import com.hera.atendeja.entity.AppointmentStatus;
import com.hera.atendeja.entity.Customer;
import com.hera.atendeja.entity.Professional;
import com.hera.atendeja.entity.ServiceCatalog;
import com.hera.atendeja.exception.AppointmentConflictException;
import com.hera.atendeja.exception.BusinessRuleException;
import com.hera.atendeja.exception.ResourceNotFoundException;
import com.hera.atendeja.mapper.AppointmentMapper;
import com.hera.atendeja.repository.AppointmentRepository;
import com.hera.atendeja.repository.CustomerRepository;
import com.hera.atendeja.repository.ProfessionalRepository;
import com.hera.atendeja.repository.ServiceCatalogRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProfessionalRepository professionalRepository;

    @Mock
    private ServiceCatalogRepository serviceCatalogRepository;

    private AppointmentService appointmentService;

    @BeforeEach
    void setUp() {
        appointmentService = new AppointmentService(
                appointmentRepository,
                customerRepository,
                professionalRepository,
                serviceCatalogRepository,
                new AppointmentMapper()
        );
    }

    @Test
    void shouldCreateAppointmentWithCalculatedEndAt() {
        Instant startAt = Instant.parse("2030-01-20T10:00:00Z");
        Customer customer = customer();
        Professional professional = professional();
        ServiceCatalog service = service(45, 15);
        mockActiveResources(customer, professional, service);
        when(appointmentRepository.existsScheduleConflict(eq(2L), eq(startAt), eq(startAt.plusSeconds(3600)), eq(null), anyCollection()))
                .thenReturn(false);
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        appointmentService.create(new AppointmentCreateRequest(1L, 2L, 3L, startAt));

        ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentRepository).save(captor.capture());
        verify(professionalRepository).findByIdForUpdate(2L);
        Appointment savedAppointment = captor.getValue();
        assertThat(savedAppointment.getCustomer()).isSameAs(customer);
        assertThat(savedAppointment.getProfessional()).isSameAs(professional);
        assertThat(savedAppointment.getService()).isSameAs(service);
        assertThat(savedAppointment.getStartAt()).isEqualTo(startAt);
        assertThat(savedAppointment.getEndAt()).isEqualTo(Instant.parse("2030-01-20T11:00:00Z"));
        assertThat(savedAppointment.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
    }

    @Test
    void shouldBlockOverlappingAppointment() {
        Instant startAt = Instant.parse("2030-01-20T10:30:00Z");
        ServiceCatalog service = service(45, 15);
        mockActiveResources(customer(), professional(), service);
        when(appointmentRepository.existsScheduleConflict(eq(2L), eq(startAt), eq(startAt.plusSeconds(3600)), eq(null), anyCollection()))
                .thenReturn(true);

        assertThatThrownBy(() -> appointmentService.create(new AppointmentCreateRequest(1L, 2L, 3L, startAt)))
                .isInstanceOf(AppointmentConflictException.class)
                .hasMessageContaining("Já existe um agendamento");
    }

    @Test
    void shouldAllowAppointmentStartingExactlyWhenAnotherEnds() {
        Instant startAt = Instant.parse("2030-01-20T11:00:00Z");
        ServiceCatalog service = service(45, 15);
        mockActiveResources(customer(), professional(), service);
        when(appointmentRepository.existsScheduleConflict(eq(2L), eq(startAt), eq(startAt.plusSeconds(3600)), eq(null), anyCollection()))
                .thenReturn(false);
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        appointmentService.create(new AppointmentCreateRequest(1L, 2L, 3L, startAt));

        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    void shouldRescheduleAppointment() {
        Instant newStartAt = Instant.parse("2030-01-21T14:00:00Z");
        Appointment appointment = appointment();
        when(appointmentRepository.findById(10L)).thenReturn(Optional.of(appointment));
        when(professionalRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(appointment.getProfessional()));
        when(appointmentRepository.existsScheduleConflict(eq(2L), eq(newStartAt), eq(newStartAt.plusSeconds(3600)), eq(10L), anyCollection()))
                .thenReturn(false);

        appointmentService.reschedule(10L, new AppointmentRescheduleRequest(newStartAt));

        assertThat(appointment.getStartAt()).isEqualTo(newStartAt);
        assertThat(appointment.getEndAt()).isEqualTo(Instant.parse("2030-01-21T15:00:00Z"));
        verify(professionalRepository).findByIdForUpdate(2L);
    }

    @Test
    void shouldBlockConflictingReschedule() {
        Instant newStartAt = Instant.parse("2030-01-21T14:00:00Z");
        Appointment appointment = appointment();
        when(appointmentRepository.findById(10L)).thenReturn(Optional.of(appointment));
        when(professionalRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(appointment.getProfessional()));
        when(appointmentRepository.existsScheduleConflict(eq(2L), eq(newStartAt), eq(newStartAt.plusSeconds(3600)), eq(10L), anyCollection()))
                .thenReturn(true);

        assertThatThrownBy(() -> appointmentService.reschedule(10L, new AppointmentRescheduleRequest(newStartAt)))
                .isInstanceOf(AppointmentConflictException.class);
    }

    @Test
    void shouldCancelAppointmentWithReason() {
        Appointment appointment = appointment();
        when(appointmentRepository.findById(10L)).thenReturn(Optional.of(appointment));

        appointmentService.cancel(10L, new AppointmentCancelRequest("  Cliente solicitou remarcação  "));

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CANCELED);
        assertThat(appointment.getCancelReason()).isEqualTo("Cliente solicitou remarcação");
    }

    @ParameterizedTest
    @EnumSource(value = AppointmentStatus.class, names = {"COMPLETED", "CANCELED", "NO_SHOW"})
    void shouldBlockCancelWhenAppointmentStatusIsClosed(AppointmentStatus status) {
        Appointment appointment = appointment();
        appointment.setStatus(status);
        when(appointmentRepository.findById(10L)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> appointmentService.cancel(10L, new AppointmentCancelRequest("Cliente solicitou cancelamento")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("não podem ser cancelados");
    }

    @Test
    void shouldFilterAppointmentsByBusinessDateUsingSaoPauloTimeZone() {
        var pageable = PageRequest.of(0, 10);
        Appointment appointment = appointment();
        when(appointmentRepository.search(
                null,
                null,
                null,
                null,
                Instant.parse("2030-01-20T03:00:00Z"),
                Instant.parse("2030-01-21T03:00:00Z"),
                pageable
        )).thenReturn(new PageImpl<>(List.of(appointment), pageable, 1));

        var result = appointmentService.findAll(LocalDate.of(2030, 1, 20), null, null, null, null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void shouldThrowWhenCustomerDoesNotExist() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.create(new AppointmentCreateRequest(
                99L,
                2L,
                3L,
                Instant.parse("2030-01-20T10:00:00Z")
        )))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Cliente");
    }

    @Test
    void shouldThrowWhenProfessionalDoesNotExist() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer()));
        when(professionalRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.create(new AppointmentCreateRequest(
                1L,
                99L,
                3L,
                Instant.parse("2030-01-20T10:00:00Z")
        )))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Profissional");
    }

    @Test
    void shouldThrowWhenServiceDoesNotExist() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer()));
        when(professionalRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(professional()));
        when(serviceCatalogRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.create(new AppointmentCreateRequest(
                1L,
                2L,
                99L,
                Instant.parse("2030-01-20T10:00:00Z")
        )))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Serviço");
    }

    @Test
    void shouldBlockInactiveProfessionalWhenCreatingAppointment() {
        Professional professional = professional();
        professional.setActive(false);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer()));
        when(professionalRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(professional));

        assertThatThrownBy(() -> appointmentService.create(new AppointmentCreateRequest(
                1L,
                2L,
                3L,
                Instant.parse("2030-01-20T10:00:00Z")
        )))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Profissional inativo");
    }

    @Test
    void shouldBlockInactiveServiceWhenReschedulingAppointment() {
        Appointment appointment = appointment();
        appointment.getService().setActive(false);
        when(appointmentRepository.findById(10L)).thenReturn(Optional.of(appointment));
        when(professionalRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(appointment.getProfessional()));

        assertThatThrownBy(() -> appointmentService.reschedule(
                10L,
                new AppointmentRescheduleRequest(Instant.parse("2030-01-21T14:00:00Z"))
        ))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Serviço inativo");
    }

    private void mockActiveResources(Customer customer, Professional professional, ServiceCatalog service) {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(professionalRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(professional));
        when(serviceCatalogRepository.findById(3L)).thenReturn(Optional.of(service));
    }

    private Appointment appointment() {
        Appointment appointment = new Appointment();
        ReflectionTestUtils.setField(appointment, "id", 10L);
        appointment.setCustomer(customer());
        appointment.setProfessional(professional());
        appointment.setService(service(45, 15));
        appointment.setStartAt(Instant.parse("2030-01-20T10:00:00Z"));
        appointment.setEndAt(Instant.parse("2030-01-20T11:00:00Z"));
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        return appointment;
    }

    private Customer customer() {
        Customer customer = new Customer();
        ReflectionTestUtils.setField(customer, "id", 1L);
        customer.setName("Maria Souza");
        customer.setPhone("11999999999");
        customer.setActive(true);
        return customer;
    }

    private Professional professional() {
        Professional professional = new Professional();
        ReflectionTestUtils.setField(professional, "id", 2L);
        professional.setName("Ana Profissional");
        professional.setPhone("11988887777");
        professional.setActive(true);
        return professional;
    }

    private ServiceCatalog service(Integer durationMinutes, Integer bufferMinutes) {
        ServiceCatalog service = new ServiceCatalog();
        ReflectionTestUtils.setField(service, "id", 3L);
        service.setName("Consulta inicial");
        service.setDurationMinutes(durationMinutes);
        service.setBufferMinutes(bufferMinutes);
        service.setPrice(new BigDecimal("150.00"));
        service.setActive(true);
        return service;
    }
}
