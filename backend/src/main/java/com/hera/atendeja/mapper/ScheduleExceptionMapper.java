package com.hera.atendeja.mapper;

import com.hera.atendeja.dto.calendar.ScheduleExceptionRequest;
import com.hera.atendeja.dto.calendar.ScheduleExceptionResponse;
import com.hera.atendeja.entity.Professional;
import com.hera.atendeja.entity.ScheduleException;
import org.springframework.stereotype.Component;

@Component
public class ScheduleExceptionMapper {

    public ScheduleException toEntity(Professional professional, ScheduleExceptionRequest request) {
        ScheduleException exception = new ScheduleException();
        exception.setProfessional(professional);
        updateEntity(exception, request);
        return exception;
    }

    public void updateEntity(ScheduleException exception, ScheduleExceptionRequest request) {
        exception.setDate(request.date());
        exception.setStartTime(request.startTime());
        exception.setEndTime(request.endTime());
        exception.setType(request.type());
        exception.setReason(TextNormalizer.optional(request.reason()));
    }

    public ScheduleExceptionResponse toResponse(ScheduleException exception) {
        return new ScheduleExceptionResponse(
                exception.getId(),
                exception.getProfessional().getId(),
                exception.getDate(),
                exception.getStartTime(),
                exception.getEndTime(),
                exception.getType(),
                exception.getReason(),
                exception.getCreatedAt(),
                exception.getUpdatedAt()
        );
    }
}
