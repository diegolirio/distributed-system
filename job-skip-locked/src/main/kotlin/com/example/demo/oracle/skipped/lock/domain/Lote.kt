package com.example.demo.oracle.skipped.lock.domain

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "lotes")
class Lote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(name = "data_lote", nullable = false, unique = true)
    var dataLote: LocalDate = LocalDate.now()

    @Column(name = "created_at")
    var createdAt: LocalDateTime = LocalDateTime.now()
}
