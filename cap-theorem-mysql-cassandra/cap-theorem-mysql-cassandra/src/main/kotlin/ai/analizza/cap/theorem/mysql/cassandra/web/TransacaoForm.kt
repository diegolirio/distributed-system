package ai.analizza.cap.theorem.mysql.cassandra.web

import ai.analizza.cap.theorem.mysql.cassandra.dto.TransacaoRequest
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.util.UUID

class TransacaoForm(
    @field:NotNull(message = "Selecione a conta")
    var idConta: Int? = null,

    @field:NotNull(message = "Selecione o produto")
    var idProduto: Int? = null,

    @field:NotNull(message = "Selecione o tipo de transação")
    var idTipo: Int? = null,

    var idContratacao: Int? = null,

    @field:NotNull(message = "Informe o valor")
    @field:DecimalMin(value = "0.0", inclusive = false, message = "Valor deve ser maior que zero")
    var valor: BigDecimal? = null,

    @field:NotBlank(message = "Informe o id de idempotência")
    @field:Size(max = 36)
    var idIdempotencia: String = UUID.randomUUID().toString(),
) {
    fun toRequest() = TransacaoRequest(
        idConta = idConta!!,
        idProduto = idProduto!!,
        idTipo = idTipo!!,
        idContratacao = idContratacao,
        valor = valor!!,
        idIdempotencia = idIdempotencia.trim(),
    )
}
