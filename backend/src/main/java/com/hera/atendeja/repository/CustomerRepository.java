package com.hera.atendeja.repository;

import com.hera.atendeja.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @Query("""
            SELECT customer
            FROM Customer customer
            WHERE (:active IS NULL OR customer.active = :active)
              AND (
                :search IS NULL
                OR LOWER(customer.name) LIKE :search
                OR LOWER(customer.phone) LIKE :search
                OR (
                    :phoneDigitsSearch IS NOT NULL
                    AND CAST(FUNCTION('regexp_replace', customer.phone, '[^0-9]', '', 'g') AS string) LIKE :phoneDigitsSearch
                )
                OR LOWER(COALESCE(customer.email, '')) LIKE :search
                OR LOWER(COALESCE(customer.document, '')) LIKE :search
              )
            """)
    Page<Customer> search(
            @Param("search") String search,
            @Param("phoneDigitsSearch") String phoneDigitsSearch,
            @Param("active") Boolean active,
            Pageable pageable
    );

    @Query("""
            SELECT customer
            FROM Customer customer
            WHERE customer.active = true
              AND (
                :search IS NULL
                OR LOWER(customer.name) LIKE :search
                OR LOWER(customer.phone) LIKE :search
                OR (
                    :phoneDigitsSearch IS NOT NULL
                    AND CAST(FUNCTION('regexp_replace', customer.phone, '[^0-9]', '', 'g') AS string) LIKE :phoneDigitsSearch
                )
                OR LOWER(COALESCE(customer.email, '')) LIKE :search
              )
            """)
    Page<Customer> searchActive(
            @Param("search") String search,
            @Param("phoneDigitsSearch") String phoneDigitsSearch,
            Pageable pageable
    );
}
