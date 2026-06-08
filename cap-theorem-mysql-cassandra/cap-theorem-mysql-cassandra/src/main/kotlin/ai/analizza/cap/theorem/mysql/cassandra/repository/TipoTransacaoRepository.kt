package ai.analizza.cap.theorem.mysql.cassandra.repository

import ai.analizza.cap.theorem.mysql.cassandra.entity.TipoTransacao
import org.springframework.data.jpa.repository.JpaRepository

interface TipoTransacaoRepository : JpaRepository<TipoTransacao, Int>
