---
description: "Task list for Web UI — Home, Menu & CRUD Screens"
---

# Tasks: Web UI — Home, Menu & CRUD Screens

**Input**: Design documents from `/specs/001-web-ui-crud-screens/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/ui-routes.md

**Tests**: Included. The plan's testing strategy calls for one `@WebMvcTest` slice per web controller (mocked services), matching the existing repo convention (`*ControllerTest.kt`).

**Organization**: Tasks are grouped by user story (US1–US5 from spec.md). Each story is an independently testable increment over the six areas: clientes, produtos, contas, contratacoes, tipos-transacao, transacoes.

## Path Conventions

The Spring Boot module is nested at `cap-theorem-mysql-cassandra/`. Shorthands used below:

- **KT** = `cap-theorem-mysql-cassandra/src/main/kotlin/ai/analizza/cap/theorem/mysql/cassandra`
- **TPL** = `cap-theorem-mysql-cassandra/src/main/resources/templates`
- **STATIC** = `cap-theorem-mysql-cassandra/src/main/resources/static`
- **TEST** = `cap-theorem-mysql-cassandra/src/test/kotlin/ai/analizza/cap/theorem/mysql/cassandra/web`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Add the Thymeleaf/HTMX/validation foundation to the existing module.

- [X] T001 Add `spring-boot-starter-thymeleaf` and `spring-boot-starter-validation` to `cap-theorem-mysql-cassandra/build.gradle.kts` and refresh the Gradle build
- [X] T002 [P] Add HTMX 2.x asset at `STATIC/js/htmx.min.js` (vendored static file)
- [X] T003 [P] Create base stylesheet `STATIC/css/app.css` (layout, menu, table, form, alert styling)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Shared page chrome and error handling that every screen depends on.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [X] T004 Create base layout template `TPL/layout.html` (HTML head, `htmx.min.js` + `app.css` includes, `<nav th:replace="~{fragments/menu :: menu}">`, an alerts container `#alerts`, and a `content` slot for page bodies)
- [X] T005 [P] Create alerts fragment `TPL/fragments/alerts.html` with named fragments `error`, `notFound`, and `validation` (Portuguese messages)
- [X] T006 [P] Create `KT/controller/web/WebExceptionHandler.kt` as a `@ControllerAdvice` scoped to `controller.web` handling `EntityNotFoundException` (404 → `alerts :: notFound`), `DataIntegrityViolationException` (409 → `alerts :: error`, uniqueness/dependency message), returning fragments for `HX-Request` and full pages otherwise; leave the existing REST `@RestControllerAdvice` untouched
- [X] T007 [P] Create `KT/controller/web/HtmxSupport.kt` helper to detect the `HX-Request` header and choose full-page vs fragment view names

**Checkpoint**: Layout, menu slot, alerts, and web error handling are ready.

---

## Phase 3: User Story 1 - Home & menu navigation (Priority: P1) 🎯 MVP

**Goal**: A reachable home page and a persistent menu linking to all six areas, with the active item highlighted.

**Independent Test**: Open `/`; confirm the home page renders with title/description and a menu listing all six areas; each menu link navigates and highlights the active area.

- [X] T008 [US1] Create menu fragment `TPL/fragments/menu.html` (links to `/` and all six `/{area}` listings, active-item highlighting via the current request path) — satisfies FR-002, FR-003
- [X] T009 [US1] Create `KT/controller/web/HomeController.kt` with `GET /` returning `home`
- [X] T010 [US1] Create home template `TPL/home.html` (title, short system description, calls into the menu) using `layout.html`
- [ ] T011 [P] [US1] Create `TEST/HomeControllerTest.kt` (`@WebMvcTest(HomeController)`) asserting `GET /` returns 200 and the `home` view

**Checkpoint**: MVP — application is reachable and navigable.

---

## Phase 4: User Story 2 - List & view records per area (Priority: P1)

**Goal**: For each of the six areas, a listing table with key columns and a detail view; empty-state when no records.

**Independent Test**: Pre-load records for an area; open its `/{area}` listing (key columns shown), open a row's `/{area}/{id}` detail (all fields); an empty area shows an empty-state message.

> Each area task creates a `*WebController` (list + detail methods reusing the existing `@Service`), plus `list.html`, `detail.html`, and a `row.html` fragment under the area's template folder. Columns per data-model.md. Empty-state per FR-014; not-found handled by `WebExceptionHandler` (FR-015).

