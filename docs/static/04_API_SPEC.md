# 1. Document Metadata

| Attribute | Detail |
| --- | --- |
| **Document Name** | `04_API_SPEC.md` |
| **Project Name** | ServiceSync |
| **Version** | 1.0.0 |
| **Status** | **LOCKED / APPROVED** |
| **Purpose** | Authoritative REST API Contract and Integration Specification |
| **Intended Readers** | Human Developers, AI Coding Agents (Google Antigravity), UI Integrators |
| **Source-of-Truth Designation** | **Static, human-approved API contract** |
| **Update Authority** | Human Developer ONLY |

*Note: This is a static, human-approved API contract. AI coding agents are strictly forbidden from modifying this specification or inventing unapproved endpoints.*

---

# 2. API Design Principles

ServiceSync's API adheres to the following principles:

* **Business-Capability Oriented:** Endpoints reflect business actions (e.g., consuming a part) rather than raw database CRUD operations.
* **RESTful Resource Design:** Uses standard HTTP verbs (`GET`, `POST`, `PATCH`, `DELETE`) operating on noun-based resource URIs.
* **Separation of Concerns:** API layer (Controllers/DTOs) is strictly decoupled from the Persistence layer (Entities). No JPA entities are ever returned directly in an API response.
* **Predictable HTTP Semantics:** Meaningful status codes represent business state (e.g., `409 Conflict` for inventory contention).
* **Least Privilege:** All endpoints default to secured unless explicitly whitelisted (e.g., the public customer lookup).
* **Explicit Validation:** Fail-fast at the API boundary before hitting business logic.

---

# 3. API Boundaries

The API architecture recognizes three distinct integration boundaries:

1. **Frontend to Monolith (Main Application API):**
* *Consumer:* Vanilla JS + Bootstrap Web Application.
* *Provider:* Spring Boot Monolith (`/api/v1/...`).
* *Contract:* REST / JSON over HTTPS.


2. **Monolith to SLA Microservice (Internal API):**
* *Consumer:* Spring Boot Monolith (via OpenFeign).
* *Provider:* SLA Monitoring Microservice (`/api/internal/v1/...`).
* *Contract:* REST / JSON. Secure, server-to-server.


3. **Legacy Kiosk Boundary:**
* *Consumer:* Servlet/JSP Application.
* *Contract:* **None.** The legacy kiosk explicitly bypasses the REST API and queries its authorized MySQL `VIEW` directly via JDBC. *No REST endpoints will be built to serve the kiosk.*



---

# 4. Base URLs and Environments

* **Conceptual Base URL:** `/api/v1`
* **Internal Service Base URL:** `/api/internal/v1`
* **Environment Strategy:** The API contract remains identical across `local`, `development`, and `production`. Deployment hostnames (e.g., `localhost:8080` vs `api.servicesync.com`) are managed by environment variables at runtime, not within this contract.

---

# 5. API Versioning Strategy

* **Strategy:** URI Path Versioning.
* **Current Version:** `v1`
* **Compatibility:** Non-breaking changes (adding fields, adding endpoints) will be applied to `v1`. Breaking changes (renaming fields, changing payload structures) require human approval to create a `v2`.

---

# 6. Resource Naming Conventions

* **Format:** Lowercase, plural nouns separated by hyphens (kebab-case).
* **Identifiers:** Path variables are used for resource identification (e.g., `/tickets/{ticketId}`).
* **Nested Resources:** Used when a sub-resource is intrinsically bound to a parent (e.g., `/tickets/{ticketId}/parts`).
* **Action Endpoints:** For state transitions that don't neatly fit CRUD, verb-like sub-paths are acceptable but minimized (e.g., `/tickets/{ticketId}/status`).

---

# 7. Authentication Architecture

* **Mechanism:** JSON Web Token (JWT) Bearer Authentication.
* **Login Flow:** Client sends credentials to `POST /api/v1/auth/login`. Server returns a signed JWT. Client attaches `Authorization: Bearer <token>` to subsequent requests.
* **OAuth2 (Google):** *DEFERRED* (per Stage 2 Audit).
* **Token Storage (Client):** `localStorage` or `sessionStorage` (with strict XSS mitigation handling in the UI layer).
* **Revocation/Logout:** Managed client-side by destroying the token.

---

# 8. Authorization Model

Authorization is enforced via Role-Based Access Control (RBAC).

