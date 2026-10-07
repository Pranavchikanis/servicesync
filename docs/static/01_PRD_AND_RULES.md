# 1. Document Metadata

| Attribute | Detail |
| --- | --- |
| **Document Name** | `01_PRD_AND_RULES.md` |
| **Project Name** | ServiceSync |
| **Document Purpose** | Authoritative Product Requirements and Business Rules |
| **Document Status** | **LOCKED / APPROVED** |
| **Version** | 1.0.0 |
| **Source of Truth** | Yes - Primary source for all business logic and product requirements. |
| **Intended Readers** | Human Developers, AI Coding Agents (Google Antigravity), Academic Evaluators. |
| **Update Authority** | Human Developer ONLY. (AI agents are strictly forbidden from modifying this document). |

*Note: This is a static, human-approved requirements document. It governs the behavioral constraints of the ServiceSync project.*

---

# 2. Product Overview

**ServiceSync** is an Enterprise Service Lifecycle and SLA Management Platform designed for physical appliance and IT repair centers.

Currently, local repair shops rely on fragmented paper tickets or basic spreadsheets, leading to untracked SLAs (Service Level Agreements), lost inventory, and poor customer communication. ServiceSync solves this by providing a unified digital workflow.

The system tracks a service request (Ticket) from initial walk-in creation, through diagnosis and technician repair, to final resolution and customer pickup. It inherently prevents inventory loss by tightly coupling spare-part usage to specific service tickets and monitors operational bottlenecks through a dedicated background SLA tracker.

**Project Boundaries:** ServiceSync focuses exclusively on ticket workflow, technician assignment, inventory deduction, and SLA monitoring. It explicitly avoids broader ERP, accounting, or payroll functionalities to maintain a concentrated, high-quality codebase suitable for an internship-level Java Full Stack demonstration.

---

# 3. Product Goals

* **Service-Ticket Lifecycle Management:** Digitally track 100% of device repairs through a deterministic state machine.
* **Technician Coordination:** Provide technicians with clear queues of assigned diagnostic and repair tasks.
* **Inventory Coordination:** Prevent "ghost inventory" by mandating that physical parts are digitally deducted the moment they are attached to a ticket.
* **SLA Monitoring:** Ensure no ticket sits in a stalled state (e.g., waiting for diagnosis) beyond acceptable business thresholds.
* **Customer Visibility:** Allow walk-in customers to view real-time shop-floor status via a physical kiosk, and check their individual ticket status via a public web lookup.
* **Operational Transparency:** Provide administrators with reporting on ticket volume and SLA performance.

---

# 4. Non-Goals

ServiceSync is explicitly **NOT**:

* A full ERP (Enterprise Resource Planning) system.
* An accounting or invoicing platform (it calculates a `total_cost`, but does not process payments).
* A payroll or HR management system.
* An e-commerce platform for selling parts directly to consumers.
* A complex CRM (Customer Relationship Management) tool.

*Scope creep into these areas is strictly forbidden.*

---

# 5. Actors and Roles

| Role | Purpose | Capabilities | Restrictions |
| --- | --- | --- | --- |
| **Administrator (Admin)** | Shop Owner / Manager | Full CRUD on Users, Inventory, and Tickets. Can reassign technicians. Views SLA reports. | None. |
| **Technician (Tech)** | Repair Staff | Views assigned tickets. Updates ticket status. Consumes inventory parts for a ticket. | Cannot alter base inventory prices. Cannot delete users or tickets. |
| **Customer** | Device Owner | Views read-only ticket status via public API or physical Shop Kiosk. | Cannot log in. Cannot create their own tickets. Cannot view internal technician notes. |
| **System (Microservice)** | Background Monitor | Polls core app for SLA breaches. Generates async PDF reports. Triggers notifications. | Cannot alter ticket diagnostic states. Cannot deduct inventory. |

---

# 6. Core Domain Concepts

* **Service Ticket:** The central business entity representing a customer's broken device, the reported issue, and its current repair lifecycle state.
* **Inventory Part:** A physical spare part (e.g., "Samsung Screen", "16GB RAM") with an associated SKU, stock quantity, and base price.
* **Ticket-Part Usage:** The transactional linkage between a Ticket and an Inventory Part. Represents a part permanently consumed to fix a device.
* **SLA (Service Level Agreement):** A defined time limit for a ticket to remain in a specific state before being flagged as breached.
* **Audit Log:** An immutable historical record of state changes, noting who changed a ticket's status and when.
* **Shop Kiosk:** A physical display monitor located in the repair shop's waiting area, showing a live queue of active ticket statuses.

