package com.hera.atendeja.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "atendeja.auth.demo-users")
public record DemoUsersProperties(
        boolean enabled,
        String adminEmail,
        String adminPassword,
        String attendantEmail,
        String attendantPassword
) {
}
