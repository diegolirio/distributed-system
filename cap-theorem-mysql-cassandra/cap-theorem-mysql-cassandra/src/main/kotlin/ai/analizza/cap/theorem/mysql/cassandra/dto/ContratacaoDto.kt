package ai.analizza.cap.theorem.mysql.cassandra.dto

import ai.analizza.cap.theorem.mysql.cassandra.entity.Contratacao
import ai.analizza.cap.theorem.mysql.cassandra.entity.StatusContratacao
import java.time.LocalDate

data class ContratacaoRequest(
    val idCliente: Int,
    val idProduto: Int,
    val dataContratacao: LocalDate,
    val status: StatusContratacao = StatusContratacao.ATIVA
)

data class ContratacaoResponse(
    val idContratacao: Int,
    val idCliente: Int,
    val idProduto: Int,
    val dataContratacao: LocalDate,
    val status: StatusContratacao
) {
    companion object {
        fun from(e: Contratacao) = ContratacaoResponse(
            e.idContratacao, e.cliente.idCliente, e.produto.idProduto, e.dataContratacao, e.status
        )
    }
}
