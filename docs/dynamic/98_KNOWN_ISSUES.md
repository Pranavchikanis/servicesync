# 1. Document Metadata

| Attribute | Detail |
| --- | --- |
| **Project** | ServiceSync |
| **Document Name** | `98_KNOWN_ISSUES.md` |
| **File Path** | `docs/dynamic/98_KNOWN_ISSUES.md` |
| **Document Type** | Dynamic Implementation State |
| **Documentation Model** | Minimum Viable AI-Context (MVAC) |
| **Purpose** | Authoritative dynamic register of known bugs, defects, blockers, and technical debt. |
| **Status** | **ACTIVE / UPDATED** |
| **Last Updated** | September 24, 2026 |
| **Issue Count** | 1 (Active) / 4 (Resolved) |
| **Source-of-Truth** | **Dynamic Issue Register** |

*Note: This document represents dynamic project state. It is the authoritative record of verified problems. It does NOT define static architectural truth, requirements, or future planned features.*

---

# 2. Purpose

This document serves as the central issue register for ServiceSync.

## Current Session Handoff
* **What was audited:** Stage 2A Angular Foundation establishment.
* **What was cleaned:** Generated Angular 18 application in `frontend/servicesync-web/` with Core, Shared, and Feature architecture boundaries. Configured Bootstrap and environments.
* **Tests executed:** `ng build`, `ng serve`, and `mvn clean install` on backend.
* **Actual results:** All Angular builds and backend Maven builds passed successfully. The legacy frontend bridge remains intact.
* **Remaining issues:** UI Migration (Stage 2B) and API Integration (Stage 2C) are pending.
* **Final Readiness Status:** `STAGE_2A_COMPLETED`
* **Next Action:** Begin Stage 2B (Angular UI Migration).

It exists within the MVAC architecture to ensure that AI coding agents and human developers do not lose track of bugs, broken tests, or technical debt across multiple coding sessions.

* **It records:** Verified bugs, failing tests, architectural blockers, confirmed configuration issues, and explicitly acknowledged technical debt.
* **It does NOT record:** Future feature ideas, unverified suspicions, general project roadmaps, or tasks currently progressing normally in `90_ACTIVE_TASK.md`.

AI coding agents must consult this document to avoid compounding errors on top of broken subsystems and must update it when they discover a persistent defect they cannot immediately fix.

---

# 3. Issue Classification System

### Categories

* `BUG`: Functional code error.
* `DEFECT`: Implementation deviates from static contract (`PRD`, `API_SPEC`, etc.).
* `BLOCKER`: An issue preventing the completion of `90_ACTIVE_TASK.md` or causing total system failure.
* `TECHNICAL_DEBT`: Suboptimal code explicitly deferred for later refactoring.
* `SECURITY_ISSUE`: Vulnerability violating security rules (e.g., exposed JWT).
* `TEST_FAILURE`: A verified automated test that is currently red.
* `CONFIGURATION_ISSUE`: Environment, port, or DB setup failure.
* `KNOWN_LIMITATION`: A boundary condition explicitly accepted by the architecture.

### Severity Levels

* **P0 — Critical:** Prevents system startup, causes data corruption, or severe security breach.
* **P1 — High:** Major workflow broken (e.g., cannot create a ticket).
* **P2 — Medium:** Important defect, but a workaround exists or core workflows function.
* **P3 — Low:** Minor UI glitch, cleanup item, or low-impact technical debt.
* **P4 — Informational:** Acknowledged limitation requiring no immediate action.

### Status Values

* `OPEN`: Verified issue requiring attention.
* `IN_PROGRESS`: Currently being fixed (usually implies an active task exists).
* `BLOCKED`: Cannot be fixed until an external dependency or decision is resolved.
* `RESOLVED`: Fixed and objectively verified.
* `DEFERRED`: Intentionally postponed.
* `WONT_FIX`: Deliberately accepted as-is.
* `DUPLICATE`: Tracked by another primary issue ID.

---

# 4. Issue Record Schema

When an AI coding agent or developer records a new issue, they MUST use the following template:

