-- ============================================================
-- V1: Schema iniziale del database
-- Crea le tabelle principali, i vincoli e gli indici.
-- ============================================================

-- Tipo enum per lo stato dell'utente.
-- Usare un tipo nativo PostgreSQL garantisce integrità a livello DB
-- senza dipendere dalla sola validazione applicativa.
CREATE TYPE user_status AS ENUM ('ACTIVE', 'DISABLED', 'DELETED');

-- Tabella principale degli utenti
CREATE TABLE users (
    id               UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    username         VARCHAR(50)  NOT NULL,
    email            VARCHAR(255) NOT NULL,
    codice_fiscale   CHAR(16)     NOT NULL,
    first_name       VARCHAR(100) NOT NULL,
    last_name        VARCHAR(100) NOT NULL,
    status           user_status  NOT NULL DEFAULT 'ACTIVE',
    -- Riferimento all'utente su Keycloak (sincronizzato alla creazione)
    keycloak_id      VARCHAR(255),
    created_at       TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP    NOT NULL DEFAULT now(),

    CONSTRAINT uq_users_username       UNIQUE (username),
    CONSTRAINT uq_users_email          UNIQUE (email),
    CONSTRAINT uq_users_codice_fiscale UNIQUE (codice_fiscale),
    CONSTRAINT uq_users_keycloak_id    UNIQUE (keycloak_id)
);

-- Tabella dei ruoli applicativi.
-- Lookup table preferibile a una colonna enum per permettere join
-- espliciti e facilitare l'estensione futura senza migration DDL.
CREATE TABLE roles (
    id   SMALLINT    PRIMARY KEY,
    name VARCHAR(50) NOT NULL,

    CONSTRAINT uq_roles_name UNIQUE (name)
);

-- Tabella di associazione utente-ruoli (many-to-many).
-- La PK composita impedisce duplicati a livello DB.
CREATE TABLE user_roles (
    user_id UUID     NOT NULL,
    role_id SMALLINT NOT NULL,

    PRIMARY KEY (user_id, role_id),

    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE,

    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id)
        REFERENCES roles (id) ON DELETE RESTRICT
);

-- ============================================================
-- Indici
-- ============================================================

-- Ricerche per stato (es. lista utenti ACTIVE)
CREATE INDEX idx_users_status ON users (status);

-- Ricerche per ruolo su un utente specifico
CREATE INDEX idx_user_roles_user_id ON user_roles (user_id);

-- ============================================================
-- Dati iniziali — ruoli applicativi
-- Inseriti nella migration per garantire coerenza in tutti
-- gli ambienti (dev, staging, prod) senza script manuali.
-- ============================================================
INSERT INTO roles (id, name) VALUES
    (1, 'OWNER'),
    (2, 'OPERATOR'),
    (3, 'MAINTAINER'),
    (4, 'DEVELOPER'),
    (5, 'REPORTER');
