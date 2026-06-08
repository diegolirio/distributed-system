package ai.analizza.cap.theorem.mysql.cassandra.controller

import ai.analizza.cap.theorem.mysql.cassandra.dto.ProdutoRequest
import ai.analizza.cap.theorem.mysql.cassandra.dto.ProdutoResponse
import ai.analizza.cap.theorem.mysql.cassandra.service.ProdutoService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/produtos")
class ProdutoController(private val service: ProdutoService) {

    @GetMapping
    fun list() = service.findAll().map(ProdutoResponse::from)

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Int) = ProdutoResponse.from(service.findById(id))

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody req: ProdutoRequest) = ProdutoResponse.from(service.create(req))

    @PutMapping("/{id}")
    fun update(@PathVariable id: Int, @RequestBody req: ProdutoRequest) =
        ProdutoResponse.from(service.update(id, req))

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Int) = service.delete(id)
}
