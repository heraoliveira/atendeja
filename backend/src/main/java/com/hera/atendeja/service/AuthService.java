package com.hera.atendeja.service;

import com.hera.atendeja.dto.auth.LoginRequest;
import com.hera.atendeja.dto.auth.LoginResponse;
import com.hera.atendeja.entity.UserAccount;
import com.hera.atendeja.exception.InvalidCredentialsException;
import com.hera.atendeja.repository.UserAccountRepository;
import com.hera.atendeja.security.IssuedToken;
import com.hera.atendeja.security.JwtTokenService;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public AuthService(
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService
    ) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        UserAccount userAccount = userAccountRepository.findByEmail(normalizeEmail(request.email()))
                .filter(UserAccount::isActive)
                .filter(user -> passwordEncoder.matches(request.password(), user.getPasswordHash()))
                .orElseThrow(InvalidCredentialsException::new);
        IssuedToken token = jwtTokenService.issue(userAccount);
        return new LoginResponse(
                token.value(),
                "Bearer",
                token.expiresAt(),
                userAccount.getEmail(),
                userAccount.getRole()
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
