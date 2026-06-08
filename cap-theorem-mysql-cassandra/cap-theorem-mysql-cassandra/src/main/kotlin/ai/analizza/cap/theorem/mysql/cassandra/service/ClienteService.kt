package ai.analizza.cap.theorem.mysql.cassandra.service

import ai.analizza.cap.theorem.mysql.cassandra.dto.ClienteRequest
import ai.analizza.cap.theorem.mysql.cassandra.entity.Cliente
import ai.analizza.cap.theorem.mysql.cassandra.repository.ClienteRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service

@Service
class ClienteService(private val repository: ClienteRepository) {

    fun findAll(): List<Cliente> = repository.findAll()

    fun findById(id: Int): Cliente =
        repository.findById(id).orElseThrow { EntityNotFoundException("Cliente $id not found") }

    fun create(req: ClienteRequest): Cliente = repository.save(
        Cliente(cpf = req.cpf, cnpj = req.cnpj, nome = req.nome, email = req.email, segmento = req.segmento)
    )

    fun update(id: Int, req: ClienteRequest): Cliente {
        val entity = findById(id)
        entity.cpf = req.cpf
        entity.cnpj = req.cnpj
        entity.nome = req.nome
        entity.email = req.email
        entity.segmento = req.segmento
        return repository.save(entity)
    }

    fun delete(id: Int) {
        val entity = findById(id)
        repository.delete(entity)
    }
}
