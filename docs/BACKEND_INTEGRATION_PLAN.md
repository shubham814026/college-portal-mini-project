# Backend Integration Plan

Last updated: 2026-03-21

Stack baseline: Servlet, JSP, JDBC, RMI, TCP/UDP, Multicast, Java I/O

This plan translates the PRD/TRD into an implementation-ready backend integration design focused on stability, clarity, and debuggability, with strict separation between web transport, business logic, persistence, and network adapters.

## 0. Current Project Structure Mapping

- `src/com/college/servlets` -> HTTP transport layer
- `src/com/college/dao` -> JDBC persistence layer
- `src/com/college/models` -> domain models
- `src/com/college/utils` -> shared utilities and adapters
- `src/com/college/rmi` -> RMI service and server
- `src/com/college/chat` -> TCP chat server and handlers
- `src/com/college/network` -> multicast and notification queue
- `src/com/college/filter` -> authentication/authorization filter
- `WebContent` -> JSP and static resources
- `db/schema.sql` -> database schema

## 1. Request Handling Flow

Canonical backend path:

`JSP -> Servlet -> Service -> DAO/RMI/Socket Adapter -> Database/Network -> Service Result -> Servlet -> JSP/JSON`

### 1.1 Read Flow (example: notices page)

1. Browser requests `/notices`.
2. `NoticeServlet.doGet` validates session context and delegates to `NoticeService`.
3. `NoticeService` calls `NoticeDAO.getAllActiveNotices()`.
4. DAO executes prepared SQL and maps rows to `Notice` models.
5. Service returns typed result list.
6. Servlet places list in request attributes and forwards to `student/notices.jsp`.

### 1.2 Write Flow (example: post notice)

1. Admin submits `post_notice.jsp` form.
2. `NoticeServlet.doPost` validates input and role.
3. Service persists notice via DAO.
4. Service triggers multicast event and best-effort RMI audit log.
5. Servlet redirects using PRG (`/admin/notices/new?success=posted` or `error=validation`).

### 1.3 Polling Flow (status)

1. Frontend polls `/status` with optional `type`.
2. `StatusServlet` reads in-memory queue first.
3. If queue miss, fallback to DB source of truth (alerts/notices).
4. Servlet returns JSON payload.

## 2. Servlet Design Pattern

Design rule: one servlet per module, one clear responsibility per servlet.

### 2.1 Module Servlets

- `LoginServlet` -> login GET/POST
- `LogoutServlet` -> logout and session termination
- `DashboardServlet` -> role-based dashboard loading
- `NoticeServlet` -> notice listing and posting
- `FileServlet` -> list/upload/download resources
- `ChatServlet` -> chat send proxy and chat page load
- `AdminAlertServlet` -> urgent alert send
- `StatusServlet` -> JSON status polling endpoint
- `LogServlet` -> admin log view

### 2.2 Servlet Rules

1. No SQL in servlets.
2. No direct business decisions in JSP.
3. No direct low-level socket protocol handling in JSP.
4. Use request scope for read data, redirect tokens for write outcomes.
5. Keep servlet methods small and delegate orchestration to services.

## 3. Service Layer Structure

Create/maintain a thin service layer between servlet and DAO/network integrations.

Suggested package: `src/com/college/service`

### 3.1 Services and Responsibilities

1. `AuthService`
- Authenticate via RMI.
- Coordinate lockout checks and failed-attempt updates.
- Build login outcome object for servlet.

2. `NoticeService`
- Validate notice payload.
- Persist notices.
- Trigger multicast notice event.
- Trigger best-effort audit log.

3. `FileService`
- Validate upload type and size.
- Manage stored file naming and save strategy.
- Persist metadata.
- Resolve and stream download target safely.

4. `ChatService`
- Handle chat send to TCP server with bounded timeout.
- Persist message metadata.
- Fetch online users through status bridge.

5. `AlertService`
- Validate alert content.
- Send multicast alert.
- Persist alert row.
- Trigger best-effort audit log.

6. `DashboardService`
- Aggregate recent notices/files/logs with bounded queries.
- Return role-specific dashboard DTO.

### 3.2 Dependency Direction

`Servlet -> Service -> DAO/Adapter`

Never invert this direction. DAOs and adapters should not call servlets.

## 4. Error Handling Strategy

Use explicit, predictable error handling with consistent user feedback.

### 4.1 Input Validation

- Validate required fields, length, and format in servlet boundary.
- Re-validate business invariants in service layer.
- Return deterministic error tokens:
  - `error=validation`
  - `error=auth`
  - `error=network`
  - `error=db`
  - `error=storage`

### 4.2 Database Failures

- DAO throws `SQLException` upward.
- Service maps to domain-level failure.
- Servlet chooses safe response:
  - read operations -> forward shared error page
  - write operations -> redirect with `error=db`

### 4.3 Network Failures

- RMI auth failure -> fail login with safe message.
- RMI audit logging failure -> log warning, continue business flow.
- TCP chat failure -> fail fast with `chat-offline` JSON.
- UDP/multicast send failure -> persist DB entry and return warning status if needed.

