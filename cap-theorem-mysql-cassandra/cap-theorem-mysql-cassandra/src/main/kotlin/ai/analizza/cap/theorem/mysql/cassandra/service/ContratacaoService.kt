package ai.analizza.cap.theorem.mysql.cassandra.service

import ai.analizza.cap.theorem.mysql.cassandra.dto.ContratacaoRequest
import ai.analizza.cap.theorem.mysql.cassandra.entity.Contratacao
import ai.analizza.cap.theorem.mysql.cassandra.repository.ClienteRepository
import ai.analizza.cap.theorem.mysql.cassandra.repository.ContratacaoRepository
import ai.analizza.cap.theorem.mysql.cassandra.repository.ProdutoRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service

@Service
class ContratacaoService(
    private val repository: ContratacaoRepository,
    private val clienteRepository: ClienteRepository,
    private val produtoRepository: ProdutoRepository
) {

    fun findAll(): List<Contratacao> = repository.findAll()

    fun findById(id: Int): Contratacao =
        repository.findById(id).orElseThrow { EntityNotFoundException("Contratacao $id not found") }

    fun create(req: ContratacaoRequest): Contratacao {
        val cliente = clienteRepository.findById(req.idCliente)
            .orElseThrow { EntityNotFoundException("Cliente ${req.idCliente} not found") }
        val produto = produtoRepository.findById(req.idProduto)
            .orElseThrow { EntityNotFoundException("Produto ${req.idProduto} not found") }
        return repository.save(
            Contratacao(cliente = cliente, produto = produto, dataContratacao = req.dataContratacao, status = req.status)
        )
    }

    fun update(id: Int, req: ContratacaoRequest): Contratacao {
        val entity = findById(id)
        val cliente = clienteRepository.findById(req.idCliente)
            .orElseThrow { EntityNotFoundException("Cliente ${req.idCliente} not found") }
        val produto = produtoRepository.findById(req.idProduto)
            .orElseThrow { EntityNotFoundException("Produto ${req.idProduto} not found") }
        entity.cliente = cliente
        entity.produto = produto
        entity.dataContratacao = req.dataContratacao
        entity.status = req.status
        return repository.save(entity)
    }

    fun delete(id: Int) {
        val entity = findById(id)
        repository.delete(entity)
    }
}
