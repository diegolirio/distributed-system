package ai.analizza.cap.theorem.mysql.cassandra.controller.web

import jakarta.servlet.http.HttpServletRequest

/**
 * Helpers to drive partial (HTMX) vs full-page rendering.
 *
 * Direct navigation (menu links, refresh) renders the full page; HTMX-initiated
 * requests render only the relevant Thymeleaf fragment so the targeted region is
 * swapped without a full reload.
 */
object Htmx {
    fun isHtmx(request: HttpServletRequest): Boolean = request.getHeader("HX-Request") != null

    /** Returns the full view name, or `view :: fragment` when the request is HTMX-driven. */
    fun view(view: String, fragment: String, htmx: Boolean): String =
        if (htmx) "$view :: $fragment" else view
}
