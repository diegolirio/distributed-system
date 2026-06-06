## Context

The application runs in a horizontally-scaled deployment (multiple JVM instances). A `requests` table accumulates payment proposals that must each be processed exactly once: validated, paid, and bundled into a lote. Without coordination, concurrent instances race to claim the same rows.

The chosen mechanism is PostgreSQL/Oracle's `SELECT ... FOR UPDATE SKIP LOCKED`: rows being processed by another transaction are transparently skipped, giving each instance an exclusive, non-overlapping batch with zero wait time.

## Goals / Non-Goals

**Goals:**
- Process all rows in the `requests` table exactly once, even under concurrent instances.
- Sustain a tight polling loop (no idle sleep between batches when work exists).
- Validate each proposta + certificate before writing financial records.
- Persist a `Pagamento` and associate it with a `Lote` (batch) in the same transaction.
- Provide a REST endpoint (`POST /api/pagamentos/processar`) that reuses the same processing pipeline as the job.
- Provide a comprehensive TestContainers integration test suite written before production code.
- Prove correctness under 5 concurrent callers against 500 rows: zero `PENDING` rows remain after all calls complete.

**Non-Goals:**
- Dead-letter / retry queue for permanently invalid proposals (out of scope — mark as `FAILED` and continue).
- Async / reactive processing (synchronous JDBC is sufficient for this workload).
- Distributed tracing or metrics dashboards (can be added later).
- Multi-table fan-out beyond `Pagamento` + `Lote`.

## Decisions

### D1 — Skip Locked via native query

**Choice**: Use a `@Query` with `nativeQuery = true` and `FOR UPDATE SKIP LOCKED` rather than JPA Pessimistic Lock.

**Rationale**: JPA's `LockModeType.PESSIMISTIC_WRITE` does not propagate `SKIP LOCKED` on all providers. A native query is explicit, portable between Oracle 11g+ and PostgreSQL 9.5+, and readable.

**Alternative considered**: `SELECT FOR UPDATE NOWAIT` — rejected because it throws an exception on contention instead of skipping, requiring catch-and-retry logic.

### D2 — Batch size of 100 rows per iteration

**Choice**: Each `findTop100ForUpdateSkipLocked()` call fetches at most 100 rows.

**Rationale**: Keeps transaction duration short, reduces lock hold time, and fits comfortably in a single JPA flush. Configurable via `app.job.batch-size` property.

### D3 — Single transaction per row (not per batch)

**Choice**: `PagamentoProcessor.process(request)` runs in its own `@Transactional` method.

**Rationale**: A batch-level transaction means one validation failure rolls back 99 good payments. Per-row transactions isolate failures; a failed row is marked `FAILED` and the batch continues.

**Trade-off**: More commits per second. Acceptable at 100 rows/batch; revisit if throughput demands it.

### D4 — TestContainers first

**Choice**: Write `JobPagamentosIT` using TestContainers (Oracle XE or PostgreSQL container) before any production code.

**Rationale**: Ensures the SKIP LOCKED query actually runs against a real database engine. H2 does not support `SKIP LOCKED`, making in-memory tests meaningless for this feature.

### D5 — Lote grouping by processing date

**Choice**: All `Pagamento` records created in the same job execution share a single `Lote` keyed by `LocalDate.now()`.

**Rationale**: Simple, auditable, and matches typical batch-payment reconciliation requirements. Concurrent instances on the same date append to the same `Lote` row (upsert).

### D6 — Endpoint delegates to the same ProcessingService used by the job

**Choice**: Extract the loop logic from `JobPagamentos` into a `PagamentoProcessingService.runLoop()` method. Both `JobPagamentos` and `PagamentosController` call this service.

**Rationale**: Avoids duplicating the SKIP LOCKED loop. The endpoint is a synchronous trigger; it runs the loop to completion and returns the count of rows processed. The service is stateless and safe to call concurrently.

**Alternative considered**: Duplicate the loop inline in the controller — rejected because divergence between job and endpoint behavior would be a silent bug source.

### D7 — Concurrent endpoint test with 5 threads and 500 rows

**Choice**: `PagamentosConcurrentIT` seeds 500 valid rows, submits 5 `POST /api/pagamentos/processar` calls via `ExecutorService`, waits for all to complete, then asserts `SELECT COUNT(*) FROM requests WHERE status = 'PENDING' = 0`.

**Rationale**: This is the definitive proof that SKIP LOCKED works end-to-end: no row is missed, no row is duplicated. 500 rows ÷ 5 threads ÷ 100-row batches = each thread does ~1 batch on average, but the test remains correct regardless of how batches distribute.

**Trade-off**: Test takes longer than unit tests (container startup + 500 inserts). Acceptable in the integration test phase; excluded from unit test suite via `@Tag("integration")`.

## Risks / Trade-offs

| Risk | Mitigation |
|------|-----------|
| Long-running job holds locks beyond transaction timeout | Batch size of 100 + per-row transactions keep each lock short-lived |
| DB connection pool exhaustion under high concurrency | Pool size tuned to `max-instances × 2`; job uses a dedicated DataSource if needed |
| Oracle XE licence in CI | Use `gvenzl/oracle-xe` image (free tier) or swap for PostgreSQL in tests |
| Clock skew between instances splitting a `Lote` across two dates at midnight | Acceptable — split lotes are reconcilable; add `job_run_id` UUID if stricter grouping is required |
| Validation false-negatives leaving rows stuck | Rows marked `FAILED` with error message; separate cleanup job or manual requeue |
| Endpoint called while job is also running | SKIP LOCKED handles this naturally — they pick non-overlapping rows |
| Endpoint timeout if 500+ rows in flight | Processing is synchronous; set `spring.mvc.async.request-timeout` or make endpoint async if needed |
| 5 concurrent threads saturating DB connection pool | Pool must have ≥ 5 connections; default HikariCP pool (10) is sufficient |

## Migration Plan

1. Add Flyway migration: create `requests`, `pagamentos`, `lotes` tables (if not existing).
2. Add `status` column to `requests` (`PENDING`, `PROCESSING`, `PROCESSED`, `FAILED`).
3. Deploy new code alongside existing instances — SKIP LOCKED ensures safe coexistence.
4. Rollback: disable `@Scheduled` via feature flag (`app.job.enabled=false`); no schema rollback needed.

## Open Questions

- Is the target database Oracle or PostgreSQL? (affects container image in TestContainers)
- Should `Lote` be keyed by date or by job-run UUID?
- What certificate validation logic is expected — external service call or local keystore check?
