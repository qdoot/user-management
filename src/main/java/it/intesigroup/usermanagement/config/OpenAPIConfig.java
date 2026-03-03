package it.intesigroup.usermanagement.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configurazione della documentazione OpenAPI 3 tramite springdoc.
 *
 * <p>Swagger UI raggiungibile su:
 * {@code http://localhost:8080/api/swagger-ui.html}
 */
@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("User Management API")
                        .description("""
                                Servizio backend enterprise-grade per la gestione degli utenti.
                                Architettura a microservizi con autenticazione JWT tramite Keycloak.
                                
                                Autenticazione: ottenere un token JWT da Keycloak e inserirlo
                                nel campo 'Authorize' in formato: Bearer <token>
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Intesi Group")
                                .email("dev@intesigroup.it")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Inserire il token JWT ottenuto da Keycloak")));
    }
}
