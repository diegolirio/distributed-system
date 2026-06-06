package com.example.demo.oracle.skipped.lock.repository

import com.example.demo.oracle.skipped.lock.domain.Request
import com.example.demo.oracle.skipped.lock.domain.RequestStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional

interface RequestRepository : JpaRepository<Request, Long> {

    @Query(
        value = "SELECT * FROM requests WHERE status = 'PENDING' AND ROWNUM <= :batchSize FOR UPDATE SKIP LOCKED",
        nativeQuery = true
    )
    fun findForUpdateSkipLocked(@Param("batchSize") batchSize: Int): List<Request>

    @Modifying
    @Transactional
    @Query("UPDATE Request r SET r.status = :status, r.errorMessage = :errorMessage WHERE r.id = :id")
    fun updateStatus(
        @Param("id") id: Long,
        @Param("status") status: RequestStatus,
        @Param("errorMessage") errorMessage: String?
    )

    fun countByStatus(status: RequestStatus): Long
}
