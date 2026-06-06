## ADDED Requirements

### Requirement: Repository fetches rows with SKIP LOCKED
The system SHALL provide a repository method that executes `SELECT ... FOR UPDATE SKIP LOCKED`, returning at most N rows that are not currently locked by another transaction.

#### Scenario: Rows available and not locked
- **WHEN** 200 rows with status `PENDING` exist and no other transaction holds locks
- **THEN** the method returns up to N rows and each is locked for the duration of the caller's transaction

#### Scenario: All pending rows locked by another instance
- **WHEN** all `PENDING` rows are locked by concurrent transactions
- **THEN** the method returns an empty list without blocking or throwing

#### Scenario: Mix of locked and unlocked rows
- **WHEN** 150 rows are `PENDING`, 80 are locked by another instance, and N=100
- **THEN** the method returns the 70 unlocked rows (up to N)

### Requirement: Repository updates row status atomically
The system SHALL update the `status` of a `Request` row within the same transaction that fetched it, preventing other instances from re-selecting it.

#### Scenario: Status updated to PROCESSING before processing begins
- **WHEN** a row is fetched by the skip-locked query
- **THEN** its status is set to `PROCESSING` before `PagamentoProcessor` is called

#### Scenario: Status updated to PROCESSED on success
- **WHEN** `PagamentoProcessor.process()` returns without exception
- **THEN** the row status is updated to `PROCESSED`

#### Scenario: Status updated to FAILED on validation error
- **WHEN** `PagamentoProcessor.process()` throws a `ValidationException`
- **THEN** the row status is updated to `FAILED` and the error message is stored in `requests.error_message`
