package ai.analizza.cap.theorem.mysql.cassandra.web

import ai.analizza.cap.theorem.mysql.cassandra.dto.ProdutoRequest
import ai.analizza.cap.theorem.mysql.cassandra.entity.CategoriaProduto
import ai.analizza.cap.theorem.mysql.cassandra.entity.Produto
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.math.BigDecimal

class ProdutoForm(
    @field:NotBlank(message = "Informe o nome")
    @field:Size(max = 120)
    var nome: String = "",

    @field:NotNull(message = "Selecione a categoria")
    var categoria: CategoriaProduto? = null,

    @field:NotNull(message = "Informe a taxa de juros")
    @field:DecimalMin(value = "0.0", message = "Taxa de juros não pode ser negativa")
    var taxaJuros: BigDecimal? = null,

    var ativo: Boolean = true,
) {
    fun toRequest() = ProdutoRequest(
        nome = nome.trim(),
        categoria = categoria!!,
        taxaJuros = taxaJuros!!,
        ativo = ativo,
    )

    companion object {
        fun from(p: Produto) = ProdutoForm(p.nome, p.categoria, p.taxaJuros, p.ativo)
    }
}