| Resource / Operation | `ADMIN` | `TECH` | Customer (Public) |
| --- | --- | --- | --- |
| **Users / Staff** | Full CRUD | Denied | Denied |
| **Inventory Catalog** | Full CRUD | Read-Only | Denied |
| **Ticket Creation** | Allowed | Allowed | Denied |
| **Ticket Assignment** | Allowed | Denied | Denied |
| **Ticket Status** | Allowed | Allowed (If assigned) | Denied |
| **Consume Part** | Allowed | Allowed (If assigned) | Denied |
| **Public Lookup** | N/A | N/A | Allowed (Via specific endpoint & Auth Key) |

---

# 9. Standard Request Conventions

* **Content-Type:** `application/json` (except for specific export endpoints).
* **Accept:** `application/json`
* **Authorization Header:** `Bearer {jwt}`
* **Date/Time Format:** ISO-8601 UTC (e.g., `2026-09-20T21:31:02Z`).
* **Pagination Params:** `?page=0&size=20` (Zero-based indexing).
* **Sorting Params:** `?sort=createdAt,desc`

---

# 10. Standard Response Conventions

* **Resource Representation:** Direct JSON object for single resources to minimize wrapper bloat.
* **Pagination Representation:** Standard Spring Data `Page<T>` representation containing `content[]`, `totalElements`, `totalPages`, `number`, and `size`.
* **Null Handling:** Null fields are omitted from the JSON response payloads to reduce payload size.

---

# 11. Standard Error Model

ServiceSync utilizes an RFC 7807 inspired error format for all non-2xx responses.

```json
{
  "timestamp": "2026-09-20T21:31:02Z",
  "status": 400,
  "error": "BAD_REQUEST",
  "message": "Validation failed",
  "path": "/api/v1/tickets/101/parts",
  "details": [
    {
      "field": "quantity",
      "issue": "Must be greater than 0"
    }
  ]
}

```

* **Validation Errors:** Provide field-level `details` array.
* **Business Errors:** Set the `error` string to a specific code (e.g., `INSUFFICIENT_STOCK`).

---

# 12. HTTP Status Code Standards

| Code | Standard Usage |
| --- | --- |
| **200 OK** | Successful read, update, or action. |
| **201 Created** | Successful resource creation. |
| **202 Accepted** | Async operation accepted (e.g., SLA PDF report generation). |
| **204 No Content** | Successful deletion or action with no response body. |
| **400 Bad Request** | Malformed JSON or input validation failure. |
| **401 Unauthorized** | Missing or invalid JWT. |
| **403 Forbidden** | Valid JWT, but lacking required Role (e.g., TECH accessing Admin route). |
| **404 Not Found** | Requested resource ID does not exist. |
| **409 Conflict** | Business rule violation (e.g., Optimistic Lock failure, Insufficient Stock). |
| **500 Internal Error** | Unhandled backend exception. |

---

# 13. API Resource Catalogue

| Resource | Purpose | Owning Module | Roles |
| --- | --- | --- | --- |
| **Auth** | Login and token generation. | Security | Public |
| **Tickets** | Core service lifecycle operations. | Tickets | Admin, Tech |
| **Public Tickets** | Read-only status for customers. | Tickets | Public |
| **Inventory** | Parts catalog and consumption. | Inventory | Admin, Tech |
| **Reports** | Operational metrics. | Tickets / SLA | Admin |
| **SLA Events** | Internal trigger receivers. | SLA Microservice | Internal |

---

# 14. Authentication Endpoints

### Login

* **Method/Path:** `POST /api/v1/auth/login`
* **Purpose:** Exchange credentials for a JWT.
* **Auth:** Public
* **Request:** `{"email": "...", "password": "..."}`
* **Response (200):** `{"token": "eyJhb...", "role": "ADMIN", "name": "..."}`
* **Error (401):** Invalid credentials.

*(Note: OAuth2 endpoints are deferred based on PRD OAD-001).*

---

# 15. User and Role Endpoints

* **Method/Path:** `GET /api/v1/users/technicians`
* **Purpose:** Fetch list of technicians for ticket assignment.
* **Auth:** `ADMIN`
* **Response (200):** `[{"id": 1, "name": "..."}]`

*(Note: Full User CRUD is minimized for MVP. Admin DB seeding handles initial users).*

---

# 16. Customer APIs

