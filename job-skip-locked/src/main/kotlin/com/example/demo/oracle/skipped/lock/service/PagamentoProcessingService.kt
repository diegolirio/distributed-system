package com.example.demo.oracle.skipped.lock.service

import com.example.demo.oracle.skipped.lock.domain.RequestStatus
import com.example.demo.oracle.skipped.lock.repository.RequestRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class PagamentoProcessingService(
    private val batchClaimService: BatchClaimService,
    private val pagamentoProcessor: PagamentoProcessor,
    private val requestRepository: RequestRepository,
    @Value("\${app.job.batch-size:100}") private val batchSize: Int
) {
    fun runLoop(): ProcessingResult {
        var processed = 0
        var failed = 0

        while (true) {
            val batch = batchClaimService.claimBatch(batchSize)
            if (batch.isEmpty()) break

            for (request in batch) {
                try {
                    pagamentoProcessor.process(request)
                    requestRepository.updateStatus(request.id!!, RequestStatus.PROCESSED, null)
                    processed++
                } catch (e: ValidationException) {
                    requestRepository.updateStatus(request.id!!, RequestStatus.FAILED, e.message)
                    failed++
                }
            }
        }

        return ProcessingResult(processed, failed)
    }
}
