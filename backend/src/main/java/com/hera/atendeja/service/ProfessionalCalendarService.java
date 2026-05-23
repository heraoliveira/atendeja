package com.hera.atendeja.service;

import com.hera.atendeja.dto.calendar.AvailabilityRuleCreateRequest;
import com.hera.atendeja.dto.calendar.AvailabilityRuleResponse;
import com.hera.atendeja.dto.calendar.AvailabilityRuleUpdateRequest;
import com.hera.atendeja.dto.calendar.ScheduleExceptionRequest;
import com.hera.atendeja.dto.calendar.ScheduleExceptionResponse;
import com.hera.atendeja.entity.AvailabilityRule;
import com.hera.atendeja.entity.Professional;
import com.hera.atendeja.entity.ScheduleException;
import com.hera.atendeja.exception.BusinessRuleException;
import com.hera.atendeja.exception.ResourceNotFoundException;
import com.hera.atendeja.mapper.AvailabilityRuleMapper;
import com.hera.atendeja.mapper.ScheduleExceptionMapper;
import com.hera.atendeja.repository.AvailabilityRuleRepository;
import com.hera.atendeja.repository.ProfessionalRepository;
import com.hera.atendeja.repository.ScheduleExceptionRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfessionalCalendarService {

    private final ProfessionalRepository professionalRepository;
    private final AvailabilityRuleRepository availabilityRuleRepository;
    private final ScheduleExceptionRepository scheduleExceptionRepository;
    private final AvailabilityRuleMapper availabilityRuleMapper;
    private final ScheduleExceptionMapper scheduleExceptionMapper;

    public ProfessionalCalendarService(
            ProfessionalRepository professionalRepository,
            AvailabilityRuleRepository availabilityRuleRepository,
            ScheduleExceptionRepository scheduleExceptionRepository,
            AvailabilityRuleMapper availabilityRuleMapper,
            ScheduleExceptionMapper scheduleExceptionMapper
    ) {
        this.professionalRepository = professionalRepository;
        this.availabilityRuleRepository = availabilityRuleRepository;
        this.scheduleExceptionRepository = scheduleExceptionRepository;
        this.availabilityRuleMapper = availabilityRuleMapper;
        this.scheduleExceptionMapper = scheduleExceptionMapper;
    }

    @Transactional(readOnly = true)
    public List<AvailabilityRuleResponse> findAvailabilityRules(Long professionalId) {
        getProfessional(professionalId);
        return availabilityRuleRepository.findByProfessionalId(professionalId)
                .stream()
                .sorted(Comparator.comparing(AvailabilityRule::getDayOfWeek)
                        .thenComparing(AvailabilityRule::getStartTime))
                .map(availabilityRuleMapper::toResponse)
                .toList();
    }

    @Transactional
    public AvailabilityRuleResponse createAvailabilityRule(
            Long professionalId,
            AvailabilityRuleCreateRequest request
    ) {
        validateWindow(request.startTime(), request.endTime());
        Professional professional = getProfessional(professionalId);
        AvailabilityRule rule = availabilityRuleMapper.toEntity(professional, request);
        return availabilityRuleMapper.toResponse(availabilityRuleRepository.save(rule));
    }

    @Transactional
    public AvailabilityRuleResponse updateAvailabilityRule(
            Long professionalId,
            Long ruleId,
            AvailabilityRuleUpdateRequest request
    ) {
        validateWindow(request.startTime(), request.endTime());
        getProfessional(professionalId);
        AvailabilityRule rule = getAvailabilityRule(professionalId, ruleId);
        availabilityRuleMapper.updateEntity(rule, request);
        return availabilityRuleMapper.toResponse(rule);
    }

    @Transactional
    public void deactivateAvailabilityRule(Long professionalId, Long ruleId) {
        getProfessional(professionalId);
        AvailabilityRule rule = getAvailabilityRule(professionalId, ruleId);
        rule.setActive(false);
    }

    @Transactional(readOnly = true)
    public List<ScheduleExceptionResponse> findScheduleExceptions(
            Long professionalId,
            LocalDate from,
            LocalDate to
    ) {
        validateDateRange(from, to);
        getProfessional(professionalId);
        return scheduleExceptionRepository
                .findByProfessionalIdAndDateBetweenOrderByDateAscStartTimeAsc(professionalId, from, to)
                .stream()
                .map(scheduleExceptionMapper::toResponse)
                .toList();
    }

    @Transactional
    public ScheduleExceptionResponse createScheduleException(
            Long professionalId,
            ScheduleExceptionRequest request
    ) {
        validateWindow(request.startTime(), request.endTime());
        Professional professional = getProfessional(professionalId);
        ScheduleException exception = scheduleExceptionMapper.toEntity(professional, request);
        return scheduleExceptionMapper.toResponse(scheduleExceptionRepository.save(exception));
    }

    @Transactional
    public ScheduleExceptionResponse updateScheduleException(
            Long professionalId,
            Long exceptionId,
            ScheduleExceptionRequest request
    ) {
        validateWindow(request.startTime(), request.endTime());
        getProfessional(professionalId);
        ScheduleException exception = getScheduleException(professionalId, exceptionId);
        scheduleExceptionMapper.updateEntity(exception, request);
        return scheduleExceptionMapper.toResponse(exception);
    }

    @Transactional
    public void deleteScheduleException(Long professionalId, Long exceptionId) {
        getProfessional(professionalId);
        scheduleExceptionRepository.delete(getScheduleException(professionalId, exceptionId));
    }

    private Professional getProfessional(Long id) {
        return professionalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profissional", id));
    }

    private AvailabilityRule getAvailabilityRule(Long professionalId, Long ruleId) {
        return availabilityRuleRepository.findByIdAndProfessionalId(ruleId, professionalId)
                .orElseThrow(() -> new ResourceNotFoundException("Regra de disponibilidade", ruleId));
    }

    private ScheduleException getScheduleException(Long professionalId, Long exceptionId) {
        return scheduleExceptionRepository.findByIdAndProfessionalId(exceptionId, professionalId)
                .orElseThrow(() -> new ResourceNotFoundException("Exceção de agenda", exceptionId));
    }

    private void validateWindow(LocalTime startTime, LocalTime endTime) {
        if (!endTime.isAfter(startTime)) {
            throw new BusinessRuleException("Horário final deve ser posterior ao horário inicial.");
        }
    }

    private void validateDateRange(LocalDate from, LocalDate to) {
        if (to.isBefore(from)) {
            throw new BusinessRuleException("Data final deve ser igual ou posterior à data inicial.");
        }
    }
}
