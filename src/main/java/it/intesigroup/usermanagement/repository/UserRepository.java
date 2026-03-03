package it.intesigroup.usermanagement.repository;

import it.intesigroup.usermanagement.domain.User;
import it.intesigroup.usermanagement.domain.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository per l'entità {@link User}.
 *
 * <p>Estende {@link JpaRepository} per le operazioni CRUD standard.
 * Le query custom usano JPQL per restare indipendenti dal dialetto SQL
 * specifico di PostgreSQL dove possibile.
 *
 * <p>Nota: {@code @SQLRestriction("status <> 'DELETED'")} definita
 * sull'entità si applica automaticamente a tutti i metodi di questo
 * repository — gli utenti con status DELETED sono invisibili
 * senza alcun filtro aggiuntivo.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Recupera una pagina di utenti filtrando per status.
     * Usato dal listing API per restituire solo utenti ACTIVE di default.
     */
    Page<User> findAllByStatus(UserStatus status, Pageable pageable);

    /**
     * Cerca un utente per email.
     * Usato per il controllo di unicità prima della creazione.
     */
    Optional<User> findByEmail(String email);

    /**
     * Cerca un utente per username.
     * Usato per il controllo di unicità prima della creazione.
     */
    Optional<User> findByUsername(String username);

    /**
     * Cerca un utente per codice fiscale.
     * Usato per il controllo di unicità prima della creazione.
     */
    Optional<User> findByCodiceFiscale(String codiceFiscale);

    /**
     * Verifica l'esistenza di un utente con la email specificata,
     * escludendo un determinato ID.
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND u.id <> :id")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("id") UUID id);

    /**
     * Verifica l'esistenza di un utente con il codice fiscale specificato,
     * escludendo un determinato ID.
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.codiceFiscale = :cf AND u.id <> :id")
    boolean existsByCodiceFiscaleAndIdNot(@Param("cf") String codiceFiscale, @Param("id") UUID id);
}