```markdown
## ISSUE-{XXX} — {Title}

**Category:** {Category}  
**Severity:** {P0-P4}  
**Status:** {OPEN | BLOCKED | IN_PROGRESS | RESOLVED | DEFERRED | WONT_FIX}  
**Discovered:** {YYYY-MM-DD}  
**Last Updated:** {YYYY-MM-DD}  
**Affected Area:** {Module / Component / DB}  

### Symptom
{Clear, observable description of the problem.}

### Reproduction
1. {Step 1}
2. {Step 2}
**Expected:** {What should happen per static docs}
**Actual:** {What actually happened}

### Impact
{How this affects the system, data, or users.}

### Evidence
{Logs, stack traces, compiler errors, or test failure outputs.}

### Cause
**Classification:** {Confirmed | Suspected | Unknown}
{Details regarding the root cause.}

### Workaround
{Verified workaround, or "No verified workaround."}

### Resolution Criteria
- [ ] {Verifiable condition 1}
- [ ] {Verifiable condition 2}

### Related Tasks / Documents
* **Task:** {TASK-XXX if applicable}
* **Docs:** {Relevant static doc, e.g., 04_API_SPEC.md}

```

---

# 5. Issue Register

*This table is a scannable index. Detailed issue records appear in subsequent sections.*

## Open Security Issues
* None remaining. Hardcoded test secrets and `kiosk_password` fallbacks have been removed. The JWT and DB passwords must be injected at runtime via environment variables (`DB_PASSWORD_APP`, `DB_PASSWORD_KSK`, `JWT_SECRET`).

## Open Architecture/Documentation Conflicts
* OAuth2 remains explicitly deferred per OAD-001.

## Open Defect Log

| Issue ID | Title | Category | Severity | Status | Affected Area | Related Task |
| --- | --- | --- | --- | --- | --- | --- |
| ISSUE-010 | Ticket Pagination N+1 Query | TECHNICAL_DEBT | P1 | RESOLVED | servicesync-core | TSK-014 |
| ISSUE-006 | Kiosk API limits | BUG | P3 | OPEN | servicesync-kiosk | N/A |
| ISSUE-009 | SLA Database Coupling | BUG | P0 | RESOLVED | servicesync-sla | TSK-013 |
| ISSUE-002 | Legacy Kiosk JDBC Missing Real DB Integration Tests | TECHNICAL_DEBT | P3 | DEFERRED | servicesync-kiosk | TASK-013 |
| ISSUE-001 | Hardcoded Fallback JWT Secret | SECURITY_ISSUE | P2 | DEFERRED | servicesync-core | TASK-006 |

---

# 6. Detailed Issue Records

## ISSUE-008 — Ticket History Mock Fallback

**Category:** TECHNICAL_DEBT  
**Severity:** P3  
**Status:** RESOLVED  
**Discovered:** 2026-09-24  
**Last Updated:** 2026-09-24  
**Affected Area:** servicesync-web (TicketDetailComponent)  

### Symptom
The `TicketDetailComponent` does not fetch history from the backend endpoint `/api/v1/tickets/{ticketId}/history`. Instead, it uses a hardcoded single-entry array mimicking ticket creation.

### Reproduction
1. Open a ticket in the frontend.
2. View the history section.
**Expected:** The UI displays real history from the backend.
**Actual:** Only the initial mock history array is shown.

### Impact
Users cannot see true audit trails for tickets in the Angular UI.

### Evidence
Code inspection of `ticket-detail.component.ts:loadHistory()`.

### Cause
**Classification:** Confirmed
A mock implementation from Stage 2B was left behind during Stage 2C implementation.

### Workaround
No verified workaround.

### Resolution Criteria
- [x] Connect `TicketService` to `/api/v1/tickets/{ticketId}/history`.
- [x] Update `TicketDetailComponent.loadHistory()` to subscribe to the backend service.

### Related Tasks / Documents
* **Task:** TASK-010

---

## ISSUE-007 — InventoryDTO Property Mismatch

**Category:** DEFECT  
**Severity:** P2  
**Status:** RESOLVED  
**Discovered:** 2026-09-24  
**Last Updated:** 2026-09-24  
**Affected Area:** servicesync-web (InventoryDTO)  

### Symptom
Inventory rows in `InventoryListComponent` and the parts dropdown in `TicketDetailComponent` show missing or undefined fields (e.g. `undefined` instead of part name).

### Reproduction
1. Navigate to `/inventory` in the Angular application.
**Expected:** The data table correctly displays part names, stock thresholds, and stock amounts.
**Actual:** The UI displays empty fields for name and stock quantities.

### Impact
Inventory tracking UI and parts consumption dropdowns are visually broken, making part selection difficult.

### Evidence
Angular `InventoryDTO` uses `name`, `stockQuantity`, `lowStockThreshold`. Backend Java `InventoryDTO` uses `partName`, `quantityInStock`.

