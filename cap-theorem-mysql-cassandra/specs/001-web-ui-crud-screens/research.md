# Phase 0 Research: Web UI — Home, Menu & CRUD Screens

All Technical Context items were resolvable from the existing codebase and stable framework conventions. No `NEEDS CLARIFICATION` remained. Decisions below.

## 1. Rendering approach — Spring MVC + Thymeleaf + HTMX

- **Decision**: Use Spring MVC `@Controller` methods returning Thymeleaf view names. For HTMX-initiated requests, return a **fragment** (`view :: fragment`) so only the targeted region is swapped; for direct navigation/refresh, return the full page (layout + fragment). Detect HTMX via the `HX-Request` request header.
- **Rationale**: Matches the requested stack exactly, keeps all rendering server-side (no SPA build pipeline), and reuses the existing service layer. HTMX gives the "no full page reload" behavior (FR-010, SC-004) with attributes (`hx-get/post/put/delete`, `hx-target`, `hx-swap`) and no custom JavaScript.
- **Alternatives considered**: Full SPA (React/Vue) — rejected: out of scope, adds a build toolchain and a separate API contract. Plain Thymeleaf with full-page POST-redirect-GET — rejected: violates the no-full-reload requirement.

## 2. Dependencies to add

- **Decision**: Add `org.springframework.boot:spring-boot-starter-thymeleaf` and `org.springframework.boot:spring-boot-starter-validation`. Serve HTMX as a static asset (`static/js/htmx.min.js`) or via the `org.webjars.npm:htmx.org` webjar.
- **Rationale**: Thymeleaf starter auto-configures the template engine and resolver against `classpath:/templates/`. Validation starter brings Jakarta Bean Validation (Hibernate Validator) for `@Valid` form binding (FR-009). HTMX is a single ~14KB JS file with no build step.
- **Alternatives considered**: Mustache/Freemarker — rejected: Thymeleaf was explicitly requested and integrates best with Spring form binding and `th:errors`. CDN-hosted HTMX — rejected: prefer a local asset so the demo works offline.

## 3. Form binding & validation

- **Decision**: Introduce dedicated `*Form` backing beans (one per area) with Jakarta validation annotations (`@NotBlank`, `@Size`, `@Email`, `@Pattern`, `@NotNull`, `@DecimalMin`, etc.) and String/nullable fields suited to HTML inputs. Controllers bind with `@Valid @ModelAttribute`, and on `BindingResult.hasErrors()` re-render the form fragment with `th:errors`. Map `*Form` → existing `*Request` DTO (or directly to the service) on success.
- **Rationale**: The existing `*Request` DTOs use non-null Kotlin types and enum types directly, which are awkward to bind from raw form input and to validate field-by-field. Separate form beans isolate UI concerns and let `th:field`/`th:errors` show field-level messages without losing input (FR-009, US3/US4 scenario 2).
- **Alternatives considered**: Binding straight onto `*Request` — rejected: enum/non-null fields produce binding exceptions instead of friendly field errors. Binding onto JPA entities — rejected: leaks persistence into the view and risks detached-entity issues.

## 4. Error / feedback handling for HTML + HTMX

- **Decision**: Add a separate `@ControllerAdvice` (NOT `@RestControllerAdvice`) scoped to the web controllers that handles `EntityNotFoundException` (→ "não encontrado" fragment, FR-015), `DataIntegrityViolationException` (→ uniqueness/dependency message, FR-012/FR-013), and binding errors. For HTMX requests it returns the alert fragment with an appropriate status (e.g., 422) and uses HTMX response headers (`HX-Retarget`, `HX-Reswap`) to place the message; for full-page requests it renders the page with an inline alert. The existing `@RestControllerAdvice` continues to serve `/api/**` and is left untouched.
- **Rationale**: The current `GlobalExceptionHandler` returns JSON `ProblemDetail`, which is wrong for an HTML UI. Two advices coexist cleanly because the web one is restricted (by `assignableTypes`/`basePackages` = `controller.web`) and the REST one targets the REST controllers. This satisfies "zero unhandled error pages" (SC-005).
- **Alternatives considered**: Reusing the REST advice — rejected: would emit JSON into HTML targets. Per-controller try/catch — rejected: duplicative across 7 controllers.

## 5. Dependency-block on delete (FR-012)

- **Decision**: Rely on database FK constraints (`ON DELETE RESTRICT` already defined for cliente←conta/contratacao, produto←contratacao/transacao, etc.). A blocked delete surfaces as `DataIntegrityViolationException`, which the web advice translates into a human-readable "não é possível excluir: existem registros vinculados" message. Optionally pre-check with repository `count`/`exists` to show a friendlier message before attempting.
- **Rationale**: The constraints already enforce referential integrity at the source of truth; the UI only needs to surface the failure (per Assumptions). Avoids re-implementing dependency rules in the UI.
- **Alternatives considered**: UI-side cascade delete — rejected: dangerous and contradicts the spec's "prevent and explain" requirement.

## 6. Reference selection controls (FR-011, FR-016)

- **Decision**: For FK fields, populate `<select>` options from the referenced service's `findAll()` in the controller (added to the model), showing a human-readable label (e.g., Cliente nome + CPF) with the id as the option value. For enum fields, populate `<select>` from the Kotlin enum's `values()`.
- **Rationale**: Directly satisfies FR-011 (pick referenced record, not raw id) and FR-016 (enum-constrained selects). Reuses existing read services.
- **Alternatives considered**: Free-text id entry — rejected by FR-011. HTMX-driven async typeahead — deferred: nice-to-have, demo data volume is small (Assumptions).

## 7. Transação edit scope (open item from spec Assumptions)

- **Decision**: For the Transações area, implement list, detail, create, and delete-with-confirmation; **omit an edit form** for posted transactions (ledger/idempotency immutability). All other five areas get full CRUD.
- **Rationale**: Aligns with the spec assumption and the domain's idempotency key. Still satisfies "CRUD per table" intent (create/read/delete present). Flagged for confirmation; can be revisited in `/speckit-clarify` if the user wants editable transactions.
- **Alternatives considered**: Full edit of transação — possible but semantically questionable for a financial movement; left out by default.

## 8. Testing strategy

- **Decision**: One `@WebMvcTest` per web controller with the corresponding `@Service` mocked (`@MockkBean`/`@MockBean`), asserting status, view/fragment name, model attributes, and validation re-render. Keep the existing Testcontainers integration tests for the REST/persistence layer untouched.
- **Rationale**: `@WebMvcTest` is fast, isolates the MVC layer (binding, validation, view selection, HTMX header handling) without a database, matching the existing test style in the repo.
- **Alternatives considered**: Full `@SpringBootTest` per screen — rejected: slower, redundant with existing integration coverage.
