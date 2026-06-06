package com.example.demo.oracle.skipped.lock.repository

import com.example.demo.oracle.skipped.lock.domain.Pagamento
import org.springframework.data.jpa.repository.JpaRepository

interface PagamentoRepository : JpaRepository<Pagamento, Long>
