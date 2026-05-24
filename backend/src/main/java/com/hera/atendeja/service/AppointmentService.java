package com.hera.atendeja.service;

import com.hera.atendeja.dto.appointment.AppointmentCancelRequest;
import com.hera.atendeja.dto.appointment.AppointmentCreateRequest;
import com.hera.atendeja.dto.appointment.AppointmentNoShowRequest;
import com.hera.atendeja.dto.appointment.AppointmentRescheduleRequest;
import com.hera.atendeja.dto.appointment.AppointmentResponse;
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
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppointmentService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentService.class);

    private static final Set<AppointmentStatus> NON_BLOCKING_STATUSES = Set.of(
            AppointmentStatus.CANCELED,
            AppointmentStatus.NO_SHOW
    );

    private static final Set<AppointmentStatus> CLOSED_STATUSES = Set.of(
            AppointmentStatus.COMPLETED,
            AppointmentStatus.CANCELED,
            AppointmentStatus.NO_SHOW
    );

    private static final Instant MIN_SEARCH_INSTANT = Instant.parse("1900-01-01T00:00:00Z");
    private static final Instant MAX_SEARCH_INSTANT = Instant.parse("9999-12-31T23:59:59Z");

    private final AppointmentRepository appointmentRepository;
    private final CustomerRepository customerRepository;
    private final ProfessionalRepository professionalRepository;
    private final ServiceCatalogRepository serviceCatalogRepository;
    private final AppointmentMapper appointmentMapper;
    private final BusinessTime businessTime;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            CustomerRepository customerRepository,
            ProfessionalRepository professionalRepository,
            ServiceCatalogRepository serviceCatalogRepository,
            AppointmentMapper appointmentMapper,
            BusinessTime businessTime
    ) {
        this.appointmentRepository = appointmentRepository;
        this.customerRepository = customerRepository;
        this.professionalRepository = professionalRepository;
        this.serviceCatalogRepository = serviceCatalogRepository;
        this.appointmentMapper = appointmentMapper;
        this.businessTime = businessTime;
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponse> findAll(
            LocalDate date,
            Long professionalId,
            Long customerId,
            Long serviceId,
            AppointmentStatus status,
            Pageable pageable
    ) {
        Instant dayStart = date == null ? MIN_SEARCH_INSTANT : businessTime.startOfDay(date);
        Instant dayEnd = date == null ? MAX_SEARCH_INSTANT : businessTime.endOfDay(date);
        return appointmentRepository.search(
                        professionalId,
                        customerId,
                        serviceId,
                        status,
                        dayStart,
                        dayEnd,
                        pageable
                )
                .map(appointmentMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public AppointmentResponse findById(Long id) {
        return appointmentMapper.toResponse(getById(id));
    }

    @Transactional
    public AppointmentResponse create(AppointmentCreateRequest request) {
        Customer customer = getActiveCustomer(request.customerId());
        Professional professional = getActiveProfessionalForScheduling(request.professionalId());
        ServiceCatalog service = getActiveService(request.serviceId());
        Instant endAt = calculateEndAt(request.startAt(), service);

        ensureNoScheduleConflict(null, professional.getId(), request.startAt(), endAt);

        Appointment appointment = appointmentMapper.toEntity(
                customer,
                professional,
                service,
                request.startAt(),
                endAt
        );
        Appointment savedAppointment = appointmentRepository.save(appointment);
        log.info(
                "Appointment created: id={}, customerId={}, professionalId={}, serviceId={}, startAt={}, endAt={}",
                savedAppointment.getId(),
                customer.getId(),
                professional.getId(),
                service.getId(),
                savedAppointment.getStartAt(),
                savedAppointment.getEndAt()
        );
        return appointmentMapper.toResponse(savedAppointment);
    }

    @Transactional
    public AppointmentResponse reschedule(Long id, AppointmentRescheduleRequest request) {
        Appointment appointment = getById(id);
        ensureAppointmentCanChange(appointment);
        Professional professional = getActiveProfessionalForScheduling(appointment.getProfessional().getId());
        ensureActiveAppointmentResources(appointment);

        Instant endAt = calculateEndAt(request.startAt(), appointment.getService());
        ensureNoScheduleConflict(
                appointment.getId(),
                professional.getId(),
                request.startAt(),
                endAt
        );

        appointment.setStartAt(request.startAt());
        appointment.setEndAt(endAt);
        return appointmentMapper.toResponse(appointment);
    }

    @Transactional
    public AppointmentResponse cancel(Long id, AppointmentCancelRequest request) {
        Appointment appointment = getById(id);
        if (CLOSED_STATUSES.contains(appointment.getStatus())) {
            throw new BusinessRuleException("Agendamentos finalizados, cancelados ou com falta não podem ser cancelados.");
        }

        appointment.setStatus(AppointmentStatus.CANCELED);
        appointment.setCancelReason(request.cancelReason().trim());
        log.info("Appointment canceled: id={}, professionalId={}", appointment.getId(), appointment.getProfessional().getId());
        return appointmentMapper.toResponse(appointment);
    }

    @Transactional
    public AppointmentResponse confirm(Long id) {
        Appointment appointment = getById(id);
        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new BusinessRuleException("Somente agendamentos pendentes podem ser confirmados.");
        }
        ensureAppointmentIsOpen(appointment, "confirmado");

        appointment.setStatus(AppointmentStatus.CONFIRMED);
        return appointmentMapper.toResponse(appointment);
    }

    @Transactional
    public AppointmentResponse checkIn(Long id) {
        Appointment appointment = getById(id);
        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new BusinessRuleException("Somente agendamentos confirmados podem registrar check-in.");
        }
        if (!businessTime.isCurrentBusinessDate(appointment.getStartAt())) {
            throw new BusinessRuleException("Check-in só pode ser registrado no dia do agendamento.");
        }

        appointment.setStatus(AppointmentStatus.CHECKED_IN);
        appointment.setCheckedInAt(businessTime.now());
        return appointmentMapper.toResponse(appointment);
    }

    @Transactional
    public AppointmentResponse complete(Long id) {
        Appointment appointment = getById(id);
        if (appointment.getStatus() != AppointmentStatus.CHECKED_IN
                && appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new BusinessRuleException("Somente agendamentos confirmados ou com check-in podem ser concluídos.");
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);
        return appointmentMapper.toResponse(appointment);
    }

    @Transactional
    public AppointmentResponse noShow(Long id, AppointmentNoShowRequest request) {
        Appointment appointment = getById(id);
        if (appointment.getStatus() != AppointmentStatus.SCHEDULED
                && appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new BusinessRuleException("Somente agendamentos pendentes ou confirmados podem ser marcados como falta.");
        }
        if (appointment.getEndAt().isAfter(businessTime.now())) {
            throw new BusinessRuleException("Falta só pode ser registrada após o horário final do agendamento.");
        }

        appointment.setStatus(AppointmentStatus.NO_SHOW);
        appointment.setNoShowReason(request == null ? null : normalizeOptional(request.noShowReason()));
        return appointmentMapper.toResponse(appointment);
    }

    private void ensureNoScheduleConflict(Long ignoredAppointmentId, Long professionalId, Instant startAt, Instant endAt) {
        boolean hasConflict = appointmentRepository.existsScheduleConflict(
                professionalId,
                startAt,
                endAt,
                ignoredAppointmentId,
                NON_BLOCKING_STATUSES
        );
        if (hasConflict) {
            throw new AppointmentConflictException();
        }
    }

    private Instant calculateEndAt(Instant startAt, ServiceCatalog service) {
        long totalMinutes = service.getDurationMinutes() + service.getBufferMinutes();
        return startAt.plus(Duration.ofMinutes(totalMinutes));
    }

    private void ensureAppointmentIsOpen(Appointment appointment, String action) {
        if (!appointment.getEndAt().isAfter(businessTime.now())) {
            throw new BusinessRuleException("Agendamento encerrado não pode ser " + action + ".");
        }
    }

    private void ensureAppointmentCanChange(Appointment appointment) {
        if (CLOSED_STATUSES.contains(appointment.getStatus())) {
            throw new BusinessRuleException("Agendamentos finalizados, cancelados ou com falta não podem ser remarcados.");
        }
    }

    private void ensureActiveAppointmentResources(Appointment appointment) {
        if (!appointment.getCustomer().isActive()) {
            throw new BusinessRuleException("Cliente inativo não pode receber novos agendamentos.");
        }
        if (!appointment.getProfessional().isActive()) {
            throw new BusinessRuleException("Profissional inativo não pode receber novos agendamentos.");
        }
        if (!appointment.getService().isActive()) {
            throw new BusinessRuleException("Serviço inativo não pode ser agendado.");
        }
    }

    private Customer getActiveCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", id));
        if (!customer.isActive()) {
            throw new BusinessRuleException("Cliente inativo não pode receber novos agendamentos.");
        }
        return customer;
    }

    private Professional getActiveProfessionalForScheduling(Long id) {
        Professional professional = professionalRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profissional", id));
        if (!professional.isActive()) {
            throw new BusinessRuleException("Profissional inativo não pode receber novos agendamentos.");
        }
        return professional;
    }

    private ServiceCatalog getActiveService(Long id) {
        ServiceCatalog service = serviceCatalogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço", id));
        if (!service.isActive()) {
            throw new BusinessRuleException("Serviço inativo não pode ser agendado.");
        }
        return service;
    }

    private Appointment getById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento", id));
    }

    private String normalizeOptional(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        return text.trim();
    }
}
