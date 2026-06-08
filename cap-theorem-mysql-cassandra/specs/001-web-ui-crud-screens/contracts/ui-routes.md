# UI Route Contract: Web Screens

The UI exposes server-rendered HTML routes (distinct from the existing JSON `/api/**` routes). Each route is served by a Spring MVC `@Controller`. Responses depend on the `HX-Request` header:

- **Normal navigation** (no `HX-Request`) → full page = `layout.html` + the area fragment.
- **HTMX request** (`HX-Request: true`) → only the relevant fragment (`view :: fragment`), swapped into the target.

`{area}` is one of: `clientes`, `produtos`, `contas`, `contratacoes`, `tipos-transacao`, `transacoes`.
`{id}` is the integer/long primary key.

## Global

| Method | Path | Purpose | Success | Fragment / View |
|--------|------|---------|---------|-----------------|
| GET | `/` | Home page with intro + menu | 200 | `home.html` |

The persistent menu (`fragments/menu.html`) is included by `layout.html` on every page and links to `/` and each `/{area}` listing, marking the active item (FR-002, FR-003).

## Per-area CRUD (generic contract, applied to all six areas)

| Method | Path | Purpose | Success | On error |
|--------|------|---------|---------|----------|
| GET | `/{area}` | List records (table) | 200 → `list` (full or `:: table`) | — |
| GET | `/{area}/new` | Empty create form | 200 → `form :: form` | — |
| POST | `/{area}` | Create record | 200/201 → updated `:: table` (HX) or redirect to `/{area}` | 422 → `form :: form` with `th:errors`; 409 → alert fragment |
| GET | `/{area}/{id}` | View detail | 200 → `detail` | 404 → not-found fragment |
| GET | `/{area}/{id}/edit` | Pre-filled edit form | 200 → `form :: form` | 404 → not-found fragment |
| PUT | `/{area}/{id}` | Update record | 200 → updated `:: row`/`:: table` | 422 → `form :: form` with errors; 404; 409 |
| DELETE | `/{area}/{id}` | Delete record | 200 → removed row swap / updated `:: table` | 404; 409 → "registros vinculados" alert |

Notes:
- HTMX `PUT`/`DELETE` are emitted via `hx-put` / `hx-delete`; Spring receives them directly (no `_method` override needed since HTMX issues real verbs).
- Delete is preceded by a client-side `hx-confirm` prompt (FR-008).
- Create/update success responses re-render the listing region (or the affected row) so the screen updates without a full reload (FR-010, SC-004).

## Area-specific deviations

| Area | Deviation |
|------|-----------|
| `transacoes` | `GET /{area}/{id}/edit` and `PUT /{area}/{id}` are **NOT** implemented (transactions are immutable; research §7). List, detail, create, delete only. |

## HTMX response-header conventions

| Situation | Header(s) |
|-----------|-----------|
| Validation error on submit | HTTP 422 + `form :: form` body (HTMX swaps form with errors) |
| Conflict / dependency block | `HX-Retarget` → alert container, `HX-Reswap: innerHTML`, body = `fragments/alerts :: error` |
| Not found | `HX-Retarget` → alert container, body = `fragments/alerts :: notFound` |
| Successful create/delete | optional `HX-Trigger` (e.g., `recordSaved`) to refresh counters |

## Selection-control data (FR-011, FR-016)

Controllers add to the model, for forms that need them:
- `clientes` (id + nome + cpf) — for Conta, Contratação, used as `<select>` options.
- `produtos`, `tiposTransacao`, `contas`, `contratacoes` — likewise for Transação and Contratação forms.
- Enum value lists — from each enum's `values()` for `segmento`, `categoria`, `tipoConta`, `status` selects.
