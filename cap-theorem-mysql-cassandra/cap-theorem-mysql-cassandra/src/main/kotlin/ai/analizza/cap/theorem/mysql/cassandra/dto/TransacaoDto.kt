package ai.analizza.cap.theorem.mysql.cassandra.dto

import ai.analizza.cap.theorem.mysql.cassandra.entity.Transacao
import java.math.BigDecimal
import java.time.LocalDateTime

data class TransacaoRequest(
    val idConta: Int,
    val idProduto: Int,
    val idTipo: Int,
    val idContratacao: Int?,
    val valor: BigDecimal,
    val idIdempotencia: String
)

data class TransacaoResponse(
    val idTransacao: Long,
    val idConta: Int,
    val idProduto: Int,
    val idTipo: Int,
    val idContratacao: Int?,
    val valor: BigDecimal,
    val dataHora: LocalDateTime,
    val idIdempotencia: String
) {
    companion object {
        fun from(e: Transacao) = TransacaoResponse(
            e.idTransacao,
            e.conta.idConta,
            e.produto.idProduto,
            e.tipoTransacao.idTipo,
            e.contratacao?.idContratacao,
            e.valor,
            e.dataHora,
            e.idIdempotencia
        )
    }
}
