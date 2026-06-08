package ai.analizza.cap.theorem.mysql.cassandra.controller.web

import ai.analizza.cap.theorem.mysql.cassandra.entity.StatusContratacao
import ai.analizza.cap.theorem.mysql.cassandra.service.ClienteService
import ai.analizza.cap.theorem.mysql.cassandra.service.ContratacaoService
import ai.analizza.cap.theorem.mysql.cassandra.service.ProdutoService
import ai.analizza.cap.theorem.mysql.cassandra.web.ContratacaoForm
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/contratacoes")
class ContratacaoWebController(
    private val service: ContratacaoService,
    private val clienteService: ClienteService,
    private val produtoService: ProdutoService,
) {

    private fun selects(model: Model) {
        model.addAttribute("clientes", clienteService.findAll())
        model.addAttribute("produtos", produtoService.findAll())
        model.addAttribute("statusList", StatusContratacao.entries)
    }

    private fun listView(model: Model, req: HttpServletRequest): String {
        model.addAttribute("contratacoes", service.findAll())
        return Htmx.view("contratacao/list", "content", Htmx.isHtmx(req))
    }

    private fun formView(model: Model, req: HttpServletRequest, titulo: String, url: String): String {
        model.addAttribute("titulo", titulo)
        model.addAttribute("hxUrl", url)
        selects(model)
        return Htmx.view("contratacao/form", "form", Htmx.isHtmx(req))
    }

    @GetMapping
    fun list(model: Model, req: HttpServletRequest) = listView(model, req)

    @GetMapping("/new")
    fun new(model: Model, req: HttpServletRequest): String {
        model.addAttribute("form", ContratacaoForm())
        return formView(model, req, "Nova Contratação", "/contratacoes")
    }

    @PostMapping
    fun create(
        @Valid @ModelAttribute("form") form: ContratacaoForm, br: BindingResult,
        model: Model, req: HttpServletRequest,
    ): String {
        if (br.hasErrors()) return formView(model, req, "Nova Contratação", "/contratacoes")
        service.create(form.toRequest())
        return listView(model, req)
    }

    @GetMapping("/{id}")
    fun detail(@PathVariable id: Int, model: Model, req: HttpServletRequest): String {
        model.addAttribute("contratacao", service.findById(id))
        return Htmx.view("contratacao/detail", "content", Htmx.isHtmx(req))
    }

    @GetMapping("/{id}/edit")
    fun edit(@PathVariable id: Int, model: Model, req: HttpServletRequest): String {
        model.addAttribute("form", ContratacaoForm.from(service.findById(id)))
        return formView(model, req, "Editar Contratação", "/contratacoes/$id")
    }

    @PostMapping("/{id}")
    fun update(
        @PathVariable id: Int, @Valid @ModelAttribute("form") form: ContratacaoForm, br: BindingResult,
        model: Model, req: HttpServletRequest,
    ): String {
        if (br.hasErrors()) return formView(model, req, "Editar Contratação", "/contratacoes/$id")
        service.update(id, form.toRequest())
        return listView(model, req)
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Int, model: Model, req: HttpServletRequest): String {
        service.delete(id)
        return listView(model, req)
    }
}
