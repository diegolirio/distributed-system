# Feature Specification: Web UI — Home, Menu & CRUD Screens

**Feature Branch**: `001-web-ui-crud-screens`  
**Created**: 2026-06-07  
**Status**: Draft  
**Input**: User description: "criar telas com MVC + Thymeleaf + HTMX setup da aplicacao cap-*, home, menu, CRUD de cada tabela"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Navigate the application from a home page and menu (Priority: P1)

An operator opens the application in a browser and lands on a home page that introduces the system and presents a navigation menu linking to every managed area (Tipos de Transação, Clientes, Produtos, Contas, Contratações, Transações). From any screen the operator can return home or jump to another area using the same persistent menu.

**Why this priority**: Without an entry point and navigation, none of the management areas are reachable. This is the smallest slice that makes the application usable as a web product and is a prerequisite for every other story.

**Independent Test**: Open the application root in a browser and confirm the home page renders, the menu lists all six areas, and each menu item navigates to the corresponding listing screen.

**Acceptance Scenarios**:

1. **Given** the operator opens the application root, **When** the page loads, **Then** a home page is shown with a title, a short description of the system, and a navigation menu listing all six management areas.
2. **Given** the operator is on any management screen, **When** they use the navigation menu, **Then** they are taken to the selected area without losing the menu.
3. **Given** the operator selects a menu item, **When** the target screen loads, **Then** the active area is visually indicated in the menu.

---

### User Story 2 - List and view records for each area (Priority: P1)

For each of the six areas, the operator can see a listing of existing records in a table, with the most relevant columns visible, and can open the details of any single record.

**Why this priority**: Reading existing data is the most common operation and delivers immediate value (visibility of the current state) even before create/edit/delete exist. It is independently demonstrable.

**Independent Test**: Seed or pre-load records for an area, open its listing screen, confirm the records appear in a table with key columns, and confirm a record's full details can be viewed.

**Acceptance Scenarios**:

1. **Given** records exist in an area, **When** the operator opens that area's listing screen, **Then** the records are displayed in a table showing the key columns for that area.
2. **Given** no records exist in an area, **When** the operator opens its listing screen, **Then** an empty-state message is shown instead of an empty table.
3. **Given** a record is shown in a listing, **When** the operator chooses to view it, **Then** the full set of that record's fields is displayed.

---

### User Story 3 - Create new records (Priority: P2)

For each area, the operator can open a form, fill in the required fields, submit it, and have the new record appear in the listing without a full page reload.

**Why this priority**: Creating data is essential for an operational tool, but it depends on listing (Story 2) already existing to confirm the result. It is the next increment of value after read-only browsing.

**Independent Test**: Open an area's create form, submit valid data, and confirm the new record is persisted and appears in the listing.

**Acceptance Scenarios**:

1. **Given** the operator is on an area's listing, **When** they choose to add a new record and submit a valid form, **Then** the record is saved and the listing updates to include it without a full page reload.
2. **Given** the operator submits a form with missing or invalid required fields, **When** the form is processed, **Then** the record is not saved and field-level validation messages are shown next to the offending fields.
3. **Given** an area references another area (for example a Conta belongs to a Cliente), **When** the operator opens the create form, **Then** the referenced record can be chosen from a selection control populated with existing options rather than typed by raw identifier.

---

### User Story 4 - Edit existing records (Priority: P2)

For each area, the operator can open an existing record in an edit form pre-filled with its current values, change fields, and save the changes.

**Why this priority**: Editing keeps data correct over time. It depends on create/list patterns already in place and reuses the same form, so it follows them in sequence.

**Independent Test**: Open an existing record's edit form, confirm it is pre-filled, change a field, save, and confirm the change is reflected in the listing and detail view.

**Acceptance Scenarios**:

1. **Given** an existing record, **When** the operator opens it for editing, **Then** the form is pre-populated with the record's current values.
2. **Given** the operator changes fields and submits valid data, **When** the form is processed, **Then** the changes are persisted and reflected in the listing without a full page reload.
3. **Given** the operator submits invalid data while editing, **When** the form is processed, **Then** the changes are rejected and validation messages are shown without losing the entered values.

---

### User Story 5 - Delete records (Priority: P3)

For each area, the operator can delete a record after an explicit confirmation, and the record is removed from the listing.

**Why this priority**: Deletion is the least frequent and most destructive operation; it rounds out CRUD but is the lowest-value increment and benefits from the safety net of confirmation.

**Independent Test**: Choose a deletable record, confirm the deletion prompt, confirm, and verify the record disappears from the listing.

**Acceptance Scenarios**:

1. **Given** a record in a listing, **When** the operator chooses to delete it, **Then** a confirmation is requested before any change occurs.
2. **Given** the operator confirms the deletion, **When** the action completes, **Then** the record is removed and the listing updates without a full page reload.
3. **Given** a record is referenced by records in another area (for example a Cliente with Contas), **When** the operator attempts to delete it, **Then** the deletion is prevented and the operator is told why.

---

### Edge Cases

