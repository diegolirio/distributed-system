package com.example.demo.oracle.skipped.lock

import com.example.demo.oracle.skipped.lock.domain.Request
import com.example.demo.oracle.skipped.lock.domain.RequestStatus
import com.example.demo.oracle.skipped.lock.repository.LoteRepository
import com.example.demo.oracle.skipped.lock.repository.PagamentoRepository
import com.example.demo.oracle.skipped.lock.repository.RequestRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.context.TestPropertySource
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.oracle.OracleContainer
import org.testcontainers.utility.DockerImageName
import java.math.BigDecimal
import java.net.URI
import java.time.Duration
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestPropertySource(properties = ["app.job.enabled=false"])
class PagamentosConcurrentIT {

    companion object {
        @Container
        @ServiceConnection
        val oracle: OracleContainer =
            OracleContainer(DockerImageName.parse("gvenzl/oracle-free:latest"))
                .withStartupTimeout(Duration.ofMinutes(5))
    }

    @LocalServerPort
    private var port: Int = 0

    @Autowired lateinit var requestRepository: RequestRepository
    @Autowired lateinit var pagamentoRepository: PagamentoRepository
    @Autowired lateinit var loteRepository: LoteRepository

    private val httpClient: HttpClient = HttpClient.newHttpClient()

    @BeforeEach
    fun setup() {
        pagamentoRepository.deleteAll()
        loteRepository.deleteAll()
        requestRepository.deleteAll()

        val requests = (1..700).map { i ->
            Request().apply {
                propostaId = "P-$i"
                valor = BigDecimal("100.00")
                certificate = "VALID_CERT_$i"
                status = RequestStatus.PENDING
            }
        }
        requestRepository.saveAll(requests)

        assertThat(requestRepository.countByStatus(RequestStatus.PENDING)).isEqualTo(700)
    }

    @Test
    fun fiveConcurrentCallsProcessAllRows() {
        val executor = Executors.newFixedThreadPool(5)
        val latch = CountDownLatch(1)

        val futures: List<CompletableFuture<Int>> = (1..5).map {
            CompletableFuture.supplyAsync({
                latch.await()
                val req = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:$port/api/pagamentos/processar"))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .header("Content-Type", "application/json")
                    .build()
                httpClient.send(req, HttpResponse.BodyHandlers.ofString()).statusCode()
            }, executor)
        }

        latch.countDown()

        val statusCodes: List<Int> = futures.map { it.join() }
        executor.shutdown()
        executor.awaitTermination(120, TimeUnit.SECONDS)

        statusCodes.forEach { code ->
            assertThat(code).isEqualTo(200)
        }

        assertThat(requestRepository.countByStatus(RequestStatus.PENDING)).isEqualTo(0)
        assertThat(pagamentoRepository.count()).isEqualTo(700)
        assertThat(loteRepository.count()).isEqualTo(1)

    }
}
