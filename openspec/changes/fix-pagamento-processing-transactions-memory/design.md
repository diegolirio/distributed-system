## Proposed Solution

Refactor `PagamentoProcessingService` to use `TransactionTemplate` for managing batch transactions. Each iteration of the `while(true)` loop will process up to 100 records in a single transaction.

### 1. PagamentoProcessingService Refactoring

The `runLoop` method will be updated as follows:

- Inject `TransactionTemplate`.
- Wrap the contents of `while(true)` in `transactionTemplate.execute { status -> ... }`.
- Inside the transaction:
  1. Resolve the `Lote` for the current date.
  2. Claim a batch of 100 requests (using `BatchClaimService`).
  3. Process each request in the batch.
  4. Update request status to `PROCESSED` or `FAILED`.
- The loop continues until `claimBatch` returns an empty list.

### 2. Transaction Management Strategy

To ensure memory efficiency and avoid "Estouro de memoria":
- The transaction boundary is at the batch level (100 items).
- `PagamentoProcessor.process` will be updated to accept a pre-resolved `Lote`.
- We will consider if `REQUIRES_NEW` is still needed for individual records. If the user wants batch consistency, we might move to `REQUIRED` (default) so if one fails, the whole batch rolls back, OR we keep the `try-catch` and handle individual failures within the batch transaction. 
  - *Decision*: Keep `try-catch` inside the batch transaction to allow partial success, but ensure that the `TransactionManager` doesn't leak memory by limiting the transaction scope to 100 items.

### 3. Visibility of Lote Creation

The logic currently in `PagamentoProcessor.resolveOrCreateLote` will be moved/exposed so it can be called at the beginning of the `runLoop` transaction block.

```kotlin
// Inside runLoop
while (true) {
    val result = transactionTemplate.execute { status ->
        val lote = try {
            loteService.getOrCreate(LocalDate.now())
        } catch (e: DataIntegrityViolationException) {
            loteService.getOrCreate(LocalDate.now())
        }
        
        val batch = batchClaimService.claimBatch(batchSize)
        if (batch.isEmpty()) return@execute null
        
        // ... process batch ...
    }
    if (result == null) break
}
```

## Architectural Changes

- **TransactionTemplate Integration**: Move from declarative `@Transactional` to programmatic `TransactionTemplate` in the main loop to better control batching and error handling.
- **Service Decoupling**: `PagamentoProcessor` becomes more "dumb" by receiving the `Lote` instead of resolving it itself.

## Data Model Changes

*(none)*

## Performance and Scalability

- **Memory**: 100 items per transaction is a safe limit to avoid `OutOfMemoryError` in Hibernate's `Session` / `TransactionManager`.
- **Concurrency**: `SKIP LOCKED` in `claimBatch` ensures multiple instances don't collide. The `Lote` resolution handles concurrent creation via `REQUIRES_NEW` and retry logic.
