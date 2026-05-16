package com.hera.atendeja.service;

final class SearchNormalizer {

    private SearchNormalizer() {
    }

    static String toLikePattern(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return "%" + value.trim().toLowerCase() + "%";
    }
}
