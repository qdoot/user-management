package it.intesigroup.usermanagement.service.impl;

import it.intesigroup.usermanagement.domain.Role;
import it.intesigroup.usermanagement.domain.User;
import it.intesigroup.usermanagement.domain.UserStatus;
import it.intesigroup.usermanagement.dto.request.CreateUserRequest;
import it.intesigroup.usermanagement.dto.request.UpdateUserRequest;
import it.intesigroup.usermanagement.dto.response.UserResponse;
import it.intesigroup.usermanagement.event.UserCreatedEvent;
import it.intesigroup.usermanagement.event.UserEventPublisher;
import it.intesigroup.usermanagement.exception.DuplicateFieldException;
import it.intesigroup.usermanagement.exception.InvalidStatusTransitionException;
import it.intesigroup.usermanagement.exception.UserNotFoundException;
import it.intesigroup.usermanagement.mapper.UserMapper;
import it.intesigroup.usermanagement.repository.RoleRepository;
import it.intesigroup.usermanagement.repository.UserRepository;
import it.intesigroup.usermanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementazione del service layer per la gestione degli utenti.
 *
 * <p>Responsabilità:
 * <ul>
 *   <li>Validazione delle regole di business (unicità, transizioni di stato)</li>
 *   <li>Orchestrazione tra repository, mapper ed event publisher</li>
 *   <li>Gestione delle transazioni</li>
 * </ul>
 *
 * <p>{@code @Transactional(readOnly = true)} sui metodi di lettura ottimizza
 * le performance: Hibernate disabilita il dirty checking.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final UserEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> findAllUsers(Pageable pageable) {
        log.debug("Recupero lista utenti — pagina={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());

        return userRepository
                .findAllByStatus(UserStatus.ACTIVE, pageable)
                .map(userMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findUserById(UUID id) {
        log.debug("Recupero utente id={}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        log.info("Creazione utente username={}, email={}",
                request.getUsername(), request.getEmail());

        checkDuplicateUsername(request.getUsername(), null);
        checkDuplicateEmail(request.getEmail(), null);
        checkDuplicateCodiceFiscale(request.getCodiceFiscale(), null);

        Set<Role> roles = roleRepository.findByNameIn(request.getRoles());

        User user = userMapper.toEntity(request);
        user.setRoles(roles);

        User savedUser = userRepository.save(user);
        log.info("Utente creato con id={}", savedUser.getId());

        publishUserCreatedEvent(savedUser);

        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional
    public UserResponse updateUser(UUID id, UpdateUserRequest request) {
        log.info("Aggiornamento utente id={}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (request.getUsername() != null) {
            checkDuplicateUsername(request.getUsername(), id);
            user.setUsername(request.getUsername());
        }

        if (request.getCodiceFiscale() != null) {
            checkDuplicateCodiceFiscale(request.getCodiceFiscale(), id);
            user.setCodiceFiscale(request.getCodiceFiscale());
        }

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }

        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            Set<Role> roles = roleRepository.findByNameIn(request.getRoles());
            user.setRoles(roles);
        }

        User updatedUser = userRepository.save(user);
        log.info("Utente id={} aggiornato", updatedUser.getId());

        return userMapper.toResponse(updatedUser);
    }

    @Override
    @Transactional
    public void disableUser(UUID id) {
        log.info("Disabilitazione utente id={}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new InvalidStatusTransitionException(user.getStatus(), UserStatus.DISABLED);
        }

        user.setStatus(UserStatus.DISABLED);
        userRepository.save(user);
        log.info("Utente id={} disabilitato", id);
    }

    @Override
    @Transactional
    public void deleteUser(UUID id) {
        log.info("Eliminazione logica utente id={}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (user.getStatus() == UserStatus.DELETED) {
            throw new InvalidStatusTransitionException(user.getStatus(), UserStatus.DELETED);
        }

        user.setStatus(UserStatus.DELETED);
        userRepository.save(user);
        log.info("Utente id={} eliminato logicamente", id);
    }

    // ─────────────────────────────────────────────
    // Metodi privati di supporto
    // ─────────────────────────────────────────────

    private void checkDuplicateUsername(String username, UUID excludeId) {
        userRepository.findByUsername(username).ifPresent(existing -> {
            if (excludeId == null || !existing.getId().equals(excludeId)) {
                throw new DuplicateFieldException("username", username);
            }
        });
    }

    private void checkDuplicateEmail(String email, UUID excludeId) {
        if (excludeId != null) {
            if (userRepository.existsByEmailAndIdNot(email, excludeId)) {
                throw new DuplicateFieldException("email", email);
            }
        } else {
            userRepository.findByEmail(email).ifPresent(existing -> {
                throw new DuplicateFieldException("email", email);
            });
        }
    }

    private void checkDuplicateCodiceFiscale(String codiceFiscale, UUID excludeId) {
        if (excludeId != null) {
            if (userRepository.existsByCodiceFiscaleAndIdNot(codiceFiscale, excludeId)) {
                throw new DuplicateFieldException("codiceFiscale", codiceFiscale);
            }
        } else {
            userRepository.findByCodiceFiscale(codiceFiscale).ifPresent(existing -> {
                throw new DuplicateFieldException("codiceFiscale", codiceFiscale);
            });
        }
    }

    private void publishUserCreatedEvent(User user) {
        UserCreatedEvent event = UserCreatedEvent.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet()))
                .build();

        eventPublisher.publishUserCreated(event);
    }
}
