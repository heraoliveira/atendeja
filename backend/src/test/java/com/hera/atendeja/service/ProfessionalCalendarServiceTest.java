package com.hera.atendeja.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hera.atendeja.dto.calendar.AvailabilityRuleCreateRequest;
import com.hera.atendeja.dto.calendar.AvailabilityRuleUpdateRequest;
import com.hera.atendeja.dto.calendar.ScheduleExceptionRequest;
import com.hera.atendeja.entity.AvailabilityRule;
import com.hera.atendeja.entity.Professional;
import com.hera.atendeja.entity.ScheduleException;
import com.hera.atendeja.entity.ScheduleExceptionType;
import com.hera.atendeja.exception.BusinessRuleException;
import com.hera.atendeja.exception.ResourceNotFoundException;
import com.hera.atendeja.mapper.AvailabilityRuleMapper;
import com.hera.atendeja.mapper.ScheduleExceptionMapper;
import com.hera.atendeja.repository.AvailabilityRuleRepository;
import com.hera.atendeja.repository.ProfessionalRepository;
import com.hera.atendeja.repository.ScheduleExceptionRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ProfessionalCalendarServiceTest {

    @Mock
    private ProfessionalRepository professionalRepository;

    @Mock
    private AvailabilityRuleRepository availabilityRuleRepository;

    @Mock
    private ScheduleExceptionRepository scheduleExceptionRepository;

    private ProfessionalCalendarService professionalCalendarService;

    @BeforeEach
    void setUp() {
        professionalCalendarService = new ProfessionalCalendarService(
                professionalRepository,
                availabilityRuleRepository,
                scheduleExceptionRepository,
                new AvailabilityRuleMapper(),
                new ScheduleExceptionMapper()
        );
    }

    @Test
    void shouldCreateWeeklyAvailabilityRule() {
        Professional professional = professional();
        when(professionalRepository.findById(1L)).thenReturn(Optional.of(professional));
        when(availabilityRuleRepository.save(any())).thenAnswer(invocation -> withRuleId(invocation.getArgument(0), 10L));

        var response = professionalCalendarService.createAvailabilityRule(
                1L,
                new AvailabilityRuleCreateRequest(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0))
        );

        assertThat(response.professionalId()).isEqualTo(1L);
        assertThat(response.dayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(response.active()).isTrue();
    }

    @Test
    void shouldRejectInvalidAvailabilityWindow() {
        assertThatThrownBy(() -> professionalCalendarService.createAvailabilityRule(
                1L,
                new AvailabilityRuleCreateRequest(DayOfWeek.MONDAY, LocalTime.NOON, LocalTime.NOON)
        ))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Horário final");
    }

    @Test
    void shouldUpdateAndDeactivateAvailabilityRule() {
        AvailabilityRule rule = availabilityRule(professional());
        when(professionalRepository.findById(1L)).thenReturn(Optional.of(rule.getProfessional()));
        when(availabilityRuleRepository.findByIdAndProfessionalId(10L, 1L)).thenReturn(Optional.of(rule));

        professionalCalendarService.updateAvailabilityRule(
                1L,
                10L,
                new AvailabilityRuleUpdateRequest(
                        DayOfWeek.TUESDAY,
                        LocalTime.of(13, 0),
                        LocalTime.of(18, 0),
                        true
                )
        );
        professionalCalendarService.deactivateAvailabilityRule(1L, 10L);

        assertThat(rule.getDayOfWeek()).isEqualTo(DayOfWeek.TUESDAY);
        assertThat(rule.getStartTime()).isEqualTo(LocalTime.of(13, 0));
        assertThat(rule.isActive()).isFalse();
    }

    @Test
    void shouldListScheduleExceptionsByDateRange() {
        Professional professional = professional();
        ScheduleException exception = scheduleException(professional);
        when(professionalRepository.findById(1L)).thenReturn(Optional.of(professional));
        when(scheduleExceptionRepository.findByProfessionalIdAndDateBetweenOrderByDateAscStartTimeAsc(
                1L,
                LocalDate.of(2030, 1, 20),
                LocalDate.of(2030, 1, 21)
        )).thenReturn(List.of(exception));

        var result = professionalCalendarService.findScheduleExceptions(
                1L,
                LocalDate.of(2030, 1, 20),
                LocalDate.of(2030, 1, 21)
        );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).type()).isEqualTo(ScheduleExceptionType.BLOCKED);
    }

    @Test
    void shouldCreateAvailableScheduleExceptionWithTrimmedReason() {
        Professional professional = professional();
        when(professionalRepository.findById(1L)).thenReturn(Optional.of(professional));
        when(scheduleExceptionRepository.save(any())).thenAnswer(invocation -> withExceptionId(invocation.getArgument(0), 20L));

        var response = professionalCalendarService.createScheduleException(
                1L,
                new ScheduleExceptionRequest(
                        LocalDate.of(2030, 1, 20),
                        LocalTime.of(18, 0),
                        LocalTime.of(20, 0),
                        ScheduleExceptionType.AVAILABLE,
                        "  Horário extra  "
                )
        );

        assertThat(response.reason()).isEqualTo("Horário extra");
    }

    @Test
    void shouldDeleteScheduleExceptionOwnedByProfessional() {
        Professional professional = professional();
        ScheduleException exception = scheduleException(professional);
        when(professionalRepository.findById(1L)).thenReturn(Optional.of(professional));
        when(scheduleExceptionRepository.findByIdAndProfessionalId(20L, 1L)).thenReturn(Optional.of(exception));

        professionalCalendarService.deleteScheduleException(1L, 20L);

        verify(scheduleExceptionRepository).delete(exception);
    }

    @Test
    void shouldRejectMissingProfessional() {
        when(professionalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> professionalCalendarService.findAvailabilityRules(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Profissional");
    }

    private Professional professional() {
        Professional professional = new Professional();
        ReflectionTestUtils.setField(professional, "id", 1L);
        professional.setName("Ana Profissional");
        professional.setPhone("11999999999");
        return professional;
    }

    private AvailabilityRule availabilityRule(Professional professional) {
        AvailabilityRule rule = new AvailabilityRule();
        ReflectionTestUtils.setField(rule, "id", 10L);
        rule.setProfessional(professional);
        rule.setDayOfWeek(DayOfWeek.MONDAY);
        rule.setStartTime(LocalTime.of(8, 0));
        rule.setEndTime(LocalTime.of(12, 0));
        rule.setActive(true);
        return rule;
    }

    private ScheduleException scheduleException(Professional professional) {
        ScheduleException exception = new ScheduleException();
        ReflectionTestUtils.setField(exception, "id", 20L);
        exception.setProfessional(professional);
        exception.setDate(LocalDate.of(2030, 1, 20));
        exception.setStartTime(LocalTime.of(8, 0));
        exception.setEndTime(LocalTime.of(12, 0));
        exception.setType(ScheduleExceptionType.BLOCKED);
        return exception;
    }

    private AvailabilityRule withRuleId(AvailabilityRule rule, Long id) {
        ReflectionTestUtils.setField(rule, "id", id);
        return rule;
    }

    private ScheduleException withExceptionId(ScheduleException exception, Long id) {
        ReflectionTestUtils.setField(exception, "id", id);
        return exception;
    }
}
