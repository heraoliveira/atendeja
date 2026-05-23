package com.hera.atendeja.service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import org.springframework.stereotype.Component;

@Component
public class BusinessTime {

    private static final ZoneId BUSINESS_ZONE = ZoneId.of("America/Sao_Paulo");

    private final Clock clock;

    public BusinessTime(Clock clock) {
        this.clock = clock;
    }

    public Instant now() {
        return clock.instant();
    }

    public LocalDate today() {
        return businessDate(now());
    }

    public Instant startOfDay(LocalDate date) {
        return date.atStartOfDay(BUSINESS_ZONE).toInstant();
    }

    public Instant endOfDay(LocalDate date) {
        return startOfDay(date.plusDays(1));
    }

    public boolean isCurrentBusinessDate(Instant instant) {
        return businessDate(instant).equals(businessDate(now()));
    }

    private LocalDate businessDate(Instant instant) {
        return instant.atZone(BUSINESS_ZONE).toLocalDate();
    }
}
