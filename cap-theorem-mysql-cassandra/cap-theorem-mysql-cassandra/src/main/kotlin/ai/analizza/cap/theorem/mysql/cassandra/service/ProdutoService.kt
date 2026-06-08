package ai.analizza.cap.theorem.mysql.cassandra.service

import ai.analizza.cap.theorem.mysql.cassandra.dto.ProdutoRequest
import ai.analizza.cap.theorem.mysql.cassandra.entity.Produto
import ai.analizza.cap.theorem.mysql.cassandra.repository.ProdutoRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service

@Service
class ProdutoService(private val repository: ProdutoRepository) {

    fun findAll(): List<Produto> = repository.findAll()

    fun findById(id: Int): Produto =
        repository.findById(id).orElseThrow { EntityNotFoundException("Produto $id not found") }

    fun create(req: ProdutoRequest): Produto {
        require(req.taxaJuros >= java.math.BigDecimal.ZERO) { "taxaJuros must be >= 0" }
        return repository.save(Produto(nome = req.nome, categoria = req.categoria, taxaJuros = req.taxaJuros, ativo = req.ativo))
    }

    fun update(id: Int, req: ProdutoRequest): Produto {
        require(req.taxaJuros >= java.math.BigDecimal.ZERO) { "taxaJuros must be >= 0" }
        val entity = findById(id)
        entity.nome = req.nome
        entity.categoria = req.categoria
        entity.taxaJuros = req.taxaJuros
        entity.ativo = req.ativo
        return repository.save(entity)
    }

    fun delete(id: Int) {
        val entity = findById(id)
        repository.delete(entity)
    }
}
