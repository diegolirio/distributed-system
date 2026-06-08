## 1. Research and Preparation
- [x] 1.1 Verify current `TransactionManager` configuration in `application.properties` or configuration classes.
- [x] 1.2 Identify all dependencies of `PagamentoProcessingService` that might be affected by batch transactions.

## 2. PagamentoProcessor Refactoring
- [x] 2.1 Modify `PagamentoProcessor.process` to accept `Lote` as a parameter.
- [x] 2.2 Change `PagamentoProcessor.process` transaction propagation from `REQUIRES_NEW` to `REQUIRED` (or remove `@Transactional` if it should always be part of the batch transaction).
- [x] 2.3 Expose `resolveOrCreateLote` or ensure `LoteService.getOrCreate` is accessible for batch-level resolution.

## 3. PagamentoProcessingService Implementation
- [x] 3.1 Inject `TransactionTemplate` into `PagamentoProcessingService`.
- [x] 3.2 Refactor `runLoop` to use `TransactionTemplate.execute` within the `while(true)` loop.
- [x] 3.3 Implement `Lote` resolution at the beginning of each transaction block.
- [x] 3.4 Move `batchClaimService.claimBatch` inside the transaction block.
- [x] 3.5 Update the processing loop to pass the pre-resolved `Lote` to the processor.

## 4. Verification and Testing
- [x] 4.1 Update existing integration tests (`JobPagamentosIT`) to verify batch processing still works correctly.
- [x] 4.2 Add a new test case to verify that a failure in one request doesn't roll back the whole batch if that's the desired behavior, OR verify batch rollback if preferred.
- [x] 4.3 Monitor memory usage during high-load processing to confirm OOM issues are resolved.
