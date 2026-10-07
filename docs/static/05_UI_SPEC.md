# 1. Document Metadata

| Attribute | Detail |
| --- | --- |
| **Project Name** | ServiceSync |
| **Document Name** | `05_UI_SPEC.md` |
| **Document Purpose** | Authoritative User Interface Specification and Frontend Contract |
| **Version** | 1.0.0 |
| **Status** | **LOCKED / APPROVED** |
| **Authority** | Human Developer ONLY (AI coding agents are strictly prohibited from modifying this specification) |
| **Last Updated** | September 20, 2026 |
| **Source Documents** | Stage 2 Audit, `01_PRD_AND_RULES.md`, `02_ARCHITECTURE.md`, `03_DATABASE.md`, `04_API_SPEC.md` |
| **Dependency Documents** | `04_API_SPEC.md` (API contracts are authoritative for all backend communications) |
| **Classification** | Static, Human-Approved UI Source of Truth |

---

# 2. UI Design Goals

* **Operational Clarity:** Provide high-density, low-friction interfaces designed for rapid service-ticket triage and processing in active physical repair shops.
* **Role-Specific Workflows:** Tailor screen visibility strictly to authorized operational roles (`ADMIN`, `TECH`, and Customer Public views) to prevent unauthorized mutations.
* **SLA Visibility:** Expose time-sensitive SLA indicators clearly across dashboards and lists to minimize breached repair commitments.
* **Inventory Transparency:** Ensure parts stock counts and low-stock warnings are instantly visible to technicians and administrators.
* **Strict Framework Compliance:** Maintain an intentionally lightweight footprint using Vanilla JavaScript, HTML5, and Bootstrap 5 without introducing heavy JavaScript frameworks (React/Vue/Angular).
* **Accessibility & Responsiveness:** Ensure Bootstrap-driven layouts adapt seamlessly from desktop shop terminals to field tablets used by technicians.

---

# 3. UI Design Principles

* **API-First Alignment:** Every UI interaction maps directly to endpoints defined in `04_API_SPEC.md`. The UI never mimics business validation locally without server-side verification.
* **Explicit System State:** Every data request must explicitly handle loading, empty, success, and error states visually.
* **Progressive Disclosure:** Complex details (such as ticket histories or parts usage breakdowns) are nested behind modal views or tabs to keep primary screens clean.
* **Destructive Action Protection:** State-mutating actions (such as status transitions or assignment overrides) require explicit user confirmation.
* **Server-Side Security Reliance:** Hiding UI elements based on roles is purely for user experience; backend authorization remains the absolute security boundary.

---

# 4. Frontend Technology Constraints

* **Approved Stack:**
* HTML5 (Semantic structural layouts)
* CSS3 (Custom utility styling over Bootstrap defaults)
* Bootstrap 5 (Responsive grid system, utility classes, and components)
* Vanilla JavaScript (ES6+ modular script execution, DOM manipulation, and native `fetch()` API calls)


* **Prohibited Technologies:**
* NO React
* NO Vue.js
* NO Angular
* NO Next.js
* NO Tailwind CSS (Bootstrap 5 is mandatory)
* NO GraphQL or WebSockets (Standard REST over HTTP/HTTPS)
* NO frontend state-management libraries (Redux, Zustand, Vuex)


* **Organizational Pattern:** JavaScript source files should be modularized by domain functionality (e.g., `auth.js`, `api-client.js`, `tickets.js`, `inventory.js`) and loaded natively via ES6 `type="module"` imports.

---

# 5. Frontend Architecture

The frontend acts as a thin, responsive client communicating exclusively via asynchronous JSON requests.

```text
User Interaction → UI Event Listener → Vanilla JS Module → Centralized API Client (`fetch`) → REST API (`/api/v1`) → Spring Boot Monolith

```

* **Page Layer:** Individual HTML templates mapped to specific URLs, loading standard Bootstrap shells and domain scripts.
* **API Integration Layer (`api-client.js`):** A centralized wrapper around the native `fetch()` API that injects JWT Bearer tokens from `localStorage`, handles common HTTP error codes (401, 403, 409, 500), and normalizes JSON response payloads.
* **Authentication State:** Maintained locally by reading token validity and role claims stored in browser `localStorage`.
* **Authorization-Aware Rendering:** DOM elements containing role-restricted actions are conditionally rendered based on the decoded JWT role claim.

