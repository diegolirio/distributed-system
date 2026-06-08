## Why

The current payment processing logic in `PagamentoProcessingService` handles transactions at the individual request level (`REQUIRES_NEW` for each row). While this ensures that a single failure doesn't roll back other successes, it can lead to performance overhead and potential memory issues (OOM) in the `TransactionManager` if the batch size is large or if many entities are accumulated in the persistence context without proper management.

The user wants to transition to a batch-level transaction strategy using `TransactionTemplate` to better control consistency and memory usage. This includes ensuring that the `Lote` (batch) record is resolved once at the beginning of the batch processing and that the batch processing itself happens within a controlled transaction boundary.

## What Changes

- **Refactor `PagamentoProcessingService.runLoop()`**: Use `TransactionTemplate` to wrap each batch iteration (100 items).
- **Batch Transaction Management**: Ensure that `claimBatch`, `Lote` resolution, and individual payment processing occur within a managed transaction.
- **Visible Lote Resolution**: Move the `Lote` resolution logic from the individual processor into the `runLoop` method, making it visible and executed once per batch.
- **Memory Management**: By using `TransactionTemplate` and controlling the batch size, we prevent the `TransactionManager` from accumulating too much state, avoiding `OutOfMemoryError`.
- **Consistency**: Batch-level transactions ensure that either the entire batch is processed (and status updated) or it rolls back correctly, maintaining data integrity.

## Capabilities

### New Capabilities

- **Batch-Aware Transaction Management**: `PagamentoProcessingService` now manages transactions at the batch level (100 items) instead of row-by-row.
- **Explicit Lote Handling**: The `Lote` for the day is resolved once per batch iteration.

### Modified Capabilities

- `PagamentoProcessingService`: Refactored `runLoop` to use `TransactionTemplate`.
- `PagamentoProcessor`: Modified to accept a `Lote` and potentially participate in an existing transaction instead of always requiring a new one.
- `BatchClaimService`: Ensure it works correctly within the outer `TransactionTemplate`.

## Impact

- **Performance**: Reduced transaction overhead by committing in batches of 100 instead of row-by-row.
- **Reliability**: Better memory management prevents OOM in long-running jobs.
- **Code Clarity**: The `Lote` creation/lookup logic is now visible at the start of the batch processing loop.
- **Database**: No changes to the database schema.
