package it.intesigroup.usermanagement.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Handler centralizzato per tutte le eccezioni dell'applicazione.
 *
 * <p>{@code @RestControllerAdvice} intercetta le eccezioni lanciate
 * da qualsiasi controller e le trasforma in risposte HTTP strutturate,
 * garantendo un formato di errore coerente su tutta l'API.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Gestisce la validazione fallita dei campi del request body.
     * HTTP 400 Bad Request.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        log.warn("Validazione fallita per request {}: {}",
                request.getDescription(false), fieldErrors);

        return buildResponse(HttpStatus.BAD_REQUEST, "Validazione fallita", request, fieldErrors);
    }

    /** Gestisce utente non trovato. HTTP 404. */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFoundException(
            UserNotFoundException ex, WebRequest request) {

        log.warn("Utente non trovato: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
    }

    /** Gestisce conflitti su campi univoci. HTTP 409. */
    @ExceptionHandler(DuplicateFieldException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateFieldException(
            DuplicateFieldException ex, WebRequest request) {

        log.warn("Conflitto su campo '{}': {}", ex.getField(), ex.getMessage());
        Map<String, String> details = Map.of(ex.getField(), ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, "Conflitto sui dati", request, details);
    }

    /** Gestisce transizioni di stato non consentite. HTTP 422. */
    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidStatusTransition(
            InvalidStatusTransitionException ex, WebRequest request) {

        log.warn("Transizione stato non valida: {}", ex.getMessage());
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), request, null);
    }

    /** Gestisce accesso negato per ruolo insufficiente. HTTP 403. */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {

        log.warn("Accesso negato a {}: {}", request.getDescription(false), ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN,
                "Accesso negato: permessi insufficienti", request, null);
    }

    /** Fallback per tutte le eccezioni non gestite. HTTP 500. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception ex, WebRequest request) {

        log.error("Errore non gestito per request {}: ", request.getDescription(false), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "Errore interno del server", request, null);
    }

    private ResponseEntity<Map<String, Object>> buildResponse(
            HttpStatus status, String message, WebRequest request, Object details) {

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", message);
        body.put("path", request.getDescription(false).replace("uri=", ""));

        if (details != null) {
            body.put("details", details);
        }

        return ResponseEntity.status(status).body(body);
    }
}
