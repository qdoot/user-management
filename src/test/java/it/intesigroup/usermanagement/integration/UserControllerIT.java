package it.intesigroup.usermanagement.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.intesigroup.usermanagement.domain.RoleName;
import it.intesigroup.usermanagement.dto.request.CreateUserRequest;
import it.intesigroup.usermanagement.dto.request.UpdateUserRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test di integrazione per {@link it.intesigroup.usermanagement.controller.UserController}.
 *
 * <p>Usa Testcontainers per avviare container Docker reali di PostgreSQL e RabbitMQ.
 * I test girano contro un database reale — questo garantisce che le query JPA,
 * i vincoli e le migration Flyway funzionino esattamente come in produzione.
 *
 * <p>L'autenticazione è simulata tramite {@code @WithMockUser} — evita
 * la dipendenza da Keycloak nei test di integrazione.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class UserControllerIT {

    /**
     * Container PostgreSQL condiviso tra tutti i test della classe.
     * Campo statico = un solo container per tutta la suite, non uno per ogni test.
     */
    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("usermanagement_test")
                    .withUsername("test")
                    .withPassword("test");

    /** Container RabbitMQ — necessario perché Spring tenta la connessione al boot. */
    @Container
    static RabbitMQContainer rabbitmq =
            new RabbitMQContainer("rabbitmq:3.13-alpine");

    /**
     * Sovrascrive le proprietà Spring con i valori dinamici dei container.
     * Testcontainers assegna porte casuali per evitare conflitti.
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.rabbitmq.host", rabbitmq::getHost);
        registry.add("spring.rabbitmq.port", rabbitmq::getAmqpPort);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ─────────────────────────────────────────────
    // POST /v1/users
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("POST /v1/users — OWNER — crea utente e restituisce 201")
    @WithMockUser(roles = "OWNER")
    void createUser_asOwner_returns201() throws Exception {
        CreateUserRequest request = CreateUserRequest.builder()
                .username("mrossi")
                .email("mario.rossi@example.com")
                .codiceFiscale("RSSMRA80A01H501U")
                .firstName("Mario")
                .lastName("Rossi")
                .roles(Set.of(RoleName.DEVELOPER))
                .build();

        mockMvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.username").value("mrossi"))
                .andExpect(jsonPath("$.email").value("mario.rossi@example.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("POST /v1/users — REPORTER — restituisce 403")
    @WithMockUser(roles = "REPORTER")
    void createUser_asReporter_returns403() throws Exception {
        CreateUserRequest request = CreateUserRequest.builder()
                .username("test")
                .email("test@example.com")
                .codiceFiscale("RSSMRA80A01H501U")
                .firstName("Test").lastName("User")
                .roles(Set.of(RoleName.REPORTER))
                .build();

        mockMvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /v1/users — email non valida — restituisce 400")
    @WithMockUser(roles = "OWNER")
    void createUser_withInvalidEmail_returns400() throws Exception {
        CreateUserRequest request = CreateUserRequest.builder()
                .username("mrossi2")
                .email("non-una-email")
                .codiceFiscale("RSSMRA80A01H501U")
                .firstName("Mario").lastName("Rossi")
                .roles(Set.of(RoleName.DEVELOPER))
                .build();

        mockMvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.email").exists());
    }

    @Test
    @DisplayName("POST /v1/users — email duplicata — restituisce 409")
    @WithMockUser(roles = "OWNER")
    void createUser_withDuplicateEmail_returns409() throws Exception {
        CreateUserRequest first = CreateUserRequest.builder()
                .username("utente1")
                .email("duplicato@example.com")
                .codiceFiscale("BNCMRA80A01H501A")
                .firstName("Mario").lastName("Rossi")
                .roles(Set.of(RoleName.DEVELOPER))
                .build();

        CreateUserRequest second = CreateUserRequest.builder()
                .username("utente2")
                .email("duplicato@example.com")
                .codiceFiscale("VRDGNN85M01F205I")
                .firstName("Giovanni").lastName("Verdi")
                .roles(Set.of(RoleName.DEVELOPER))
                .build();

        mockMvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(first)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(second)))
                .andExpect(status().isConflict());
    }

    // ─────────────────────────────────────────────
    // GET /v1/users
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("GET /v1/users — autenticato — restituisce 200 con pagina")
    @WithMockUser(roles = "DEVELOPER")
    void getAllUsers_authenticated_returns200() throws Exception {
        mockMvc.perform(get("/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.pageable").exists());
    }

    @Test
    @DisplayName("GET /v1/users — non autenticato — restituisce 401")
    void getAllUsers_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/v1/users"))
                .andExpect(status().isUnauthorized());
    }

    // ─────────────────────────────────────────────
    // GET /v1/users/{id}
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("GET /v1/users/{id} — id inesistente — restituisce 404")
    @WithMockUser(roles = "OPERATOR")
    void getUserById_withUnknownId_returns404() throws Exception {
        mockMvc.perform(get("/v1/users/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    // ─────────────────────────────────────────────
    // PATCH + DELETE — cicli completi
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("PATCH /v1/users/{id}/disable — ciclo creazione e disabilitazione")
    @WithMockUser(roles = "OWNER")
    void disableUser_afterCreation_returns204() throws Exception {
        CreateUserRequest request = CreateUserRequest.builder()
                .username("daDisabilitare")
                .email("disable@example.com")
                .codiceFiscale("TRMCRL81D01L218D")
                .firstName("Alberto").lastName("Bianchi")
                .roles(Set.of(RoleName.REPORTER))
                .build();

        MvcResult result = mockMvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        UUID userId = UUID.fromString(
                objectMapper.readTree(result.getResponse().getContentAsString())
                        .get("id").asText());

        mockMvc.perform(patch("/v1/users/{id}/disable", userId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /v1/users/{id} — OWNER — elimina logicamente e verifica invisibilità")
    @WithMockUser(roles = "OWNER")
    void deleteUser_asOwner_returns204AndUserBecomesInvisible() throws Exception {
        CreateUserRequest request = CreateUserRequest.builder()
                .username("daEliminare")
                .email("delete@example.com")
                .codiceFiscale("SLMCRL77P14L218F")
                .firstName("Aria").lastName("Mari")
                .roles(Set.of(RoleName.REPORTER))
                .build();

        MvcResult result = mockMvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        UUID userId = UUID.fromString(
                objectMapper.readTree(result.getResponse().getContentAsString())
                        .get("id").asText());

        mockMvc.perform(delete("/v1/users/{id}", userId))
                .andExpect(status().isNoContent());

        // Verifica che l'utente eliminato non sia più visibile tramite GET
        mockMvc.perform(get("/v1/users/{id}", userId))
                .andExpect(status().isNotFound());
    }
}
