package ai.analizza.cap.theorem.mysql.cassandra.controller

import ai.analizza.cap.theorem.mysql.cassandra.dto.ContratacaoRequest
import ai.analizza.cap.theorem.mysql.cassandra.dto.ContratacaoResponse
import ai.analizza.cap.theorem.mysql.cassandra.service.ContratacaoService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/contratacoes")
class ContratacaoController(private val service: ContratacaoService) {

    @GetMapping
    fun list() = service.findAll().map(ContratacaoResponse::from)

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Int) = ContratacaoResponse.from(service.findById(id))

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody req: ContratacaoRequest) = ContratacaoResponse.from(service.create(req))

    @PutMapping("/{id}")
    fun update(@PathVariable id: Int, @RequestBody req: ContratacaoRequest) =
        ContratacaoResponse.from(service.update(id, req))

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Int) = service.delete(id)
}
