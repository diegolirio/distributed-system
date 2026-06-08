package ai.analizza.cap.theorem.mysql.cassandra.controller.web

import ai.analizza.cap.theorem.mysql.cassandra.service.*
import ai.analizza.cap.theorem.mysql.cassandra.web.TransacaoForm
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*

/**
 * Transações são imutáveis (ledger/idempotência): expõe lista, detalhe, criação e
 * exclusão — sem edição de um lançamento já efetuado.
 */
@Controller
@RequestMapping("/transacoes")
class TransacaoWebController(
    private val service: TransacaoService,
    private val contaService: ContaService,
    private val produtoService: ProdutoService,
    private val tipoTransacaoService: TipoTransacaoService,
    private val contratacaoService: ContratacaoService,
) {

    private fun selects(model: Model) {
        model.addAttribute("contas", contaService.findAll())
        model.addAttribute("produtos", produtoService.findAll())
        model.addAttribute("tipos", tipoTransacaoService.findAll())
        model.addAttribute("contratacoes", contratacaoService.findAll())
    }

    private fun listView(model: Model, req: HttpServletRequest): String {
        model.addAttribute("transacoes", service.findAll())
        return Htmx.view("transacao/list", "content", Htmx.isHtmx(req))
    }

    @GetMapping
    fun list(model: Model, req: HttpServletRequest) = listView(model, req)

    @GetMapping("/new")
    fun new(model: Model, req: HttpServletRequest): String {
        model.addAttribute("form", TransacaoForm())
        model.addAttribute("titulo", "Nova Transação")
        model.addAttribute("hxUrl", "/transacoes")
        selects(model)
        return Htmx.view("transacao/form", "form", Htmx.isHtmx(req))
    }

    @PostMapping
    fun create(
        @Valid @ModelAttribute("form") form: TransacaoForm, br: BindingResult,
        model: Model, req: HttpServletRequest,
    ): String {
        if (br.hasErrors()) {
            model.addAttribute("titulo", "Nova Transação")
            model.addAttribute("hxUrl", "/transacoes")
            selects(model)
            return Htmx.view("transacao/form", "form", Htmx.isHtmx(req))
        }
        service.create(form.toRequest())
        return listView(model, req)
    }

    @GetMapping("/{id}")
    fun detail(@PathVariable id: Long, model: Model, req: HttpServletRequest): String {
        model.addAttribute("transacao", service.findById(id))
        return Htmx.view("transacao/detail", "content", Htmx.isHtmx(req))
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long, model: Model, req: HttpServletRequest): String {
        service.delete(id)
        return listView(model, req)
    }
}
