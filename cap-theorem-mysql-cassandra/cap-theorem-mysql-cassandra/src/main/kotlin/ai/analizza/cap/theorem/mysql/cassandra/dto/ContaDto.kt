package ai.analizza.cap.theorem.mysql.cassandra.dto

import ai.analizza.cap.theorem.mysql.cassandra.entity.Conta
import ai.analizza.cap.theorem.mysql.cassandra.entity.StatusConta
import ai.analizza.cap.theorem.mysql.cassandra.entity.TipoConta
import java.math.BigDecimal

data class ContaRequest(
    val idCliente: Int,
    val numero: String,
    val tipoConta: TipoConta,
    val saldo: BigDecimal = BigDecimal.ZERO,
    val status: StatusConta = StatusConta.ATIVA
)

data class ContaResponse(
    val idConta: Int,
    val idCliente: Int,
    val numero: String,
    val tipoConta: TipoConta,
    val saldo: BigDecimal,
    val status: StatusConta
) {
    companion object {
        fun from(e: Conta) = ContaResponse(e.idConta, e.cliente.idCliente, e.numero, e.tipoConta, e.saldo, e.status)
    }
}
