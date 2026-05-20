package com.hera.atendeja.mapper;

import com.hera.atendeja.dto.appointment.AppointmentResponse;
import com.hera.atendeja.entity.Appointment;
import com.hera.atendeja.entity.AppointmentStatus;
import com.hera.atendeja.entity.Customer;
import com.hera.atendeja.entity.Professional;
import com.hera.atendeja.entity.ServiceCatalog;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class AppointmentMapper {

    public Appointment toEntity(
            Customer customer,
            Professional professional,
            ServiceCatalog service,
            Instant startAt,
            Instant endAt
    ) {
        Appointment appointment = new Appointment();
        appointment.setCustomer(customer);
        appointment.setProfessional(professional);
        appointment.setService(service);
        appointment.setStartAt(startAt);
        appointment.setEndAt(endAt);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        return appointment;
    }

    public AppointmentResponse toResponse(Appointment appointment) {
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getCustomer().getId(),
                appointment.getCustomer().getName(),
                appointment.getProfessional().getId(),
                appointment.getProfessional().getName(),
                appointment.getService().getId(),
                appointment.getService().getName(),
                appointment.getStartAt(),
                appointment.getEndAt(),
                appointment.getStatus(),
                appointment.getCancelReason(),
                appointment.getNoShowReason(),
                appointment.getCheckedInAt(),
                appointment.getCreatedAt(),
                appointment.getUpdatedAt()
        );
    }
}
