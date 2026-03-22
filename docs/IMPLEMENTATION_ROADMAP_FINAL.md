# Final Implementation Roadmap

This is the unified execution roadmap for the College Communication and Resource Sharing System.
It combines the practical low-risk structure of the primary roadmap with the stronger test and demo rigor from the detailed roadmap.

Version: 1.1
Team size: 4
Timeline: 8 weeks
Stack: Servlet, JSP, JDBC, RMI, TCP, UDP, Multicast, Java I/O
Runtime baseline: Tomcat 9 (javax.servlet), MySQL 8+, Java 8+

## 0. Environment and Setup Gate

Complete this before module coding starts.

- Tomcat 9 running on localhost:8080
- MySQL 8 running and college_db schema created
- mysql-connector-j jar available to web app runtime
- jstl-1.2.jar present under WEB-INF/lib
- JDK available and javac/java commands working
- DB schema applied from db/schema.sql
- Seed users present for STUDENT and ADMIN
- Startup order understood from scripts/startup-order.md

## 1. Module Implementation Order

Build in this exact order.

1. Shared foundation
- utils, constants, DB connection, sanitization, response helpers, models

2. Data and access layer
- all DAO classes with PreparedStatement and try-with-resources

3. RMI module
- CollegeService interface, implementation, RMIServer bootstrap

4. Authentication and session control
- LoginServlet, LogoutServlet, AuthFilter, timeout and role gates

5. Notice module
- NoticeServlet, notice JSPs, multicast notice broadcast

6. File module
- FileServlet, upload/download JSPs, file I/O pipeline + metadata

7. Alert module
- AdminAlertServlet, alert JSP, multicast alert broadcast and persistence

8. Chat module
- standalone ChatServer and ClientHandler, ChatServlet proxy, online status bridge

9. Dashboard and logs integration
- DashboardServlet, LogServlet, status polling, shared UI shell

## 2. Backend Wiring Order per Module

Implement each module in this sequence.

1. JSP contract
- input fields, validation display slots, output placeholders

2. Servlet contract
- doGet and doPost responsibilities, parameter validation, PRG pattern

3. Service boundary
- RMI call where needed, network call where needed

4. DAO operation
- SQL in DAO only, PreparedStatement only

5. Database interaction
- read/write with controlled exception handling

6. Response strategy
- forward for read views, redirect-after-post for mutating actions, JSON for polling APIs

## 3. Data Flow Mapping

## Standard request flow

Browser -> Servlet -> validation and auth check -> DAO or RMI -> DB -> attributes or redirect -> JSP/JSON response

## Login flow

login.jsp POST -> LoginServlet -> hash password -> RMI authenticate -> user/session setup -> redirect dashboard

## Notice flow

admin POST notice -> NoticeServlet -> NoticeDAO insert -> MulticastSender NEW_NOTICE -> RMI logEvent -> redirect

## File flow

upload POST -> FileServlet validate size and extension -> FileStorageUtil write to disk -> FileDAO insert metadata -> RMI logEvent -> redirect

download GET -> FileServlet lookup metadata -> file existence check -> stream response

## Chat flow TCP

chat.jsp POST send -> ChatServlet -> socket to ChatServer:9100 -> route by recipient -> MessageDAO persistence -> JSON status

## Alert flow UDP multicast

admin POST alert -> AdminAlertServlet -> UDP send ALERT to group -> AlertDAO insert -> RMI logEvent -> student status poll -> banner render

## Online users bridge flow

StatusServlet -> ChatStatusClient socket call -> ChatStatusServer on 9101 -> users list -> JSON users array

## 4. Session Management Plan

- On successful login create HttpSession with userId, username, role
- Set timeout to 30 minutes
- AuthFilter enforces access for all private paths
- Admin-only routes require role ADMIN
- Invalid or expired sessions redirect to login with timeout signal
- Logout invalidates session and logs event through RMI

## 5. Dependency and Integration Sequence

## Must be completed first

1. db/schema.sql applied
2. utils and models
3. DAO layer
4. RMI server
5. authentication flow

## Feature integration sequence

1. notices
2. files
3. alerts
4. chat
5. dashboards and logs
6. full system regression

## 6. Risk Areas and Controls

1. Socket communication failures
- Use timeouts, non-blocking servlet behavior, graceful JSON fallback for offline state

2. RMI connection failures
- Catch and degrade safely with user-facing service unavailable message

3. File handling errors
- Sanitize names, enforce extension whitelist and size limits, safe not-found and storage-failure handling

4. DB connection/query failures
- strict try-with-resources, centralized error responses, no raw SQL string concat in servlets

5. Session and authorization bypass
- filter-first enforcement and explicit admin route checks

6. UDP packet loss
- expected behavior acknowledged, DB remains source of truth, polling fallback

## 7. Testing Strategy

## Module-wise testing

- Authentication: valid/invalid login, lockout, timeout, logout, role guard
- RMI: authenticate and logEvent happy and failure paths
- Notices: post/list and multicast trigger
- Files: upload and download plus rejection paths
- Alerts: send and banner visibility via polling
- Chat: send, offline fallback, status list accuracy

## Integration testing

- Student end-to-end: login -> notices -> files -> chat -> logout
- Admin end-to-end: login -> post notice -> send alert -> view logs -> logout
- Failure-path regression: DB down, RMI down, chat down, unauthorized access

## Demo readiness checks

- all six technologies demonstrated in one run
- no silent failure in primary user journeys
- PRG pattern active for all post forms
- shared error handling visible for controlled failures

## 8. Week-by-Week Plan

Week 1
- setup gate, schema, seed data, utilities, models, DAO smoke checks

Week 2
- RMI server + authentication + session filter + login/logout

Week 3
- notice module + multicast notice path + status polling base

Week 4
- file upload/download pipeline + validation + DB metadata

Week 5
- chat server and chat servlet proxy + message persistence

Week 6
- alert module + UDP multicast + alert history/logging

Week 7
- dashboards, logs, shared UI shell consistency, integration fixes

Week 8
- full integration testing, bug fixing, demo rehearsal, risk fallback runbook

## 9. Execution Commands and Runbook

Use scripts and docs already provided.

- scripts/start-rmi.bat or scripts/start-rmi.sh
- scripts/start-chat.bat or scripts/start-chat.sh
- scripts/startup-order.md
- docs/INTEGRATION_TEST_CHECKLIST.md
- docs/RISK_REGISTER.md

Follow startup order strictly:
1. MySQL
2. schema apply
3. RMI server
4. Chat server
5. Tomcat deploy/start

## 10. Definition of Done

- functional completion of all core modules
- all required technologies proven in integrated run
- stable auth/session behavior
- controlled error handling for core failure modes
- integration checklist completed and signed off
