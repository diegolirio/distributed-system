## ADDED Requirements

### Requirement: Concurrent endpoint test seeds 500 rows before execution
The system SHALL insert exactly 500 valid `PENDING` rows into the `requests` table via the TestContainers-managed database before any HTTP call is made.

#### Scenario: 500 rows seeded successfully
- **WHEN** the test setup runs
- **THEN** `SELECT COUNT(*) FROM requests WHERE status = 'PENDING'` equals 500

### Requirement: Test fires 5 parallel HTTP requests to the processing endpoint
The system SHALL submit 5 simultaneous `POST /api/pagamentos/processar` calls using a fixed-thread `ExecutorService`, all starting as close to the same instant as possible.

#### Scenario: All 5 requests dispatched concurrently
- **WHEN** `CountDownLatch` is released
- **THEN** all 5 threads call the endpoint before any has returned

#### Scenario: All 5 requests complete successfully
- **WHEN** the `ExecutorService` awaits termination
- **THEN** all 5 HTTP responses have status `HTTP 200` with no exceptions

### Requirement: After all requests complete, no PENDING rows remain
The system SHALL assert that the total count of rows with `status = 'PENDING'` is zero after all concurrent calls finish.

#### Scenario: Zero pending rows after concurrent processing
- **WHEN** all 5 `POST /api/pagamentos/processar` calls have returned
- **THEN** `SELECT COUNT(*) FROM requests WHERE status = 'PENDING'` equals `0`

### Requirement: No row is processed more than once
The system SHALL assert that the count of `Pagamento` records equals exactly 500 (one per request row), proving no duplicates were created.

#### Scenario: Exactly 500 Pagamento records exist
- **WHEN** all 5 concurrent calls have completed
- **THEN** `SELECT COUNT(*) FROM pagamentos` equals `500`

### Requirement: Test uses shared TestContainers instance for performance
The system SHALL use a single static container instance shared across all test methods in `PagamentosConcurrentIT` to avoid container restart overhead.

#### Scenario: Container reused across test methods
- **WHEN** multiple test methods run in the same test class
- **THEN** only one container instance is started and stopped per test class lifecycle
