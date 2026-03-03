package it.intesigroup.usermanagement.event;

import it.intesigroup.usermanagement.domain.RoleName;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Evento pubblicato su RabbitMQ alla creazione di un nuovo utente.
 *
 * <p>Scopo: notificare i sistemi downstream (es. servizio di notifiche,
 * audit log, provisioning risorse) in modo asincrono e disaccoppiato.
 *
 * <p>Il payload è volutamente minimale: contiene solo i dati necessari
 * ai consumer più comuni. Campi sensibili come {@code codiceFiscale}
 * sono esclusi per design.
 */
@Data
@Builder
public class UserCreatedEvent {

    /** Identificatore univoco dell'evento — per idempotenza nei consumer. */
    private UUID eventId;

    /** Tipo evento — utile per routing e filtering nei consumer. */
    private String eventType;

    /** Timestamp di pubblicazione dell'evento. */
    private LocalDateTime timestamp;

    private UUID userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;

    /** Ruoli assegnati all'utente al momento della creazione. */
    private Set<RoleName> roles;
}
