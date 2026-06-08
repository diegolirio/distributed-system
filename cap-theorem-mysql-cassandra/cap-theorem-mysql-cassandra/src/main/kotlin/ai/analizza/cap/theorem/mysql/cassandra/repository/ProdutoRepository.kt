package ai.analizza.cap.theorem.mysql.cassandra.repository

import ai.analizza.cap.theorem.mysql.cassandra.entity.Produto
import org.springframework.data.jpa.repository.JpaRepository

interface ProdutoRepository : JpaRepository<Produto, Int>