---

# 6. Application Shell and Global Layout

The standard application shell governs all authenticated pages (`dashboard.html`, `tickets.html`, `inventory.html`, etc.):

* **Header / Navbar:** Responsive Bootstrap 5 navbar featuring the ServiceSync brand, primary navigation links (Dashboard, Tickets, Inventory, Reports), and a right-aligned user profile dropdown displaying the active username and a Logout action.
* **Sidebar / Breadcrumbs:** Contextual navigation helper indicating the current module path.
* **Main Content Area:** Fluid Bootstrap container (`.container-fluid`) designated for dynamic view rendering.
* **Global Notification Area:** A designated fixed-position toast container (`#toast-container`) used for rendering success alerts, validation summaries, and error notices.
* **Logout Behavior:** Clears local storage credentials (`localStorage.clear()`) and redirects the user immediately to the login page (`/index.html`).

---

# 7. Authentication UI

* **Login Page (`/index.html`):** A centered Bootstrap card containing an email input, password input, and a primary submit button.
* **Invalid Credentials Handling:** Intercepts HTTP 401 responses and displays an inline danger alert: *"Invalid email or password."*
* **Session Expiration / 401 Interception:** If any API request returns an HTTP 401 due to token expiration, the `api-client` automatically purges local credentials and forces a redirect to the login screen with a session-expired message.
* **Protected Routes:** Client-side route guard checks for the presence of a valid JWT in `localStorage` before rendering protected DOM nodes. If missing, execution halts and redirects to `/index.html`.

---

# 8. Role-Based UI Model

| Role | Primary Dashboard | Accessible Screens | Restricted Screens | Primary Actions |
| --- | --- | --- | --- | --- |
| **ADMIN** | Admin Operations Dashboard | Tickets, Inventory, Reports, Audit Logs, Users | None | Create/Assign Tickets, Adjust Inventory, View SLA Reports, Reassign Techs. |
| **TECH** | Technician Task Dashboard | Assigned Tickets, Inventory Catalog | User Administration, Reports, Audit Logs | Update Ticket Status, Consume Parts, Add Service Notes. |
| **Customer** | Public Track-and-Trace View | Public Lookup Screen Only | All internal management views | Query ticket status using Ticket ID + Phone Number. |

---

# 9. Navigation Architecture

| Role | Navigation Item | Destination | Access | Primary Purpose |
| --- | --- | --- | --- | --- |
| **Admin / Tech** | Dashboard | `/dashboard.html` | Authenticated | Operational metrics and quick queues. |
| **Admin / Tech** | Service Tickets | `/tickets.html` | Authenticated | Search, filter, and manage repair lifecycle. |
| **Admin / Tech** | Inventory | `/inventory.html` | Authenticated | Track spare parts catalog and stock levels. |
| **Admin Only** | SLA Reports | `/reports.html` | `ADMIN` | Analyze breach statistics and operational volume. |
| **Public** | Track Repair | `/track.html` | Public | Customer self-service status check. |

---

# 10. Page Inventory

### Authentication

* `index.html` — Staff Login Form.
* `track.html` — Public Customer Ticket Lookup.

### Core Management (`ADMIN` & `TECH`)

* `dashboard.html` — Role-adjusted operational overview.
* `tickets.html` — Paginated master ticket list with status filters.
* `ticket-detail.html` — Deep inspection view for a single ticket, handling status changes, part assignments, and history logs.
* `inventory.html` — Parts catalog and stock availability grid.

### Administrative (`ADMIN` Only)

* `reports.html` — SLA breach metrics and operational summaries.
* `audit.html` — Immutable audit trail of ticket history events.

---

# 11. Page Specification Template

Standardized properties applied across all major pages:

* **UI States:** Every page must implement an initial loading skeleton/spinner, a fully populated state, an empty state (e.g., "No tickets found"), and a graceful network failure state.
* **Forms:** Validated via HTML5 attributes (`required`, `pattern`) combined with programmatic JS checks. Server-side validation errors return HTTP 400 and populate inline field danger text.
* **Success Feedback:** Trigger green Bootstrap toasts on successful mutations (e.g., *"Ticket status updated successfully"*).

