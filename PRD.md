# Product Requirements Document
# College Communication & Resource Sharing System

> **Version:** 1.0 | **Team Size:** 4 Members | **Timeline:** 2 Months | **Stack:** Java EE

---

## 1. Problem Statement

College campuses rely on a fragmented mix of WhatsApp groups, physical notice boards, email chains, and informal file-sharing to manage communication. This leads to:

- Notices reaching students late or not at all due to inconsistent distribution
- No centralised repository for academic notes and shared resources
- Student-to-student communication happening on unmanaged personal platforms
- Admins lacking a reliable channel for urgent, time-sensitive alerts
- No accountability or logging of system activity for audit purposes

> **Goal:** Build a unified, Java-based web platform that brings notice management, file sharing, real-time chat, and emergency alerts into a single controlled environment — purpose-built for a college setting.

---

## 2. Target Users

### 2.1 Student

| Attribute | Details |
|---|---|
| Who | Enrolled students of the college |
| Primary Goals | Stay updated on notices, access study material, chat with peers |
| Technical Access | College LAN / campus Wi-Fi via web browser |
| Pain Points | Missed notices, scattered notes, no single communication channel |

### 2.2 Admin

| Attribute | Details |
|---|---|
| Who | Faculty coordinators and college administration staff |
| Primary Goals | Post and broadcast notices, send urgent alerts, manage the system |
| Technical Access | Admin portal on the same web server |
| Pain Points | No guaranteed delivery of urgent notices, no system-wide broadcast |

---

## 3. Core User Flows

### 3.1 Login Flow

1. User opens the web portal in a browser (Servlet/JSP)
2. Enters username and password, submits the login form
3. Servlet forwards credentials to RMI Authentication Server
4. RMI server validates against JDBC user store, returns session token and role
5. On success: redirect to Student Dashboard or Admin Dashboard based on role
6. On failure: display error message, increment failed-attempt counter in DB

### 3.2 View Notices (Student)

1. Student logs in and lands on the Notice Board page
2. Servlet fetches all active notices from DB via JDBC; JSP renders them sorted by date
3. When admin posts a new notice, it is sent to a multicast group
4. Background multicast listener on each connected client receives the datagram
5. Student sees the new notice via a page refresh or lightweight AJAX poll

### 3.3 Upload and Download Files (Student)

1. Student navigates to the Resource Library section
2. To upload: selects file via multipart form — Servlet writes file to disk (Java I/O) and inserts metadata into DB via JDBC
3. To download: clicks file name — Servlet sets Content-Disposition header and streams file bytes
4. File listing shows: file name, subject tag, uploader, date, and file size

### 3.4 Student Chat

1. Student opens the Chat page in the browser
2. Page connects to the TCP Chat Server on a fixed port via a Servlet-based proxy
3. Student selects a peer for one-to-one chat, or enters the class group room
4. Messages are sent and received over TCP sockets; server relays to recipient(s)
5. Messages are persisted to the database with sender, receiver, and timestamp

### 3.5 Receive Admin Alerts

1. Admin types an urgent alert message in the Admin Console
2. Servlet sends the message as a UDP DatagramPacket to the configured multicast group
3. Background MulticastSocket listener on each student session triggers a JS pop-up
4. Alert is simultaneously logged to the alerts table in the database via JDBC

---

## 4. Feature List

### 4.1 MVP — Must Implement

#### Authentication Module

| Feature | Technical Implementation |
|---|---|
| Student and Admin login | JSP form → Servlet → RMI `authenticate()` remote method call |
| Session management | `HttpSession` with role attribute; checked by Servlet filter on every request |
| Logout | Session invalidation in `LogoutServlet`; redirect to login page |
| Activity logging | RMI `logEvent()` records login, logout, and file actions to DB via JDBC |

#### Notice Management

| Feature | Technical Implementation |
|---|---|
| Admin posts notice | Admin JSP form → `NoticeServlet` → JDBC INSERT into notices table |
| Students view notices | Servlet fetches active notices; JSP renders sorted table |
| Notice broadcast | On INSERT, server sends UDP datagram to multicast group; clients refresh on receipt |
| Notice persistence | JDBC — notices table: `id, title, body, posted_by, created_at` |

