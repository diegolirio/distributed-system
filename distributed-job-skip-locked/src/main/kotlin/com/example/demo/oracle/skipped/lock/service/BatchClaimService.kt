package com.example.demo.oracle.skipped.lock.service

import com.example.demo.oracle.skipped.lock.domain.Request
import com.example.demo.oracle.skipped.lock.domain.RequestStatus
import com.example.demo.oracle.skipped.lock.repository.RequestRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BatchClaimService(private val requestRepository: RequestRepository) {

    /**
     * Fetches up to batchSize PENDING rows using SKIP LOCKED and marks them PROCESSING atomically.
     * Commits immediately so the lock is released and other instances can proceed.
     */
    @Transactional
    fun claimBatch(batchSize: Int): List<Request> {
        val batch = requestRepository.findForUpdateSkipLocked(batchSize)
        if (batch.isNotEmpty()) {
            batch.forEach { it.status = RequestStatus.PROCESSING }
            requestRepository.saveAll(batch)
        }
        return batch.toList()
    }
}
