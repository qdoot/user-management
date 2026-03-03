package it.intesigroup.usermanagement.exception;

import java.util.UUID;

/**
 * Eccezione lanciata quando un utente non viene trovato nel sistema.
 * Produce una risposta HTTP 404.
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(UUID id) {
        super("Utente non trovato con id: " + id);
    }

    public UserNotFoundException(String message) {
        super(message);
    }
}
