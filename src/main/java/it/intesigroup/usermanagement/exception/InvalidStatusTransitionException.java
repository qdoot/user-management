package it.intesigroup.usermanagement.exception;

import it.intesigroup.usermanagement.domain.UserStatus;

/**
 * Eccezione lanciata quando si tenta una transizione di stato
 * non consentita sul ciclo di vita di un utente.
 * Produce una risposta HTTP 422 Unprocessable Entity.
 */
public class InvalidStatusTransitionException extends RuntimeException {

    public InvalidStatusTransitionException(UserStatus from, UserStatus to) {
        super("Transizione di stato non consentita: " + from + " → " + to);
    }
}
