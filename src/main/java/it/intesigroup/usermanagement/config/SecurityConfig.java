package it.intesigroup.usermanagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Configurazione della sicurezza HTTP e JWT.
 *
 * <p>Il servizio è un OAuth2 Resource Server: non gestisce login,
 * si limita a validare i JWT emessi da Keycloak e ad estrarne i ruoli.
 *
 * <p>{@code @EnableMethodSecurity} abilita {@code @PreAuthorize}
 * sui metodi del controller per il controllo degli accessi a livello
 * di singolo endpoint.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Configura la filter chain HTTP:
     * <ul>
     *   <li>CSRF disabilitato — API REST stateless, nessun cookie di sessione</li>
     *   <li>Sessione STATELESS — ogni request è autenticata tramite JWT</li>
     *   <li>Endpoint pubblici: Swagger UI, OpenAPI docs, Actuator health</li>
     * </ul>
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs/**"
                ).permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            );

        return http.build();
    }

    /**
     * Converter che estrae i ruoli dal claim Keycloak {@code realm_access.roles}
     * e li trasforma in {@link SimpleGrantedAuthority} con prefisso {@code ROLE_}.
     *
     * <p>Il prefisso {@code ROLE_} è richiesto da Spring Security per il
     * funzionamento di {@code hasRole()} nelle espressioni {@code @PreAuthorize}.
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");

            if (realmAccess == null || !realmAccess.containsKey("roles")) {
                return List.of();
            }

            @SuppressWarnings("unchecked")
            Collection<String> roles = (Collection<String>) realmAccess.get("roles");

            return roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());
        });

        return converter;
    }
}