- What happens when the operator requests a record that does not exist or was deleted in another session? The system shows a "not found" message rather than an error page.
- How does the system handle a uniqueness conflict (for example a duplicate CPF, e-mail, account number, or transaction idempotency key)? The save is rejected and the conflicting field is flagged.
- How does the system handle deletion of a record that other records depend on? The deletion is blocked and the dependency is explained.
- How does the system behave for the Transações area, where records carry an immutability/idempotency concern? See Assumptions for the chosen scope of edit/delete on financial transactions.
- What happens on a server or validation error during an in-place (no full reload) action? The relevant screen region shows an error message and the rest of the page remains usable.
- How does a listing behave with a large number of records? See Assumptions regarding pagination.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST present a home page at the application root that introduces the system and provides navigation to all six management areas.
- **FR-002**: The system MUST present a persistent navigation menu, available from every screen, linking to each of the six areas (Tipos de Transação, Clientes, Produtos, Contas, Contratações, Transações) and back to the home page.
- **FR-003**: The system MUST indicate which area is currently active in the navigation menu.
- **FR-004**: For each area, the system MUST provide a listing screen that displays existing records in a table with the key columns relevant to that area.
- **FR-005**: For each area, the system MUST allow viewing the full details of a single record.
- **FR-006**: For each area, the system MUST provide a form to create a new record and persist it when valid.
- **FR-007**: For each area, the system MUST provide a form to edit an existing record, pre-filled with current values, and persist valid changes.
- **FR-008**: For each area, the system MUST allow deleting a record only after an explicit confirmation.
- **FR-009**: The system MUST validate required fields and field-specific rules on create and edit, and MUST display field-level messages when validation fails without discarding the operator's input.
- **FR-010**: The system MUST update the affected screen region (listing, detail, or form) after create, edit, and delete actions without requiring a full page reload.
- **FR-011**: For fields that reference another area (Conta→Cliente; Contratação→Cliente, Produto; Transação→Conta, Produto, Tipo de Transação, and optionally Contratação), the system MUST let the operator pick the referenced record from a selection control populated with existing records, showing a human-readable label rather than a raw identifier.
- **FR-012**: The system MUST prevent deletion of a record that is referenced by records in another area and MUST explain to the operator why the deletion was blocked.
- **FR-013**: The system MUST surface uniqueness and constraint violations (e.g., duplicate CPF, e-mail, account number, transaction idempotency key, or duplicate contracting of the same product by the same client on the same date) as understandable messages tied to the relevant field.
- **FR-014**: The system MUST show an empty-state message on a listing screen when an area has no records.
- **FR-015**: The system MUST show a clear "not found" message when an operator opens a record that no longer exists.
- **FR-016**: The system MUST present enumerated fields (Cliente.segmento, Produto.categoria, Conta.tipo_conta, Conta.status, Contratação.status) as constrained selection controls limited to their allowed values.
- **FR-017**: The screens MUST display labels, field names, and messages in Brazilian Portuguese, consistent with the existing domain naming.

### Key Entities *(include if feature involves data)*

- **Tipo de Transação**: A category of financial movement. Key attributes: descrição (unique), sinal (credit/debit indicator, +1 or -1). Referenced by Transações.
- **Cliente**: A customer of the institution. Key attributes: nome, CPF (unique), CNPJ (optional, unique), e-mail (unique), segmento (Médico/Dentista/Fisioterapeuta/Outro), data de criação. Referenced by Contas and Contratações.
- **Produto**: A financial product or service. Key attributes: nome (unique), categoria (Crédito/Investimento/SaaS/Serviço), taxa de juros, ativo (yes/no). Referenced by Contratações and Transações.
- **Conta**: An account belonging to a Cliente. Key attributes: número (unique), tipo (PF/PJ), saldo, status (Ativa/Bloqueada/Encerrada), and a reference to its Cliente. Referenced by Transações.
- **Contratação**: A client's contracting of a product. Key attributes: data de contratação, status (Ativa/Suspensa/Liquidada/Cancelada), references to Cliente and Produto, unique per (Cliente, Produto, data). Optionally referenced by Transações.
- **Transação**: A financial movement against an account. Key attributes: valor (positive), data e hora, identificador de idempotência (unique), and references to Conta, Produto, Tipo de Transação, and optionally Contratação.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: From the home page, an operator can reach the listing screen of any of the six areas in a single click via the menu.
- **SC-002**: An operator can complete a full create→view→edit→delete cycle for any area without leaving the application or manually editing data outside the screens.
- **SC-003**: 100% of the six areas expose all four CRUD operations (create, read/list+detail, update, delete) through the screens.
- **SC-004**: After a create, edit, or delete action, the operator sees the updated result without a full page reload in at least the listing region.
- **SC-005**: Every validation failure (required field, format, uniqueness, dependency block) results in a human-readable message tied to the relevant field or action, with zero unhandled error pages shown to the operator across the tested scenarios.
- **SC-006**: A new operator can locate and open the correct management area for a given task within 10 seconds using only the menu, without external guidance.

## Assumptions

- The screens are an administrative/operator-facing UI layered on top of the existing `cap-theorem-mysql-cassandra` domain and its six tables; no new business entities are introduced by this feature.
- The UI reuses the existing domain operations and persistence already present in the application; this feature adds presentation and interaction, not new business rules.
- Access to the UI is open within the deployment environment (no end-user authentication/authorization screens are in scope for this version), consistent with the demo/educational nature of the project. Authentication can be added later as a separate feature.
- The interface language is Brazilian Portuguese, matching the existing domain vocabulary (Cliente, Conta, Transação, etc.).
- Transações (financial movements) support create, view, and delete-with-confirmation; full editing of a posted transaction is treated as out of scope for this version because of its idempotency/ledger nature. List/detail/create/delete still satisfy the "CRUD per table" intent for this area. (Open for confirmation during planning.)
- Listings assume a modest data volume for the demo; pagination or filtering is treated as a nice-to-have and not a hard requirement for this version. If volumes grow, pagination is a follow-up.
- The application is accessed through a modern desktop web browser; responsive/mobile layout is desirable but not a gating requirement for this version.
- Referential and uniqueness constraints already enforced by the underlying data model are surfaced to the operator as messages rather than re-implemented in the UI.