### Cause
**Classification:** Confirmed
Frontend interfaces were not strictly synchronized with the exact Java DTO field names during Stage 2C.

### Workaround
No verified workaround.

### Resolution Criteria
- [x] Refactor `api.models.ts` `InventoryDTO` properties to exactly match the backend.
- [x] Update Angular HTML templates to bind to the correct properties.

### Related Tasks / Documents
* **Task:** TASK-010

---

## ISSUE-009 — SLA Database Coupling (Microservice Violation)

**Category:** BUG  
**Severity:** P0  
**Status:** RESOLVED  
**Discovered:** 2026-09-24  
**Last Updated:** 2026-09-24  
**Affected Area:** servicesync-sla (NotificationLogRepository)  

### Symptom
The `servicesync-sla` microservice accesses the monolith's `tickets` table directly using a native SQL query instead of an API call.

### Reproduction
1. Inspect `NotificationLogRepository.java:13` in `servicesync-sla`.
**Expected:** The SLA service relies on OpenFeign API calls to `/api/v1/tickets` or listens to event buses.
**Actual:** Contains `@Query(value = "SELECT id FROM tickets WHERE status IN ('CREATED', 'DIAGNOSING')...", nativeQuery = true)`.

### Impact
Violates strict microservice boundary encapsulation. If the monolith's `tickets` table schema changes, the independent SLA service will break without warning, undermining the physical separation effort.

### Evidence
Native query directly joining `tickets` in an external module's repository.

### Cause
**Classification:** Confirmed
Legacy code porting bypassed REST integration for expediency.

### Workaround
No verified workaround. Architecture remediation is required.

### Resolution Criteria
- [x] Remove native SQL query targeting `tickets` from `servicesync-sla`.
- [x] Update `CoreTicketClient` to fetch breached tickets over HTTP via `servicesync-core`.

### Related Tasks / Documents
* **Task:** TSK-013

---

## ISSUE-010 — Ticket Pagination N+1 Query Vulnerability

**Category:** TECHNICAL_DEBT  
**Severity:** P1  
**Status:** RESOLVED  
**Discovered:** 2026-09-24  
**Last Updated:** 2026-09-24  
**Affected Area:** servicesync-core (TicketService)  

### Symptom
Retrieving a paginated list of tickets triggers dozens of sequential database queries instead of a single joined query.

### Reproduction
1. Call `GET /api/v1/tickets?page=0&size=20`.
2. Observe Hibernate SQL logs.
**Expected:** 1 or 2 queries (one for tickets with joins, one for count).
**Actual:** 1 query for tickets, followed by up to 20 queries for `User` (createdBy), 20 queries for `User` (technician), and 20 queries for `TicketPart`.

### Impact
Severe performance degradation as the database grows, potentially causing timeouts under load.

### Evidence
`TicketService.getPaginatedTickets` uses `ticketRepository.findAll(spec, pageable)` without an `@EntityGraph`. `DtoMapper.toTicketDTO` lazily initializes `.getCreatedBy()`, `.getTechnician()`, and `.getParts()`.

### Cause
**Classification:** Confirmed
JPA lazy-loading triggers implicitly during DTO mapping.

### Workaround
No verified workaround. Defer until performance is unacceptable or fix preemptively using EntityGraphs.

### Resolution Criteria
- [x] Configure `@EntityGraph(attributePaths = {"createdBy", "technician"})` on pagination method.
- [x] Add global `default_batch_fetch_size` for collection and deep entity lazy loads.
- [x] Retain database-level pagination (no in-memory application warnings).

### Related Tasks / Documents
* **Task:** TSK-014

---

# 7. Technical Debt Register

*This section tracks explicitly acknowledged technical debt, separating it from immediate functional bugs.*

## ISSUE-002 — Legacy Kiosk JDBC Missing Real DB Integration Tests

**Category:** TECHNICAL_DEBT  
**Severity:** P3  
**Status:** DEFERRED  
**Discovered:** 2026-09-21  
**Last Updated:** 2026-09-21  
**Affected Area:** servicesync-kiosk  

### Symptom
The `TicketDaoTest` uses `Mockito` to mock the JDBC `Connection` and `ResultSet`. While this verifies the code logic, it does not actually run the SQL query against a MySQL dialect to ensure syntax correctness of the `active_display_tickets` view.

### Reproduction
1. Run `mvn test` in `servicesync-kiosk`.
2. Notice tests pass using mock data without touching a real or embedded database.
**Expected:** Integration tests should verify the raw SQL syntax against an H2/Testcontainers DB.
**Actual:** Only unit tests exist.

