package ai.analizza.cap.theorem.mysql.cassandra.repository

import ai.analizza.cap.theorem.mysql.cassandra.entity.Contratacao
import org.springframework.data.jpa.repository.JpaRepository

interface ContratacaoRepository : JpaRepository<Contratacao, Int>
