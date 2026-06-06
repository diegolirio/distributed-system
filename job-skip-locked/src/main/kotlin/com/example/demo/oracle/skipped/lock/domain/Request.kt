package com.example.demo.oracle.skipped.lock.domain

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "requests")
class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(name = "proposta_id")
    var propostaId: String? = null

    @Column(name = "valor", nullable = false)
    var valor: BigDecimal = BigDecimal.ZERO

    @Column(name = "certificate")
    var certificate: String? = null

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: RequestStatus = RequestStatus.PENDING

    @Column(name = "error_message")
    var errorMessage: String? = null

    @Column(name = "created_at")
    var createdAt: LocalDateTime = LocalDateTime.now()
}
