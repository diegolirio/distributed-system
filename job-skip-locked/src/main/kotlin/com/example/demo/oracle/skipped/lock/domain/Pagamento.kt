package com.example.demo.oracle.skipped.lock.domain

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "pagamentos")
class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(name = "proposta_id", nullable = false)
    var propostaId: String = ""

    @Column(name = "valor", nullable = false)
    var valor: BigDecimal = BigDecimal.ZERO

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id", nullable = false)
    lateinit var lote: Lote

    @Column(name = "request_id", nullable = false)
    var requestId: Long = 0L

    @Column(name = "created_at")
    var createdAt: LocalDateTime = LocalDateTime.now()
}
