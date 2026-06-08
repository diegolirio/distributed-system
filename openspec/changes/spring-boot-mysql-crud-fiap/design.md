## Context

The workspace already contains a `cap-theorem-mysql` Spring Boot module. This change adds a sibling module `cap-theorem-mysql-cassandra` (inside `cap-theorem-mysql-cassandra/`) with a full REST CRUD stack on top of the Quantum Finance MySQL schema (6 tables). The schema is already fully specified in DDL; we migrate it via Flyway and expose each table as a REST resource. Integration tests use Testcontainers to spin up a real MySQL instance.

Existing module: `cap-theorem-mysql-cassandra/cap-theorem-mysql/` (Spring Boot 4.x, Kotlin 2.x, Gradle).  
New module: `cap-theorem-mysql-cassandra/cap-theorem-mysql-cassandra/` — same toolchain, same group (`ai.analizza`).

## Goals / Non-Goals

**Goals:**
- New Gradle module with Spring Boot, JPA, Flyway, Testcontainers
- 6 Flyway migration scripts in dependency-safe order
- JPA entities + Spring Data repositories for all 6 tables
- REST controllers with Create / Read (single + list) / Update / Delete for each entity
- Integration tests with `@Testcontainers` + MySQL container that validates each CRUD operation

**Non-Goals:**
- Authentication / authorization
- Pagination or advanced filtering beyond basic list
- Cassandra side (separate module)
- GraphQL or reactive stack

## Decisions

### D1 — Module location: new sub-project, not an addition to cap-theorem-mysql

The two modules (mysql-only vs mysql+cassandra) serve different demonstration purposes and will diverge in dependencies. A clean separation avoids dependency pollution.

**Alternatives:** Adding Cassandra to the existing mysql module — rejected because it muddies the pure-MySQL CAP demo.

### D2 — Flyway over Hibernate DDL generation

`spring.jpa.hibernate.ddl-auto=validate` (from the FIAP application.yml) means Hibernate never creates tables — Flyway owns schema lifecycle. Scripts are numbered V1–V6 in foreign-key-safe creation order: tipo_transacao → cliente → produto → conta → contratacao → transacao.

**Alternatives:** Hibernate `create-drop` for tests — rejected because Flyway scripts are the artifact under test.

### D3 — Spring Data JPA repositories + `@RestController`

Each table gets: Entity → Repository (JpaRepository) → Service → Controller. Service layer keeps controllers thin and repositories unit-testable.

**Alternatives:** JDBC template — rejected; JPA is standard for the team's stack and reduces boilerplate.

### D4 — Testcontainers with `@SpringBootTest` + `@Testcontainers`

A single shared `MySQLContainer` per test class (via `companion object`) reuses the container across test methods for speed. Flyway runs automatically on context startup.  
Dependencies: `spring-boot-starter-testcontainers`, `testcontainers-mysql`, `spring-boot-starter-flyway-test` (as per requirement).

### D5 — Idempotency key for `transacao` is a UUID sent by the client

The `id_idempotencia CHAR(36)` column has a UNIQUE constraint. The REST API accepts it as a required field in the request body; the service layer enforces uniqueness at the DB level (let the unique constraint bubble up as 409 Conflict).

## Risks / Trade-offs

- **Flyway script ordering** → If FK order is wrong the migration fails on a clean DB. Mitigation: scripts are named and ordered by FK depth.
- **`transacao.valor` CHECK (valor > 0)** → Updates that set valor ≤ 0 will get a DB-level error. Mitigation: validate in service layer before hitting DB.
- **Spring Boot 4.x** uses Jakarta EE 10 namespace — ensure all imports use `jakarta.*` not `javax.*`.
- **Kotlin data classes with JPA** require `@Entity` + `open` or the `kotlin-jpa` plugin. Use `kotlin("plugin.jpa")` in `build.gradle.kts`.

## Migration Plan

1. Create the new module directory and `build.gradle.kts`
2. Register the module in the root `settings.gradle.kts` (if a multi-module root exists)
3. Write Flyway migrations V1–V6
4. Implement entities, repositories, services, controllers
5. Write integration tests
6. Verify with `./gradlew :cap-theorem-mysql-cassandra:test`

## Open Questions

- Should the root `settings.gradle.kts` include the new module? (Assume yes — add it during implementation.)
- Is there a Docker Compose file to update? (Copy the FIAP-provided `docker-compose.mysql.yml` into the new module.)
