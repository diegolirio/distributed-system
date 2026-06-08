package ai.analizza.cap.theorem.mysql.cassandra.controller

import ai.analizza.cap.theorem.mysql.cassandra.dto.ContaRequest
import ai.analizza.cap.theorem.mysql.cassandra.dto.ContaResponse
import ai.analizza.cap.theorem.mysql.cassandra.service.ContaService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/contas")
class ContaController(private val service: ContaService) {

    @GetMapping
    fun list() = service.findAll().map(ContaResponse::from)

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Int) = ContaResponse.from(service.findById(id))

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody req: ContaRequest) = ContaResponse.from(service.create(req))

    @PutMapping("/{id}")
    fun update(@PathVariable id: Int, @RequestBody req: ContaRequest) =
        ContaResponse.from(service.update(id, req))

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Int) = service.delete(id)
}
