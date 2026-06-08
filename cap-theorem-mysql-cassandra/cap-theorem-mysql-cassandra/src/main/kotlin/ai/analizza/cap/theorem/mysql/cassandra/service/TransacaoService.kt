package ai.analizza.cap.theorem.mysql.cassandra.service

import ai.analizza.cap.theorem.mysql.cassandra.dto.TransacaoRequest
import ai.analizza.cap.theorem.mysql.cassandra.entity.Transacao
import ai.analizza.cap.theorem.mysql.cassandra.repository.*
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class TransacaoService(
    private val repository: TransacaoRepository,
    private val contaRepository: ContaRepository,
    private val produtoRepository: ProdutoRepository,
    private val tipoTransacaoRepository: TipoTransacaoRepository,
    private val contratacaoRepository: ContratacaoRepository
) {

    fun findAll(): List<Transacao> = repository.findAll()

    fun findById(id: Long): Transacao =
        repository.findById(id).orElseThrow { EntityNotFoundException("Transacao $id not found") }

    fun create(req: TransacaoRequest): Transacao {
        require(req.valor > BigDecimal.ZERO) { "valor must be > 0" }
        val conta = contaRepository.findById(req.idConta)
            .orElseThrow { EntityNotFoundException("Conta ${req.idConta} not found") }
        val produto = produtoRepository.findById(req.idProduto)
            .orElseThrow { EntityNotFoundException("Produto ${req.idProduto} not found") }
        val tipo = tipoTransacaoRepository.findById(req.idTipo)
            .orElseThrow { EntityNotFoundException("TipoTransacao ${req.idTipo} not found") }
        val contratacao = req.idContratacao?.let {
            contratacaoRepository.findById(it).orElseThrow { EntityNotFoundException("Contratacao $it not found") }
        }
        return repository.save(
            Transacao(
                conta = conta,
                produto = produto,
                tipoTransacao = tipo,
                contratacao = contratacao,
                valor = req.valor,
                idIdempotencia = req.idIdempotencia
            )
        )
    }

    fun update(id: Long, req: TransacaoRequest): Transacao {
        require(req.valor > BigDecimal.ZERO) { "valor must be > 0" }
        val entity = findById(id)
        val conta = contaRepository.findById(req.idConta)
            .orElseThrow { EntityNotFoundException("Conta ${req.idConta} not found") }
        val produto = produtoRepository.findById(req.idProduto)
            .orElseThrow { EntityNotFoundException("Produto ${req.idProduto} not found") }
        val tipo = tipoTransacaoRepository.findById(req.idTipo)
            .orElseThrow { EntityNotFoundException("TipoTransacao ${req.idTipo} not found") }
        val contratacao = req.idContratacao?.let {
            contratacaoRepository.findById(it).orElseThrow { EntityNotFoundException("Contratacao $it not found") }
        }
        entity.conta = conta
        entity.produto = produto
        entity.tipoTransacao = tipo
        entity.contratacao = contratacao
        entity.valor = req.valor
        entity.idIdempotencia = req.idIdempotencia
        return repository.save(entity)
    }

    fun delete(id: Long) {
        val entity = findById(id)
        repository.delete(entity)
    }
}
