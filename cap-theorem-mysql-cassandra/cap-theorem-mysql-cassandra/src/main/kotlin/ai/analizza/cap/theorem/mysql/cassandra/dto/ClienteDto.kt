package ai.analizza.cap.theorem.mysql.cassandra.dto

import ai.analizza.cap.theorem.mysql.cassandra.entity.Cliente
import ai.analizza.cap.theorem.mysql.cassandra.entity.SegmentoCliente
import java.time.LocalDateTime

data class ClienteRequest(
    val cpf: String,
    val cnpj: String?,
    val nome: String,
    val email: String,
    val segmento: SegmentoCliente
)

data class ClienteResponse(
    val idCliente: Int,
    val cpf: String,
    val cnpj: String?,
    val nome: String,
    val email: String,
    val segmento: SegmentoCliente,
    val criadoEm: LocalDateTime
) {
    companion object {
        fun from(e: Cliente) = ClienteResponse(e.idCliente, e.cpf, e.cnpj, e.nome, e.email, e.segmento, e.criadoEm)
    }
}
