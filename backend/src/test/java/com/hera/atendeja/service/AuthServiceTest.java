package com.hera.atendeja.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hera.atendeja.dto.auth.LoginRequest;
import com.hera.atendeja.entity.UserAccount;
import com.hera.atendeja.entity.UserRole;
import com.hera.atendeja.exception.InvalidCredentialsException;
import com.hera.atendeja.repository.UserAccountRepository;
import com.hera.atendeja.security.IssuedToken;
import com.hera.atendeja.security.JwtTokenService;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenService jwtTokenService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userAccountRepository, passwordEncoder, jwtTokenService);
    }

    @Test
    void shouldLoginActiveUserWithNormalizedEmailAndIssuedJwt() {
        UserAccount user = user("admin@atendeja.local", UserRole.ADMIN, true);
        Instant expiresAt = Instant.parse("2030-01-20T18:00:00Z");
        when(userAccountRepository.findByEmail("admin@atendeja.local")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret-pass", user.getPasswordHash())).thenReturn(true);
        when(jwtTokenService.issue(user)).thenReturn(new IssuedToken("jwt-token", expiresAt));

        var response = authService.login(new LoginRequest("  ADMIN@AtendeJa.Local  ", "secret-pass"));

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresAt()).isEqualTo(expiresAt);
        assertThat(response.email()).isEqualTo("admin@atendeja.local");
        assertThat(response.role()).isEqualTo(UserRole.ADMIN);
        verify(userAccountRepository).findByEmail("admin@atendeja.local");
        verify(passwordEncoder).matches("secret-pass", user.getPasswordHash());
        verify(jwtTokenService).issue(user);
    }

    @Test
    void shouldRejectUnknownUserCredentials() {
        when(userAccountRepository.findByEmail("missing@atendeja.local")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("missing@atendeja.local", "secret-pass")))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(passwordEncoder, never()).matches(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
        verify(jwtTokenService, never()).issue(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldRejectInactiveUserCredentials() {
        UserAccount inactiveUser = user("attendant@atendeja.local", UserRole.ATTENDANT, false);
        when(userAccountRepository.findByEmail("attendant@atendeja.local")).thenReturn(Optional.of(inactiveUser));

        assertThatThrownBy(() -> authService.login(new LoginRequest("attendant@atendeja.local", "secret-pass")))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(passwordEncoder, never()).matches(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
        verify(jwtTokenService, never()).issue(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldRejectInvalidPassword() {
        UserAccount user = user("admin@atendeja.local", UserRole.ADMIN, true);
        when(userAccountRepository.findByEmail("admin@atendeja.local")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-pass", user.getPasswordHash())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("admin@atendeja.local", "wrong-pass")))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(jwtTokenService, never()).issue(org.mockito.ArgumentMatchers.any());
    }

    private UserAccount user(String email, UserRole role, boolean active) {
        UserAccount userAccount = new UserAccount();
        ReflectionTestUtils.setField(userAccount, "id", 1L);
        userAccount.setName("User " + role.name());
        userAccount.setEmail(email);
        userAccount.setPasswordHash("$2a$10$encoded-password-hash");
        userAccount.setRole(role);
        userAccount.setActive(active);
        return userAccount;
    }
}