Customers do not have a dedicated CRM endpoint in this MVP as customer data (name, phone) is embedded in the ticket.

### Public Ticket Lookup

* **Method/Path:** `GET /api/v1/public/tickets/{ticketId}`
* **Purpose:** Allow walk-in customers to check status without an account.
* **Auth:** Public (Secured via composite knowledge).
* **Query Param:** `?phone={last4digits}` (Required validation to prevent ID enumeration).
* **Response (200):** `{"id": 101, "device": "iPhone", "status": "IN_REPAIR"}`
* **Error (404):** Returned if ticket ID doesn't exist OR if phone does not match.

---

# 17. Service Ticket APIs

### Create Ticket

* **Method/Path:** `POST /api/v1/tickets`
* **Auth:** `ADMIN`, `TECH`
* **Request:** `{"customerName": "...", "customerPhone": "...", "deviceInfo": "...", "issueDesc": "..."}`
* **Response (201):** Full Ticket DTO. Status is inherently `CREATED`.

### List Tickets

* **Method/Path:** `GET /api/v1/tickets`
* **Auth:** `ADMIN`, `TECH`
* **QueryParams:** `page`, `size`, `status`, `technicianId`
* **Response (200):** Paginated `Page<TicketDTO>`.

### Retrieve Ticket

* **Method/Path:** `GET /api/v1/tickets/{ticketId}`
* **Auth:** `ADMIN`, `TECH`
* **Response (200):** Detailed Ticket DTO (includes nested Parts used).

### Update Ticket Status (State Transition)

* **Method/Path:** `PATCH /api/v1/tickets/{ticketId}/status`
* **Purpose:** Business operation to progress the state machine (e.g., `DIAGNOSING`, `RESOLVED`).
* **Auth:** `ADMIN`, `TECH` (Tech must be assigned to the ticket).
* **Request:** `{"status": "IN_REPAIR", "notes": "Screen replaced."}`
* **Response (200):** Updated Ticket DTO.
* **Error (409):** `INVALID_STATE_TRANSITION` if transitioning backwards improperly.

---

# 18. Technician APIs

Handled via the standard Ticket listing with filtering: `GET /api/v1/tickets?technicianId={id}`. Technician profiles are handled via the User endpoints.

---

# 19. Assignment APIs

### Assign/Reassign Technician

* **Method/Path:** `PATCH /api/v1/tickets/{ticketId}/assignment`
* **Auth:** `ADMIN`
* **Request:** `{"technicianId": 45}`
* **Response (200):** Updated Ticket DTO.

---

# 20. Diagnosis and Service Execution APIs

Diagnosis and service notes are recorded as part of the state transition endpoint (`PATCH /api/v1/tickets/{ticketId}/status`) using the `notes` field, which writes to the Audit/History log. No standalone diagnosis endpoint is required.

---

# 21. Inventory APIs

### List Inventory (Catalog)

* **Method/Path:** `GET /api/v1/inventory`
* **Auth:** `ADMIN`, `TECH`
* **Response (200):** Paginated `Page<InventoryDTO>`.

### Consume Part (Ticket Association)

* **Method/Path:** `POST /api/v1/tickets/{ticketId}/parts`
* **Purpose:** Permanently assigns a physical part to a ticket and deducts stock.
* **Auth:** `ADMIN`, `TECH`
* **Request:** `{"inventoryId": 89, "quantity": 1}`
* **Response (201):** TicketPart DTO.
* **Error (409 Conflict):** `INSUFFICIENT_STOCK` or `CONCURRENT_MODIFICATION` (Optimistic Locking failure).

---

# 22. SLA APIs

### Internal Event Trigger (Monolith → SLA Microservice)

* **Method/Path:** `POST /api/internal/v1/sla/events`
* **Provider:** SLA Microservice
* **Auth:** Internal Service Token (or Network Boundary Security).
* **Request:** `{"ticketId": 101, "eventType": "STATE_CHANGE", "newStatus": "RESOLVED"}`
* **Response (202 Accepted):** Microservice acknowledges receipt and processes asynchronously.

---

# 23. Notification APIs

Explicitly **Out of Scope** for public/frontend APIs. Notifications are handled internally by the SLA Microservice logging to its database.

---

# 24. Reporting APIs

### Retrieve SLA Breaches

* **Method/Path:** `GET /api/v1/reports/sla-breaches`
* **Auth:** `ADMIN`
* **QueryParams:** `days=30`
* **Response (200):** JSON array of breached ticket metrics.

