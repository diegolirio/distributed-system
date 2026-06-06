## Why

Requests (propostas) accumulate in a database table and need to be processed reliably in a distributed environment. Without a coordinated locking strategy, multiple job instances can pick up the same record, causing duplicate payments. Using `SELECT ... FOR UPDATE SKIP LOCKED` ensures each row is claimed by exactly one worker at a time without blocking others.

## What Changes

- Introduce a scheduled Spring job (`JobPagamentos`) that polls a `requests` table in a tight loop.
- Each iteration fetches up to 100 unprocessed rows using `SKIP LOCKED` to guarantee exclusive, non-blocking access.
- Each row is validated (proposta + certificate), a payment record and a lote (batch) record are persisted, and the row status is updated to `PROCESSED`.
- The loop terminates when no more rows are found.
- TestContainers integration tests are added **before** any production code, enforcing a test-first workflow.

## Capabilities

### New Capabilities

- `job-pagamentos`: Scheduled job that processes payment requests from the `requests` table using Skip Locked.
- `skipped-lock-repository`: Repository layer that executes `SELECT ... FOR UPDATE SKIP LOCKED` to fetch and lock rows atomically.
- `pagamento-processor`: Service that validates a proposta/certificate and persists a `Pagamento` + `Lote` record.
- `job-pagamentos-integration-test`: TestContainers-based integration test suite covering the full job loop.
- `pagamentos-endpoint`: REST endpoint (`POST /api/pagamentos/processar`) that triggers the same processing pipeline as the scheduled job, enabling on-demand execution.
- `pagamentos-concurrent-endpoint-test`: TestContainers integration test that seeds 500 rows, fires 5 concurrent HTTP requests to the endpoint, and asserts `SELECT COUNT(*) FROM requests WHERE status = 'PENDING'` = 0.

### Modified Capabilities

*(none — this is a greenfield feature)*

## Impact

- **New tables / entities**: `Pagamento`, `Lote` (written by the processor); reads from `requests` (existing or new).
- **New Spring beans**: `@Scheduled` job, `SkippedLockRepository`, `PagamentoProcessor`, `PagamentosController`.
- **Dependencies added**: `spring-boot-starter-data-jpa`, `spring-boot-starter-web`, `testcontainers` (BOM + Oracle/PostgreSQL module), `spring-boot-testcontainers`.
- **New endpoint**: `POST /api/pagamentos/processar` — triggers processing loop synchronously, returns count of rows processed.
- **Concurrency**: Safe for multi-instance deployments; SKIP LOCKED prevents duplicate processing both from job and endpoint callers.
- **No breaking changes** to existing APIs or tables.
