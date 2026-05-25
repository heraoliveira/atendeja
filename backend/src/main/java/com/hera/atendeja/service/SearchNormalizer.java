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

    static String toDigitsLikePattern(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String digits = value.replaceAll("\\D", "");
        if (digits.isBlank()) {
            return null;
        }
        return "%" + digits + "%";
    }
}