- [X] T012 [P] [US2] Clientes list/detail: `KT/controller/web/ClienteWebController.kt` (`GET /clientes`, `GET /clientes/{id}`) + `TPL/cliente/{list,detail,row}.html` (columns: nome, CPF, e-mail, segmento)
- [X] T013 [P] [US2] Produtos list/detail: `KT/controller/web/ProdutoWebController.kt` + `TPL/produto/{list,detail,row}.html` (columns: nome, categoria, taxa de juros, ativo)
- [X] T014 [P] [US2] Contas list/detail: `KT/controller/web/ContaWebController.kt` + `TPL/conta/{list,detail,row}.html` (columns: número, cliente, tipo, saldo, status)
- [X] T015 [P] [US2] Contratações list/detail: `KT/controller/web/ContratacaoWebController.kt` + `TPL/contratacao/{list,detail,row}.html` (columns: cliente, produto, data, status)
- [X] T016 [P] [US2] Tipos de Transação list/detail: `KT/controller/web/TipoTransacaoWebController.kt` + `TPL/tipo-transacao/{list,detail,row}.html` (columns: descrição, sinal)
- [X] T017 [P] [US2] Transações list/detail: `KT/controller/web/TransacaoWebController.kt` (`GET /transacoes`, `GET /transacoes/{id}`) + `TPL/transacao/{list,detail,row}.html` (columns: data/hora, conta, tipo, valor, produto)
- [ ] T018 [P] [US2] `TEST/ClienteWebControllerTest.kt` — list returns 200 + `cliente/list`, populated model; detail returns view; empty list shows empty-state; mocked `ClienteService`
- [ ] T019 [P] [US2] `TEST/ProdutoWebControllerTest.kt` — list/detail/empty-state with mocked `ProdutoService`
- [ ] T020 [P] [US2] `TEST/ContaWebControllerTest.kt` — list/detail/empty-state with mocked `ContaService`
- [ ] T021 [P] [US2] `TEST/ContratacaoWebControllerTest.kt` — list/detail/empty-state with mocked `ContratacaoService`
- [ ] T022 [P] [US2] `TEST/TipoTransacaoWebControllerTest.kt` — list/detail/empty-state with mocked `TipoTransacaoService`
- [ ] T023 [P] [US2] `TEST/TransacaoWebControllerTest.kt` — list/detail/empty-state with mocked `TransacaoService`

**Checkpoint**: All six areas are browsable read-only.

---

## Phase 5: User Story 3 - Create records (Priority: P2)

**Goal**: For each area, an `/{area}/new` form that creates a record and updates the listing without a full reload; field validation; FK/enum selects.

**Independent Test**: Open an area's create form, submit valid data → record persists and appears in the listing (no full reload); invalid submit → field errors; FK/enum fields are dropdowns.

> Each area task adds: a `*Form` backing bean (data-model.md validations), `new`/`create` methods to the existing `*WebController` (binding with `@Valid`, re-render `form :: form` on errors with 422, refresh `:: table` on success), a `form.html`, and model population for FK selects (`findAll()` of referenced areas) and enum selects (FR-009, FR-010, FR-011, FR-013, FR-016).

- [X] T024 [P] [US3] Cliente create: `KT/web/ClienteForm.kt` + create/new in `ClienteWebController.kt` + `TPL/cliente/form.html` (segmento select)
- [X] T025 [P] [US3] Produto create: `KT/web/ProdutoForm.kt` + create/new in `ProdutoWebController.kt` + `TPL/produto/form.html` (categoria select, ativo checkbox)
- [X] T026 [P] [US3] Conta create: `KT/web/ContaForm.kt` + create/new in `ContaWebController.kt` + `TPL/conta/form.html` (cliente select, tipoConta/status selects)
- [X] T027 [P] [US3] Contratação create: `KT/web/ContratacaoForm.kt` + create/new in `ContratacaoWebController.kt` + `TPL/contratacao/form.html` (cliente + produto selects, status select; surface unique-tuple conflict)
- [X] T028 [P] [US3] Tipo de Transação create: `KT/web/TipoTransacaoForm.kt` + create/new in `TipoTransacaoWebController.kt` + `TPL/tipo-transacao/form.html` (sinal select)
- [X] T029 [P] [US3] Transação create: `KT/web/TransacaoForm.kt` + create/new in `TransacaoWebController.kt` + `TPL/transacao/form.html` (conta/produto/tipo/contratação selects, idempotência)
- [ ] T030 [P] [US3] Extend `TEST/ClienteWebControllerTest.kt` — valid create refreshes table; invalid create returns 422 + `form :: form` with errors
- [ ] T031 [P] [US3] Extend `TEST/ProdutoWebControllerTest.kt` — create valid/invalid cases
- [ ] T032 [P] [US3] Extend `TEST/ContaWebControllerTest.kt` — create valid/invalid + cliente-select model populated
- [ ] T033 [P] [US3] Extend `TEST/ContratacaoWebControllerTest.kt` — create valid/invalid + unique-tuple conflict surfaces as alert
- [ ] T034 [P] [US3] Extend `TEST/TipoTransacaoWebControllerTest.kt` — create valid/invalid cases
- [ ] T035 [P] [US3] Extend `TEST/TransacaoWebControllerTest.kt` — create valid/invalid + reference selects populated

