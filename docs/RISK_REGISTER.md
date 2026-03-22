# Risk Register

Last updated: 2026-03-21

## High

1. Cross-process chat status drift
- Impact: online users list incorrect.
- Mitigation: dedicated status socket on port 9101, timeout fallback, and failure logging in chat status client.
- Current status: Mitigated with residual risk (network partitions can still temporarily hide users).

2. RMI startup dependency
- Impact: login and logging paths degrade.
- Mitigation: strict startup order, safe fallback message on login, and best-effort (non-blocking) RMI event logging for other flows.
- Current status: Partially mitigated (authentication still depends on RMI by design).

3. File storage path inconsistency
- Impact: uploaded files not downloadable.
- Mitigation: deterministic upload base path and filename sanitization.
- Current status: Mitigated.

4. Blocking socket connect in chat send path
- Impact: request thread can block when chat server is unavailable.
- Mitigation: explicit connect/read timeout in chat servlet and service-unavailable fallback response.
- Current status: Mitigated.

5. XSS via online users rendering
- Impact: malicious username could execute script in chat page.
- Mitigation: DOM-safe rendering via text nodes (`textContent`) instead of HTML string injection.
- Current status: Mitigated.

## Medium

1. UDP packet loss for alerts/notices
- Impact: some clients miss push updates.
- Mitigation: database remains source of truth; polling refresh fallback includes DB-backed fallback in status endpoint.
- Current status: Mitigated with residual risk (small delivery delay possible under packet loss).

2. Session timeout UX confusion
- Impact: perceived random logouts.
- Mitigation: redirect with timeout param and explicit banner.
- Current status: Mitigated.

3. Concurrent chat client cleanup
- Impact: stale online users.
- Mitigation: ensure ACTIVE_CLIENTS removal in finally block.
- Current status: Mitigated.

## Low

1. JSP rendering inconsistencies
- Impact: minor UI mismatch.
- Mitigation: common header/navbar/footer includes and JSTL-only rendering.
- Current status: Mitigated.

2. Manual dependency setup errors
- Impact: local setup delays.
- Mitigation: README dependency section and startup scripts.
- Current status: Mitigated.

3. Cross-module debugging ambiguity
- Impact: hard to trace one failing request across filter, servlet, service, and network adapters.
- Mitigation: per-request correlation id (`X-Request-Id`) with request-context-aware logs in services/utilities.
- Current status: Mitigated.
