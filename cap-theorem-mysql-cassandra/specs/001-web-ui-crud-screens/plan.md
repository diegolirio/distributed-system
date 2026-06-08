# Implementation Plan: Web UI — Home, Menu & CRUD Screens

**Branch**: `001-web-ui-crud-screens` | **Date**: 2026-06-07 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/001-web-ui-crud-screens/spec.md`

## Summary

Add a server-rendered, operator-facing web UI on top of the existing `cap-theorem-mysql-cassandra` Spring Boot application: a home page, a persistent navigation menu, and full CRUD screens for each of the six domain areas (Tipos de Transação, Clientes, Produtos, Contas, Contratações, Transações). The UI is built with **Spring MVC `@Controller` + Thymeleaf templates + HTMX** for partial (no-full-reload) updates. It reuses the existing `@Service` layer unchanged; no new business entities or persistence are introduced. A web-specific `@ControllerAdvice` renders validation, conflict, dependency-block, and not-found feedback as HTML fragments so the operator never sees a raw error page.

## Technical Context

**Language/Version**: Kotlin 2.3.0, JVM toolchain 25  
**Primary Dependencies**: Spring Boot 4.0.6 (spring-boot-starter-web, -data-jpa, -actuator, -flyway), **adding**: spring-boot-starter-thymeleaf, spring-boot-starter-validation; HTMX 2.x (served as a static asset / webjar)  
**Storage**: MySQL 8 (existing `fiapdb` schema, 6 tables, Flyway-managed); no schema changes  
**Testing**: JUnit 5 + Kotlin test, `@WebMvcTest` for web controllers (slice tests with mocked services), Testcontainers MySQL for existing integration tests  
**Target Platform**: Linux server; accessed via modern desktop web browser  
**Project Type**: Web application (single Spring Boot module serving both REST `/api/**` and server-rendered UI)  
**Performance Goals**: Demo-scale; partial-render responses are small HTML fragments; no specific throughput target  
**Constraints**: UI language Brazilian Portuguese; no authentication in scope; reuse existing service layer without modification; do not alter existing REST controllers  
**Scale/Scope**: 6 areas × ~4 screen states (list, detail, form, row) ≈ 24 templates/fragments + 7 controllers (1 home + 6 area) + form-backing beans + 1 web advice

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

The project constitution (`.specify/memory/constitution.md`) is an **unratified template** (placeholder tokens only). No enforceable principles are defined, so there are no gates to violate. Default engineering guardrails applied voluntarily:

- **Reuse over rewrite**: web controllers consume the existing `@Service` beans; no duplication of business logic. ✅
- **Separation of concerns**: web (`controller/web`) kept distinct from REST (`controller`); web exception handling kept separate from the REST `@RestControllerAdvice`. ✅
- **No scope creep**: no auth, no new tables, no changes to `/api/**`. ✅

**Result**: PASS (no constitution gates defined).

## Project Structure

### Documentation (this feature)

```text
specs/001-web-ui-crud-screens/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
│   └── ui-routes.md     # Web route ↔ template/fragment contract
└── tasks.md             # Phase 2 output (/speckit-tasks — NOT created here)
```

### Source Code (repository root)

The application lives in the nested module `cap-theorem-mysql-cassandra/` (Gradle root). All new code is added inside that module's existing package `ai.analizza.cap.theorem.mysql.cassandra`; nothing is moved.

```text
cap-theorem-mysql-cassandra/
├── build.gradle.kts                         # + thymeleaf, + validation starters; + htmx webjar
└── src/
    ├── main/
    │   ├── kotlin/ai/analizza/cap/theorem/mysql/cassandra/
    │   │   ├── controller/                  # EXISTING REST controllers (untouched)
    │   │   │   └── web/                      # NEW @Controller (server-rendered)
    │   │   │       ├── HomeController.kt
    │   │   │       ├── ClienteWebController.kt
    │   │   │       ├── ProdutoWebController.kt
    │   │   │       ├── ContaWebController.kt
    │   │   │       ├── ContratacaoWebController.kt
    │   │   │       ├── TipoTransacaoWebController.kt
    │   │   │       ├── TransacaoWebController.kt
    │   │   │       └── WebExceptionHandler.kt  # @ControllerAdvice → HTML fragments
    │   │   ├── web/                          # NEW form-backing beans + validation
    │   │   │   ├── ClienteForm.kt
    │   │   │   ├── ProdutoForm.kt
    │   │   │   ├── ContaForm.kt
    │   │   │   ├── ContratacaoForm.kt
    │   │   │   ├── TipoTransacaoForm.kt
    │   │   │   └── TransacaoForm.kt
    │   │   ├── service/                      # EXISTING (reused, unchanged)
    │   │   ├── repository/                   # EXISTING (reused; may add count/exists helpers)
    │   │   ├── entity/                       # EXISTING
    │   │   └── dto/                          # EXISTING
    │   └── resources/
    │       ├── templates/
    │       │   ├── layout.html              # base layout + persistent menu fragment
    │       │   ├── fragments/
    │       │   │   ├── menu.html
    │       │   │   └── alerts.html          # error/validation/conflict fragments
    │       │   ├── home.html
    │       │   ├── cliente/                 # list.html, detail.html, form.html, row.html
    │       │   ├── produto/
    │       │   ├── conta/
    │       │   ├── contratacao/
    │       │   ├── tipo-transacao/
    │       │   └── transacao/
    │       └── static/
    │           ├── css/app.css
    │           └── js/htmx.min.js           # (or webjar)
    └── test/
        └── kotlin/ai/analizza/cap/theorem/mysql/cassandra/
            └── web/                         # NEW @WebMvcTest per web controller
                ├── HomeControllerTest.kt
                ├── ClienteWebControllerTest.kt
                └── ... (one per area)
```

**Structure Decision**: Single Spring Boot module (Project Type: web application). The server-rendered UI is added alongside the existing REST API in the same deployable, under a dedicated `controller/web` package and a `web` package for form beans, keeping a clean line between the JSON API and the HTML UI. Templates and static assets use Spring Boot's conventional `resources/templates` and `resources/static` locations.

## Complexity Tracking

> No constitution violations to justify (constitution is an unratified placeholder). Table intentionally omitted.
