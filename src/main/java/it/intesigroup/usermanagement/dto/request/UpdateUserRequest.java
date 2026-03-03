package it.intesigroup.usermanagement.dto.request;

import it.intesigroup.usermanagement.domain.RoleName;
import it.intesigroup.usermanagement.validation.ValidCodiceFiscale;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

/**
 * DTO per la richiesta di modifica di un utente esistente.
 *
 * <p>Tutti i campi sono opzionali — il service applica solo
 * le modifiche per i campi non null ricevuti nella request (partial update).
 *
 * <p>Nota: l'email non è presente perché non modificabile
 * dopo la creazione — vincolo dichiarato nell'entità tramite
 * {@code updatable = false}.
 */
@Data
@Builder
public class UpdateUserRequest {

    @Size(min = 3, max = 50, message = "Lo username deve essere tra 3 e 50 caratteri")
    private String username;

    @ValidCodiceFiscale
    private String codiceFiscale;

    @Size(min = 1, max = 100, message = "Il nome non può essere vuoto se specificato")
    private String firstName;

    @Size(min = 1, max = 100, message = "Il cognome non può essere vuoto se specificato")
    private String lastName;

    /**
     * Se presente, sostituisce integralmente l'insieme dei ruoli
     * correnti dell'utente. Se null, i ruoli non vengono modificati.
     */
    @Size(min = 1, message = "Se specificati, i ruoli non possono essere un insieme vuoto")
    private Set<RoleName> roles;
}
