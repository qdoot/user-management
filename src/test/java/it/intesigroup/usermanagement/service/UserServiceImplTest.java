package it.intesigroup.usermanagement.service;

import it.intesigroup.usermanagement.domain.Role;
import it.intesigroup.usermanagement.domain.RoleName;
import it.intesigroup.usermanagement.domain.User;
import it.intesigroup.usermanagement.domain.UserStatus;
import it.intesigroup.usermanagement.dto.request.CreateUserRequest;
import it.intesigroup.usermanagement.dto.request.UpdateUserRequest;
import it.intesigroup.usermanagement.dto.response.UserResponse;
import it.intesigroup.usermanagement.event.UserEventPublisher;
import it.intesigroup.usermanagement.exception.DuplicateFieldException;
import it.intesigroup.usermanagement.exception.InvalidStatusTransitionException;
import it.intesigroup.usermanagement.exception.UserNotFoundException;
import it.intesigroup.usermanagement.mapper.UserMapper;
import it.intesigroup.usermanagement.repository.RoleRepository;
import it.intesigroup.usermanagement.repository.UserRepository;
import it.intesigroup.usermanagement.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test unitari per {@link UserServiceImpl}.
 *
 * <p>Usa Mockito per isolare il service da tutte le dipendenze esterne.
 * Ogni test verifica un singolo comportamento in modo indipendente.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private UserMapper userMapper;
    @Mock private UserEventPublisher eventPublisher;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserResponse testUserResponse;
    private Role ownerRole;

    @BeforeEach
    void setUp() {
        ownerRole = new Role((short) 1, RoleName.OWNER);

        testUser = User.builder()
                .id(UUID.randomUUID())
                .username("mrossi")
                .email("mario.rossi@example.com")
                .codiceFiscale("RSSMRA80A01H501U")
                .firstName("Mario")
                .lastName("Rossi")
                .status(UserStatus.ACTIVE)
                .roles(Set.of(ownerRole))
                .build();

        testUserResponse = UserResponse.builder()
                .id(testUser.getId())
                .username(testUser.getUsername())
                .email(testUser.getEmail())
                .firstName(testUser.getFirstName())
                .lastName(testUser.getLastName())
                .status(UserStatus.ACTIVE)
                .roles(Set.of(RoleName.OWNER))
                .build();
    }

    @Test
    @DisplayName("findAllUsers — restituisce pagina di utenti attivi")
    void findAllUsers_returnsPageOfActiveUsers() {
        PageRequest pageable = PageRequest.of(0, 20);
        Page<User> userPage = new PageImpl<>(List.of(testUser));

        when(userRepository.findAllByStatus(UserStatus.ACTIVE, pageable)).thenReturn(userPage);
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        Page<UserResponse> result = userService.findAllUsers(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUsername()).isEqualTo("mrossi");
        verify(userRepository).findAllByStatus(UserStatus.ACTIVE, pageable);
    }

    @Test
    @DisplayName("findUserById — utente trovato — restituisce response")
    void findUserById_whenFound_returnsResponse() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        UserResponse result = userService.findUserById(testUser.getId());

        assertThat(result.getId()).isEqualTo(testUser.getId());
    }

    @Test
    @DisplayName("findUserById — utente non trovato — lancia UserNotFoundException")
    void findUserById_whenNotFound_throwsUserNotFoundException() {
        UUID unknownId = UUID.randomUUID();
        when(userRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findUserById(unknownId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(unknownId.toString());
    }

    @Test
    @DisplayName("createUser — dati validi — crea utente e pubblica evento")
    void createUser_withValidData_createsUserAndPublishesEvent() {
        CreateUserRequest request = CreateUserRequest.builder()
                .username("mrossi")
                .email("mario.rossi@example.com")
                .codiceFiscale("RSSMRA80A01H501U")
                .firstName("Mario")
                .lastName("Rossi")
                .roles(Set.of(RoleName.OWNER))
                .build();

        when(userRepository.findByUsername("mrossi")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("mario.rossi@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByCodiceFiscale("RSSMRA80A01H501U")).thenReturn(Optional.empty());
        when(roleRepository.findByNameIn(Set.of(RoleName.OWNER))).thenReturn(Set.of(ownerRole));
        when(userMapper.toEntity(request)).thenReturn(testUser);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        UserResponse result = userService.createUser(request);

        assertThat(result.getUsername()).isEqualTo("mrossi");
        verify(userRepository).save(any(User.class));
        verify(eventPublisher).publishUserCreated(any());
    }

    @Test
    @DisplayName("createUser — email duplicata — lancia DuplicateFieldException")
    void createUser_withDuplicateEmail_throwsDuplicateFieldException() {
        CreateUserRequest request = CreateUserRequest.builder()
                .username("altro")
                .email("mario.rossi@example.com")
                .codiceFiscale("RSSMRA80A01H501U")
                .firstName("Mario").lastName("Rossi")
                .roles(Set.of(RoleName.OWNER))
                .build();

        when(userRepository.findByUsername("altro")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("mario.rossi@example.com"))
                .thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(DuplicateFieldException.class)
                .hasMessageContaining("email");

        verify(userRepository, never()).save(any());
        verify(eventPublisher, never()).publishUserCreated(any());
    }

    @Test
    @DisplayName("createUser — username duplicato — lancia DuplicateFieldException")
    void createUser_withDuplicateUsername_throwsDuplicateFieldException() {
        CreateUserRequest request = CreateUserRequest.builder()
                .username("mrossi")
                .email("altro@example.com")
                .codiceFiscale("RSSMRA80A01H501U")
                .firstName("Mario").lastName("Rossi")
                .roles(Set.of(RoleName.OWNER))
                .build();

        when(userRepository.findByUsername("mrossi")).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(DuplicateFieldException.class)
                .hasMessageContaining("username");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("disableUser — utente ACTIVE — imposta status DISABLED")
    void disableUser_whenActive_setsStatusDisabled() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.disableUser(testUser.getId());

        assertThat(testUser.getStatus()).isEqualTo(UserStatus.DISABLED);
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("disableUser — utente già DISABLED — lancia InvalidStatusTransitionException")
    void disableUser_whenAlreadyDisabled_throwsInvalidStatusTransition() {
        testUser.setStatus(UserStatus.DISABLED);
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> userService.disableUser(testUser.getId()))
                .isInstanceOf(InvalidStatusTransitionException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteUser — utente ACTIVE — imposta status DELETED")
    void deleteUser_whenActive_setsStatusDeleted() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.deleteUser(testUser.getId());

        assertThat(testUser.getStatus()).isEqualTo(UserStatus.DELETED);
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("deleteUser — utente già DELETED — lancia InvalidStatusTransitionException")
    void deleteUser_whenAlreadyDeleted_throwsInvalidStatusTransition() {
        testUser.setStatus(UserStatus.DELETED);
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> userService.deleteUser(testUser.getId()))
                .isInstanceOf(InvalidStatusTransitionException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateUser — modifica parziale — aggiorna solo i campi non null")
    void updateUser_withPartialData_updatesOnlyNonNullFields() {
        UpdateUserRequest request = UpdateUserRequest.builder()
                .firstName("Giuseppe")
                .build();

        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        userService.updateUser(testUser.getId(), request);

        assertThat(testUser.getFirstName()).isEqualTo("Giuseppe");
        assertThat(testUser.getLastName()).isEqualTo("Rossi");
        verify(userRepository).save(testUser);
    }
}
