package ai.analizza.cap.theorem.mysql.cassandra.dto

import ai.analizza.cap.theorem.mysql.cassandra.entity.TipoTransacao

data class TipoTransacaoRequest(val descricao: String, val sinal: Int)

data class TipoTransacaoResponse(val idTipo: Int, val descricao: String, val sinal: Int) {
    companion object {
        fun from(e: TipoTransacao) = TipoTransacaoResponse(e.idTipo, e.descricao, e.sinal)
    }
}
