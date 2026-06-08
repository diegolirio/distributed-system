package ai.analizza.cap.theorem.mysql.cassandra.service

import ai.analizza.cap.theorem.mysql.cassandra.dto.ContaRequest
import ai.analizza.cap.theorem.mysql.cassandra.entity.Conta
import ai.analizza.cap.theorem.mysql.cassandra.repository.ClienteRepository
import ai.analizza.cap.theorem.mysql.cassandra.repository.ContaRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service

@Service
class ContaService(
    private val repository: ContaRepository,
    private val clienteRepository: ClienteRepository
) {

    fun findAll(): List<Conta> = repository.findAll()

    fun findById(id: Int): Conta =
        repository.findById(id).orElseThrow { EntityNotFoundException("Conta $id not found") }

    fun create(req: ContaRequest): Conta {
        val cliente = clienteRepository.findById(req.idCliente)
            .orElseThrow { EntityNotFoundException("Cliente ${req.idCliente} not found") }
        return repository.save(
            Conta(cliente = cliente, numero = req.numero, tipoConta = req.tipoConta, saldo = req.saldo, status = req.status)
        )
    }

    fun update(id: Int, req: ContaRequest): Conta {
        val entity = findById(id)
        val cliente = clienteRepository.findById(req.idCliente)
            .orElseThrow { EntityNotFoundException("Cliente ${req.idCliente} not found") }
        entity.cliente = cliente
        entity.numero = req.numero
        entity.tipoConta = req.tipoConta
        entity.saldo = req.saldo
        entity.status = req.status
        return repository.save(entity)
    }

    fun delete(id: Int) {
        val entity = findById(id)
        repository.delete(entity)
    }
}