---

# 12. Dashboard Specifications

* **Admin Dashboard:** Displays high-level counters for active tickets (`CREATED`, `DIAGNOSING`, `WAITING_PARTS`), low-stock inventory warnings, and a direct link to SLA breach metrics.
* **Technician Dashboard:** Filters tickets explicitly assigned to the logged-in technician (`?technicianId={id}`), highlighting items requiring immediate diagnosis or parts fulfillment.
* **Refresh Behavior:** Manual refresh via standard button click; no automated polling loops to preserve bandwidth and backend query performance.

---

# 13. Service Ticket UI

* **State Machine Enforcement:** The UI must dynamically enable or disable status transition buttons based on the current state defined in `01_PRD_AND_RULES.md`.
* *Example:* A ticket in `CREATED` can only transition to `DIAGNOSING`. A ticket in `RESOLVED` cannot be transitioned backward.


* **Parts Attachment Form:** A modal or inline form on the ticket detail view allowing technicians to select an inventory part and specify a quantity. The UI immediately disables submission if the requested quantity exceeds available stock or if stock is zero.
* **History Timeline:** A vertical chronological render of the `ticket_history` audit endpoint (`GET /api/v1/tickets/{id}/history`), showing timestamps, status changes, and technician notes.

---

# 14. Technician UI

* **Assigned Queue:** A dedicated table view populated by querying `GET /api/v1/tickets?technicianId={id}`.
* **Execution Actions:** Technicians can directly trigger status change modals, attach inventory parts, and append technical notes without leaving the ticket detail screen.

---

# 15. Inventory UI

* **Catalog Grid:** Displays part names, SKUs, current stock quantities, and base catalog prices.
* **Stock Warnings:** Quantities dropping below a safety threshold are visually highlighted using Bootstrap warning badges (`.bg-warning`).
* **Concurrency Feedback:** If a technician attempts to consume a part and receives an HTTP `409 Conflict` (Optimistic Locking failure due to simultaneous usage), the UI must present an alert: *"Stock level changed concurrently. Please refresh inventory."*

---

# 16. SLA UI

* **Breach Indicators:** Tickets exceeding the 48-hour threshold (`CREATED` or `DIAGNOSING`) are highlighted with Bootstrap danger badges (`.bg-danger`).
* **Reporting View (`/reports.html`):** Renders operational breach summaries retrieved from `GET /api/v1/reports/sla-breaches`.

---

# 17. Notification UI

* **System Feedback:** Handled entirely via Bootstrap toast notifications (`.toast`) injected into the global notification container for success, warning, and error events. No independent notification bell/dropdown exists for MVP.

---

# 18. Reporting UI

* **SLA Breach Report:** Tabular presentation of breached tickets with date-range filters (`?days=30`). Exports or advanced interactive charting are excluded from MVP.

---

# 19. Audit UI

* **History Audit:** Read-only data table displaying timestamp, actor, previous status, new status, and contextual notes for a selected ticket. Restricted to `ADMIN` roles.

---

# 20. Forms and Validation

* **Inline Validation:** Bootstrap validation classes (`.is-invalid`, `.invalid-feedback`) are toggled dynamically upon failed client-side checks or when catching RFC 7807 error details returned by the API.
* **Double-Submission Prevention:** Submit buttons are programmatically disabled (`disabled = true`) and display a loading spinner text during active `fetch()` operations.

---

# 21. Tables, Lists, Search, Filtering, and Pagination

* **Data Tables:** Styled with `.table .table-striped .table-hover`.
* **Pagination Controls:** Standard Bootstrap pagination components bound to Spring Data `Page<T>` metadata (`number`, `totalPages`, `size`).
* **Filtering:** Dropdown filters for `status` and search inputs for phone numbers or part names instantly re-query the appropriate API endpoint with updated query parameters.

---

# 22. UI State Model

* **Initial / Loading:** Bootstrap spinner (`.spinner-border`) centered inside content containers.
* **Loaded:** Active data rendered into tables or cards.
* **Empty:** Clean placeholder message with muted text (e.g., *"No service tickets currently active."*).
* **Error / Conflict:** Alert banner rendered at the top of the form or view detailing the rejection reason.

