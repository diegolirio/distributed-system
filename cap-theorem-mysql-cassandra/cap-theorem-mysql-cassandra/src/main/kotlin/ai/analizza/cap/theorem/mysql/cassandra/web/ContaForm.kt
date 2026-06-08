package ai.analizza.cap.theorem.mysql.cassandra.web

import ai.analizza.cap.theorem.mysql.cassandra.dto.ContaRequest
import ai.analizza.cap.theorem.mysql.cassandra.entity.Conta
import ai.analizza.cap.theorem.mysql.cassandra.entity.StatusConta
import ai.analizza.cap.theorem.mysql.cassandra.entity.TipoConta
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.math.BigDecimal

class ContaForm(
    @field:NotNull(message = "Selecione o cliente")
    var idCliente: Int? = null,

    @field:NotBlank(message = "Informe o número da conta")
    @field:Size(max = 20)
    var numero: String = "",

    @field:NotNull(message = "Selecione o tipo de conta")
    var tipoConta: TipoConta? = null,

    @field:NotNull(message = "Informe o saldo")
    var saldo: BigDecimal? = null,

    @field:NotNull(message = "Selecione o status")
    var status: StatusConta? = null,
) {
    fun toRequest() = ContaRequest(
        idCliente = idCliente!!,
        numero = numero.trim(),
        tipoConta = tipoConta!!,
        saldo = saldo!!,
        status = status!!,
    )

    companion object {
        fun from(c: Conta) = ContaForm(c.cliente.idCliente, c.numero, c.tipoConta, c.saldo, c.status)
    }
}
