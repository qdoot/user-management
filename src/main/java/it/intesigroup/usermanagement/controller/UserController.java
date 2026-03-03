package it.intesigroup.usermanagement.controller;

import it.intesigroup.usermanagement.dto.request.CreateUserRequest;
import it.intesigroup.usermanagement.dto.request.UpdateUserRequest;
import it.intesigroup.usermanagement.dto.response.UserResponse;
import it.intesigroup.usermanagement.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller REST per la gestione degli utenti.
 *
 * <p>Responsabilità di questo layer:
 * <ul>
 *   <li>Ricezione e deserializzazione delle request HTTP</li>
 *   <li>Validazione sintattica degli input tramite {@code @Valid}</li>
 *   <li>Autorizzazione a livello endpoint tramite {@code @PreAuthorize}</li>
 *   <li>Delega della logica al {@link UserService}</li>
 * </ul>
 *
 * <p>Il controller non contiene logica di business.
 */
@Slf4j
@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "API per la gestione degli utenti")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER','OPERATOR','MAINTAINER','DEVELOPER','REPORTER')")
    @Operation(summary = "Lista utenti", description = "Restituisce la lista paginata degli utenti attivi")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista recuperata con successo"),
            @ApiResponse(responseCode = "401", description = "Non autenticato"),
            @ApiResponse(responseCode = "403", description = "Accesso negato")
    })
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @PageableDefault(size = 20, sort = "username") Pageable pageable) {

        log.debug("GET /v1/users — pagina={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());
        return ResponseEntity.ok(userService.findAllUsers(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER','OPERATOR','MAINTAINER','DEVELOPER','REPORTER')")
    @Operation(summary = "Dettaglio utente", description = "Restituisce i dati di un singolo utente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Utente trovato"),
            @ApiResponse(responseCode = "404", description = "Utente non trovato"),
            @ApiResponse(responseCode = "401", description = "Non autenticato")
    })
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "ID dell'utente") @PathVariable UUID id) {

        log.debug("GET /v1/users/{}", id);
        return ResponseEntity.ok(userService.findUserById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER','OPERATOR')")
    @Operation(summary = "Crea utente", description = "Crea un nuovo utente con i ruoli specificati")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Utente creato con successo"),
            @ApiResponse(responseCode = "400", description = "Dati non validi"),
            @ApiResponse(responseCode = "409", description = "Email, username o codice fiscale già esistenti"),
            @ApiResponse(responseCode = "403", description = "Accesso negato")
    })
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request) {

        log.info("POST /v1/users — username={}", request.getUsername());
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER','OPERATOR','MAINTAINER')")
    @Operation(summary = "Aggiorna utente", description = "Modifica i dati e/o i ruoli di un utente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Utente aggiornato"),
            @ApiResponse(responseCode = "400", description = "Dati non validi"),
            @ApiResponse(responseCode = "404", description = "Utente non trovato"),
            @ApiResponse(responseCode = "409", description = "Username o codice fiscale già esistenti"),
            @ApiResponse(responseCode = "403", description = "Accesso negato")
    })
    public ResponseEntity<UserResponse> updateUser(
            @Parameter(description = "ID dell'utente") @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request) {

        log.info("PUT /v1/users/{}", id);
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @PatchMapping("/{id}/disable")
    @PreAuthorize("hasAnyRole('OWNER','OPERATOR')")
    @Operation(summary = "Disabilita utente", description = "Imposta lo stato dell'utente a DISABLED")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Utente disabilitato"),
            @ApiResponse(responseCode = "404", description = "Utente non trovato"),
            @ApiResponse(responseCode = "422", description = "Transizione di stato non consentita"),
            @ApiResponse(responseCode = "403", description = "Accesso negato")
    })
    public ResponseEntity<Void> disableUser(
            @Parameter(description = "ID dell'utente") @PathVariable UUID id) {

        log.info("PATCH /v1/users/{}/disable", id);
        userService.disableUser(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Elimina utente", description = "Esegue la cancellazione logica dell'utente (soft delete)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Utente eliminato"),
            @ApiResponse(responseCode = "404", description = "Utente non trovato"),
            @ApiResponse(responseCode = "422", description = "Transizione di stato non consentita"),
            @ApiResponse(responseCode = "403", description = "Accesso negato")
    })
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID dell'utente") @PathVariable UUID id) {

        log.info("DELETE /v1/users/{}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