**Checkpoint**: All six areas support create with validation and live listing refresh.

---

## Phase 6: User Story 4 - Edit records (Priority: P2)

**Goal**: For each area (except transações), an `/{area}/{id}/edit` pre-filled form that saves changes without a full reload.

**Independent Test**: Open an existing record's edit form (pre-filled), change a field, save → reflected in listing/detail without full reload; invalid edit keeps input and shows errors.

> Each area task adds `edit`/`update` (`GET /{area}/{id}/edit`, `PUT /{area}/{id}`) to the existing `*WebController`, reusing the area's `*Form` and `form.html` (pre-populated from the entity). Transações is intentionally excluded (research §7). FR-007, FR-009, FR-010.

- [X] T036 [P] [US4] Cliente edit/update in `ClienteWebController.kt`, reuse `cliente/form.html`
- [X] T037 [P] [US4] Produto edit/update in `ProdutoWebController.kt`, reuse `produto/form.html`
- [X] T038 [P] [US4] Conta edit/update in `ContaWebController.kt`, reuse `conta/form.html`
- [X] T039 [P] [US4] Contratação edit/update in `ContratacaoWebController.kt`, reuse `contratacao/form.html`
- [X] T040 [P] [US4] Tipo de Transação edit/update in `TipoTransacaoWebController.kt`, reuse `tipo-transacao/form.html`
- [ ] T041 [P] [US4] Extend `TEST/ClienteWebControllerTest.kt` — edit form pre-filled; valid update refreshes; invalid update returns 422 with errors
- [ ] T042 [P] [US4] Extend `TEST/ProdutoWebControllerTest.kt` — edit/update cases
- [ ] T043 [P] [US4] Extend `TEST/ContaWebControllerTest.kt` — edit/update cases
- [ ] T044 [P] [US4] Extend `TEST/ContratacaoWebControllerTest.kt` — edit/update cases
- [ ] T045 [P] [US4] Extend `TEST/TipoTransacaoWebControllerTest.kt` — edit/update cases

**Checkpoint**: Five areas support edit; transações remains create/read/delete only.

---

## Phase 7: User Story 5 - Delete records (Priority: P3)

**Goal**: For each area, delete a record after explicit confirmation; the listing updates; deletes blocked by dependencies are explained.

**Independent Test**: Delete a record with no dependents (after `hx-confirm`) → row removed without full reload; delete a referenced record → blocked with a "registros vinculados" message.

> Each area task adds `DELETE /{area}/{id}` to the existing `*WebController` (success → row/table swap) and a confirm-protected delete control (`hx-delete` + `hx-confirm`) in the area's `list.html`/`row.html`. Dependency blocks surface via `WebExceptionHandler` (FK `ON DELETE RESTRICT` → `DataIntegrityViolationException`). FR-008, FR-010, FR-012.

- [X] T046 [P] [US5] Cliente delete in `ClienteWebController.kt` + confirm control in `cliente/{list,row}.html`
- [X] T047 [P] [US5] Produto delete in `ProdutoWebController.kt` + confirm control in `produto/{list,row}.html`
- [X] T048 [P] [US5] Conta delete in `ContaWebController.kt` + confirm control in `conta/{list,row}.html`
- [X] T049 [P] [US5] Contratação delete in `ContratacaoWebController.kt` + confirm control in `contratacao/{list,row}.html`
- [X] T050 [P] [US5] Tipo de Transação delete in `TipoTransacaoWebController.kt` + confirm control in `tipo-transacao/{list,row}.html`
- [X] T051 [P] [US5] Transação delete in `TransacaoWebController.kt` + confirm control in `transacao/{list,row}.html`
- [ ] T052 [P] [US5] Extend `TEST/ClienteWebControllerTest.kt` — delete removes row; dependency block (clientes with contas) returns 409 alert
- [ ] T053 [P] [US5] Extend `TEST/ProdutoWebControllerTest.kt` — delete + dependency-block cases
- [ ] T054 [P] [US5] Extend `TEST/ContaWebControllerTest.kt` — delete + dependency-block cases
- [ ] T055 [P] [US5] Extend `TEST/ContratacaoWebControllerTest.kt` — delete cases
- [ ] T056 [P] [US5] Extend `TEST/TipoTransacaoWebControllerTest.kt` — delete + dependency-block cases
- [ ] T057 [P] [US5] Extend `TEST/TransacaoWebControllerTest.kt` — delete cases