---

# 23. API Integration Mapping

| UI Area | Page / Action | API Endpoint | Method | Auth | Expected Result / Handling |
| --- | --- | --- | --- | --- | --- |
| **Login** | `index.html` / Submit Form | `/api/v1/auth/login` | POST | Public | Stores JWT in `localStorage`, redirects to dashboard. |
| **Ticket List** | `tickets.html` / Load Grid | `/api/v1/tickets` | GET | `ADMIN`, `TECH` | Renders paginated table of service tickets. |
| **Ticket Status** | `ticket-detail.html` / Status Change | `/api/v1/tickets/{id}/status` | PATCH | `ADMIN`, `TECH` | Updates state machine, logs history. |
| **Consume Part** | `ticket-detail.html` / Add Part | `/api/v1/tickets/{id}/parts` | POST | `ADMIN`, `TECH` | Decrements stock; handles 409 Conflict. |
| **Public Track** | `track.html` / Query Status | `/api/v1/public/tickets/{id}` | GET | Public | Displays sanitized ticket status using ID + Phone. |
| **Inventory Grid** | `inventory.html` / Load Catalog | `/api/v1/inventory` | GET | `ADMIN`, `TECH` | Renders parts catalog table. |
| **SLA Reports** | `reports.html` / Load Metrics | `/api/v1/reports/sla-breaches` | GET | `ADMIN` | Renders breach report table. |

---

# 24. Authentication and Authorization Rendering Rules

* **Conditional DOM Rendering:** Elements marked with role-specific visibility attributes are hidden or shown upon client-side token parsing.
* **Backend Supremacy:** If a user manually crafts a network request to bypass UI restrictions, the Spring Security layer enforces actual access control, returning HTTP 403 Forbidden.

---

# 25. Error Handling and User Feedback

* **RFC 7807 Mapping:** The `api-client` parses standardized error responses and surfaces user-friendly messages, stripping out technical stack traces or raw database exceptions.
* **Conflict Management:** HTTP 409 responses trigger specific contextual alerts (e.g., guiding the user to refresh due to concurrent inventory updates).

---

# 26. Responsive Design

* **Mobile/Tablet Viewports:** Tables collapse or hide non-critical columns using Bootstrap responsive display utilities (`.d-none .d-md-table-cell`). Forms stack vertically to ensure easy tapping on touch devices.

---

# 27. Accessibility

* **Semantic Markup:** Extensive use of HTML5 semantic elements (`<header>`, `<main>`, `<nav>`, `<section>`).
* **Form Association:** All form inputs maintain explicit `<label for="...">` pairings for screen-reader compatibility.
* **Color Independence:** Status badges combine text labels with Bootstrap contextual colors (`.badge .bg-success`, `.bg-danger`) so information is never conveyed through color alone.

---

# 28. UI Components and Reusable Patterns

* Rely strictly on native Bootstrap 5 components: Cards, Modals, Toasts, Tables, Navbars, Badges, and Buttons. No external component libraries are permitted.

---

# 29. Kiosk UI Specification

* **Subsystem Boundary:** The Shop Kiosk Display is a completely separate legacy Servlet/JSP application (`kiosk-display` module).
* **UI Characteristics:** Renders a simple, auto-refreshing HTML table (`<meta http-equiv="refresh" content="30">`) reflecting live ticket states.
* **Constraints:** It does NOT share the Vanilla JS SPA codebase, does NOT consume the REST API, and interacts exclusively via JDBC with the `active_display_tickets` SQL View.

---

# 30. Security-Conscious UI Rules

* **Credential Handling:** Never log JWT tokens or user passwords to the browser console.
* **XSS Mitigation:** Avoid direct `innerHTML` injection of unvalidated user input (such as customer-provided device descriptions). Use text content binding (`textContent`) or sanitize strings.

---

# 31. Performance and Usability Considerations

* Limit initial list queries to standard page sizes (`size=20`).
* Prevent multiple rapid clicks on form submission buttons by enforcing programmatic disabling state during `fetch` execution.

---

# 32. Browser and Compatibility Assumptions

* Targeted exclusively at modern ES6-compliant evergreen browsers (Chrome, Firefox, Edge, Safari) operating on desktop and mobile viewports.

