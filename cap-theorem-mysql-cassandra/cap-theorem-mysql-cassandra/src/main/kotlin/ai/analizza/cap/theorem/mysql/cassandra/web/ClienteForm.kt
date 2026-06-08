package ai.analizza.cap.theorem.mysql.cassandra.web

import ai.analizza.cap.theorem.mysql.cassandra.dto.ClienteRequest
import ai.analizza.cap.theorem.mysql.cassandra.entity.Cliente
import ai.analizza.cap.theorem.mysql.cassandra.entity.SegmentoCliente
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

class ClienteForm(
    @field:NotBlank(message = "Informe o CPF")
    @field:Pattern(regexp = "\\d{11}", message = "CPF deve conter 11 dígitos")
    var cpf: String = "",

    @field:Pattern(regexp = "(\\d{14})?", message = "CNPJ deve conter 14 dígitos")
    var cnpj: String? = null,

    @field:NotBlank(message = "Informe o nome")
    @field:Size(max = 150)
    var nome: String = "",

    @field:NotBlank(message = "Informe o e-mail")
    @field:Email(message = "E-mail inválido")
    @field:Size(max = 150)
    var email: String = "",

    @field:NotNull(message = "Selecione o segmento")
    var segmento: SegmentoCliente? = null,
) {
    fun toRequest() = ClienteRequest(
        cpf = cpf.trim(),
        cnpj = cnpj?.takeIf { it.isNotBlank() },
        nome = nome.trim(),
        email = email.trim(),
        segmento = segmento!!,
    )

    companion object {
        fun from(c: Cliente) = ClienteForm(c.cpf, c.cnpj, c.nome, c.email, c.segmento)
    }
}