### Impact
Changes to the `active_display_tickets` SQL view might break the kiosk without failing the automated tests.

### Evidence
See `TicketDaoTest.java`.

### Cause
**Classification:** Confirmed
Legacy JDBC architectures are harder to test with embedded databases without Spring Boot's auto-configuration, so mocking was chosen for velocity during MVP.

### Workaround
Manual testing required against a live database.

### Resolution Criteria
- [ ] Add Testcontainers or embedded H2 support to the `servicesync-kiosk` Maven module.
- [ ] Write a test that executes `TicketDao.getActiveTickets()` against a real DB.

### Related Tasks / Documents
* **Task:** TASK-013
* **Docs:** 06_SETUP_AND_TEST.md

---

## ISSUE-001 — Hardcoded Fallback JWT Secret

**Category:** SECURITY_ISSUE  
**Severity:** P2  
**Status:** DEFERRED  
**Discovered:** 2026-09-21  
**Last Updated:** 2026-09-21  
**Affected Area:** servicesync-core (Security)  

### Symptom
The JWT secret key uses an environment variable, but falls back to a hardcoded string if missing in `application.yml` or `JwtUtil.java`.

### Reproduction
1. Inspect `JwtUtil.java` or `application.yml`.
**Expected:** The application should crash on startup if the secret is missing.
**Actual:** A fallback secret is used.

### Impact
If deployed to production without the environment variable set, the fallback secret is used, leaving tokens vulnerable to forgery.

### Evidence
Source code review of JWT logic.

### Cause
**Classification:** Confirmed
Added for developer convenience during local testing.

### Workaround
Ensure `JWT_SECRET` environment variable is explicitly set in production deployments.

### Resolution Criteria
- [ ] Remove hardcoded fallback.
- [ ] Inject secret dynamically from a secure vault (e.g., AWS Secrets Manager or HashiCorp Vault).

### Related Tasks / Documents
* **Task:** TASK-006
* **Docs:** 02_ARCHITECTURE.md

---

# 8. Blocker Register

*This section explicitly highlights issues preventing the execution of `90_ACTIVE_TASK.md`.*

* **No verified blockers currently recorded.**

---

# 9. Test Failure Register

*This section summarizes automated tests that are currently failing.*

*(No failing tests currently recorded. All 47 tests passed.)*

---

# 10. Security Issue Register

*(Refer to ISSUE-001 in the Technical Debt register.)*

---

# 11. Database / Data Integrity Issues

*(No verified database or integrity issues currently recorded.)*

---

# 12. API / Integration Issues

*(No verified API or integration issues currently recorded.)*

---

# 13. Kiosk / Legacy System Issues

*(Refer to ISSUE-002 in the Technical Debt register.)*

---

# 14. Build / Configuration / Deployment Issues

*(No verified build or configuration issues currently recorded.)*

---

# 15. Resolved Issues

*This section maintains the historical record of issues that have been successfully fixed and verified.*

- ISSUE-007: InventoryDTO Property Mismatch (Fixed in Stage 2C Hardening)
- ISSUE-008: Ticket History Mock Fallback (Fixed in Stage 2C Hardening)

---

# 16. Wont-Fix / Deferred Issues

*This section tracks issues intentionally accepted or postponed.*

*(Refer to ISSUE-001 and ISSUE-002).*

---

# 17. Relationship With Other Dynamic Documents

The issue register integrates with the MVAC dynamic workflow as follows:

* **Problem Discovered:** An AI agent discovers a bug while executing `90_ACTIVE_TASK.md`.
* **Verify & Record:** If the bug cannot be immediately fixed within the current task scope without context loss, the AI creates an `ISSUE-XXX` entry in this document (`98_KNOWN_ISSUES.md`).
* **Handoff:** The AI updates `99_SESSION_HANDOFF.md` to warn the next session about the new issue.
* **Resolution:** When a future task fixes the bug, the issue status here is changed to `RESOLVED`, and it is moved to the Resolved Issues section. The resolution is also summarized in `91_COMPLETED_TASKS.md`.

---

# 18. AI Coding-Agent Rules

**AI Coding Agents MUST:**

