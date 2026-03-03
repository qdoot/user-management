package it.intesigroup.usermanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto di ingresso dell'applicazione.
 *
 * <p>{@code @SpringBootApplication} è una meta-annotazione che combina:
 * <ul>
 *   <li>{@code @Configuration} — questa classe è una sorgente di bean</li>
 *   <li>{@code @EnableAutoConfiguration} — attiva l'autoconfigurazione Spring Boot</li>
 *   <li>{@code @ComponentScan} — scansione dei componenti nel package corrente e sottopacchetti</li>
 * </ul>
 */
@SpringBootApplication
public class UserManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserManagementApplication.class, args);
    }
}