---

# 25. Audit APIs

### Retrieve Ticket History

* **Method/Path:** `GET /api/v1/tickets/{ticketId}/history`
* **Auth:** `ADMIN`, `TECH`
* **Response (200):** Array of state transition events (previous state, new state, user, timestamp, notes).

---

# 26. Legacy Kiosk API Boundary

* **API Exposure:** NONE.
* **Contract Rule:** The Spring Boot Monolith exposes NO endpoints intended for the Kiosk. The Kiosk operates entirely out-of-band by issuing raw JDBC `SELECT` statements against the `active_display_tickets` database VIEW.

---

# 27. Pagination

* **Parameters:** `page` (int, default 0), `size` (int, default 20).
* **Max Limit:** *OPEN API DECISION* (Defaulting to 100 max size for safety unless overridden by implementation).
* **Structure:** Follows Spring Data's default pagination format to prevent reinventing JSON structures.

---

# 28. Filtering

Filtering is explicit and business-driven. Arbitrary query string mapping to database columns is prohibited.

* **Tickets:** Allow `?status={val}`, `?technicianId={val}`
* **Inventory:** Allow `?name={val}` (Partial match search).

---

# 29. Sorting

* **Parameter:** `sort={field},{direction}` (e.g., `sort=createdAt,desc`).
* **Allowed Fields:** Restricted at the controller level to prevent SQL injection or indexing failures. Defaults to `createdAt,desc` for Tickets.

---

# 30. Validation Rules

* **Request Validation:** Executed by Spring Boot `@Valid` / JSR-380 annotations.
* **Behavior:** Failure results in `400 Bad Request` with the standardized error format detailing the exact field (e.g., "customerName cannot be blank").
* **Business Validation:** Handled in the Service layer (e.g., cannot transition directly from `CREATED` to `RESOLVED`).

---

# 31. Concurrency and Conflict Handling

* **Contract:** API clients must expect `409 Conflict` during critical operations, primarily `POST /api/v1/tickets/{id}/parts`.
* **Client Behavior:** The Vanilla JS frontend must catch HTTP 409 and display a user-friendly message ("This part was just consumed by another technician. Please refresh inventory.") rather than a generic error.

---

# 32. Idempotency

* **REST Standard:** `GET` and `PATCH` operations are idempotent by design.
* **Internal SLA Trigger:** The internal `POST /api/internal/v1/sla/events` is safely repeatable if the SLA microservice implements logical deduping (e.g., ignoring a duplicate `RESOLVED` event for the same ticket).
* **General App:** No explicit `Idempotency-Key` header is required for the MVP frontend workflows.

---

# 33. Transaction and Atomicity Expectations

* **Observable Behavior:** If `POST /api/v1/tickets/{id}/parts` returns a `201`, the client is guaranteed that the ticket was linked AND the inventory was decremented. If it returns `409` or `500`, the client is guaranteed that neither action occurred. The API will not expose partial successes.

---

# 34. API Security

* **Transport:** Must be configured for HTTPS in deployed environments.
* **CORS:** Configured on the Monolith to accept requests from the deployed Frontend origin.
* **Input Protection:** All path variables and JSON bodies must be strongly typed (e.g., expecting `Long` for IDs) to prevent injection vectors at the API boundary.

---

# 35. API-to-Database Boundary

* **Strict Layering:**
* API Endpoint (`@RestController`) maps JSON to DTO.
* Passes DTO/Primitives to Business Layer (`@Service`).
* Business Layer coordinates Entities and Persistence (`@Repository`).


* **Rule:** Repositories must never be injected directly into Controllers.

---

# 36. DTO and Representation Rules

* **No Entity Leakage:** Database entities (`Ticket`, `Inventory`) are strictly prohibited from being returned from Controllers. They must be mapped to `TicketDTO`, `InventoryDTO`.
* **Separation:** Input structures (e.g., `TicketCreateRequestDTO`) must be separate from Output structures (e.g., `TicketResponseDTO`) to prevent mass-assignment vulnerabilities.

---

# 37. API Documentation and Testing Requirements

* **Endpoint Tests:** Tested via `@WebMvcTest` in Spring Boot, asserting JSON serialization, status codes, and HTTP 403 authorization rejections.
* **Documentation:** A Postman Collection will be generated separately as part of the dynamic implementation phase (Ref: `DOC-15`). No OpenAPI/Swagger generation is strictly required for the MVP unless requested.

