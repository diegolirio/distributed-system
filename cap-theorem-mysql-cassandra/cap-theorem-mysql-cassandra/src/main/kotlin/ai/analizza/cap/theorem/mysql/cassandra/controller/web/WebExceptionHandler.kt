package ai.analizza.cap.theorem.mysql.cassandra.controller.web

import jakarta.persistence.EntityNotFoundException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.propertyeditors.StringTrimmerEditor
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.ui.Model
import org.springframework.web.bind.WebDataBinder
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.InitBinder

/**
 * Renders errors for the server-rendered UI as HTML (fragment for HTMX requests,
 * full page otherwise). Restricted to the web controllers so the existing
 * RestControllerAdvice keeps serving JSON for the REST API endpoints.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice(basePackages = ["ai.analizza.cap.theorem.mysql.cassandra.controller.web"])
class WebExceptionHandler {

    /** Treat blank text inputs (e.g. optional CNPJ / contratação) as null. */
    @InitBinder
    fun initBinder(binder: WebDataBinder) {
        binder.registerCustomEditor(String::class.java, StringTrimmerEditor(true))
    }

    @ExceptionHandler(EntityNotFoundException::class)
    fun handleNotFound(
        ex: EntityNotFoundException, model: Model,
        request: HttpServletRequest, response: HttpServletResponse,
    ): String = render(model, request, response, HttpStatus.NOT_FOUND, "Registro não encontrado.")

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleConflict(
        ex: DataIntegrityViolationException, model: Model,
        request: HttpServletRequest, response: HttpServletResponse,
    ): String = render(
        model, request, response, HttpStatus.CONFLICT,
        "Não foi possível concluir: valor duplicado ou existem registros vinculados a este item.",
    )

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleBadRequest(
        ex: IllegalArgumentException, model: Model,
        request: HttpServletRequest, response: HttpServletResponse,
    ): String = render(model, request, response, HttpStatus.BAD_REQUEST, ex.message ?: "Requisição inválida.")

    private fun render(
        model: Model, request: HttpServletRequest, response: HttpServletResponse,
        status: HttpStatus, mensagem: String,
    ): String {
        model.addAttribute("mensagem", mensagem)
        return if (Htmx.isHtmx(request)) {
            // Keep 200 so HTMX performs the swap, and redirect it to the alerts region.
            response.status = HttpStatus.OK.value()
            response.setHeader("HX-Retarget", "#alerts")
            response.setHeader("HX-Reswap", "innerHTML")
            "fragments/alerts :: alert"
        } else {
            response.status = status.value()
            "fragments/alerts :: page"
        }
    }
}
