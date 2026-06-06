package com.example.demo.oracle.skipped.lock

import com.example.demo.oracle.skipped.lock.domain.Request
import com.example.demo.oracle.skipped.lock.domain.RequestStatus
import com.example.demo.oracle.skipped.lock.repository.LoteRepository
import com.example.demo.oracle.skipped.lock.repository.PagamentoRepository
import com.example.demo.oracle.skipped.lock.repository.RequestRepository
import com.example.demo.oracle.skipped.lock.service.PagamentoProcessingService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.context.TestPropertySource
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.oracle.OracleContainer
import org.testcontainers.utility.DockerImageName
import java.math.BigDecimal
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

@SpringBootTest
@Testcontainers
@TestPropertySource(properties = ["app.job.enabled=false"])
class JobPagamentosIT {

    companion object {
        @Container
        @ServiceConnection
        val oracle: OracleContainer =
            OracleContainer(DockerImageName.parse("gvenzl/oracle-free:latest"))
                .withStartupTimeout(Duration.ofMinutes(5))
    }

    @Autowired lateinit var service: PagamentoProcessingService
    @Autowired lateinit var requestRepository: RequestRepository
    @Autowired lateinit var pagamentoRepository: PagamentoRepository
    @Autowired lateinit var loteRepository: LoteRepository

    @BeforeEach
    fun cleanup() {
        pagamentoRepository.deleteAll()
        loteRepository.deleteAll()
        requestRepository.deleteAll()
    }

    @Test
    fun allPendingRowsAreProcessed() {
        repeat(50) { i -> requestRepository.save(validRequest(i)) }

        service.runLoop()

        assertThat(requestRepository.countByStatus(RequestStatus.PENDING)).isEqualTo(0)
        assertThat(requestRepository.countByStatus(RequestStatus.PROCESSED)).isEqualTo(50)
        assertThat(pagamentoRepository.count()).isEqualTo(50)
    }

    @Test
    fun invalidRowsMarkedFailed() {
        repeat(10) { i -> requestRepository.save(validRequest(i)) }
        repeat(2) { i -> requestRepository.save(invalidRequest(100 + i)) }

        service.runLoop()

        assertThat(requestRepository.countByStatus(RequestStatus.PROCESSED)).isEqualTo(10)
        assertThat(requestRepository.countByStatus(RequestStatus.FAILED)).isEqualTo(2)
        assertThat(requestRepository.countByStatus(RequestStatus.PENDING)).isEqualTo(0)
        assertThat(pagamentoRepository.count()).isEqualTo(10)
    }

    @Test
    fun skipLockedPreventsDoubleProcessing() {
        repeat(200) { i -> requestRepository.save(validRequest(i)) }

        val executor = Executors.newFixedThreadPool(2)
        val f1 = CompletableFuture.runAsync({ service.runLoop() }, executor)
        val f2 = CompletableFuture.runAsync({ service.runLoop() }, executor)
        f1.join()
        f2.join()
        executor.shutdown()

        assertThat(requestRepository.countByStatus(RequestStatus.PENDING)).isEqualTo(0)
        val processed = requestRepository.countByStatus(RequestStatus.PROCESSED)
        val failed = requestRepository.countByStatus(RequestStatus.FAILED)
        assertThat(processed + failed).isEqualTo(200)
        assertThat(pagamentoRepository.count()).isEqualTo(processed)
    }

    private fun validRequest(i: Int) = Request().apply {
        propostaId = "P-$i"
        valor = BigDecimal("100.00")
        certificate = "VALID_CERT_$i"
        status = RequestStatus.PENDING
    }

    private fun invalidRequest(i: Int) = Request().apply {
        propostaId = null
        valor = BigDecimal("50.00")
        certificate = "VALID_CERT_$i"
        status = RequestStatus.PENDING
    }
}
