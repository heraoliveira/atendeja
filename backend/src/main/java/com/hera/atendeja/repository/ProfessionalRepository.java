package com.hera.atendeja.repository;

import com.hera.atendeja.entity.Professional;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProfessionalRepository extends JpaRepository<Professional, Long> {

    @Query("""
            SELECT professional
            FROM Professional professional
            WHERE (:active IS NULL OR professional.active = :active)
              AND (
                :search IS NULL
                OR LOWER(professional.name) LIKE :search
                OR LOWER(professional.phone) LIKE :search
                OR LOWER(COALESCE(professional.email, '')) LIKE :search
              )
            """)
    Page<Professional> search(
            @Param("search") String search,
            @Param("active") Boolean active,
            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT professional
            FROM Professional professional
            WHERE professional.id = :id
            """)
    Optional<Professional> findByIdForUpdate(@Param("id") Long id);
}