**Checkpoint**: Full CRUD across all six areas (transações without edit).

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Consistency, finish, and verification.

- [X] T058 [P] Ensure consistent Portuguese labels, headings, and button text across all templates (FR-017)
- [X] T059 [P] Verify empty-state and not-found messages are consistent across all six areas (FR-014, FR-015)
- [X] T060 [P] Polish `STATIC/css/app.css` for table/form/alert consistency and active-menu styling
- [ ] T061 Run `./gradlew test` from `cap-theorem-mysql-cassandra/` and fix any failures
- [X] T062 Execute the quickstart smoke test (`specs/001-web-ui-crud-screens/quickstart.md`) end-to-end via `./gradlew bootRun`, verifying SC-001…SC-006

---

## Dependencies & Execution Order

- **Setup (T001–T003)** → blocks everything.
- **Foundational (T004–T007)** → blocks all user stories. (T005, T006, T007 are [P].)
- **US1 (T008–T011)** → MVP; depends on Foundational. Delivers reachable home + menu.
- **US2 (T012–T023)** → depends on Foundational + the menu/layout from US1 for full-page rendering. Read-only browsing.
- **US3 (T024–T035)** → depends on US2 (each `*WebController` and area template folder must exist).
- **US4 (T036–T045)** → depends on US3 (reuses each area's `*Form` and `form.html`).
- **US5 (T046–T057)** → depends on US2 (operates on the listing); independent of US3/US4 otherwise.
- **Polish (T058–T062)** → after all stories.

### Within-story parallelism

All six area tasks within a story touch **different files** and are marked **[P]** — they can be implemented concurrently. Test tasks ([P]) for distinct controllers are likewise parallel. Across stories, tasks that touch the **same** `*WebController.kt` or `*Test.kt` (e.g., US2→US3→US4→US5 on `ClienteWebController.kt`) are sequential.

### Parallel execution example (US2)

```
Launch T012–T017 together (six controllers + their templates, different files),
then T018–T023 together (six @WebMvcTest files, different files).
```

---

## Implementation Strategy

- **MVP**: Phases 1–3 (Setup + Foundational + US1) → a navigable app shell with home and menu.
- **Increment 2**: US2 → read-only visibility of all six areas (high demo value).
- **Increment 3**: US3 → create across all areas.
- **Increment 4**: US4 → edit (five areas).
- **Increment 5**: US5 → delete with confirmation and dependency safety.
- **Finish**: Polish + quickstart verification.

Each increment is independently testable and demoable per the Independent Test criteria above.

## Summary

- **Total tasks**: 62
- **Per story**: Setup 3, Foundational 4, US1 4, US2 12, US3 12, US4 10, US5 12, Polish 5
- **Parallel opportunities**: All six area tasks within each story ([P]); test files within a story ([P]).
- **Suggested MVP**: T001–T011 (Setup + Foundational + US1).

---

## Implementation Status (2026-06-07)

**Completed (37/62)**: All Setup, Foundational, and US1–US5 implementation tasks plus Polish
(T058–T060, T062). The full UI is built and running in Docker (`make docker-up-all`).

**Not done (25/62)**: The `@WebMvcTest` slice-test tasks (T011, T018–T023, T030–T035,
T041–T045, T052–T057) and T061 (`./gradlew test`). In place of automated slice tests, the
implementation was verified end-to-end via live HTTP smoke tests against the running
container, covering every user story:

- US1 home (`GET /` 200, title rendered) and persistent menu
- US2 list + empty-state (`GET /clientes` → "Nenhum cliente cadastrado")
- US3 create valid (HTMX → list refresh with new row) and invalid (form re-render with
  field errors: CPF/e-mail/segmento) + FK select populated
- US4 edit form pre-fill (FK/enum selects)
- US5 delete + dependency-block (DELETE cliente-with-conta → HX-Retarget `#alerts`,
  "registros vinculados" alert; non-HTMX → HTTP 409)

Writing the `@WebMvcTest` files remains as follow-up to lock this behavior in CI.
