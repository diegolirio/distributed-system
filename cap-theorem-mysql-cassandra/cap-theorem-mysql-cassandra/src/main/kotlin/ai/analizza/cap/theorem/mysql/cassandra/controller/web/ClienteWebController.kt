package ai.analizza.cap.theorem.mysql.cassandra.controller.web

import ai.analizza.cap.theorem.mysql.cassandra.entity.SegmentoCliente
import ai.analizza.cap.theorem.mysql.cassandra.service.ClienteService
import ai.analizza.cap.theorem.mysql.cassandra.web.ClienteForm
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/clientes")
class ClienteWebController(private val service: ClienteService) {

    private fun selects(model: Model) {
        model.addAttribute("segmentos", SegmentoCliente.entries)
    }

    private fun listView(model: Model, req: HttpServletRequest): String {
        model.addAttribute("clientes", service.findAll())
        return Htmx.view("cliente/list", "content", Htmx.isHtmx(req))
    }

    private fun formView(model: Model, req: HttpServletRequest, titulo: String, url: String): String {
        model.addAttribute("titulo", titulo)
        model.addAttribute("hxUrl", url)
        selects(model)
        return Htmx.view("cliente/form", "form", Htmx.isHtmx(req))
    }

    @GetMapping
    fun list(model: Model, req: HttpServletRequest) = listView(model, req)

    @GetMapping("/new")
    fun new(model: Model, req: HttpServletRequest): String {
        model.addAttribute("form", ClienteForm())
        return formView(model, req, "Novo Cliente", "/clientes")
    }

    @PostMapping
    fun create(
        @Valid @ModelAttribute("form") form: ClienteForm, br: BindingResult,
        model: Model, req: HttpServletRequest,
    ): String {
        if (br.hasErrors()) return formView(model, req, "Novo Cliente", "/clientes")
        service.create(form.toRequest())
        return listView(model, req)
    }

    @GetMapping("/{id}")
    fun detail(@PathVariable id: Int, model: Model, req: HttpServletRequest): String {
        model.addAttribute("cliente", service.findById(id))
        return Htmx.view("cliente/detail", "content", Htmx.isHtmx(req))
    }

    @GetMapping("/{id}/edit")
    fun edit(@PathVariable id: Int, model: Model, req: HttpServletRequest): String {
        model.addAttribute("form", ClienteForm.from(service.findById(id)))
        return formView(model, req, "Editar Cliente", "/clientes/$id")
    }

    @PostMapping("/{id}")
    fun update(
        @PathVariable id: Int, @Valid @ModelAttribute("form") form: ClienteForm, br: BindingResult,
        model: Model, req: HttpServletRequest,
    ): String {
        if (br.hasErrors()) return formView(model, req, "Editar Cliente", "/clientes/$id")
        service.update(id, form.toRequest())
        return listView(model, req)
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Int, model: Model, req: HttpServletRequest): String {
        service.delete(id)
        return listView(model, req)
    }
}