#### File Sharing — Resource Library

| Feature | Technical Implementation |
|---|---|
| File upload | Multipart HTTP form → `FileServlet` → Java `FileOutputStream` → DB metadata insert |
| File download | Servlet sets `Content-Disposition` header; `FileInputStream` streams bytes to response |
| File listing | JDBC query on files table → JSP renders name, size, uploader, date |
| Basic validation | Size check (max 10 MB); extension whitelist (`.pdf`, `.docx`, `.pptx`, `.zip`) |

#### Student Chat

| Feature | Technical Implementation |
|---|---|
| One-to-one messaging | TCP socket connection to `ChatServer`; server routes by username |
| Group / class broadcast | `ChatServer` maintains room map; message relayed to all sockets in room |
| Message history | JDBC — messages table: `sender_id, receiver_id/room, content, timestamp` |
| Online user list | Server holds active socket map; JSP polls a `StatusServlet` every 5 seconds |

#### Admin Alert System

| Feature | Technical Implementation |
|---|---|
| Send urgent alert | `AdminAlertServlet` → `DatagramSocket` sends packet to multicast group address |
| Client receives alert | `MulticastSocket` listener thread on client triggers JavaScript alert pop-up |
| Alert logging | JDBC INSERT to alerts table: `message, sent_by, sent_at` |

#### RMI Central Server

| Feature | Technical Implementation |
|---|---|
| Authentication service | Remote method: `authenticate(String user, String pass)` → `String role` or `null` |
| Logging service | Remote method: `logEvent(int userId, String action, Timestamp time)` |
| Startup and binding | RMI Registry on port `1099`; server binds `CollegeService` remote object at startup |

---

### 4.2 Future Enhancements (Optional — Not Required for MVP)

> ⚠️ Do not plan for these during the 2-month window.

- Email notification to students when a critical notice is posted
- In-browser PDF preview for uploaded files
- Admin analytics dashboard — total logins, notices posted, file uploads per week
- Notice categories and subject-based filtering for students
- Mobile-responsive UI using Bootstrap 5
- Student profile page showing upload history and chat activity
- Two-factor authentication for Admin accounts

---

## 5. Edge Cases

| Edge Case | Expected Behaviour | Implementation Note |
|---|---|---|
| Invalid login credentials | Show generic error: "Invalid username or password." Do not say which is wrong. | RMI returns `null` role; Servlet forwards with error param. |
| Account does not exist | Same generic error as invalid credentials. | Prevents user enumeration. |
| 3 consecutive login failures | Lock account for 15 minutes; show lockout message. | Track counter in DB; Servlet checks before calling RMI. |
| RMI server unavailable | "Authentication service unavailable. Try again later." | Catch `RemoteException`; do not expose stack trace. |
| TCP chat server down | "Chat server offline." Disable the send button. | Catch `ConnectException` in chat init; render disabled state. |
| Network drop during chat | "Connection lost. Reconnecting..." Attempt up to 3 retries. | Handle `SocketException` in listener thread; retry with backoff. |
| File upload exceeds 10 MB | Reject: "File size exceeds the 10 MB limit." | Check `content-length` header in Servlet before writing to disk. |
| Unsupported file type uploaded | Reject: "Only .pdf, .docx, .pptx, and .zip files are allowed." | Extension whitelist check in `FileServlet`. |
| Duplicate file name uploaded | Append timestamp to stored file name (e.g. `notes_1711012345.pdf`). | Prevents overwrite; original name stored separately in DB. |
| File not found on download | Return HTTP 404 with "Resource not available." | Check `File.exists()` before streaming; log missing file event. |
| Disk full during upload | Return HTTP 500 with "Storage unavailable. Contact admin." | Catch `IOException` on `FileOutputStream`; delete partial file. |
| Multicast notice not received | Student refreshes page to pull latest notices from DB. | DB is source of truth; multicast is a convenience push only. |
| UDP alert packet lost | Alert may not appear on all clients — acceptable for UDP. | Document as known limitation; alert is always logged to DB. |
| Session expired mid-use | Redirect to login with "Your session has expired." | Set session timeout to 30 min in `web.xml`; Servlet filter checks. |
| Student accesses admin URL directly | Redirect to login or return HTTP 403 Forbidden. | Servlet filter reads session role; blocks non-admin access. |
| JDBC connection unavailable | Return HTTP 503 with "Server busy. Please try again shortly." | Set connection pool max; catch `SQLException` gracefully. |

