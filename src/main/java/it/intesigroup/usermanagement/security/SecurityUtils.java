package it.intesigroup.usermanagement.security;

import it.intesigroup.usermanagement.domain.RoleName;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility statica per l'accesso al contesto di sicurezza corrente.
 *
 * <p>Centralizza l'estrazione dei ruoli dal JWT Keycloak, evitando
 * di replicare la logica di parsing nei mapper e nei service.
 */
public class SecurityUtils {

    private SecurityUtils() {
        // Classe di utility — non istanziabile
    }

    /**
     * Restituisce i ruoli del chiamante corrente estratti dal JWT.
     *
     * @return lista dei {@link RoleName} del chiamante,
     *         vuota se non autenticato o se i ruoli non sono mappabili
     */
    public static List<RoleName> getCurrentUserRoles() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Collections.emptyList();
        }

        return authentication.getAuthorities().stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .filter(SecurityUtils::isKnownRole)
                .map(RoleName::valueOf)
                .collect(Collectors.toList());
    }

    /**
     * Verifica se il chiamante corrente ha un ruolo privilegiato
     * (OWNER o OPERATOR) che abilita la visualizzazione dei campi sensibili.
     */
    public static boolean isPrivilegedCaller() {
        List<RoleName> roles = getCurrentUserRoles();
        return roles.contains(RoleName.OWNER) || roles.contains(RoleName.OPERATOR);
    }

    /**
     * Verifica se la stringa corrisponde a un {@link RoleName} noto,
     * evitando eccezioni su ruoli Keycloak non applicativi
     * (es. "offline_access", "uma_authorization").
     */
    private static boolean isKnownRole(String roleName) {
        try {
            RoleName.valueOf(roleName);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
