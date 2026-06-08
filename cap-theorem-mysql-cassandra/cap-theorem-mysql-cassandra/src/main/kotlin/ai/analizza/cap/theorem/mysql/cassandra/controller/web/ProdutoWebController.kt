package ai.analizza.cap.theorem.mysql.cassandra.controller.web

import ai.analizza.cap.theorem.mysql.cassandra.entity.CategoriaProduto
import ai.analizza.cap.theorem.mysql.cassandra.service.ProdutoService
import ai.analizza.cap.theorem.mysql.cassandra.web.ProdutoForm
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/produtos")
class ProdutoWebController(private val service: ProdutoService) {

    private fun selects(model: Model) {
        model.addAttribute("categorias", CategoriaProduto.entries)
    }

    private fun listView(model: Model, req: HttpServletRequest): String {
        model.addAttribute("produtos", service.findAll())
        return Htmx.view("produto/list", "content", Htmx.isHtmx(req))
    }

    private fun formView(model: Model, req: HttpServletRequest, titulo: String, url: String): String {
        model.addAttribute("titulo", titulo)
        model.addAttribute("hxUrl", url)
        selects(model)
        return Htmx.view("produto/form", "form", Htmx.isHtmx(req))
    }

    @GetMapping
    fun list(model: Model, req: HttpServletRequest) = listView(model, req)

    @GetMapping("/new")
    fun new(model: Model, req: HttpServletRequest): String {
        model.addAttribute("form", ProdutoForm())
        return formView(model, req, "Novo Produto", "/produtos")
    }

    @PostMapping
    fun create(
        @Valid @ModelAttribute("form") form: ProdutoForm, br: BindingResult,
        model: Model, req: HttpServletRequest,
    ): String {
        if (br.hasErrors()) return formView(model, req, "Novo Produto", "/produtos")
        service.create(form.toRequest())
        return listView(model, req)
    }

    @GetMapping("/{id}")
    fun detail(@PathVariable id: Int, model: Model, req: HttpServletRequest): String {
        model.addAttribute("produto", service.findById(id))
        return Htmx.view("produto/detail", "content", Htmx.isHtmx(req))
    }

    @GetMapping("/{id}/edit")
    fun edit(@PathVariable id: Int, model: Model, req: HttpServletRequest): String {
        model.addAttribute("form", ProdutoForm.from(service.findById(id)))
        return formView(model, req, "Editar Produto", "/produtos/$id")
    }

    @PostMapping("/{id}")
    fun update(
        @PathVariable id: Int, @Valid @ModelAttribute("form") form: ProdutoForm, br: BindingResult,
        model: Model, req: HttpServletRequest,
    ): String {
        if (br.hasErrors()) return formView(model, req, "Editar Produto", "/produtos/$id")
        service.update(id, form.toRequest())
        return listView(model, req)
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Int, model: Model, req: HttpServletRequest): String {
        service.delete(id)
        return listView(model, req)
    }
}
