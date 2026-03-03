package it.intesigroup.usermanagement.dto.request;

import it.intesigroup.usermanagement.domain.RoleName;
import it.intesigroup.usermanagement.validation.ValidCodiceFiscale;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

/**
 * DTO per la richiesta di creazione di un nuovo utente.
 *
 * <p>Le annotazioni di validazione Jakarta vengono valutate
 * dal controller prima che la request raggiunga il service layer.
 * Eventuali violazioni producono una risposta 400 con dettaglio
 * dei campi non validi tramite il GlobalExceptionHandler.
 */
@Data
@Builder
public class CreateUserRequest {

    @NotBlank(message = "Lo username è obbligatorio")
    @Size(min = 3, max = 50, message = "Lo username deve essere tra 3 e 50 caratteri")
    private String username;

    @NotBlank(message = "L'email è obbligatoria")
    @Email(message = "Formato email non valido")
    @Size(max = 255)
    private String email;

    /**
     * Validato tramite il validatore custom {@link ValidCodiceFiscale}
     * che implementa la formula ministeriale italiana.
     */
    @NotBlank(message = "Il codice fiscale è obbligatorio")
    @ValidCodiceFiscale
    private String codiceFiscale;

    @NotBlank(message = "Il nome è obbligatorio")
    @Size(max = 100)
    private String firstName;

    @NotBlank(message = "Il cognome è obbligatorio")
    @Size(max = 100)
    private String lastName;

    @NotEmpty(message = "Almeno un ruolo deve essere specificato")
    private Set<RoleName> roles;
}
