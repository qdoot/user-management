package it.intesigroup.usermanagement.domain;

/**
 * Enum che rappresenta lo stato del ciclo di vita di un utente.
 *
 * <p>La gestione dello stato avviene esclusivamente tramite transizioni
 * esplicite nel service layer — non è possibile impostare uno stato
 * arbitrario dall'esterno tramite API.
 *
 * <p>Transizioni consentite:
 * <ul>
 *   <li>ACTIVE → DISABLED (disabilitazione reversibile)</li>
 *   <li>DISABLED → ACTIVE (riattivazione)</li>
 *   <li>ACTIVE | DISABLED → DELETED (soft delete irreversibile)</li>
 * </ul>
 */
public enum UserStatus {

    /** Utente attivo, può autenticarsi e operare nel sistema. */
    ACTIVE,

    /** Utente disabilitato temporaneamente, non può autenticarsi. */
    DISABLED,

    /**
     * Utente eliminato logicamente (soft delete).
     * Il record rimane nel DB per motivi di audit ma è invisibile
     * alle normali query e non può essere riattivato.
     */
    DELETED
}
