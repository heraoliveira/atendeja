package com.hera.atendeja.service;

import com.hera.atendeja.dto.dashboard.DailyDashboardResponse;
import com.hera.atendeja.dto.dashboard.DashboardProfessionalResponse;
import com.hera.atendeja.entity.Appointment;
import com.hera.atendeja.entity.AppointmentStatus;
import com.hera.atendeja.entity.AvailabilityRule;
import com.hera.atendeja.entity.Professional;
import com.hera.atendeja.entity.ScheduleException;
import com.hera.atendeja.entity.ScheduleExceptionType;
import com.hera.atendeja.exception.ResourceNotFoundException;
import com.hera.atendeja.repository.AppointmentRepository;
import com.hera.atendeja.repository.AvailabilityRuleRepository;
import com.hera.atendeja.repository.ProfessionalRepository;
import com.hera.atendeja.repository.ScheduleExceptionRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private static final Set<AppointmentStatus> OCCUPYING_STATUSES = EnumSet.of(
            AppointmentStatus.SCHEDULED,
            AppointmentStatus.CONFIRMED,
            AppointmentStatus.CHECKED_IN,
            AppointmentStatus.COMPLETED
    );

    private final AppointmentRepository appointmentRepository;
    private final AvailabilityRuleRepository availabilityRuleRepository;
    private final ScheduleExceptionRepository scheduleExceptionRepository;
    private final ProfessionalRepository professionalRepository;
    private final BusinessTime businessTime;

    public DashboardService(
            AppointmentRepository appointmentRepository,
            AvailabilityRuleRepository availabilityRuleRepository,
            ScheduleExceptionRepository scheduleExceptionRepository,
            ProfessionalRepository professionalRepository,
            BusinessTime businessTime
    ) {
        this.appointmentRepository = appointmentRepository;
        this.availabilityRuleRepository = availabilityRuleRepository;
        this.scheduleExceptionRepository = scheduleExceptionRepository;
        this.professionalRepository = professionalRepository;
        this.businessTime = businessTime;
    }

    @Transactional(readOnly = true)
    public DailyDashboardResponse getDailyDashboard(LocalDate date, Long professionalId) {
        LocalDate targetDate = date == null ? businessTime.today() : date;
        Professional professional = professionalId == null ? null : getProfessional(professionalId);
        Instant dayStart = businessTime.startOfDay(targetDate);
        Instant dayEnd = businessTime.endOfDay(targetDate);

        List<Appointment> appointments = appointmentRepository.findForDailyDashboard(
                dayStart,
                dayEnd,
                professionalId
        );
        Map<AppointmentStatus, Long> appointmentsByStatus = countByStatus(appointments);
        long occupiedMinutes = calculateOccupiedMinutes(appointments, dayStart, dayEnd);
        long availableMinutes = calculateAvailableMinutes(targetDate, professionalId);

        return new DailyDashboardResponse(
                targetDate,
                professional == null ? null : new DashboardProfessionalResponse(professional.getId(), professional.getName()),
                appointments.size(),
                appointmentsByStatus,
                appointmentsByStatus.get(AppointmentStatus.CANCELED),
                appointmentsByStatus.get(AppointmentStatus.NO_SHOW),
                availableMinutes,
                occupiedMinutes,
                calculateOccupancyPercentage(occupiedMinutes, availableMinutes)
        );
    }

    private Professional getProfessional(Long professionalId) {
        return professionalRepository.findById(professionalId)
                .orElseThrow(() -> new ResourceNotFoundException("Profissional", professionalId));
    }

    private Map<AppointmentStatus, Long> countByStatus(List<Appointment> appointments) {
        Map<AppointmentStatus, Long> counts = new EnumMap<>(AppointmentStatus.class);
        for (AppointmentStatus status : AppointmentStatus.values()) {
            counts.put(status, 0L);
        }
        for (Appointment appointment : appointments) {
            counts.computeIfPresent(appointment.getStatus(), (status, total) -> total + 1);
        }

        Map<AppointmentStatus, Long> ordered = new LinkedHashMap<>();
        for (AppointmentStatus status : AppointmentStatus.values()) {
            ordered.put(status, counts.get(status));
        }
        return ordered;
    }

    private long calculateOccupiedMinutes(List<Appointment> appointments, Instant dayStart, Instant dayEnd) {
        return appointments.stream()
                .filter(appointment -> OCCUPYING_STATUSES.contains(appointment.getStatus()))
                .mapToLong(appointment -> overlapMinutes(
                        appointment.getStartAt(),
                        appointment.getEndAt(),
                        dayStart,
                        dayEnd
                ))
                .sum();
    }

    private long overlapMinutes(Instant startAt, Instant endAt, Instant dayStart, Instant dayEnd) {
        Instant effectiveStart = startAt.isBefore(dayStart) ? dayStart : startAt;
        Instant effectiveEnd = endAt.isAfter(dayEnd) ? dayEnd : endAt;
        if (!effectiveEnd.isAfter(effectiveStart)) {
            return 0;
        }
        return Duration.between(effectiveStart, effectiveEnd).toMinutes();
    }

    private long calculateAvailableMinutes(LocalDate date, Long professionalId) {
        List<AvailabilityRule> rules = availabilityRuleRepository.findActiveRulesForDashboard(
                date.getDayOfWeek(),
                professionalId
        );
        List<ScheduleException> exceptions = scheduleExceptionRepository.findByDateForDashboard(date, professionalId);

        Map<Long, List<TimeWindow>> baseWindowsByProfessional = new HashMap<>();
        for (AvailabilityRule rule : rules) {
            Long id = rule.getProfessional().getId();
            baseWindowsByProfessional
                    .computeIfAbsent(id, ignored -> new ArrayList<>())
                    .add(new TimeWindow(rule.getStartTime(), rule.getEndTime()));
        }

        Map<Long, List<TimeWindow>> blockedWindowsByProfessional = new HashMap<>();
        Map<Long, List<TimeWindow>> extraWindowsByProfessional = new HashMap<>();
        for (ScheduleException exception : exceptions) {
            Long id = exception.getProfessional().getId();
            TimeWindow window = new TimeWindow(exception.getStartTime(), exception.getEndTime());
            Map<Long, List<TimeWindow>> target = exception.getType() == ScheduleExceptionType.BLOCKED
                    ? blockedWindowsByProfessional
                    : extraWindowsByProfessional;
            target.computeIfAbsent(id, ignored -> new ArrayList<>()).add(window);
        }

        Set<Long> professionalIds = new java.util.HashSet<>();
        professionalIds.addAll(baseWindowsByProfessional.keySet());
        professionalIds.addAll(blockedWindowsByProfessional.keySet());
        professionalIds.addAll(extraWindowsByProfessional.keySet());

        long total = 0;
        for (Long id : professionalIds) {
            List<TimeWindow> available = normalize(baseWindowsByProfessional.getOrDefault(id, List.of()));
            List<TimeWindow> blocked = normalize(blockedWindowsByProfessional.getOrDefault(id, List.of()));
            List<TimeWindow> extra = normalize(extraWindowsByProfessional.getOrDefault(id, List.of()));

            available = subtractAll(available, blocked);
            List<TimeWindow> combined = new ArrayList<>(available);
            combined.addAll(extra);
            total += normalize(combined).stream()
                    .mapToLong(TimeWindow::durationMinutes)
                    .sum();
        }
        return total;
    }

    private BigDecimal calculateOccupancyPercentage(long occupiedMinutes, long availableMinutes) {
        if (availableMinutes <= 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        return BigDecimal.valueOf(occupiedMinutes)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(availableMinutes), 2, RoundingMode.HALF_UP);
    }

    private List<TimeWindow> normalize(List<TimeWindow> windows) {
        if (windows.isEmpty()) {
            return List.of();
        }

        List<TimeWindow> sorted = windows.stream()
                .sorted(Comparator.comparing(TimeWindow::start).thenComparing(TimeWindow::end))
                .toList();
        List<TimeWindow> normalized = new ArrayList<>();
        TimeWindow current = sorted.get(0);

        for (int index = 1; index < sorted.size(); index++) {
            TimeWindow next = sorted.get(index);
            if (!next.start().isAfter(current.end())) {
                current = new TimeWindow(current.start(), max(current.end(), next.end()));
            } else {
                normalized.add(current);
                current = next;
            }
        }
        normalized.add(current);
        return normalized;
    }

    private List<TimeWindow> subtractAll(List<TimeWindow> available, List<TimeWindow> blocked) {
        List<TimeWindow> result = available;
        for (TimeWindow block : blocked) {
            List<TimeWindow> next = new ArrayList<>();
            for (TimeWindow window : result) {
                next.addAll(window.subtract(block));
            }
            result = next;
        }
        return result;
    }

    private LocalTime max(LocalTime left, LocalTime right) {
        return left.isAfter(right) ? left : right;
    }

    private record TimeWindow(LocalTime start, LocalTime end) {

        long durationMinutes() {
            return Duration.between(start, end).toMinutes();
        }

        List<TimeWindow> subtract(TimeWindow block) {
            if (!block.end().isAfter(start) || !block.start().isBefore(end)) {
                return List.of(this);
            }

            List<TimeWindow> result = new ArrayList<>();
            if (block.start().isAfter(start)) {
                result.add(new TimeWindow(start, min(block.start(), end)));
            }
            if (block.end().isBefore(end)) {
                result.add(new TimeWindow(max(block.end(), start), end));
            }
            return result;
        }

        private LocalTime min(LocalTime left, LocalTime right) {
            return left.isBefore(right) ? left : right;
        }

        private LocalTime max(LocalTime left, LocalTime right) {
            return left.isAfter(right) ? left : right;
        }
    }
}