### 4.4 Exception Logging Standard

Each catch block logs:
- module
- action
- userId (if available)
- correlation id
- exception summary

No empty catch blocks.

## 5. Loading and Response Handling

### 5.1 Read Views

- Forward to JSP using request attributes.
- Render empty states explicitly.
- Use bounded query sizes for dashboard cards.

### 5.2 Write Actions (PRG)

- Always use redirect after mutation.
- Encode outcome in query params.
- JSP maps params to user-facing banners.
- Preferred tokens: `success=posted|sent|uploaded` and `error=validation|storage|db|network`.

### 5.3 JSON Endpoints

- Use shared JSON response helper.
- Return stable shape:
  - `status`
  - payload field (`users`, `alert`, `notice`)
  - optional `message`

## 6. Security Rules

### 6.1 Session Authentication

- `AuthFilter` guards private routes.
- Session must include `userId`, `username`, `role`.
- Timeout redirects to `/login?timeout=1`.

### 6.2 Access Control

- Admin routes require `role=ADMIN`.
- Unauthorized access returns 403.
- Filter plus servlet-level role checks for defense in depth.

### 6.3 Input/Output Safety

- Use input sanitization for text and filenames.
- Use prepared statements for all SQL.
- Use DOM-safe rendering (`textContent`) for untrusted UI strings.
- Escape JSON strings before response write.

### 6.4 File Safety

- Whitelist extensions and size max (10 MB).
- Sanitize submitted file name.
- Use deterministic stored name to avoid collision/overwrite.
- Validate existence on download and return 404 when missing.

## 7. Networking Integration

### 7.1 Chat Integration (TCP)

Flow:

1. `chat.jsp` submits message to `/chat`.
2. `ChatServlet` delegates to `ChatService`.
3. Service opens socket to chat server (`9100`) with connect/read timeout.
4. Service sends protocol payload (`LOGIN` then `TO`).
5. Message metadata is stored through `MessageDAO`.
6. Servlet returns JSON status.

### 7.2 Online Users Status Bridge

1. `StatusServlet` requests users via `ChatStatusClient`.
2. Client calls chat status socket (`9101`) with timeout.
3. Response parsed and returned as JSON list.
4. On failure, return empty users list plus log warning.

### 7.3 Alerts and Notices (UDP/Multicast)

1. Admin action persists alert/notice in DB.
2. Service sends multicast message.
3. Student polling endpoint reads queue first.
4. If queue miss occurs, DB fallback provides latest event.

## 8. File Handling Integration

### 8.1 Upload

1. Multipart receive in `FileServlet`.
2. Validate extension and size.
3. `FileStorageUtil.saveFile` writes bytes to upload directory.
4. `FileDAO.insertMetadata` persists metadata.
5. Best-effort audit log call.
6. Redirect with success or error token.

### 8.2 Download

1. Validate `fileId`.
2. Lookup metadata via `FileDAO.findById`.
3. Resolve physical file using stored name.
4. Stream with `Content-Disposition` and `X-Content-Type-Options`.
5. Handle missing file with HTTP 404.

## 9. Stability and Debugging Plan

### 9.1 Correlation and Logging

- Add per-request correlation id in servlet filter.
- Include correlation id in all service/DAO/network logs.
- Log latency for RMI and socket calls.

### 9.2 Timeouts and Retries

- Keep TCP connect/read timeouts centralized in constants.
- Do not retry chat send in request thread more than once.
- Avoid blocking operations without timeout.

### 9.3 Health and Startup Checks

At startup or via admin diagnostics endpoint:

1. DB connectivity check.
2. RMI lookup check.
3. Chat status port check (9101).
4. Upload directory write check.

### 9.4 Integration Debug Checklist

1. Login success/failure and lockout behavior.
2. Notice post + student notice visibility.
3. File upload/download and not-found behavior.
4. Chat send with server available/unavailable.
5. Alert send + status poll fallback.
6. Session timeout and admin route protection.

## 10. Anti-Coupling Rules (Must Follow)

1. JSP never calls DAO or network classes.
2. Servlet never builds SQL.
3. DAO never knows HTTP/session types.
4. Service owns orchestration and transaction-like sequencing.
5. Network adapters are swappable utilities used by services.
6. Shared response and sanitization helpers used consistently.

## 11. Implementation Sequence

1. Stabilize service boundaries for Login, Dashboard, Notice, File, Chat, Alert.
2. Ensure all servlets delegate to services.
3. Consolidate error/result handling in shared utility.
4. Validate startup order and health checks.
5. Run integration checklist and risk regression.

## 12. Traceability to Existing Docs

- Product behaviors and edge cases: `PRD.md`
- Module architecture and servlet contracts: `TRD.md`
- Operational risks and mitigations: `docs/RISK_REGISTER.md`
- Integration execution checks: `docs/INTEGRATION_TEST_CHECKLIST.md`