---

# 33. UI-to-Requirement Traceability

| PRD Requirement | UI Area | Page / Component | User Role | API Dependency |
| --- | --- | --- | --- | --- |
| **FR-AUTH-1** | Authentication | `index.html` | Public | `POST /api/v1/auth/login` |
| **FR-TKT-1** | Ticket Management | `tickets.html` | Admin, Tech | `POST /api/v1/tickets` |
| **FR-TKT-2** | Ticket Workflow | `ticket-detail.html` | Admin, Tech | `PATCH /api/v1/tickets/{id}/status` |
| **FR-INV-1** | Inventory Usage | `ticket-detail.html` | Admin, Tech | `POST /api/v1/tickets/{id}/parts` |
| **FR-PUB-1** | Customer Lookup | `track.html` | Public | `GET /api/v1/public/tickets/{id}` |
| **FR-KSK-1** | Kiosk Display | Legacy JSP Kiosk | Public (Shop) | None (JDBC View direct access) |

---

# 34. UI Scope Boundaries

* **In Scope:** HTML/Bootstrap static templates, Vanilla JS API integration modules, role-based conditional rendering, form validation, error handling, and public tracking views.
* **Out of Scope:** React/Vue/Angular SPAs, client-side routing libraries, native mobile apps, real-time WebSockets, and advanced graphical analytics charts.

---

# 35. AI Coding-Agent UI Rules

**CRITICAL DIRECTIVES FOR GOOGLE ANTIGRAVITY:**

1. **Strict Stack Adherence:** You MUST NOT introduce React, Vue, Angular, Tailwind, or any frontend build framework. Use only HTML5, CSS3, Bootstrap 5, and Vanilla JS.
2. **API Contract Alignment:** You MUST map all UI data fetching strictly to endpoints defined in `04_API_SPEC.md`. Do not invent custom API routes.
3. **No Schema Violations:** Do not assume database structures in the frontend; rely solely on DTO contracts returned by the API.
4. **Kiosk Isolation:** Do not attempt to refactor or modernize the legacy JSP/Servlet kiosk into this Vanilla JS frontend architecture.
5. **Conflict Halt:** If UI implementation requirements contradict backend specifications, stop and report an `OPEN API/UI CONTRACT GAP`.

---

# 36. Source-of-Truth Boundaries

| Concern | Source of Truth |
| --- | --- |
| **Business Rules & Workflows** | `01_PRD_AND_RULES.md` |
| **System & Module Architecture** | `02_ARCHITECTURE.md` |
| **Database Schema & Constraints** | `03_DATABASE.md` |
| **REST API Contracts** | `04_API_SPEC.md` |
| **UI Behavior & Layout Rules** | `05_UI_SPEC.md` (This Document) |
| **Setup & Testing Procedures** | `06_SETUP_AND_TEST.md` |

---

# 37. UI Completeness Audit

* [x] **Functional Coverage:** Login, dashboards, ticketing lifecycle, inventory consumption, reporting, audit views, and public lookup are fully defined.
* [x] **Technical Coverage:** Vanilla JS, Bootstrap 5, async `fetch()`, and JWT token handling are explicitly mandated.
* [x] **Contract Coverage:** All UI actions map directly to validated API endpoints.
* [x] **AI Safety:** Directives prohibit unauthorized framework introductions and enforce strict stack compliance.

---

# 38. Open UI Decisions

* *None currently identified. All structural, architectural, and UI integration constraints are fully resolved by authoritative project sources.*

---

# 39. Final UI Summary

The **ServiceSync Frontend** is an intentionally lightweight, framework-free single-page application experience built using **HTML5, CSS3, Bootstrap 5, and Vanilla JavaScript**. It interacts exclusively with the Spring Boot Monolith via JSON REST contracts (`/api/v1`), utilizing `localStorage` for stateless JWT session management.

The UI enforces strict role-based navigation (`ADMIN`, `TECH`, and public customer tracking), manages complex state-machine transitions for service tickets, and handles transactional inventory part consumption with robust error feedback (including HTTP 409 Optimistic Locking conflict management). The legacy Servlet/JSP Kiosk remains completely decoupled, ensuring the frontend architecture strictly satisfies all academic curriculum requirements without unnecessary complexity.