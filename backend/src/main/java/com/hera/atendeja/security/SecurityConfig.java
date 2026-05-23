package com.hera.atendeja.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableConfigurationProperties({JwtProperties.class, DemoUsersProperties.class})
public class SecurityConfig {

    private static final String[] PUBLIC_PATHS = {
            "/actuator/health",
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/api/v1/auth/login"
    };

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter jwtAuthenticationConverter,
            ProblemAuthenticationEntryPoint authenticationEntryPoint,
            ProblemAccessDeniedHandler accessDeniedHandler
    ) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/customers/**").hasAnyRole("ADMIN", "ATTENDANT")
                        .requestMatchers(HttpMethod.POST, "/api/v1/customers/**").hasAnyRole("ADMIN", "ATTENDANT")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/customers/**").hasAnyRole("ADMIN", "ATTENDANT")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/customers/**").hasAnyRole("ADMIN", "ATTENDANT")
                        .requestMatchers(HttpMethod.GET, "/api/v1/professionals/**").hasAnyRole("ADMIN", "ATTENDANT")
                        .requestMatchers(HttpMethod.POST, "/api/v1/professionals/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/professionals/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/professionals/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/services/**").hasAnyRole("ADMIN", "ATTENDANT")
                        .requestMatchers(HttpMethod.POST, "/api/v1/services/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/services/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/services/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/appointments/**").hasAnyRole("ADMIN", "ATTENDANT")
                        .requestMatchers(HttpMethod.GET, "/api/v1/dashboard/**").hasAnyRole("ADMIN", "ATTENDANT")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable);
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(
            @Value("${CORS_ALLOWED_ORIGINS:http://localhost:5173}") String allowedOrigins
    ) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(parseAllowedOrigins(allowedOrigins));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    private List<String> parseAllowedOrigins(String allowedOrigins) {
        return Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .toList();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecretKey jwtSecretKey(JwtProperties jwtProperties) {
        return new SecretKeySpec(jwtProperties.secret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecretKey));
    }

    @Bean
    JwtDecoder jwtDecoder(SecretKey jwtSecretKey) {
        return NimbusJwtDecoder.withSecretKey(jwtSecretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthoritiesClaimName("roles");
        authoritiesConverter.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
        authenticationConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return authenticationConverter;
    }
}
