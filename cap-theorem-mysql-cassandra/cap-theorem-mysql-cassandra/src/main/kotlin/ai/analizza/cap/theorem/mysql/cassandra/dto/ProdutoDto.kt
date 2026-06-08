package ai.analizza.cap.theorem.mysql.cassandra.dto

import ai.analizza.cap.theorem.mysql.cassandra.entity.CategoriaProduto
import ai.analizza.cap.theorem.mysql.cassandra.entity.Produto
import java.math.BigDecimal

data class ProdutoRequest(
    val nome: String,
    val categoria: CategoriaProduto,
    val taxaJuros: BigDecimal,
    val ativo: Boolean = true
)

data class ProdutoResponse(
    val idProduto: Int,
    val nome: String,
    val categoria: CategoriaProduto,
    val taxaJuros: BigDecimal,
    val ativo: Boolean
) {
    companion object {
        fun from(e: Produto) = ProdutoResponse(e.idProduto, e.nome, e.categoria, e.taxaJuros, e.ativo)
    }
}