---

# 7. Core Business Workflows

### 7.1 Customer Service Request (Walk-in)

A Customer brings a device to the shop. An Admin or Tech creates a new Service Ticket, capturing customer contact info and device symptoms. The system generates a unique Ticket ID.

### 7.2 Technician Assignment & Diagnosis

An Admin assigns an `OPEN` ticket to a specific Technician. The Technician begins diagnosis, transitioning the ticket to `DIAGNOSING`.

### 7.3 Repair & Inventory Usage

If the diagnosis requires spare parts, the Technician attempts to attach Inventory Parts to the Ticket.

* If parts are in stock, inventory is deducted, and the ticket moves to `IN_REPAIR`.
* If parts are out of stock, the system rejects the usage, and the ticket must manually be moved to `WAITING_PARTS`.

### 7.4 SLA Monitoring (Background)

The SLA Microservice periodically checks for tickets that have remained in the `CREATED` or `DIAGNOSING` state beyond the 48-hour threshold. If breached, the system records an SLA escalation event.

### 7.5 Ticket Resolution

Once the repair is complete, the Technician marks the ticket `RESOLVED`. The system automatically calculates the `total_cost` (Parts cost + standard labor fee).

### 7.6 Customer Status Visibility

The Customer can use their Ticket ID and Phone Number on a public web page to check if the device is `IN_REPAIR` or `RESOLVED`. Concurrently, the legacy Shop Kiosk displays active tickets on a rolling screen in the waiting room.

---

# 8. Ticket Lifecycle and State Machine

The Ticket status must strictly adhere to this state machine. Arbitrary status strings are prohibited.

* `CREATED`: Initial state. Ticket logged, pending assignment.
* `DIAGNOSING`: Technician is actively investigating the issue.
* `WAITING_PARTS`: Diagnosis complete, but required inventory is out of stock. Work is paused.
* `IN_REPAIR`: Parts are available (or no parts needed). Active repair is underway.
* `RESOLVED`: Repair is complete. Device is ready for pickup. Cost is finalized.
* `CLOSED`: Terminal state. Customer has retrieved the device.

**Valid Transitions:**

* `CREATED` → `DIAGNOSING` (Requires Technician assignment).
* `DIAGNOSING` → `IN_REPAIR` (Requires parts availability or no parts required).
* `DIAGNOSING` → `WAITING_PARTS` (If inventory check fails).
* `WAITING_PARTS` → `IN_REPAIR` (Admin updates inventory, Tech resumes work).
* `IN_REPAIR` → `RESOLVED` (Work complete).
* `RESOLVED` → `CLOSED` (Device handed back).

**Invalid Transitions:**

* Cannot move backward from `RESOLVED` to `IN_REPAIR`.
* Cannot move directly from `CREATED` to `CLOSED`.
* Cannot modify parts on a `CLOSED` ticket.

---

# 9. Business Rules

### Ticket Rules

* **BR-001:** Every ticket must have a valid Customer Name, Customer Phone, and Device Description upon creation.
* **BR-002:** Only Admins can reassign a ticket to a different Technician once it is no longer in `CREATED` state.
* **BR-003:** A ticket cannot transition to `RESOLVED` if there are pending inventory allocations that have not been fulfilled.

### Inventory Rules

* **BR-004:** Inventory stock can never drop below zero.
* **BR-005:** The price of a part consumed on a ticket is locked at the time of consumption (`price_at_time`). Subsequent price updates by an Admin to the master Inventory catalog do not retroactively alter closed tickets.

### SLA Rules

* **BR-006:** A ticket breaches SLA if it remains in `CREATED` or `DIAGNOSING` for more than 48 hours.
* **BR-007:** `WAITING_PARTS` pauses the SLA clock, as external shipping times are outside technician control.

### Kiosk Rules

* **BR-008:** The Legacy Kiosk display must automatically refresh every 30 seconds.
* **BR-009:** Tickets in the `CLOSED` state must instantly disappear from the Kiosk display.