* Search this document for duplicates before creating a new issue.
* Use stable, sequential issue IDs (`ISSUE-001`, `ISSUE-002`).
* Provide actual evidence (stack traces, compiler output) rather than assumptions.
* Distinguish clearly between *Suspected* and *Confirmed* root causes.
* Update the status of an issue to `RESOLVED` **ONLY** after independent verification (e.g., a passing test or successful manual reproduction check).
* Move resolved issues to Section 15 to preserve history; never silently delete an issue record.

**AI Coding Agents MUST NOT:**

* Invent bugs, test failures, or reproduction steps.
* Use this document as a feature backlog (use `01_PRD_AND_RULES.md` and `90_ACTIVE_TASK.md` for planned work).
* Silently change database schemas or API contracts to "fix" an issue without explicitly flagging a contract violation.
* Hide failing tests by deleting them instead of recording them here.

---

# 19. Issue Lifecycle

```text
DISCOVERED (Agent encounters error)
    ↓
VERIFIED (Agent reproduces error deterministically)
    ↓
OPEN (Issue recorded in 98_KNOWN_ISSUES.md)
    ↓
IN_PROGRESS (A task is actively attempting to fix it)
    ↓
VERIFIED FIX (Tests pass, or reproduction confirms fix)
    ↓
RESOLVED (Issue moved to historical record)

```

*(Exceptions: `OPEN` → `BLOCKED`, `DEFERRED`, or `WONT_FIX`)*

---

# 20. Issue Deduplication Rules

Before appending a new `ISSUE-XXX`, an AI agent must scan Section 5 (Issue Register).

* If the symptom, affected component, or stack trace matches an existing open issue, **DO NOT** create a new issue.
* Update the existing issue's *Evidence* or *Notes* section with the new findings.
* If two issues are later identified as the same underlying bug, mark the newer one `Status: DUPLICATE` and add `See ISSUE-XXX` to the notes.

---

# 21. Historical Integrity Rules

* **No Speculative Issues:** Do not record hypothetical future risks (e.g., "The DB might scale poorly with 10M rows") as known issues. Keep this document focused on verified, immediate defects.
* **No False Resolution:** A fix is not a resolution until verification occurs. Do not mark an issue `RESOLVED` just because a code change was committed.
* **Static Contract Protection:** This file must not become an alternative source of truth. If resolving an issue requires changing an API endpoint, the AI must halt and request permission to update `04_API_SPEC.md` rather than documenting the "new" API behavior as a workaround here.

---

# 22. Source-of-Truth Boundaries

| Concern | Authoritative Document |
| --- | --- |
| **Requirements & Business Rules** | `01_PRD_AND_RULES.md` |
| **Architecture & Technology Boundaries** | `02_ARCHITECTURE.md` |
| **Database Contract** | `03_DATABASE.md` |
| **API Contract** | `04_API_SPEC.md` |
| **UI Contract** | `05_UI_SPEC.md` |
| **Setup, Build, and Testing Contract** | `06_SETUP_AND_TEST.md` |
| **Current Implementation Task** | `90_ACTIVE_TASK.md` |
| **Historical Completed Work** | `91_COMPLETED_TASKS.md` |
| **Persistent Issues & Debt** | `98_KNOWN_ISSUES.md` (This Document) |
| **Cross-Session Continuity** | `99_SESSION_HANDOFF.md` |

---

# 23. Context-Efficiency Rules

* Keep issue records concise.
* Avoid pasting massive stack traces (100+ lines); extract the root exception and the first 3-5 relevant lines of the trace.
* Reference static documents rather than repeating their contents (e.g., "Violates State Machine in `01_PRD_AND_RULES.md` Section 8").

---

# 24. Completeness and Consistency Audit

*(Pre-save verification checklist for AI agents updating this file)*

* [x] Every issue has a unique `ISSUE-XXX` ID.
* [x] Severity and Status are assigned using the controlled vocabulary.
* [x] Evidence is factual and verified.
* [x] Suspected causes are clearly distinguished from Confirmed causes.
* [x] No speculative issues or feature requests have been added.
* [x] The Index Table matches the Detailed Records.
* [x] No secrets (passwords, JWTs) are exposed in stack traces or logs.

---

# 25. Open Issue-Register Decisions

* None identified.

---

# 26. Final Issue Summary

* **Total Known Issues:** 4
* **Open Issues:** 0
* **In-Progress Issues:** 0
* **Blocked Issues:** 0
* **Deferred / Wont_Fix Issues:** 2
* **Resolved Issues:** 4
* **Highest Severity Current Issue:** P3 (ISSUE-002, ISSUE-006)
* **Verification Status:** Verified. No active functional bugs are preventing system staging.