# Quickstart: Web UI — Home, Menu & CRUD Screens

How to build, run, and exercise the new server-rendered UI. Assumes the existing `cap-theorem-mysql-cassandra` module and MySQL `fiapdb` are in place.

## Prerequisites

- JDK 25 (project uses `jvmToolchain(25)`)
- MySQL 8 reachable at `localhost:3306`, database `fiapdb`, user `fiap`/`fiap123` (see `application.yml`)
  - Start via the repo compose file if present, otherwise an existing instance.
- Flyway migrations `V1`–`V6` applied (run automatically on boot; `ddl-auto: validate`)

## 1. Add dependencies

In `cap-theorem-mysql-cassandra/build.gradle.kts`:

```kotlin
implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
implementation("org.springframework.boot:spring-boot-starter-validation")
// HTMX: either a static file in resources/static/js/htmx.min.js
// or the webjar:
// implementation("org.webjars.npm:htmx.org:2.0.4")
```

## 2. Run the application

```bash
cd cap-theorem-mysql-cassandra
./gradlew bootRun
```

Then open <http://localhost:8080/> in a browser.

## 3. Smoke test the user stories

| Story | Steps | Expected |
|-------|-------|----------|
| US1 — Home & menu | Open `/` | Home page with title, description, and a menu listing all 6 areas; active item highlighted when you navigate |
| US2 — List & view | Click "Clientes" → open a row | Table of clientes with key columns; detail shows all fields; empty area shows empty-state message |
| US3 — Create | Clientes → "Novo" → fill form → salvar | New cliente appears in the list without full reload; invalid form shows field errors; FK/enum fields are dropdowns |
| US4 — Edit | Open a cliente → "Editar" → change nome → salvar | Form pre-filled; change reflected in list without full reload |
| US5 — Delete | Cliente with no contas → "Excluir" → confirmar | Row removed; deleting a cliente that has contas is blocked with a "registros vinculados" message |

Repeat US2–US5 for produtos, contas, contratações, tipos-transacao. For transações, exercise list/detail/create/delete (no edit — by design).

## 4. Run tests

```bash
cd cap-theorem-mysql-cassandra
./gradlew test
```

- New `@WebMvcTest` slice tests cover each web controller (view/fragment selection, validation re-render, HTMX header handling) with mocked services.
- Existing Testcontainers integration tests continue to cover REST + persistence.

## 5. Verify error handling (SC-005)

- Submit a duplicate CPF/e-mail → conflict alert, no stack trace page.
- Open `/clientes/999999` (nonexistent) → "não encontrado" message.
- Delete a referenced record → blocked with explanation.

## Out of scope (this feature)

- Authentication / authorization screens
- Pagination / filtering (demo-scale data)
- Editing posted transações
- Mobile-specific layout
