package com.hera.atendeja.repository;

import com.hera.atendeja.entity.ScheduleException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ScheduleExceptionRepository extends JpaRepository<ScheduleException, Long> {

    List<ScheduleException> findByProfessionalIdAndDateBetweenOrderByDateAscStartTimeAsc(
            Long professionalId,
            LocalDate from,
            LocalDate to
    );

    Optional<ScheduleException> findByIdAndProfessionalId(Long id, Long professionalId);

    @Query("""
            SELECT exception
            FROM ScheduleException exception
            JOIN FETCH exception.professional professional
            WHERE exception.date = :date
              AND professional.active = true
              AND (:professionalId IS NULL OR professional.id = :professionalId)
            """)
    List<ScheduleException> findByDateForDashboard(
            @Param("date") LocalDate date,
            @Param("professionalId") Long professionalId
    );
}
