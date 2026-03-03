package it.intesigroup.usermanagement.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Entità JPA che rappresenta un utente del sistema.
 *
 * <p>Corrisponde alla tabella {@code users}. La cancellazione è sempre
 * logica (soft delete) tramite il campo {@code status = DELETED}.
 *
 * <p>{@code @SQLRestriction} garantisce che Hibernate escluda
 * automaticamente gli utenti eliminati da tutte le query, senza
 * dover aggiungere il filtro manualmente in ogni repository.
 */
@Entity
@Table(name = "users")
@SQLRestriction("status <> 'DELETED'")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    /**
     * Email univoca e non modificabile dopo la creazione.
     * Il vincolo {@code updatable = false} è enforced a livello ORM.
     * Il vincolo di unicità è enforced a livello DB tramite migration.
     */
    @Column(name = "email", nullable = false, unique = true,
            length = 255, updatable = false)
    private String email;

    /**
     * Codice fiscale italiano — univoco, validato dal layer applicativo
     * tramite {@code @ValidCodiceFiscale} sui DTO di input.
     */
    @Column(name = "codice_fiscale", nullable = false,
            unique = true, length = 16)
    private String codiceFiscale;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    /**
     * Stato del ciclo di vita dell'utente.
     * Persistito come stringa per coerenza con il tipo enum PostgreSQL
     * definito in migration.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    /**
     * ID dell'utente corrispondente su Keycloak.
     * Popolato alla creazione quando la sincronizzazione con Keycloak
     * va a buon fine. Può essere null se Keycloak non è raggiungibile
     * (gestito come caso degradato).
     */
    @Column(name = "keycloak_id", unique = true, length = 255)
    private String keycloakId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Ruoli associati all'utente.
     *
     * <p>Relazione many-to-many gestita tramite la tabella {@code user_roles}.
     * {@code FetchType.EAGER} è giustificato qui perché i ruoli sono
     * sempre necessari per le decisioni di autorizzazione — evita
     * il problema N+1 nelle query di listing.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    /**
     * Imposta automaticamente i timestamp alla prima persistenza.
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * Aggiorna automaticamente {@code updatedAt} ad ogni modifica.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
