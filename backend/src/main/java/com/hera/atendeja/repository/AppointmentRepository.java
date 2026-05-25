package com.hera.atendeja.repository;

import com.hera.atendeja.entity.Appointment;
import com.hera.atendeja.entity.AppointmentStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    @Override
    @EntityGraph(attributePaths = {"customer", "professional", "service"})
    Optional<Appointment> findById(Long id);

    @Query(value = """
            SELECT EXISTS (
                SELECT 1
                FROM appointments appointment
                JOIN services service ON service.id = appointment.service_id
                WHERE appointment.professional_id = :professionalId
                  AND appointment.start_at < :newEndAt
                  AND (
                    CASE
                        WHEN appointment.status = 'COMPLETED' AND appointment.completed_at IS NOT NULL
                            THEN appointment.completed_at + (service.buffer_minutes * INTERVAL '1 minute')
                        ELSE appointment.end_at
                    END
                  ) > :newStartAt
                  AND appointment.status NOT IN (:nonBlockingStatuses)
                  AND (:ignoredAppointmentId IS NULL OR appointment.id <> :ignoredAppointmentId)
            )
            """, nativeQuery = true)
    boolean existsScheduleConflict(
            @Param("professionalId") Long professionalId,
            @Param("newStartAt") Instant newStartAt,
            @Param("newEndAt") Instant newEndAt,
            @Param("ignoredAppointmentId") Long ignoredAppointmentId,
            @Param("nonBlockingStatuses") List<String> nonBlockingStatuses
    );

    @EntityGraph(attributePaths = {"customer", "professional", "service"})
    @Query("""
            SELECT appointment
            FROM Appointment appointment
            WHERE (:professionalId IS NULL OR appointment.professional.id = :professionalId)
              AND (:customerId IS NULL OR appointment.customer.id = :customerId)
              AND (:serviceId IS NULL OR appointment.service.id = :serviceId)
              AND (:status IS NULL OR appointment.status = :status)
              AND appointment.startAt >= :dayStart
              AND appointment.startAt < :dayEnd
            """)
    Page<Appointment> search(
            @Param("professionalId") Long professionalId,
            @Param("customerId") Long customerId,
            @Param("serviceId") Long serviceId,
            @Param("status") AppointmentStatus status,
            @Param("dayStart") Instant dayStart,
            @Param("dayEnd") Instant dayEnd,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"customer", "professional", "service"})
    @Query("""
            SELECT appointment
            FROM Appointment appointment
            WHERE appointment.startAt < :dayEnd
              AND appointment.endAt > :dayStart
              AND (:professionalId IS NULL OR appointment.professional.id = :professionalId)
            """)
    List<Appointment> findForDailyDashboard(
            @Param("dayStart") Instant dayStart,
            @Param("dayEnd") Instant dayEnd,
            @Param("professionalId") Long professionalId
    );
}
