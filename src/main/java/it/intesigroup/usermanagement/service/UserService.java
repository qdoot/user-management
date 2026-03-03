package it.intesigroup.usermanagement.service;

import it.intesigroup.usermanagement.dto.request.CreateUserRequest;
import it.intesigroup.usermanagement.dto.request.UpdateUserRequest;
import it.intesigroup.usermanagement.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Contratto del service layer per la gestione degli utenti.
 *
 * <p>Definire un'interfaccia separata dall'implementazione permette di:
 * <ul>
 *   <li>Mockare facilmente il service nei test del controller</li>
 *   <li>Sostituire l'implementazione senza modificare il controller</li>
 *   <li>Rendere esplicito il contratto pubblico del layer</li>
 * </ul>
 */
public interface UserService {

    Page<UserResponse> findAllUsers(Pageable pageable);

    UserResponse findUserById(UUID id);

    UserResponse createUser(CreateUserRequest request);

    UserResponse updateUser(UUID id, UpdateUserRequest request);

    void disableUser(UUID id);

    void deleteUser(UUID id);
}
