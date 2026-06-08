package ai.analizza.cap.theorem.mysql.cassandra.controller

import ai.analizza.cap.theorem.mysql.cassandra.dto.ClienteRequest
import ai.analizza.cap.theorem.mysql.cassandra.dto.ClienteResponse
import ai.analizza.cap.theorem.mysql.cassandra.service.ClienteService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/clientes")
class ClienteController(private val service: ClienteService) {

    @GetMapping
    fun list() = service.findAll().map(ClienteResponse::from)

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Int) = ClienteResponse.from(service.findById(id))

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody req: ClienteRequest) = ClienteResponse.from(service.create(req))

    @PutMapping("/{id}")
    fun update(@PathVariable id: Int, @RequestBody req: ClienteRequest) =
        ClienteResponse.from(service.update(id, req))

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Int) = service.delete(id)
}
