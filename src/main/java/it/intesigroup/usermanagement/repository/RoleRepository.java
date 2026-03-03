package it.intesigroup.usermanagement.repository;

import it.intesigroup.usermanagement.domain.Role;
import it.intesigroup.usermanagement.domain.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

/**
 * Repository per l'entità {@link Role}.
 *
 * <p>I ruoli sono dati di sola lettura a runtime — inseriti dalla
 * migration V1 e mai modificati tramite API. Questo repository
 * espone solo metodi di lettura utilizzati dal service layer
 * durante la creazione e modifica degli utenti.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Short> {

    /**
     * Cerca un ruolo per nome.
     */
    Optional<Role> findByName(RoleName name);

    /**
     * Recupera un insieme di ruoli dato un insieme di nomi.
     * Usato durante la creazione/modifica utente per risolvere
     * tutti i ruoli richiesti in una singola query.
     */
    Set<Role> findByNameIn(Set<RoleName> names);
}
