package com.hera.atendeja.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.hera.atendeja.entity.Appointment;
import com.hera.atendeja.entity.AppointmentStatus;
import com.hera.atendeja.entity.AvailabilityRule;
import com.hera.atendeja.entity.Customer;
import com.hera.atendeja.entity.Professional;
import com.hera.atendeja.entity.ScheduleException;
import com.hera.atendeja.entity.ScheduleExceptionType;
import com.hera.atendeja.entity.ServiceCatalog;
import com.hera.atendeja.exception.ResourceNotFoundException;
import com.hera.atendeja.repository.AppointmentRepository;
import com.hera.atendeja.repository.AvailabilityRuleRepository;
import com.hera.atendeja.repository.ProfessionalRepository;
import com.hera.atendeja.repository.ScheduleExceptionRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    private static final LocalDate DASHBOARD_DATE = LocalDate.of(2030, 1, 21);
    private static final Instant DAY_START = Instant.parse("2030-01-21T03:00:00Z");
    private static final Instant DAY_END = Instant.parse("2030-01-22T03:00:00Z");

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AvailabilityRuleRepository availabilityRuleRepository;

    @Mock
    private ScheduleExceptionRepository scheduleExceptionRepository;

    @Mock
    private ProfessionalRepository professionalRepository;

    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardService(
                appointmentRepository,
                availabilityRuleRepository,
                scheduleExceptionRepository,
                professionalRepository,
                new BusinessTime(Clock.fixed(Instant.parse("2030-01-21T12:00:00Z"), ZoneOffset.UTC))
        );
    }

    @Test
    void shouldBuildDailyDashboardWithoutProfessionalFilter() {
        Professional professional = professional(2L, "Ana Profissional");
        Professional anotherProfessional = professional(3L, "Bruno Profissional");
        when(appointmentRepository.findForDailyDashboard(DAY_START, DAY_END, null)).thenReturn(List.of(
                appointment(10L, professional, AppointmentStatus.SCHEDULED, "2030-01-21T11:00:00Z", "2030-01-21T12:00:00Z"),
                appointment(11L, professional, AppointmentStatus.COMPLETED, "2030-01-21T16:00:00Z", "2030-01-21T17:00:00Z"),
                appointment(12L, professional, AppointmentStatus.CANCELED, "2030-01-21T18:00:00Z", "2030-01-21T19:00:00Z"),
                appointment(13L, anotherProfessional, AppointmentStatus.NO_SHOW, "2030-01-21T13:00:00Z", "2030-01-21T14:00:00Z"),
                appointment(14L, anotherProfessional, AppointmentStatus.CONFIRMED, "2030-01-21T14:00:00Z", "2030-01-21T15:00:00Z")
        ));
        when(availabilityRuleRepository.findActiveRulesForDashboard(DASHBOARD_DATE.getDayOfWeek(), null)).thenReturn(List.of(
                availabilityRule(professional, LocalTime.of(8, 0), LocalTime.of(12, 0)),
                availabilityRule(professional, LocalTime.of(13, 0), LocalTime.of(17, 0)),
                availabilityRule(anotherProfessional, LocalTime.of(9, 0), LocalTime.of(10, 0))
        ));
        when(scheduleExceptionRepository.findByDateForDashboard(DASHBOARD_DATE, null)).thenReturn(List.of(
                scheduleException(professional, ScheduleExceptionType.BLOCKED, LocalTime.of(10, 0), LocalTime.of(11, 0)),
                scheduleException(professional, ScheduleExceptionType.AVAILABLE, LocalTime.of(17, 0), LocalTime.of(18, 0))
        ));

        var response = dashboardService.getDailyDashboard(DASHBOARD_DATE, null);

        assertThat(response.date()).isEqualTo(DASHBOARD_DATE);
        assertThat(response.professional()).isNull();
        assertThat(response.totalAppointments()).isEqualTo(5);
        assertThat(response.appointmentsByStatus().get(AppointmentStatus.SCHEDULED)).isEqualTo(1);
        assertThat(response.appointmentsByStatus().get(AppointmentStatus.CONFIRMED)).isEqualTo(1);
        assertThat(response.appointmentsByStatus().get(AppointmentStatus.COMPLETED)).isEqualTo(1);
        assertThat(response.cancellations()).isEqualTo(1);
        assertThat(response.noShows()).isEqualTo(1);
        assertThat(response.availableMinutes()).isEqualTo(540);
        assertThat(response.occupiedMinutes()).isEqualTo(180);
        assertThat(response.occupancyPercentage()).isEqualByComparingTo(new BigDecimal("33.33"));
    }

    @Test
    void shouldBuildDailyDashboardForOneProfessionalAndNormalizeOverlappingWindows() {
        Professional professional = professional(2L, "Ana Profissional");
        when(professionalRepository.findById(2L)).thenReturn(Optional.of(professional));
        when(appointmentRepository.findForDailyDashboard(DAY_START, DAY_END, 2L)).thenReturn(List.of(
                appointment(10L, professional, AppointmentStatus.CHECKED_IN, "2030-01-21T12:00:00Z", "2030-01-21T13:00:00Z")
        ));
        when(availabilityRuleRepository.findActiveRulesForDashboard(DASHBOARD_DATE.getDayOfWeek(), 2L)).thenReturn(List.of(
                availabilityRule(professional, LocalTime.of(8, 0), LocalTime.of(12, 0)),
                availabilityRule(professional, LocalTime.of(10, 0), LocalTime.of(14, 0))
        ));
        when(scheduleExceptionRepository.findByDateForDashboard(DASHBOARD_DATE, 2L)).thenReturn(List.of(
                scheduleException(professional, ScheduleExceptionType.BLOCKED, LocalTime.of(11, 0), LocalTime.of(12, 0)),
                scheduleException(professional, ScheduleExceptionType.AVAILABLE, LocalTime.of(13, 0), LocalTime.of(15, 0))
        ));

        var response = dashboardService.getDailyDashboard(DASHBOARD_DATE, 2L);

        assertThat(response.professional().id()).isEqualTo(2L);
        assertThat(response.professional().name()).isEqualTo("Ana Profissional");
        assertThat(response.availableMinutes()).isEqualTo(360);
        assertThat(response.occupiedMinutes()).isEqualTo(60);
        assertThat(response.occupancyPercentage()).isEqualByComparingTo(new BigDecimal("16.67"));
    }

    @Test
    void shouldProtectOccupancyAgainstDivisionByZero() {
        Professional professional = professional(2L, "Ana Profissional");
        when(appointmentRepository.findForDailyDashboard(DAY_START, DAY_END, null)).thenReturn(List.of(
                appointment(10L, professional, AppointmentStatus.SCHEDULED, "2030-01-21T12:00:00Z", "2030-01-21T13:00:00Z")
        ));
        when(availabilityRuleRepository.findActiveRulesForDashboard(DASHBOARD_DATE.getDayOfWeek(), null)).thenReturn(List.of());
        when(scheduleExceptionRepository.findByDateForDashboard(DASHBOARD_DATE, null)).thenReturn(List.of());

        var response = dashboardService.getDailyDashboard(DASHBOARD_DATE, null);

        assertThat(response.availableMinutes()).isZero();
        assertThat(response.occupiedMinutes()).isEqualTo(60);
        assertThat(response.occupancyPercentage()).isEqualByComparingTo(new BigDecimal("0.00"));
    }

    @Test
    void shouldThrowWhenProfessionalFilterDoesNotExist() {
        when(professionalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dashboardService.getDailyDashboard(DASHBOARD_DATE, 99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Profissional");
    }

    private Appointment appointment(
            Long id,
            Professional professional,
            AppointmentStatus status,
            String startAt,
            String endAt
    ) {
        Appointment appointment = new Appointment();
        ReflectionTestUtils.setField(appointment, "id", id);
        appointment.setCustomer(customer());
        appointment.setProfessional(professional);
        appointment.setService(service());
        appointment.setStartAt(Instant.parse(startAt));
        appointment.setEndAt(Instant.parse(endAt));
        appointment.setStatus(status);
        return appointment;
    }

    private AvailabilityRule availabilityRule(Professional professional, LocalTime startTime, LocalTime endTime) {
        AvailabilityRule rule = new AvailabilityRule();
        rule.setProfessional(professional);
        rule.setDayOfWeek(DASHBOARD_DATE.getDayOfWeek());
        rule.setStartTime(startTime);
        rule.setEndTime(endTime);
        rule.setActive(true);
        return rule;
    }

    private ScheduleException scheduleException(
            Professional professional,
            ScheduleExceptionType type,
            LocalTime startTime,
            LocalTime endTime
    ) {
        ScheduleException exception = new ScheduleException();
        exception.setProfessional(professional);
        exception.setDate(DASHBOARD_DATE);
        exception.setStartTime(startTime);
        exception.setEndTime(endTime);
        exception.setType(type);
        return exception;
    }

    private Customer customer() {
        Customer customer = new Customer();
        ReflectionTestUtils.setField(customer, "id", 1L);
        customer.setName("Maria Souza");
        customer.setPhone("11999999999");
        return customer;
    }

    private Professional professional(Long id, String name) {
        Professional professional = new Professional();
        ReflectionTestUtils.setField(professional, "id", id);
        professional.setName(name);
        professional.setPhone("11988887777");
        professional.setActive(true);
        return professional;
    }

    private ServiceCatalog service() {
        ServiceCatalog service = new ServiceCatalog();
        ReflectionTestUtils.setField(service, "id", 3L);
        service.setName("Consulta inicial");
        service.setDurationMinutes(45);
        service.setBufferMinutes(15);
        return service;
    }
}
