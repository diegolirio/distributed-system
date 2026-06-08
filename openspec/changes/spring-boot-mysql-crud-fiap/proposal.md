## Why

The FIAP MBA Data Architecture project requires a production-grade Spring Boot (Kotlin) application backed by MySQL that demonstrates relational database ACID compliance as part of the CAP theorem study. The Quantum Finance schema — already defined in DDL — needs to be versioned with Flyway and exposed via REST CRUDs so teams can compare MySQL behavior against Cassandra side-by-side.

## What Changes

- Create a new Gradle sub-project `cap-theorem-mysql-cassandra/cap-theorem-mysql-cassandra` (Kotlin + Spring Boot)
- Implement Flyway migrations that reproduce the `quantum_finance` schema (6 tables)
- Build a full CRUD REST API for each table: `cliente`, `produto`, `tipo_transacao`, `conta`, `contratacao`, `transacao`
- Add Testcontainers-based integration tests that spin up MySQL and run migrations before each test
- Ship `docker-compose.mysql.yml` and `application.yml` matching the FIAP-provided configuration

## Capabilities

### New Capabilities

- `cliente-crud`: Full CRUD REST endpoints + JPA entity + Flyway migration for the `cliente` table
- `produto-crud`: Full CRUD REST endpoints + JPA entity + Flyway migration for the `produto` table
- `tipo-transacao-crud`: Full CRUD REST endpoints + JPA entity + Flyway migration for the `tipo_transacao` table
- `conta-crud`: Full CRUD REST endpoints + JPA entity + Flyway migration for the `conta` table (FK → cliente)
- `contratacao-crud`: Full CRUD REST endpoints + JPA entity + Flyway migration for the `contratacao` table (FK → cliente, produto)
- `transacao-crud`: Full CRUD REST endpoints + JPA entity + Flyway migration for the `transacao` table (FK → conta, produto, tipo_transacao, contratacao)
- `flyway-setup`: Flyway configuration, migration scripts V1–V6, and test support
- `testcontainers-setup`: Shared Testcontainers MySQL configuration for integration tests

### Modified Capabilities

## Impact

- **New module**: `cap-theorem-mysql-cassandra/cap-theorem-mysql-cassandra/` added to the workspace
- **Dependencies added**: `spring-boot-starter-data-jpa`, `spring-boot-starter-web`, `spring-boot-starter-flyway`, `flyway-mysql`, `mysql-connector-j`, `spring-boot-starter-testcontainers`, `testcontainers-mysql`, `spring-boot-starter-flyway-test`
- **No breaking changes** to existing `cap-theorem-mysql` module
