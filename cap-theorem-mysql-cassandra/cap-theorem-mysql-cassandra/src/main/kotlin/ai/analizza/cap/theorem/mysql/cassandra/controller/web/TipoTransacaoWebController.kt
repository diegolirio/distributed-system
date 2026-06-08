package ai.analizza.cap.theorem.mysql.cassandra.controller.web

import ai.analizza.cap.theorem.mysql.cassandra.service.TipoTransacaoService
import ai.analizza.cap.theorem.mysql.cassandra.web.TipoTransacaoForm
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/tipos-transacao")
class TipoTransacaoWebController(private val service: TipoTransacaoService) {

    private fun listView(model: Model, req: HttpServletRequest): String {
        model.addAttribute("tipos", service.findAll())
        return Htmx.view("tipo-transacao/list", "content", Htmx.isHtmx(req))
    }

    private fun formView(model: Model, req: HttpServletRequest, titulo: String, url: String): String {
        model.addAttribute("titulo", titulo)
        model.addAttribute("hxUrl", url)
        return Htmx.view("tipo-transacao/form", "form", Htmx.isHtmx(req))
    }

    @GetMapping
    fun list(model: Model, req: HttpServletRequest) = listView(model, req)

    @GetMapping("/new")
    fun new(model: Model, req: HttpServletRequest): String {
        model.addAttribute("form", TipoTransacaoForm())
        return formView(model, req, "Novo Tipo de Transação", "/tipos-transacao")
    }

    @PostMapping
    fun create(
        @Valid @ModelAttribute("form") form: TipoTransacaoForm, br: BindingResult,
        model: Model, req: HttpServletRequest,
    ): String {
        if (br.hasErrors()) return formView(model, req, "Novo Tipo de Transação", "/tipos-transacao")
        service.create(form.toRequest())
        return listView(model, req)
    }

    @GetMapping("/{id}")
    fun detail(@PathVariable id: Int, model: Model, req: HttpServletRequest): String {
        model.addAttribute("tipo", service.findById(id))
        return Htmx.view("tipo-transacao/detail", "content", Htmx.isHtmx(req))
    }

    @GetMapping("/{id}/edit")
    fun edit(@PathVariable id: Int, model: Model, req: HttpServletRequest): String {
        model.addAttribute("form", TipoTransacaoForm.from(service.findById(id)))
        return formView(model, req, "Editar Tipo de Transação", "/tipos-transacao/$id")
    }

    @PostMapping("/{id}")
    fun update(
        @PathVariable id: Int, @Valid @ModelAttribute("form") form: TipoTransacaoForm, br: BindingResult,
        model: Model, req: HttpServletRequest,
    ): String {
        if (br.hasErrors()) return formView(model, req, "Editar Tipo de Transação", "/tipos-transacao/$id")
        service.update(id, form.toRequest())
        return listView(model, req)
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Int, model: Model, req: HttpServletRequest): String {
        service.delete(id)
        return listView(model, req)
    }
}
