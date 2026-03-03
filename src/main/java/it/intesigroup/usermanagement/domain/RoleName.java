package it.intesigroup.usermanagement.domain;

/**
 * Enum che rappresenta i ruoli applicativi disponibili nel sistema.
 *
 * <p>I valori corrispondono esattamente ai record presenti nella tabella
 * {@code roles} — inseriti dalla migration V1. Qualsiasi aggiunta
 * richiede una nuova migration SQL oltre alla modifica di questo enum.
 */
public enum RoleName {

    /** Proprietario del prodotto — accesso completo a tutte le operazioni. */
    OWNER,

    /** Operatore — può creare e modificare utenti, non può eliminarli. */
    OPERATOR,

    /** Manutentore — può modificare utenti esistenti, non crearli. */
    MAINTAINER,

    /** Sviluppatore — accesso in sola lettura, campi sensibili mascherati. */
    DEVELOPER,

    /** Reporter — accesso in sola lettura, campi sensibili mascherati. */
    REPORTER
}
