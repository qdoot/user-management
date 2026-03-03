package it.intesigroup.usermanagement.mapper;

import it.intesigroup.usermanagement.domain.Role;
import it.intesigroup.usermanagement.domain.RoleName;
import it.intesigroup.usermanagement.domain.User;
import it.intesigroup.usermanagement.dto.request.CreateUserRequest;
import it.intesigroup.usermanagement.dto.response.UserResponse;
import it.intesigroup.usermanagement.security.SecurityUtils;
import org.mapstruct.*;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper MapStruct per la conversione tra entità {@link User} e DTO.
 *
 * <p>MapStruct genera l'implementazione a compile-time — zero reflection
 * a runtime, errori di mapping visibili durante la build.
 *
 * <p>Il mapping verso {@link UserResponse} applica il field masking
 * sui campi sensibili in base al ruolo del chiamante corrente.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Converte una {@link CreateUserRequest} in entità {@link User}.
     * I campi gestiti dal sistema vengono ignorati.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "keycloakId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "roles", ignore = true)
    User toEntity(CreateUserRequest request);

    /**
     * Converte un'entità {@link User} in {@link UserResponse}.
     * Applica il masking sui campi sensibili in base al ruolo del chiamante.
     */
    @Mapping(target = "roles", expression = "java(mapRoles(user.getRoles()))")
    @Mapping(target = "codiceFiscale", qualifiedByName = "maskSensitive",
            source = "codiceFiscale")
    @Mapping(target = "keycloakId", qualifiedByName = "maskSensitive",
            source = "keycloakId")
    UserResponse toResponse(User user);

    /**
     * Converte un insieme di entità {@link Role} in un insieme di {@link RoleName}.
     */
    default Set<RoleName> mapRoles(Set<Role> roles) {
        if (roles == null) return Set.of();
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }

    /**
     * Maschera un campo sensibile se il chiamante non ha privilegi sufficienti.
     * Restituisce il valore originale per OWNER e OPERATOR,
     * {@code "***"} per tutti gli altri ruoli.
     */
    @Named("maskSensitive")
    default String maskSensitiveField(String value) {
        if (value == null) return null;
        return SecurityUtils.isPrivilegedCaller() ? value : "***";
    }
}
