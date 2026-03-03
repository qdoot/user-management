package it.intesigroup.usermanagement.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entità JPA che rappresenta un ruolo applicativo.
 *
 * <p>Corrisponde alla tabella {@code roles}. I record sono immutabili
 * a runtime — vengono inseriti dalla migration V1 e non esposti
 * tramite API di scrittura.
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
public class Role {

    /**
     * ID numerico fisso, allineato ai valori inseriti in migration:
     * 1=OWNER, 2=OPERATOR, 3=MAINTAINER, 4=DEVELOPER, 5=REPORTER.
     */
    @Id
    @Column(name = "id")
    private Short id;

    /**
     * Nome del ruolo. Mappato sull'enum {@link RoleName} tramite
     * il nome stringa — coerente con i valori nella tabella {@code roles}.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, unique = true, length = 50)
    private RoleName name;

    public Role(Short id, RoleName name) {
        this.id = id;
        this.name = name;
    }
}
