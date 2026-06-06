## ADDED Requirements

### Requirement: Processor validates proposta and certificate before persisting
The system SHALL validate the `Request` entity (proposta data and certificate) before writing any financial records. If validation fails, it SHALL throw `ValidationException` and write nothing.

#### Scenario: Valid proposta and certificate
- **WHEN** the request has a non-null `propostaId`, `valor` > 0, and a non-expired certificate
- **THEN** validation passes and processing continues

#### Scenario: Missing propostaId
- **WHEN** the request has a null or blank `propostaId`
- **THEN** a `ValidationException` is thrown with message `"propostaId is required"`

#### Scenario: Invalid certificate
- **WHEN** the request certificate is expired or malformed
- **THEN** a `ValidationException` is thrown with message `"certificate invalid"`

### Requirement: Processor persists Pagamento and associates it with a Lote
The system SHALL create a `Pagamento` record and link it to a `Lote` for the current processing date, both within a single `@Transactional` boundary.

#### Scenario: New Lote created for today
- **WHEN** no `Lote` record exists for today's date
- **THEN** a new `Lote` is created and the `Pagamento` references it

#### Scenario: Existing Lote reused for today
- **WHEN** a `Lote` record already exists for today's date
- **THEN** the `Pagamento` references the existing `Lote` (no duplicate lote)

#### Scenario: Payment amount recorded correctly
- **WHEN** processing completes successfully
- **THEN** `Pagamento.valor` equals `Request.valor` and `Pagamento.propostaId` equals `Request.propostaId`

### Requirement: Processor runs in its own transaction per request
The system SHALL annotate the process method with `@Transactional(propagation = REQUIRES_NEW)` so each row is committed independently.

#### Scenario: One failure does not roll back other payments
- **WHEN** row 42 of a 100-row batch throws `ValidationException`
- **THEN** rows 1–41 and 43–100 are committed normally
