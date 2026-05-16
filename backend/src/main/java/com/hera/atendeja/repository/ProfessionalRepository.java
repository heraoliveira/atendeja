package com.hera.atendeja.repository;

import com.hera.atendeja.entity.Professional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
