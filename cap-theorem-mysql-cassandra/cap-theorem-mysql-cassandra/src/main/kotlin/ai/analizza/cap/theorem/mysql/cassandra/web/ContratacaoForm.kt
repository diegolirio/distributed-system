package ai.analizza.cap.theorem.mysql.cassandra.web

import ai.analizza.cap.theorem.mysql.cassandra.dto.ContratacaoRequest
import ai.analizza.cap.theorem.mysql.cassandra.entity.Contratacao
import ai.analizza.cap.theorem.mysql.cassandra.entity.StatusContratacao
import jakarta.validation.constraints.NotNull
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDate

class ContratacaoForm(
    @field:NotNull(message = "Selecione o cliente")
    var idCliente: Int? = null,

    @field:NotNull(message = "Selecione o produto")
    var idProduto: Int? = null,

    @field:NotNull(message = "Informe a data de contratação")
    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    var dataContratacao: LocalDate? = null,

    @field:NotNull(message = "Selecione o status")
    var status: StatusContratacao? = null,
) {
    fun toRequest() = ContratacaoRequest(
        idCliente = idCliente!!,
        idProduto = idProduto!!,
        dataContratacao = dataContratacao!!,
        status = status!!,
    )

    companion object {
        fun from(c: Contratacao) =
            ContratacaoForm(c.cliente.idCliente, c.produto.idProduto, c.dataContratacao, c.status)
    }
}
