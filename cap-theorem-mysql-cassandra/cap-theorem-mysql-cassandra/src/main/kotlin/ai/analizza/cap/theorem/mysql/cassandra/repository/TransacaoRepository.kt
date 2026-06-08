package ai.analizza.cap.theorem.mysql.cassandra.repository

import ai.analizza.cap.theorem.mysql.cassandra.entity.Transacao
import org.springframework.data.jpa.repository.JpaRepository

interface TransacaoRepository : JpaRepository<Transacao, Long>
