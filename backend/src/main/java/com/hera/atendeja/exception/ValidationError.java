package com.hera.atendeja.exception;

public record ValidationError(
        String field,
        String message
) {
}
