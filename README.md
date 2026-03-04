# User Management Service

Servizio backend enterprise-grade per la gestione degli utenti, progettato secondo principi di scalabilità, manutenibilità e solidità tipici di un sistema a microservizi.

---

## Stack Tecnologico

| Componente | Tecnologia | Motivazione |
|---|---|---|
| Runtime | Java 21 + Spring Boot 3.3 | Virtual threads, record types, LTS |
| Database | PostgreSQL 16 | ACID, enum nativi, UUID, indici parziali |
| ORM | Spring Data JPA + Hibernate 6 | Repository pattern, `@SQLRestriction` per soft delete |
| Migration | Flyway | Schema versionato e riproducibile in tutti gli ambienti |
| Auth / IAM | Keycloak 24 + Spring OAuth2 Resource Server | JWT firmati RS256, RBAC delegato all'IAM |
| Messaging | RabbitMQ 3.13 + Spring AMQP | Eventi asincroni disaccoppiati, Topic exchange |
| Mapping | MapStruct 1.5 | Mapping compile-time, zero reflection, errori visibili in build |
| Docs | springdoc-openapi 2.5 | OpenAPI 3 + Swagger UI integrata |
| Testing | JUnit 5 + Mockito + Testcontainers | Unit test isolati + integration test su container reali |
| Container | Docker + Docker Compose | Ambiente locale riproducibile |

---

## Prerequisiti

- Java 21
- Maven 3.9+
- Docker Desktop

Verifica:
```bash
java -version
mvn -version
docker -v
```

---

## Avvio locale

### 1. Clona il repository
```bash
git clone <url-repository>
cd user-management
```

### 2. Avvia l'infrastruttura con Docker Compose
```bash
docker compose up -d postgres rabbitmq keycloak
```

Attendi che tutti i servizi siano healthy (circa 30-60 secondi):
```bash
docker compose ps
```

### 3. Avvia l'applicazione
```bash
mvn spring-boot:run
```

In alternativa, avvia tutto inclusa l'applicazione:
```bash
docker compose up -d
```

### 4. Verifica

- **Swagger UI**: http://localhost:8080/api/swagger-ui.html
- **Keycloak Admin**: http://localhost:8180 (admin / admin)
- **RabbitMQ Management**: http://localhost:15672 (guest / guest)

---

## Autenticazione

Il servizio usa JWT emessi da Keycloak. Per ottenere un token:
```bash
curl -X POST http://localhost:8180/realms/user-management/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=user-management-service" \
  -d "client_secret=um-secret-change-in-production" \
  -d "username=owner-user" \
  -d "password=password"
```

Utenti di test preconfigurati nel realm:

| Username | Password | Ruolo |
|---|---|---|
| owner-user | password | OWNER |
| operator-user | password | OPERATOR |
| reporter-user | password | REPORTER |

Inserire solo il token grezzo (senza prefisso "Bearer") nel campo bearerAuth — Swagger lo aggiunge automaticamente.

---

## Esecuzione dei test
```bash
# Unit test
mvn test

# Su Windows: impostare la variabile di sistema TESTCONTAINERS_RYUK_DISABLED=true
# prima di eseguire mvn verify
mvn verify
```


---

## API REST

Base URL: `http://localhost:8080/api/v1`

| Metodo | Endpoint | Descrizione | Ruoli |
|---|---|---|---|
| GET | `/users` | Lista paginata utenti | Tutti |
| GET | `/users/{id}` | Dettaglio utente | Tutti |
| POST | `/users` | Crea utente | OWNER, OPERATOR |
| PUT | `/users/{id}` | Aggiorna utente | OWNER, OPERATOR, MAINTAINER |
| PATCH | `/users/{id}/disable` | Disabilita utente | OWNER, OPERATOR |
| DELETE | `/users/{id}` | Elimina utente (soft delete) | OWNER |

---

## Scelte Architetturali

### Layered Architecture
Il progetto segue una separazione netta in tre layer: Controller (HTTP), Service (business logic), Repository (persistenza). Ogni layer conosce solo quello immediatamente sottostante — il controller non tocca mai il repository direttamente.

### Soft Delete
La cancellazione degli utenti è logica: il campo `status` viene impostato a `DELETED`, il record rimane nel database per audit. `@SQLRestriction` su Hibernate esclude automaticamente questi record da tutte le query senza filtri manuali.

### PostgreSQL vs MongoDB
Il dominio è relazionale: utenti con ruoli in relazione many-to-many, vincoli di unicità stretti, transazioni ACID sulla creazione.

### Keycloak come IAM esterno
Il servizio non gestisce password né emette token — è esclusivamente un Resource Server. Keycloak centralizza autenticazione e gestione ruoli. In un ecosistema a microservizi tutti i servizi validano lo stesso JWT senza duplicare la logica di autenticazione.

### MapStruct vs mapping manuale
MapStruct genera codice a compile-time: zero overhead di reflection a runtime, errori di mapping visibili durante la build. Il field masking dei campi sensibili è implementato direttamente nel mapper tramite `SecurityUtils`.

### RabbitMQ Topic Exchange
L'exchange di tipo Topic permette routing flessibile tramite pattern sulla routing key. Aggiungere un nuovo tipo di evento non richiede modifiche all'infrastruttura esistente.

### Testcontainers nei test di integrazione
I test di integrazione girano contro container Docker reali di PostgreSQL e RabbitMQ. Questo garantisce che le query JPA, i vincoli e le migration Flyway si comportino esattamente come in produzione.

---

## Trade-off

| Decisione | Alternativa scartata | Motivazione |
|---|---|---|
| Keycloak | JWT self-contained con chiave simmetrica | Keycloak è più complesso operativamente ma è lo standard enterprise e scala su più microservizi |
| `FetchType.EAGER` sui ruoli | `LAZY` con fetch esplicito | I ruoli sono sempre necessari per le decisioni di autorizzazione — evita il problema N+1 nel listing |
| Soft delete via `status` | Colonna `deleted_at` separata | Lo `status` modella esplicitamente il ciclo di vita dell'utente con transizioni controllate |
| Partial update nel `PUT` | Sostituzione completa | Riduce il rischio di sovrascrivere accidentalmente campi non inclusi nella request |

---

## Pipeline CI/CD (descrizione)

Una pipeline CI/CD minimale per questo progetto includerebbe:

1. **Build & Test** — `mvn verify` con Testcontainers (richiede Docker nel runner)
2. **Code Quality** — analisi statica con SonarQube o Checkstyle
3. **Docker Build** — `docker build` e push su registry (Docker Hub, ECR, ecc.)
4. **Deploy staging** — `docker compose pull && docker compose up -d` sull'ambiente di staging
5. **Deploy production** — stesso comando su produzione, con approvazione manuale