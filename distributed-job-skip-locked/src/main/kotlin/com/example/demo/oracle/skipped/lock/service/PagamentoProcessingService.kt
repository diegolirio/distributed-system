package com.example.demo.oracle.skipped.lock.service

import com.example.demo.oracle.skipped.lock.domain.RequestStatus
import com.example.demo.oracle.skipped.lock.repository.RequestRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDate

@Service
class PagamentoProcessingService(
    private val batchClaimService: BatchClaimService,
    private val pagamentoProcessor: PagamentoProcessor,
    private val requestRepository: RequestRepository,
    private val transactionTemplate: TransactionTemplate,
    @Value("\${app.job.batch-size:100}") private val batchSize: Int
) {
    fun runLoop(): ProcessingResult {
        var processedTotal = 0
        var failedTotal = 0

        while (true) {
            val result = transactionTemplate.execute {
                val batch = batchClaimService.claimBatch(batchSize)
                if (batch.isEmpty()) return@execute null

                // Resolve the day's Lote once per batch transaction, only when there
                // is work to do — avoids creating an empty Lote on idle polls.
                val lote = pagamentoProcessor.resolveOrCreateLote(LocalDate.now())

                var processedBatch = 0
                var failedBatch = 0

                for (request in batch) {
                    try {
                        pagamentoProcessor.process(request, lote)
                        requestRepository.updateStatus(request.id!!, RequestStatus.PROCESSED, null)
                        processedBatch++
                    } catch (e: ValidationException) {
                        requestRepository.updateStatus(request.id!!, RequestStatus.FAILED, e.message)
                        failedBatch++
                    } catch (e: Exception) {
                        requestRepository.updateStatus(request.id!!, RequestStatus.FAILED, "Internal Error: ${e.message}")
                        failedBatch++
                    }
                }
                ProcessingResult(processedBatch, failedBatch)
            }

            if (result == null) break
            processedTotal += result.processed
            failedTotal += result.failed
        }

        return ProcessingResult(processedTotal, failedTotal)
    }
}
