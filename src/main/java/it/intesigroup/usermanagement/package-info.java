/**
 * Radice del progetto user-management.
 *
 * <p>Struttura dei package:
 * <ul>
 *   <li>{@code config}      — Bean di configurazione Spring (Security, RabbitMQ, OpenAPI)</li>
 *   <li>{@code controller}  — Layer REST: ricezione HTTP, validazione sintassi, risposta DTO</li>
 *   <li>{@code service}     — Business logic, orchestrazione, transazioni</li>
 *   <li>{@code repository}  — Interfacce Spring Data JPA, query custom</li>
 *   <li>{@code domain}      — Entità JPA (User, Role, UserRole)</li>
 *   <li>{@code dto}         — Oggetti di trasferimento dati (request/response separati)</li>
 *   <li>{@code mapper}      — Mapping compile-time DTO ↔ Domain via MapStruct</li>
 *   <li>{@code event}       — Classi evento e publisher RabbitMQ</li>
 *   <li>{@code exception}   — Eccezioni custom e handler globale</li>
 *   <li>{@code security}    — Estrazione JWT, RBAC evaluator, field masking</li>
 *   <li>{@code validation}  — Validatori custom (@ValidCodiceFiscale)</li>
 * </ul>
 */
package it.intesigroup.usermanagement;
