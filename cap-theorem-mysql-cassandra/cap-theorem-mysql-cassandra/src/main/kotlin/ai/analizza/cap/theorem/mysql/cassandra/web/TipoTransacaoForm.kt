package ai.analizza.cap.theorem.mysql.cassandra.web

import ai.analizza.cap.theorem.mysql.cassandra.dto.TipoTransacaoRequest
import ai.analizza.cap.theorem.mysql.cassandra.entity.TipoTransacao
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

class TipoTransacaoForm(
    @field:NotBlank(message = "Informe a descrição")
    @field:Size(max = 60)
    var descricao: String = "",

    @field:NotNull(message = "Selecione o sinal")
    var sinal: Int? = null,
) {
    fun toRequest() = TipoTransacaoRequest(
        descricao = descricao.trim(),
        sinal = sinal!!,
    )

    companion object {
        fun from(t: TipoTransacao) = TipoTransacaoForm(t.descricao, t.sinal)
    }
}
