package com.hera.atendeja.repository;

import com.hera.atendeja.entity.ServiceCatalog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ServiceCatalogRepository extends JpaRepository<ServiceCatalog, Long> {

    @Query("""
            SELECT service
            FROM ServiceCatalog service
            WHERE (:active IS NULL OR service.active = :active)
              AND (
                :search IS NULL
                OR LOWER(service.name) LIKE :search
                OR LOWER(COALESCE(service.description, '')) LIKE :search
              )
            """)
    Page<ServiceCatalog> search(
            @Param("search") String search,
            @Param("active") Boolean active,
            Pageable pageable
    );
}
