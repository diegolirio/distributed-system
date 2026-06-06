## ADDED Requirements

### Requirement: Integration tests use TestContainers with a real database engine
The system SHALL use TestContainers to spin up a real database container (Oracle XE or PostgreSQL) for all integration tests, ensuring SKIP LOCKED behaviour is tested against an actual SQL engine.

#### Scenario: Container starts and schema is applied
- **WHEN** the integration test class is loaded
- **THEN** a database container starts, Flyway migrations run, and the schema is ready before any test executes

#### Scenario: Tests pass on CI without local database
- **WHEN** tests run in a CI environment with Docker available
- **THEN** all integration tests complete successfully using the TestContainers-managed container

### Requirement: Integration test verifies SKIP LOCKED prevents duplicate processing
The system SHALL include a test that proves two concurrent job executions do not process the same row.

#### Scenario: Two threads pick non-overlapping rows
- **WHEN** 200 `PENDING` rows are seeded and two job threads run concurrently
- **THEN** each row appears in exactly one thread's processed set (no row is processed twice)

### Requirement: Integration test verifies full job loop end-to-end
The system SHALL include a test that seeds rows, triggers the job, and asserts all rows reach status `PROCESSED` with corresponding `Pagamento` records.

#### Scenario: All pending rows processed
- **WHEN** 50 valid `PENDING` rows are inserted and the job runs to completion
- **THEN** all 50 rows have status `PROCESSED` and 50 `Pagamento` records exist in the database

#### Scenario: Invalid rows marked FAILED, valid rows still processed
- **WHEN** a batch contains 10 valid rows and 2 invalid rows (missing propostaId)
- **THEN** 10 rows are `PROCESSED`, 2 rows are `FAILED`, and 10 `Pagamento` records exist

### Requirement: Integration tests are written before production code (test-first)
The system SHALL have the `JobPagamentosIT` test class committed and failing (red) before any production implementation is added.

#### Scenario: Red-green-refactor cycle enforced
- **WHEN** `JobPagamentosIT` is committed without production code
- **THEN** `mvn test` reports compilation or assertion failures for job-related classes
