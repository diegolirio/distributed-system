package ai.analizza.cap.theorem.mysql.cassandra.controller

import ai.analizza.cap.theorem.mysql.cassandra.dto.TipoTransacaoRequest
import ai.analizza.cap.theorem.mysql.cassandra.dto.TipoTransacaoResponse
import ai.analizza.cap.theorem.mysql.cassandra.service.TipoTransacaoService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/tipos-transacao")
class TipoTransacaoController(private val service: TipoTransacaoService) {

    @GetMapping
    fun list() = service.findAll().map(TipoTransacaoResponse::from)

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Int) = TipoTransacaoResponse.from(service.findById(id))

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody req: TipoTransacaoRequest) = TipoTransacaoResponse.from(service.create(req))

    @PutMapping("/{id}")
    fun update(@PathVariable id: Int, @RequestBody req: TipoTransacaoRequest) =
        TipoTransacaoResponse.from(service.update(id, req))

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Int) = service.delete(id)
}
