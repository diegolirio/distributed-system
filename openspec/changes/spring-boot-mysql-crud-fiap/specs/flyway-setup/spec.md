## ADDED Requirements

### Requirement: Flyway manages the quantum_finance schema lifecycle
The module SHALL use Flyway for all schema creation. Hibernate ddl-auto SHALL be set to `validate`. Six migration scripts SHALL exist in `src/main/resources/db/migration/` named V1–V6, applied in FK-dependency order.

#### Scenario: Clean database gets fully migrated
- **WHEN** the application starts against an empty MySQL database
- **THEN** Flyway applies V1 through V6 in order without errors and the `flyway_schema_history` table records all 6 as successful

#### Scenario: Already-migrated database passes validation
- **WHEN** the application starts a second time against a previously migrated database
- **THEN** Flyway reports 0 pending migrations and the application starts normally

### Requirement: Migration scripts are ordered by FK depth
Scripts SHALL be created in this order so no foreign key references an unbuilt table:
- V1 — `tipo_transacao`
- V2 — `cliente`
- V3 — `produto`
- V4 — `conta` (FK → cliente)
- V5 — `contratacao` (FK → cliente, produto)
- V6 — `transacao` (FK → conta, produto, tipo_transacao, contratacao)

#### Scenario: Running migrations on a clean schema
- **WHEN** Flyway runs V1 through V6 in order on a fresh schema
- **THEN** all foreign key constraints resolve without referencing a non-existent table

### Requirement: Flyway test support is configured
The test classpath SHALL include `spring-boot-starter-flyway-test` so Flyway migrations run automatically in `@SpringBootTest` integration tests.

#### Scenario: Integration test context starts with migrated schema
- **WHEN** a `@SpringBootTest` integration test starts with a Testcontainers MySQL instance
- **THEN** Flyway applies all migrations before any test method runs
