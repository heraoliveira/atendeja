package com.hera.atendeja.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resourceName, Long id) {
        super("%s não encontrado com id %d.".formatted(resourceName, id));
    }
}