---

# 38. API Traceability

| PRD Requirement | API Resource/Endpoint | Authorized Role | Acceptance Condition |
| --- | --- | --- | --- |
| **FR-AUTH-1** | `POST /api/v1/auth/login` | Public | Returns valid JWT. |
| **FR-TKT-1** | `POST /api/v1/tickets` | Admin, Tech | 201 Created w/ Ticket DTO. |
| **FR-TKT-2** | `PATCH /api/v1/tickets/{id}/status` | Admin, Tech | 200 OK or 409 for invalid state. |
| **FR-INV-1/2** | `POST /api/v1/tickets/{id}/parts` | Admin, Tech | 201 Created; UI handles out-of-stock. |
| **FR-PUB-1** | `GET /api/v1/public/tickets/{id}` | Public | 200 OK or 404 (with phone validation). |
| **FR-KSK-1** | N/A (Kiosk Boundary) | N/A | REST API is bypassed entirely. |

---

# 39. API Constraints for AI Coding Agents

**CRITICAL DIRECTIVES FOR GOOGLE ANTIGRAVITY:**

1. **Immutability:** Treat `04_API_SPEC.md` as the authoritative API contract.
2. **No Extraneous Endpoints:** You MUST NEVER generate CRUD endpoints simply because a database table exists (e.g., do not generate a `PUT /inventory/{id}` unless explicitly requested).
3. **No Entity Exposure:** You MUST NEVER return a JPA entity from a Controller. You must create and map to a DTO.
4. **No Architectural Drift:** You MUST NEVER create REST endpoints for the legacy Kiosk.
5. **State Machine Respect:** You MUST NOT allow arbitrary updates to the ticket object. Status transitions MUST go through the explicit transition logic.
6. **Conflict Protocol:** If implementation requires altering this contract (e.g., modifying a DTO structure fundamentally), STOP and report: `API CONTRACT CHANGE REQUIRED`.

---

# 40. Source-of-Truth Boundaries

| Concern | Source of Truth |
| --- | --- |
| **Business requirements & rules** | `01_PRD_AND_RULES.md` |
| **Architecture & boundaries** | `02_ARCHITECTURE.md` |
| **Database schema & state** | `03_DATABASE.md` |
| **API contracts & REST integration** | `04_API_SPEC.md` (This Document) |
| **UI requirements & DOM handling** | `05_UI_SPEC.md` |
| **Setup, Config & Testing** | `06_SETUP_AND_TEST.md` |

---

# 41. API Completeness Audit

* [x] **Resource Coverage:** Customer lookup, Ticket state machine, Inventory consumption, and internal SLA triggers are covered.
* [x] **Security:** All endpoints except `/auth/login` and `/public/tickets` are JWT secured with mapped roles.
* [x] **Contract Integrity:** Pagination, error formats, and standard status codes are definitively established.
* [x] **Business Integrity:** Inventory consumption acts as a distinct endpoint to guarantee atomicity, rather than a generic ticket update.
* [x] **AI Safety:** Explicit instructions forbid entity leakage and arbitrary CRUD generation.

---

# 42. Open API Decisions

| Decision ID | Topic | Why Unresolved | Information Required |
| --- | --- | --- | --- |
| **OAD-API-001** | **Pagination Max Size** | Exact safe limit for page sizes is unstated in source materials. | Implementation testing required. Assuming Spring Default (20) / Max 100 until verified. |

---

# 43. Final API Summary

The **ServiceSync API** is a JSON-based RESTful contract exposed by the Spring Boot Monolith (`/api/v1`). It is heavily domain-oriented, providing explicit endpoints for business actions (like consuming a part or transitioning a ticket's status) rather than exposing arbitrary database CRUD.

Security is enforced via stateless JWT Bearer tokens and strict Role-Based Access Control (`ADMIN`, `TECH`), with one explicitly secured public endpoint for customer ticket tracking. The contract defines standardized RFC-7807 error responses, Spring Data pagination, and relies on HTTP `409 Conflict` to safely communicate concurrent inventory failures to the Vanilla JavaScript client. The internal API explicitly accommodates the **SLA Microservice** via a dedicated event-receiver endpoint, while the **Legacy Kiosk** is strictly forbidden from consuming the REST API, preserving the system's architectural constraints.