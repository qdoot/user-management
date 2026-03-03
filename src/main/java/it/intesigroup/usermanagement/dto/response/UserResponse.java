package it.intesigroup.usermanagement.dto.response;

import it.intesigroup.usermanagement.domain.RoleName;
import it.intesigroup.usermanagement.domain.UserStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * DTO di risposta per un utente.
 *
 * <p>Questo oggetto è ciò che viene serializzato e restituito
 * al client — mai l'entità JPA direttamente. Questo disaccoppia
 * il modello di dominio dal contratto API.
 *
 * <p>I campi sensibili ({@code codiceFiscale}, {@code keycloakId})
 * vengono mascherati dal mapper in base al ruolo del chiamante:
 * visibili solo a OWNER e OPERATOR, sostituiti con {@code "***"}
 * per tutti gli altri ruoli.
 */
@Data
@Builder
public class UserResponse {

    private UUID id;
    private String username;
    private String email;

    /** Può contenere il valore reale o {@code "***"} in base al ruolo del chiamante. */
    private String codiceFiscale;

    private String firstName;
    private String lastName;
    private UserStatus status;

    /** Può contenere il valore reale o {@code "***"} in base al ruolo del chiamante. */
    private String keycloakId;

    private Set<RoleName> roles;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
