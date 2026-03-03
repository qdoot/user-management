package it.intesigroup.usermanagement.mapper;

import it.intesigroup.usermanagement.domain.User;
import it.intesigroup.usermanagement.dto.request.CreateUserRequest;
import it.intesigroup.usermanagement.dto.response.UserResponse;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-03T22:06:35+0100",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public User toEntity(CreateUserRequest request) {
        if ( request == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        user.username( request.getUsername() );
        user.email( request.getEmail() );
        user.codiceFiscale( request.getCodiceFiscale() );
        user.firstName( request.getFirstName() );
        user.lastName( request.getLastName() );

        return user.build();
    }

    @Override
    public UserResponse toResponse(User user) {
        if ( user == null ) {
            return null;
        }

        UserResponse.UserResponseBuilder userResponse = UserResponse.builder();

        userResponse.codiceFiscale( maskSensitiveField( user.getCodiceFiscale() ) );
        userResponse.keycloakId( maskSensitiveField( user.getKeycloakId() ) );
        userResponse.id( user.getId() );
        userResponse.username( user.getUsername() );
        userResponse.email( user.getEmail() );
        userResponse.firstName( user.getFirstName() );
        userResponse.lastName( user.getLastName() );
        userResponse.status( user.getStatus() );
        userResponse.createdAt( user.getCreatedAt() );
        userResponse.updatedAt( user.getUpdatedAt() );

        userResponse.roles( mapRoles(user.getRoles()) );

        return userResponse.build();
    }
}
