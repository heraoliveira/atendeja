package com.hera.atendeja.exception;

public class AppointmentConflictException extends RuntimeException {

    public AppointmentConflictException() {
        super("Já existe um agendamento para este profissional neste horário.");
    }
}
