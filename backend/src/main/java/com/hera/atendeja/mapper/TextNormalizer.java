package com.hera.atendeja.mapper;

final class TextNormalizer {

    private TextNormalizer() {
    }

    static String required(String value) {
        return value.trim();
    }

    static String optional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
