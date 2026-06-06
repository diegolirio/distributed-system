## ADDED Requirements

### Requirement: Endpoint triggers the payment processing loop on demand
The system SHALL expose `POST /api/pagamentos/processar` that runs the same SKIP LOCKED processing loop as the scheduled job and returns a summary of rows processed.

#### Scenario: Endpoint processes all pending rows
- **WHEN** `POST /api/pagamentos/processar` is called and 50 `PENDING` rows exist
- **THEN** the response is `HTTP 200` with body `{"processed": 50, "failed": 0}` and all rows are `PROCESSED`

#### Scenario: Endpoint returns zero when no rows pending
- **WHEN** `POST /api/pagamentos/processar` is called and no `PENDING` rows exist
- **THEN** the response is `HTTP 200` with body `{"processed": 0, "failed": 0}`

#### Scenario: Endpoint reports failed rows separately
- **WHEN** `POST /api/pagamentos/processar` is called and 10 rows are valid, 2 are invalid
- **THEN** the response is `HTTP 200` with body `{"processed": 10, "failed": 2}`

### Requirement: Endpoint is safe to call concurrently with the scheduled job
The system SHALL allow the endpoint and the `@Scheduled` job to run simultaneously without causing duplicate processing, relying on SKIP LOCKED for coordination.

#### Scenario: Concurrent endpoint call and job execution
- **WHEN** the scheduled job is running and `POST /api/pagamentos/processar` is called at the same time
- **THEN** each `requests` row is processed exactly once across both callers

### Requirement: Endpoint delegates processing to PagamentoProcessingService
The system SHALL NOT duplicate the loop logic; `PagamentosController` MUST call `PagamentoProcessingService.runLoop()` which is the same method called by `JobPagamentos`.

#### Scenario: Single source of truth for processing logic
- **WHEN** the endpoint is invoked
- **THEN** the same `PagamentoProcessingService.runLoop()` code path executes as when the job runs
