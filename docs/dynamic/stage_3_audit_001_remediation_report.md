# Stage 3 — Architecture Remediation Report

## 1. Executive Summary
This report summarizes the remediation of `AUD-001` (SLA Database Coupling), a CRITICAL architectural violation identified during the Stage 2 post-migration system audit. The `servicesync-sla` microservice previously queried the core monolithic database directly using a native SQL query to find SLA breaches. This was replaced by an API-driven architecture using Spring Cloud OpenFeign. The SLA service now communicates with `servicesync-core` over an internal REST API, preserving business logic while successfully decoupling the database. All existing tests pass, and no database or frontend regressions were introduced.

## 2. Original Architecture
The `servicesync-sla` microservice operated on port 8081 and scheduled a breach check every minute. During this check, it used `NotificationLogRepository.java` to execute a native `@Query` directly against the `tickets` table owned by the `servicesync-core` monolith (port 8080).

Path: `SLA Scheduled Task -> SLA Repository -> Native SQL -> Core Database (tickets table)`

## 3. AUD-001 Root Cause
During legacy porting or MVP development, developers bypassed proper REST API integration to quickly implement SLA monitoring. This created a Shared Database Integration anti-pattern, violating microservice encapsulation and exposing the SLA module to unintended breakage if the Core database schema changed.

## 4. Remediation Architecture
The architecture was updated to use a strictly API-driven approach using OpenFeign.

New Path:
`SLA Scheduled Task -> SLA CoreTicketClient (Feign) -> HTTP GET -> Core InternalTicketController -> Core TicketService -> Core Database`

## 5. Core API Contract
A new internal endpoint was established in `servicesync-core`:
- **URL**: `GET /api/internal/v1/tickets/breaches?threshold={isoDateTime}`
- **Response**: `List<Long>` (representing breached ticket IDs)
- **Security**: Bound to `/api/internal/**`, which was added to `SecurityConfig` with `permitAll()` to allow seamless internal service-to-service communication on the trusted backend network.
- **Service Layer**: A new method `getSlaBreachedTicketIds(LocalDateTime)` was added to `TicketService`, utilizing a new custom `@Query` in `TicketRepository`.

## 6. OpenFeign Implementation
- `spring-cloud-starter-openfeign` dependency was added to `servicesync-sla/pom.xml`.
- `@EnableFeignClients` was applied to `ServiceSyncSlaApplication`.
- A new `CoreTicketClient` Feign interface was created in `servicesync-sla` mapping to the new core API.
- `SlaMonitorService` was refactored to use `CoreTicketClient` instead of `NotificationLogRepository`.

## 7. Database Coupling Removal
- Removed `findBreachedTicketIds()` and its associated native SQL query from `NotificationLogRepository.java`.
- Verified no other database queries against the `tickets` table remain in the SLA module.

## 8. Files Changed
1. `backend/servicesync-core/src/main/java/com/servicesync/core/repository/TicketRepository.java` (Added Query)
2. `backend/servicesync-core/src/main/java/com/servicesync/core/service/TicketService.java` (Added Service Logic)
3. `backend/servicesync-core/src/main/java/com/servicesync/core/api/controller/InternalTicketController.java` (Created Internal API)
4. `backend/servicesync-core/src/main/java/com/servicesync/core/security/SecurityConfig.java` (Permitted Internal Route)
5. `backend/servicesync-sla/pom.xml` (Added Feign Dependency)
6. `backend/servicesync-sla/src/main/java/com/servicesync/sla/ServiceSyncSlaApplication.java` (Enabled Feign)
7. `backend/servicesync-sla/src/main/java/com/servicesync/sla/client/CoreTicketClient.java` (Created Feign Client)
8. `backend/servicesync-sla/src/main/java/com/servicesync/sla/service/SlaMonitorService.java` (Updated Logic)
9. `backend/servicesync-sla/src/main/java/com/servicesync/sla/repository/NotificationLogRepository.java` (Removed Native Query)
10. `backend/servicesync-sla/src/test/java/com/servicesync/sla/service/SlaMonitorServiceTest.java` (Updated Mock Tests)

## 9. Tests Added/Modified
- `SlaMonitorServiceTest.java` was updated to mock `CoreTicketClient` instead of `NotificationLogRepository` for breach ticket retrieval.

## 10. Backend Build Results
- `mvn clean install` executed successfully across `servicesync-parent`.
- `servicesync-core`: 43 tests passed.
- `servicesync-sla`: 4 tests passed.
- `servicesync-kiosk`: 1 test passed.
- Total Tests: 47/47 Passed.

## 11. Angular Build Results
- `npx ng build` executed successfully.
- Application bundle generation completed cleanly.

## 12. Regression Verification
- The Angular architecture was untouched.
- The Kiosk architecture was untouched.
- Existing REST contracts in `servicesync-core` (`/api/v1/tickets`) were completely untouched.
- SLA business calculations and states remain unchanged.

## 13. Security Verification
- No hardcoded secrets were introduced.
- Internal endpoint `/api/internal/v1/tickets/breaches` was exempted from JWT requirements, acting as an internal boundary API.
- CORS wildcard rules were not relaxed.

## 14. Repository-Wide Coupling Audit
A repository search for `tickets` within `servicesync-sla` confirmed that zero references to the Core database table exist. The SLA service relies exclusively on its own internal `notification_logs` table and OpenFeign HTTP communication.

## 15. Remaining Findings
- `AUD-002` (Ticket Pagination N+1 Query) remains HIGH severity and OPEN.
- `AUD-003` (Obsolete static security configuration) remains LOW severity and OPEN.
- `AUD-004` (Frontend E2E test coverage) remains MEDIUM severity and OPEN.

## 16. Documentation Updates
- `docs/dynamic/90_ACTIVE_TASK.md` updated to COMPLETED.
- `docs/dynamic/91_COMPLETED_TASKS.md` updated.
- `docs/dynamic/98_KNOWN_ISSUES.md` updated to mark ISSUE-009 as RESOLVED.
- `docs/dynamic/99_SESSION_HANDOFF.md` updated.

## 17. Acceptance Matrix

| Requirement | Status | Evidence |
|---|---|---|
| AUD-001 identified | PASS | Found in `NotificationLogRepository.java` |
| Core API contract established | PASS | `InternalTicketController` created |
| OpenFeign integration | PASS | `CoreTicketClient` configured and used in SLA |
| Direct Core DB access removed | PASS | Native SQL deleted from `NotificationLogRepository` |
| Native SQL dependency removed | PASS | Confirmed via `grep` search for "tickets" |
| SLA business logic preserved | PASS | `threshold` calculation kept in `SlaMonitorService` |
| SLA tests pass | PASS | Mockito tests updated and pass |
| Core tests pass | PASS | All 43 tests pass |
| Kiosk tests pass | PASS | All 1 tests pass |
| Angular build passes | PASS | Built successfully |
| No schema changes | PASS | No SQL or DDL changes required |
| No secrets introduced | PASS | None added |
| No mocks/fallbacks introduced | PASS | Live Feign client utilized |
| Repository-wide coupling audit | PASS | Verified clean |

## 18. Final Verdict
`STAGE 3 VERIFIED`

## 19. Next Recommended Stage
Stage 4: Core Optimization (Targeting `AUD-002` N+1 Query Vulnerability)
