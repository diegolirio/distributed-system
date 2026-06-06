package com.example.demo.oracle.skipped.lock.service

import com.example.demo.oracle.skipped.lock.domain.Lote
import com.example.demo.oracle.skipped.lock.repository.LoteRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class LoteService(private val loteRepository: LoteRepository) {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun getOrCreate(date: LocalDate): Lote {
        return loteRepository.findByDataLote(date)
            ?: loteRepository.save(Lote().apply { dataLote = date })
    }
}