---

## 6. Non-Goals

> ⚠️ The following are explicitly out of scope. Attempting them will consume time without adding to evaluation marks.

| What We Will NOT Build | Reason |
|---|---|
| Mobile app (Android or iOS) | Web portal is sufficient; mobile dev is a separate discipline |
| HTTPS / TLS encryption | HTTP on college LAN is acceptable for this project |
| Spring Boot, Hibernate, or any modern framework | Constraints mandate raw Servlet, JSP, and JDBC only |
| WebSockets for real-time push | TCP chat server covers real-time requirements within constraints |
| Video or audio calling | Far beyond team capacity and scope in 2 months |
| External email or SMS gateway | Requires third-party API integration — not in scope |
| Cloud deployment (AWS, Azure, etc.) | LAN / localhost is the target environment |
| OAuth or social login (Google, etc.) | Standard JDBC-backed credential login meets all requirements |
| Full-text search on files or notices | Simple DB queries are adequate |
| File versioning or revision history | Single-version storage per file is sufficient |
| Dynamic role or permission management UI | Two roles (Student, Admin) are hardcoded — no UI needed |

---

## 7. Success Metrics

### 7.1 Core Evaluation Criteria

| Metric | Acceptance Criterion |
|---|---|
| All 6 technologies demonstrated | Servlet/JSP, JDBC, RMI, TCP, UDP, and Multicast each exercised visibly in the demo |
| Login works for both roles | Student and Admin can log in; wrong credentials are correctly rejected |
| Admin can post and broadcast a notice | Notice stored in DB and received by connected student via multicast |
| Student can upload and download a file | File saved to disk; metadata in DB; download streams correctly |
| TCP chat works between two students | Two browser sessions can exchange messages in real time |
| Admin UDP alert reaches students | Alert pop-up appears on connected student sessions within seconds |
| RMI handles authentication and logging | Remote method calls succeed; events written to the log table in DB |

### 7.2 Code Quality Checklist

| Metric | Target |
|---|---|
| No crash on invalid input | All edge cases — bad login, large file, server down — handled gracefully |
| SQL uses `PreparedStatement` throughout | No raw string concatenation in SQL — prevents SQL injection |
| All resources closed in `finally` blocks | Sockets, streams, and DB connections closed properly to prevent leaks |
| Clean package structure | Separate packages for `auth`, `notice`, `file`, `chat`, and `alert` modules |

### 7.3 Demo Readiness Checklist

- [ ] Server starts without errors on localhost
- [ ] Complete student workflow demonstrable end-to-end in under 5 minutes
- [ ] Complete admin workflow demonstrable end-to-end in under 5 minutes
- [ ] All 6 network technologies are demonstrably exercised in the live demo
- [ ] README or project report documents each technology and its role in the system

> 💡 **Team Tip:** Get all 6 technologies working in basic form before adding any UI polish. A working multicast + RMI demo scores higher than a beautifully styled app missing a required module.

---

## 8. Technology Stack Summary

| Technology | Used For | Location in System |
|---|---|---|
| Servlet / JSP | Web layer — handles HTTP requests and renders HTML pages | Login, Notice Board, File Library, Chat UI, Admin Console |
| JDBC | Database access — all persistent data storage | Users, notices, messages, file metadata, alert logs |
| RMI | Remote authentication service and centralised event logging | Login credential check; event logging from all components |
| TCP Sockets | Reliable, ordered real-time student-to-student chat | Standalone `ChatServer` process + Servlet proxy |
| UDP Sockets | Fast, fire-and-forget admin urgent alerts | `AdminAlertServlet` sends `DatagramPacket` to multicast group |
| Java Multicast | Broadcast notices and alerts to all connected clients | `MulticastSocket` group; clients hold listener thread |
| Java I/O | File storage and retrieval to local disk | `FileInputStream` / `FileOutputStream` in `FileServlet` |

---

*Confidential | Mini Project 2026 | PRD v1.0*
