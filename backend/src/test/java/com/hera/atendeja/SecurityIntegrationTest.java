package com.hera.atendeja;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hera.atendeja.entity.UserAccount;
import com.hera.atendeja.entity.UserRole;
import com.hera.atendeja.repository.AppointmentRepository;
import com.hera.atendeja.repository.AvailabilityRuleRepository;
import com.hera.atendeja.repository.CustomerRepository;
import com.hera.atendeja.repository.ProfessionalRepository;
import com.hera.atendeja.repository.ScheduleExceptionRepository;
import com.hera.atendeja.repository.ServiceCatalogRepository;
import com.hera.atendeja.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class SecurityIntegrationTest {

    @Container
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private AvailabilityRuleRepository availabilityRuleRepository;

    @Autowired
    private ScheduleExceptionRepository scheduleExceptionRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProfessionalRepository professionalRepository;

    @Autowired
    private ServiceCatalogRepository serviceCatalogRepository;

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @BeforeEach
    void cleanDatabase() {
        appointmentRepository.deleteAll();
        scheduleExceptionRepository.deleteAll();
        availabilityRuleRepository.deleteAll();
        serviceCatalogRepository.deleteAll();
        professionalRepository.deleteAll();
        customerRepository.deleteAll();
        userAccountRepository.deleteAll();
    }

    @Test
    void shouldLoginWithValidCredentials() throws Exception {
        createUser("admin@atendeja.local", "admin-pass", UserRole.ADMIN, true);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload("admin@atendeja.local", "admin-pass")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresAt").isNotEmpty())
                .andExpect(jsonPath("$.email").value("admin@atendeja.local"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void shouldRejectInvalidCredentials() throws Exception {
        createUser("admin@atendeja.local", "admin-pass", UserRole.ADMIN, true);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload("admin@atendeja.local", "wrong-pass")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    void shouldRejectInactiveUserCredentials() throws Exception {
        createUser("inactive@atendeja.local", "inactive-pass", UserRole.ATTENDANT, false);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload("inactive@atendeja.local", "inactive-pass")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    void shouldRejectBusinessEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/services"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void shouldRejectDashboardWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/daily")
                        .param("date", "2030-01-21"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void shouldRejectBusinessEndpointWithInvalidBearerToken() throws Exception {
        mockMvc.perform(get("/api/v1/services")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void shouldAllowCorsPreflightFromConfiguredFrontendOrigin() throws Exception {
        mockMvc.perform(options("/api/v1/dashboard/daily")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    @Test
    void shouldAllowBusinessEndpointWithValidToken() throws Exception {
        createUser("attendant@atendeja.local", "attendant-pass", UserRole.ATTENDANT, true);
        String token = login("attendant@atendeja.local", "attendant-pass");

        mockMvc.perform(get("/api/v1/services")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowAttendantToReadDashboard() throws Exception {
        createUser("attendant@atendeja.local", "attendant-pass", UserRole.ATTENDANT, true);
        String token = login("attendant@atendeja.local", "attendant-pass");

        mockMvc.perform(get("/api/v1/dashboard/daily")
                        .header("Authorization", "Bearer " + token)
                        .param("date", "2030-01-21"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2030-01-21"));
    }

    @Test
    void shouldAllowAdminToCreateService() throws Exception {
        createUser("admin@atendeja.local", "admin-pass", UserRole.ADMIN, true);
        String token = login("admin@atendeja.local", "admin-pass");

        mockMvc.perform(post("/api/v1/services")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(servicePayload("Consulta de segurança")))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldReturnForbiddenWhenAttendantTriesToManageServices() throws Exception {
        createUser("attendant@atendeja.local", "attendant-pass", UserRole.ATTENDANT, true);
        String token = login("attendant@atendeja.local", "attendant-pass");

        mockMvc.perform(post("/api/v1/services")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(servicePayload("Serviço restrito")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    void shouldReturnForbiddenWhenAttendantTriesToManageAvailability() throws Exception {
        createUser("attendant@atendeja.local", "attendant-pass", UserRole.ATTENDANT, true);
        String token = login("attendant@atendeja.local", "attendant-pass");

        mockMvc.perform(post("/api/v1/professionals/1/availability-rules")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "dayOfWeek": "MONDAY",
                                  "startTime": "08:00:00",
                                  "endTime": "12:00:00"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    private String login(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload(email, password)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
    }

    private String loginPayload(String email, String password) {
        return """
                {
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(email, password);
    }

    private String servicePayload(String name) {
        return """
                {
                  "name": "%s",
                  "description": "Serviço protegido por role.",
                  "durationMinutes": 45,
                  "bufferMinutes": 15,
                  "price": 150.00
                }
                """.formatted(name);
    }

    private void createUser(String email, String password, UserRole role, boolean active) {
        UserAccount userAccount = new UserAccount();
        userAccount.setName("Usuário " + role.name());
        userAccount.setEmail(email);
        userAccount.setPasswordHash(passwordEncoder.encode(password));
        userAccount.setRole(role);
        userAccount.setActive(active);
        userAccountRepository.save(userAccount);
    }
}
