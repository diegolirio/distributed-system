package ai.analizza.cap.theorem.mysql.cassandra.controller

import ai.analizza.cap.theorem.mysql.cassandra.dto.TransacaoRequest
import ai.analizza.cap.theorem.mysql.cassandra.dto.TransacaoResponse
import ai.analizza.cap.theorem.mysql.cassandra.service.TransacaoService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/transacoes")
class TransacaoController(private val service: TransacaoService) {

    @GetMapping
    fun list() = service.findAll().map(TransacaoResponse::from)

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long) = TransacaoResponse.from(service.findById(id))

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody req: TransacaoRequest) = TransacaoResponse.from(service.create(req))

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody req: TransacaoRequest) =
        TransacaoResponse.from(service.update(id, req))

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) = service.delete(id)
}
