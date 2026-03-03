package it.intesigroup.usermanagement.exception;

/**
 * Eccezione lanciata quando si tenta di creare o modificare un utente
 * con un valore già presente su un campo univoco (email, username, codice fiscale).
 * Produce una risposta HTTP 409 Conflict.
 */
public class DuplicateFieldException extends RuntimeException {

    /** Nome del campo che ha causato il conflitto (es. "email"). */
    private final String field;

    public DuplicateFieldException(String field, String value) {
        super("Valore già esistente per il campo '" + field + "': " + value);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
