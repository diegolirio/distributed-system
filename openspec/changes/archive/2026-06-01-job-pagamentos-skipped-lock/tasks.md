## 1. Project Setup & Dependencies

- [x] 1.1 Add `spring-boot-starter-data-jpa`, `spring-boot-starter-scheduling` to `pom.xml` / `build.gradle`
- [x] 1.2 Add TestContainers BOM + Oracle XE (or PostgreSQL) module + `spring-boot-testcontainers` to test scope
- [x] 1.3 Enable `@EnableScheduling` on the Spring Boot application class
- [x] 1.4 Add `app.job.enabled` and `app.job.batch-size` properties to `application.properties` with defaults

## 2. Database Migrations (Flyway)

- [x] 2.1 Write migration `V1__create_requests_table.sql` with columns: `id`, `proposta_id`, `valor`, `certificate`, `status` (`PENDING`/`PROCESSING`/`PROCESSED`/`FAILED`), `error_message`, `created_at`
- [x] 2.2 Write migration `V2__create_lotes_table.sql` with columns: `id`, `data_lote`, `created_at`
- [x] 2.3 Write migration `V3__create_pagamentos_table.sql` with columns: `id`, `proposta_id`, `valor`, `lote_id` (FK), `request_id` (FK), `created_at`
- [x] 2.4 Add index on `requests(status)` for fast `PENDING` scans

## 3. Entities & Repository

- [x] 3.1 Create `Request` JPA entity mapping the `requests` table
- [x] 3.2 Create `Lote` JPA entity mapping the `lotes` table
- [x] 3.3 Create `Pagamento` JPA entity mapping the `pagamentos` table
- [x] 3.4 Create `RequestRepository` extending `JpaRepository` with native query method `findTop100ForUpdateSkipLocked(@Param("batchSize") int batchSize)` using `SELECT ... FOR UPDATE SKIP LOCKED`
- [x] 3.5 Create `LoteRepository` with `findByDataLote(LocalDate date)` method
- [x] 3.6 Create `PagamentoRepository` extending `JpaRepository`

## 4. Integration Test (Test-First — write before production code)

- [x] 4.1 Create `JobPagamentosIT` annotated with `@SpringBootTest` + `@Testcontainers`
- [x] 4.2 Declare a static `OracleContainer` (or `PostgreSQLContainer`) field with `@Container`
- [x] 4.3 Configure `@DynamicPropertySource` to override `spring.datasource.url/username/password`
- [x] 4.4 Write test `allPendingRowsAreProcessed`: seed 50 valid rows, run job, assert all `PROCESSED` + 50 `Pagamento` records
- [x] 4.5 Write test `invalidRowsMarkedFailed`: seed 10 valid + 2 invalid rows, run job, assert 10 `PROCESSED` + 2 `FAILED`
- [x] 4.6 Write test `skipLockedPreventsDoubleProcessing`: seed 200 rows, run two job threads concurrently, assert each row processed exactly once
- [x] 4.7 Confirm tests compile but fail (red) before any production service code exists

## 5. Concurrent Endpoint Integration Test (Test-First — write before production code)

- [x] 5.1 Create `PagamentosConcurrentIT` annotated with `@SpringBootTest(webEnvironment = RANDOM_PORT)` + `@Testcontainers`
- [x] 5.2 Declare a static shared container (`@Container`) — same image as `JobPagamentosIT`
- [x] 5.3 Configure `@DynamicPropertySource` to wire container datasource
- [x] 5.4 Inject `TestRestTemplate` (or `WebTestClient`) for HTTP calls
- [x] 5.5 Write `@BeforeEach` setup: truncate tables, insert 500 valid `PENDING` rows
- [x] 5.6 Write test `fiveConcurrentCallsProcessAllRows`:
  - Create `ExecutorService` with 5 threads + `CountDownLatch(1)`
  - Each thread awaits latch then calls `POST /api/pagamentos/processar`
  - Release latch to start all threads simultaneously
  - `executor.awaitTermination(60, SECONDS)`
  - Assert all 5 responses are `HTTP 200`
  - Assert `SELECT COUNT(*) FROM requests WHERE status = 'PENDING'` = 0
  - Assert `SELECT COUNT(*) FROM pagamentos` = 500
- [x] 5.7 Confirm test compiles but fails (red) before endpoint and service exist

## 6. PagamentoProcessor Service

- [x] 6.1 Create `PagamentoProcessor` Spring service
- [x] 6.2 Implement `validate(Request request)` — throw `ValidationException` for null/blank `propostaId` or `valor <= 0` or invalid certificate
- [x] 6.3 Implement `process(Request request)` annotated `@Transactional(propagation = REQUIRES_NEW)`
- [x] 6.4 Inside `process`: call `validate()`, upsert `Lote` for today, create and save `Pagamento`

## 7. PagamentoProcessingService (extracted loop)

- [x] 7.1 Create `PagamentoProcessingService` with method `ProcessingResult runLoop()`
- [x] 7.2 Move the SKIP LOCKED loop logic into `runLoop()` — fetch batch → set `PROCESSING` → call `PagamentoProcessor.process()` → set `PROCESSED`/`FAILED` → repeat until empty
- [x] 7.3 `ProcessingResult` record holds `int processed` and `int failed` counts

## 8. JobPagamentos Scheduled Job

- [x] 8.1 Create `JobPagamentos` Spring component with `@Scheduled(fixedDelayString = "${app.job.delay-ms:1000}")`
- [x] 8.2 Delegate to `PagamentoProcessingService.runLoop()` — no loop logic in the job itself
- [x] 8.3 Respect `app.job.enabled` flag — return early if false
- [x] 8.4 Log batch summary (processed/failed counts) at INFO level

## 9. PagamentosController REST Endpoint

- [x] 9.1 Add `spring-boot-starter-web` dependency if not already present
- [x] 9.2 Create `PagamentosController` with `@RestController` + `@RequestMapping("/api/pagamentos")`
- [x] 9.3 Implement `POST /processar` handler that calls `PagamentoProcessingService.runLoop()`
- [x] 9.4 Return `HTTP 200` with JSON body `{"processed": N, "failed": M}`

## 10. Verification

- [x] 10.1 Run `JobPagamentosIT` — all green
- [x] 10.2 Run `PagamentosConcurrentIT` — 5 threads, 500 rows, `COUNT(PENDING) = 0` after completion
- [x] 10.3 Assert `COUNT(pagamentos) = 500` — no duplicates
- [x] 10.4 Manually seed rows and call `POST /api/pagamentos/processar` against local Docker database
- [x] 10.5 Confirm `FAILED` rows retain `error_message` and are not re-queued
