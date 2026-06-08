package ai.analizza.cap.theorem.mysql.cassandra.repository

import ai.analizza.cap.theorem.mysql.cassandra.entity.Conta
import org.springframework.data.jpa.repository.JpaRepository

interface ContaRepository : JpaRepository<Conta, Int>
