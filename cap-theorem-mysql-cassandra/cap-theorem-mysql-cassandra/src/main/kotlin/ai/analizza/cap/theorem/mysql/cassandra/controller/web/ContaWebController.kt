package ai.analizza.cap.theorem.mysql.cassandra.controller.web

import ai.analizza.cap.theorem.mysql.cassandra.entity.StatusConta
import ai.analizza.cap.theorem.mysql.cassandra.entity.TipoConta
import ai.analizza.cap.theorem.mysql.cassandra.service.ClienteService
import ai.analizza.cap.theorem.mysql.cassandra.service.ContaService
import ai.analizza.cap.theorem.mysql.cassandra.web.ContaForm
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/contas")
class ContaWebController(
    private val service: ContaService,
    private val clienteService: ClienteService,
) {

    private fun selects(model: Model) {
        model.addAttribute("clientes", clienteService.findAll())
        model.addAttribute("tiposConta", TipoConta.entries)
        model.addAttribute("statusList", StatusConta.entries)
    }

    private fun listView(model: Model, req: HttpServletRequest): String {
        model.addAttribute("contas", service.findAll())
        return Htmx.view("conta/list", "content", Htmx.isHtmx(req))
    }

    private fun formView(model: Model, req: HttpServletRequest, titulo: String, url: String): String {
        model.addAttribute("titulo", titulo)
        model.addAttribute("hxUrl", url)
        selects(model)
        return Htmx.view("conta/form", "form", Htmx.isHtmx(req))
    }

    @GetMapping
    fun list(model: Model, req: HttpServletRequest) = listView(model, req)

    @GetMapping("/new")
    fun new(model: Model, req: HttpServletRequest): String {
        model.addAttribute("form", ContaForm())
        return formView(model, req, "Nova Conta", "/contas")
    }

    @PostMapping
    fun create(
        @Valid @ModelAttribute("form") form: ContaForm, br: BindingResult,
        model: Model, req: HttpServletRequest,
    ): String {
        if (br.hasErrors()) return formView(model, req, "Nova Conta", "/contas")
        service.create(form.toRequest())
        return listView(model, req)
    }

    @GetMapping("/{id}")
    fun detail(@PathVariable id: Int, model: Model, req: HttpServletRequest): String {
        model.addAttribute("conta", service.findById(id))
        return Htmx.view("conta/detail", "content", Htmx.isHtmx(req))
    }

    @GetMapping("/{id}/edit")
    fun edit(@PathVariable id: Int, model: Model, req: HttpServletRequest): String {
        model.addAttribute("form", ContaForm.from(service.findById(id)))
        return formView(model, req, "Editar Conta", "/contas/$id")
    }

    @PostMapping("/{id}")
    fun update(
        @PathVariable id: Int, @Valid @ModelAttribute("form") form: ContaForm, br: BindingResult,
        model: Model, req: HttpServletRequest,
    ): String {
        if (br.hasErrors()) return formView(model, req, "Editar Conta", "/contas/$id")
        service.update(id, form.toRequest())
        return listView(model, req)
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Int, model: Model, req: HttpServletRequest): String {
        service.delete(id)
        return listView(model, req)
    }
}
