package com.example.demo.oracle.skipped.lock.repository

import com.example.demo.oracle.skipped.lock.domain.Lote
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional
import java.sql.Date
import java.time.LocalDate

interface LoteRepository : JpaRepository<Lote, Long> {

    fun findByDataLote(dataLote: LocalDate): Lote?

    @Modifying
    @Transactional
    @Query(
        value = """
            MERGE INTO lotes l
            USING (SELECT :dataLote AS data_lote FROM dual) src
            ON (l.data_lote = src.data_lote)
            WHEN NOT MATCHED THEN INSERT (data_lote, created_at) VALUES (:dataLote, CURRENT_TIMESTAMP)
        """,
        nativeQuery = true
    )
    fun mergeByDataLote(@Param("dataLote") dataLote: Date)
}