---

# 10. Functional Requirements

| ID | Requirement Statement | Actor | Expected Behavior |
| --- | --- | --- | --- |
| **FR-AUTH-1** | System must authenticate staff via credentials. | Admin, Tech | Return JWT token on valid credentials. Deny access on invalid. |
| **FR-TKT-1** | System must allow creation of new service tickets. | Admin, Tech | Generates Ticket ID, sets state to `CREATED`. |
| **FR-TKT-2** | System must enforce state machine transitions. | Admin, Tech | Reject API requests attempting invalid state jumps. |
| **FR-INV-1** | System must decrement stock when part added to ticket. | Tech | Stock reduces by requested quantity. |
| **FR-INV-2** | System must lock historical part pricing. | Tech | Ticket calculates total using price at exact moment of usage. |
| **FR-PUB-1** | System must allow unauthenticated ticket lookup. | Customer | If Ticket ID + Phone match, return sanitized status. |
| **FR-SLA-1** | System must flag breached SLAs. | System | Background job marks tickets exceeding 48h limits. |
| **FR-KSK-1** | Kiosk must display active tickets. | Customer | Show tabular view of `Ticket ID |

---

# 11. Non-Functional Requirements

* **NFR-SEC:** API endpoints (except public lookup and login) must be secured via HTTP Bearer Authentication (JWT).
* **NFR-PERF:** Core API state transitions and inventory deductions must respond in < 500ms.
* **NFR-UI:** The frontend must be responsive (mobile/desktop friendly) using Bootstrap 5, as technicians will use tablets on the shop floor.
* **NFR-ISO:** The legacy Kiosk must be isolated from the core application's ORM layer to prevent database mapping contamination.

---

# 12. Security and Authorization Rules

* **Authentication:** All internal actors (Admins, Techs) must authenticate to interact with the system. JWT tokens expire in 12 hours.
* **Role-Based Access Control (RBAC):**
* `/api/admin/**` routes are strictly reserved for `ROLE_ADMIN`.
* `/api/tickets/{id}/parts` requires `ROLE_TECH` or `ROLE_ADMIN`.


* **Public API Restriction:** The public lookup endpoint must never expose Technician Notes, Customer PII (beyond first name), or internal audit logs.
* **OAuth2 / Google Login:** *DEFERRED*. Standard JWT credential login is the primary requirement for MVP.

---

# 13. Data Integrity and Transactional Rules

* **Atomic Inventory Deduction:** The action of adding a part to a ticket and deducting it from master inventory must be strictly atomic (`@Transactional`). If the database fails to record the ticket-part usage, the inventory deduction must roll back immediately.
* **Concurrent Inventory Access:** If Technician A and Technician B attempt to consume the last "Samsung Screen" simultaneously, the system must utilize Optimistic Locking to guarantee that one succeeds and the other receives a `409 Conflict` (Out of Stock) error.
* **Legacy Read-Isolation:** The Kiosk must NOT read from core Hibernate tables directly. It must read from a predefined MySQL `VIEW` that sanitizes the data structure.

---

# 14. Concurrency and Asynchronous Behavior

* **Background SLA Polling:** The SLA Microservice runs independent scheduled polling threads to detect breaches without degrading core API performance.
* **Asynchronous Report Generation:** When an Admin requests a monthly PDF report, the core system offloads this task to the Notification/Report Microservice asynchronously, immediately returning an HTTP 202 Accepted to the UI.

---

# 15. Legacy Kiosk Requirements

The Shop Kiosk is a deliberate, academic subsystem designed to demonstrate Java Servlets and JSP legacy capabilities.

* **Purpose:** Act as an unattended "Flight Information Display" for the shop waiting room.
* **Constraint 1:** It must be built using raw `HttpServlet`, `jsp`, and `java.sql.Connection` (JDBC).
* **Constraint 2:** It must NEVER use Spring Boot, JPA, or Hibernate.
* **Constraint 3:** It must only perform `SELECT` queries against the `active_display_tickets` SQL View. It cannot write data.

---

# 16. Notifications and Events

* **Event Trigger:** When a Ticket state changes, the core domain must emit a `TicketStatusChangedEvent`.
* **Audit Listener:** An internal observer listens to this event and writes a record to the `ticket_history` table.
* **Microservice Listener:** An integration observer listens to this event and pushes a notification payload via OpenFeign to the external Notification Microservice to simulate an SMS/Email dispatch.

---

# 17. Reporting Requirements

The system must support the extraction of operational data:

* **Admin Dashboard:** Real-time count of tickets in `CREATED`, `DIAGNOSING`, and `WAITING_PARTS` states.
* **SLA Breach Report:** A downloadable list of all tickets that breached the 48-hour SLA in the past 30 days.
* *(Implementation Note: Do not build complex interactive charts for the MVP. Standard JSON data tables rendered via Bootstrap are sufficient).*

---

# 18. Edge Cases and Exceptional Scenarios

| Edge Case | Expected Business Behavior |
| --- | --- |
| **Insufficient Inventory** | System rejects the part-usage request, throws domain exception, prevents state change to `IN_REPAIR`, returns HTTP 409. |
| **Public Lookup with wrong Phone** | System returns generic "Not Found" to prevent brute-force discovery of ticket existence. |
| **Microservice is Down** | Core API must not crash. Event dispatches to the microservice must fail gracefully (logged as warnings) while the core ticket transition succeeds. |
| **Invalid State Transition** | System throws `InvalidTicketStateException`, returning HTTP 400 Bad Request to the frontend. |

---

# 19. Scope Boundaries

### In Scope (MVP)

* Core Spring Boot Monolith (Ticketing, Inventory, Users).
* MySQL Database with Views and Optimistic Locking.
* Notification/SLA Microservice communicating via OpenFeign.
* Legacy Servlet/JSP Kiosk.
* Vanilla JS + Bootstrap Frontend.

### Deferred / Optional (Post-MVP)

* OAuth2 Google Login for Customers.
* Real SMS gateway integration (e.g., Twilio).
* Payment Gateway integration (e.g., Stripe).

### Out of Scope

* Employee Payroll calculation.
* Mobile App development (iOS/Android).
* Frontend frameworks (React, Angular).

---

# 20. Curriculum Demonstration Requirements

| Curriculum Topic | ServiceSync Requirement | Expected Demonstration |
| --- | --- | --- |
| **Java OOP / Exceptions** | Core Domain & Edge Cases | Entities, `InvalidTicketStateException`. |
| **Multithreading** | SLA & Report Generation | `@Async` execution in Microservice. |
| **SQL / MySQL** | Data Persistence & Kiosk | Fully normalized schema, `active_display_tickets` VIEW. |
| **JDBC** | Kiosk Operation | Raw `ResultSet` mapping in Servlet. |
| **Servlets / JSP** | Kiosk UI | Deployed `.war` rendering HTML via JSP implicit objects. |
| **JPA / Hibernate** | Core App Persistence | Repositories, Entities, `@Version` optimistic locking. |
| **Spring Core / Boot** | Core Architecture | DI, Controllers, Services. |
| **Spring Security / REST** | API Access | Filter chains, JWT generation, HTTP verbs. |
| **Microservices / Feign** | SLA Monitor | Independent Spring Boot app communicating via HTTP clients. |
| **AOP** | Operational Auditing | `@Around` aspect tracking query performance. |
| **HTML / CSS / JS** | Main Dashboard | Fetch API consuming REST, Bootstrap DOM manipulation. |

---

# 21. Acceptance Criteria

* **AC-1:** An Admin can create a ticket, assign it to a Tech, and the Tech can transition it successfully to `RESOLVED` following the state machine.
* **AC-2:** If a Tech attempts to add a part with `0` stock, the system explicitly rejects the transaction and inventory remains unchanged.
* **AC-3:** If two Techs concurrently attempt to claim the last unit of a specific part, one succeeds and one receives an Optimistic Locking failure.
* **AC-4:** The public tracking endpoint successfully returns status information ONLY if the correct Ticket ID and Phone Number combination is provided.
* **AC-5:** The Kiosk web app successfully displays tickets from the database using only JDBC (no Spring/Hibernate traces in the Kiosk module).
* **AC-6:** The Microservice successfully logs an SLA breach when simulated time exceeds 48 hours.

---

# 22. AI Coding-Agent Rules

**CRITICAL INSTRUCTIONS FOR GOOGLE ANTIGRAVITY OR ANY AI AGENT:**

1. **Static Source of Truth:** This document is the absolute, immutable source of truth for business logic. You must NOT silently modify this document.
2. **No Requirements Invention:** You must not invent missing business rules, state transitions, or user roles. If a requirement is missing, halt and ask the human developer.
3. **Strict Architectural Adherence:** You must respect the validated architecture.
* You MUST NOT introduce React, Vue, Angular, or any frontend framework. Use Vanilla JS and Bootstrap.
* You MUST NOT introduce JPA/Hibernate into the Kiosk module. Use raw JDBC.
* You MUST NOT merge the SLA Microservice back into the core monolith.


4. **No Schema Alteration:** You must not modify the database schema without explicit human approval.
5. **State Machine Enforcement:** You must strictly implement the state machine exactly as defined in Section 8. Do not invent intermediate states to make code compile faster.
6. **State Management:** Use `90_ACTIVE_TASK.md` and `99_SESSION_HANDOFF.md` for execution context. Do not append implementation details to this PRD document.
7. **Conflict Resolution:** If your proposed implementation conflicts with this document, YOU MUST STOP and identify the conflict to the human user.

---

# 23. Source-of-Truth Boundaries

To prevent conflicting context, refer to specific documents for specific concerns:

| Concern | Authoritative Document |
| --- | --- |
| **Business rules, roles, state machine** | `01_PRD_AND_RULES.md` (This Document) |
| **Module boundaries, technology stack** | `02_ARCHITECTURE.md` |
| **Table definitions, SQL constraints, Views** | `03_DATABASE.md` |
| **JSON payloads, REST endpoints** | `04_API_SPEC.md` |
| **DOM interactions, frontend layout** | `05_UI_SPEC.md` |
| **Running the app, env variables, tests** | `06_SETUP_AND_TEST.md` |
| **What I am building right now** | `90_ACTIVE_TASK.md` (Dynamic) |
| **Where I left off last session** | `99_SESSION_HANDOFF.md` (Dynamic) |

---

# 24. Requirement Traceability

* **Business Goal:** Inventory Coordination → **FR-INV-1** (Decrement stock) → **BR-004** (Stock cannot be < 0) → **AC-2** (System rejects 0-stock transaction).
* **Business Goal:** Customer Visibility → **FR-KSK-1** (Kiosk display) → **BR-008** (Auto-refresh) → **AC-5** (Kiosk uses JDBC).
* **Business Goal:** SLA Monitoring → **FR-SLA-1** (Flag breaches) → **BR-006** (48h threshold) → **AC-6** (Microservice logs breach).

---

# 25. Open Decisions

* **DEC-001 (DEFERRED): Google OAuth2 Integration**
* *Topic:* Should customers authenticate via Google to view tickets?
* *Why it matters:* Adds curriculum coverage but increases UI scope and deviates from standard physical shop flows (where phone + receipt ID is standard).
* *Status:* **DEFERRED** to Post-MVP based on Stage 2 Architectural Audit. The core MVP relies on Phone + Ticket ID lookup.



---

# 26. Consistency and Completeness Audit

* *Validation Check:* Do state transitions match the allowed RBAC capabilities? **Pass.**
* *Validation Check:* Is the Kiosk constraint consistent with the Stage 2 audit? **Pass. Decoupled via SQL View.**
* *Validation Check:* Does SLA asynchronous processing violate monolith transaction rules? **Pass. Offloaded to independent microservice via OpenFeign.**

---

# 27. Final Requirement Summary

**ServiceSync** is an internship-grade, multi-module Java Full Stack platform designed to track appliance repair workflows. It empowers **Admins** and **Technicians** to progress **Service Tickets** through a strict state machine (`CREATED` → `CLOSED`), ensuring that physical **Inventory Parts** are atomically deducted using Optimistic Locking.

The system enforces isolation of legacy curriculum (JSP/Servlet/JDBC) via a read-only **Shop Kiosk**, and offloads asynchronous **SLA Monitoring** to an independent Spring Boot Microservice. The frontend relies exclusively on **Vanilla JS and Bootstrap**, avoiding unnecessary JavaScript frameworks. The primary constraints are architectural strictness, transactional integrity, and alignment with the academic curriculum.
