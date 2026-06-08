package com.example.demo.oracle.skipped.lock.service

import com.example.demo.oracle.skipped.lock.domain.Lote
import com.example.demo.oracle.skipped.lock.domain.Pagamento
import com.example.demo.oracle.skipped.lock.domain.Request
import com.example.demo.oracle.skipped.lock.repository.LoteRepository
import com.example.demo.oracle.skipped.lock.repository.PagamentoRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate

@Service
class PagamentoProcessor(
    private val loteService: LoteService,
    private val loteRepository: LoteRepository,
    private val pagamentoRepository: PagamentoRepository
) {
    fun validate(request: Request) {
        if (request.propostaId.isNullOrBlank()) {
            throw ValidationException("propostaId is required")
        }
        if (request.valor <= BigDecimal.ZERO) {
            throw ValidationException("valor must be positive")
        }
        if (request.certificate.isNullOrBlank() ||
            request.certificate!!.uppercase().contains("EXPIRED")
        ) {
            throw ValidationException("certificate invalid")
        }
    }

    fun process(request: Request, lote: Lote) {
        validate(request)
        pagamentoRepository.save(
            Pagamento().apply {
                propostaId = request.propostaId!!
                valor = request.valor
                this.lote = lote
                requestId = request.id!!
            }
        )
    }

    /**
     * Calls loteService.getOrCreate() through the Spring proxy (REQUIRES_NEW).
     * On unique constraint violation from concurrent inserts, retries once — the
     * second call finds the row committed by the winning thread.
     * After obtaining the lote, re-attaches it to the current EntityManager via
     * getReferenceById so the Pagamento FK resolves correctly.
     */
    fun resolveOrCreateLote(date: LocalDate): Lote {
        val lote = try {
            loteService.getOrCreate(date)
        } catch (e: DataIntegrityViolationException) {
            loteService.getOrCreate(date)
        }
        return loteRepository.getReferenceById(lote.id!!)
    }
}
