package com.hera.atendeja.security;

import com.hera.atendeja.entity.UserAccount;
import com.hera.atendeja.entity.UserRole;
import com.hera.atendeja.repository.UserAccountRepository;
import java.util.Locale;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Component
@ConditionalOnProperty(prefix = "atendeja.auth.demo-users", name = "enabled", havingValue = "true")
public class DemoUserBootstrap implements ApplicationRunner {

    private final DemoUsersProperties properties;
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoUserBootstrap(
            DemoUsersProperties properties,
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.properties = properties;
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        createIfMissing("Administrador local", properties.adminEmail(), properties.adminPassword(), UserRole.ADMIN);
        createIfMissing("Atendente local", properties.attendantEmail(), properties.attendantPassword(), UserRole.ATTENDANT);
    }

    private void createIfMissing(String name, String email, String password, UserRole role) {
        if (!StringUtils.hasText(email) || !StringUtils.hasText(password)) {
            throw new IllegalStateException("Configure e-mail e senha dos usuários de demonstração antes de habilitá-los.");
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        if (userAccountRepository.findByEmail(normalizedEmail).isPresent()) {
            return;
        }

        UserAccount userAccount = new UserAccount();
        userAccount.setName(name);
        userAccount.setEmail(normalizedEmail);
        userAccount.setPasswordHash(passwordEncoder.encode(password));
        userAccount.setRole(role);
        userAccountRepository.save(userAccount);
    }
}
