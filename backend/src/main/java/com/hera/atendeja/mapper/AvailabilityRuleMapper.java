package com.hera.atendeja.mapper;

import com.hera.atendeja.dto.calendar.AvailabilityRuleCreateRequest;
import com.hera.atendeja.dto.calendar.AvailabilityRuleResponse;
import com.hera.atendeja.dto.calendar.AvailabilityRuleUpdateRequest;
import com.hera.atendeja.entity.AvailabilityRule;
import com.hera.atendeja.entity.Professional;
import org.springframework.stereotype.Component;

@Component
public class AvailabilityRuleMapper {

    public AvailabilityRule toEntity(Professional professional, AvailabilityRuleCreateRequest request) {
        AvailabilityRule rule = new AvailabilityRule();
        rule.setProfessional(professional);
        rule.setDayOfWeek(request.dayOfWeek());
        rule.setStartTime(request.startTime());
        rule.setEndTime(request.endTime());
        rule.setActive(true);
        return rule;
    }

    public void updateEntity(AvailabilityRule rule, AvailabilityRuleUpdateRequest request) {
        rule.setDayOfWeek(request.dayOfWeek());
        rule.setStartTime(request.startTime());
        rule.setEndTime(request.endTime());
        rule.setActive(request.active());
    }

    public AvailabilityRuleResponse toResponse(AvailabilityRule rule) {
        return new AvailabilityRuleResponse(
                rule.getId(),
                rule.getProfessional().getId(),
                rule.getDayOfWeek(),
                rule.getStartTime(),
                rule.getEndTime(),
                rule.isActive(),
                rule.getCreatedAt(),
                rule.getUpdatedAt()
        );
    }
}
