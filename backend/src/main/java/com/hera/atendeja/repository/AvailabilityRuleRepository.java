package com.hera.atendeja.repository;

import com.hera.atendeja.entity.AvailabilityRule;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AvailabilityRuleRepository extends JpaRepository<AvailabilityRule, Long> {

    List<AvailabilityRule> findByProfessionalId(Long professionalId);

    Optional<AvailabilityRule> findByIdAndProfessionalId(Long id, Long professionalId);

    @Query("""
            SELECT rule
            FROM AvailabilityRule rule
            JOIN FETCH rule.professional professional
            WHERE rule.active = true
              AND rule.dayOfWeek = :dayOfWeek
              AND professional.active = true
              AND (:professionalId IS NULL OR professional.id = :professionalId)
            """)
    List<AvailabilityRule> findActiveRulesForDashboard(
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("professionalId") Long professionalId
    );
}
