package ai.analizza.cap.theorem.mysql.cassandra.service

import ai.analizza.cap.theorem.mysql.cassandra.dto.TipoTransacaoRequest
import ai.analizza.cap.theorem.mysql.cassandra.entity.TipoTransacao
import ai.analizza.cap.theorem.mysql.cassandra.repository.TipoTransacaoRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service

@Service
class TipoTransacaoService(private val repository: TipoTransacaoRepository) {

    fun findAll(): List<TipoTransacao> = repository.findAll()

    fun findById(id: Int): TipoTransacao =
        repository.findById(id).orElseThrow { EntityNotFoundException("TipoTransacao $id not found") }

    fun create(req: TipoTransacaoRequest): TipoTransacao {
        require(req.sinal == 1 || req.sinal == -1) { "sinal must be 1 or -1" }
        return repository.save(TipoTransacao(descricao = req.descricao, sinal = req.sinal))
    }

    fun update(id: Int, req: TipoTransacaoRequest): TipoTransacao {
        require(req.sinal == 1 || req.sinal == -1) { "sinal must be 1 or -1" }
        val entity = findById(id)
        entity.descricao = req.descricao
        entity.sinal = req.sinal
        return repository.save(entity)
    }

    fun delete(id: Int) {
        val entity = findById(id)
        repository.delete(entity)
    }
}
