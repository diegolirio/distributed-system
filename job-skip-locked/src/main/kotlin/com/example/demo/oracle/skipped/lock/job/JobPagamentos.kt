package com.example.demo.oracle.skipped.lock.job

import com.example.demo.oracle.skipped.lock.service.PagamentoProcessingService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class JobPagamentos(
    private val processingService: PagamentoProcessingService,
    @Value("\${app.job.enabled:true}") private val enabled: Boolean
) {
    private val log = LoggerFactory.getLogger(JobPagamentos::class.java)

    @Scheduled(fixedDelayString = "\${app.job.delay-ms:1000}")
    fun run() {
        if (!enabled) return
        val result = processingService.runLoop()
        if (result.processed > 0 || result.failed > 0) {
            log.info("Job batch complete — processed={}, failed={}", result.processed, result.failed)
        }
    }
}
