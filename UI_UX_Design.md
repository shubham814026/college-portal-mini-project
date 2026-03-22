# UI/UX Design & Screen Breakdown
# College Communication & Resource Sharing System

> **Version:** 1.0 | **Role:** UX Design + JSP Implementation Guide | **Stack:** Servlet / JSP / JDBC / RMI / TCP / UDP / Multicast

---

## 0. Design Principles

| Principle | What It Means for This Project |
|---|---|
| **Minimal over decorative** | No unnecessary animations, gradients, or libraries — plain HTML + CSS only |
| **Server-rendered first** | Every page is a JSP rendered by a Servlet. No client-side routing. |
| **Form-forward** | Most actions are triggered by HTML forms (POST). AJAX used only where truly needed. |
| **Role-aware layouts** | Admin and Student see different navbars, dashboards, and actions |
| **Fail visibly** | Every error state has a dedicated message. No silent failures. |
| **One page, one purpose** | Each JSP does one thing. No multi-purpose pages. |

---

## 1. Screen List

### 1.1 Public Screens (No Login Required)

| Screen | JSP File | Servlet | Purpose |
|---|---|---|---|
| Login Page | `login.jsp` | `LoginServlet` | Entry point for all users |
| Error Page | `common/error.jsp` | — | Displays all system-level errors |

### 1.2 Student Screens (Role: STUDENT)

| Screen | JSP File | Servlet | Purpose |
|---|---|---|---|
| Student Dashboard | `student/dashboard.jsp` | `DashboardServlet` | Overview — recent notices, quick links |
| Notice Board | `student/notices.jsp` | `NoticeServlet` | Browse all posted notices |
| Resource Library | `student/resources.jsp` | `FileServlet` | List + download uploaded files |
| Upload File | `student/upload.jsp` | `FileServlet` | Upload notes or resources |
| Chat | `student/chat.jsp` | `ChatServlet` | 1-to-1 or group chat via TCP |
| Online Users | `student/online_users.jsp` | `StatusServlet` | AJAX-polled online user list (partial/fragment) |

### 1.3 Admin Screens (Role: ADMIN)

| Screen | JSP File | Servlet | Purpose |
|---|---|---|---|
| Admin Dashboard | `admin/dashboard.jsp` | `DashboardServlet` | Overview — pending notices, system status |
| Post Notice | `admin/post_notice.jsp` | `NoticeServlet` | Compose and broadcast a notice |
| Send Alert | `admin/send_alert.jsp` | `AdminAlertServlet` | Send urgent UDP alert to all students |
| View Logs | `admin/view_logs.jsp` | `LogServlet` | Browse system activity log |

### 1.4 Shared / Utility Screens

| Screen | JSP File | Purpose |
|---|---|---|
| Logout (redirect only) | — | `LogoutServlet` handles; no JSP needed |
| Session Expired | `login.jsp?timeout=1` | Login page with timeout banner |
| 403 Forbidden | `common/error.jsp` | Role violation |
| 404 Not Found | `common/error.jsp` | Missing resource |
| 500 Server Error | `common/error.jsp` | Unhandled server exception |

---

## 2. Reusable JSP Components

These partials are included via `<jsp:include>` or `<%@ include %>` on every page.

```
WebContent/
└── common/
    ├── header.jsp       ← <head> tag, CSS links, meta
    ├── navbar.jsp       ← Top navigation bar (role-aware)
    ├── footer.jsp       ← Page footer, copyright
    ├── alert_banner.jsp ← Multicast alert pop-up listener
    └── error.jsp        ← Shared error display page
```

### 2.1 `header.jsp`

```jsp
<%-- Included at the top of every JSP --%>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>College Portal — ${pageTitle}</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
```

### 2.2 `navbar.jsp`

```
┌──────────────────────────────────────────────────────────────────┐
│  🏫 College Portal          [Notices] [Files] [Chat]   👤 Rahul ▼ │
│                                                          [Logout]  │
└──────────────────────────────────────────────────────────────────┘
```

- Nav links differ by role:
  - **Student:** Notices | Files | Chat
  - **Admin:** Post Notice | Send Alert | View Logs
- Username is pulled from `session.getAttribute("username")`
- Logout link → `GET /logout`

```jsp
<%-- common/navbar.jsp --%>
<nav class="navbar">
  <a class="brand" href="${pageContext.request.contextPath}/dashboard">🏫 College Portal</a>
  <ul class="nav-links">
    <c:choose>
      <c:when test="${session.role == 'ADMIN'}">
        <li><a href="/notices">Post Notice</a></li>
        <li><a href="/alert">Send Alert</a></li>
        <li><a href="/logs">View Logs</a></li>
      </c:when>
      <c:otherwise>
        <li><a href="/notices">Notices</a></li>
        <li><a href="/files">Files</a></li>
        <li><a href="/chat">Chat</a></li>
      </c:otherwise>
    </c:choose>
  </ul>
  <div class="user-info">
    👤 ${sessionScope.username}
    <a href="${pageContext.request.contextPath}/logout" class="btn-logout">Logout</a>
  </div>
</nav>
```

### 2.3 `alert_banner.jsp`

Included on every student page. Polls the server every 5 seconds for new multicast alerts. When one arrives, renders a dismissible banner at the top of the viewport.

```jsp
<%-- Included on all student pages --%>
<div id="alert-banner" class="alert-banner hidden"></div>
<script>
  setInterval(function() {
    fetch('${pageContext.request.contextPath}/status?type=alert')
      .then(r => r.json())
      .then(data => {
        if (data.alert) {
          document.getElementById('alert-banner').textContent = '⚠️ ' + data.alert;
          document.getElementById('alert-banner').classList.remove('hidden');
        }
      });
  }, 5000);
</script>
```

### 2.4 `footer.jsp`

```
┌──────────────────────────────────────────────────────────────────┐
│           College Portal  |  Mini Project 2026  |  Java EE        │
└──────────────────────────────────────────────────────────────────┘
```

---

## 3. Layout Hierarchy Per Screen

### 3.1 Base Layout Template (applied to all pages)

```
┌─────────────────────────────────────────────┐
│              header.jsp (HTML <head>)        │
├─────────────────────────────────────────────┤
│              navbar.jsp                      │
├─────────────────────────────────────────────┤
│  [alert_banner.jsp]   ← only on student pages│
├─────────────────────────────────────────────┤
│                                              │
│           PAGE CONTENT AREA                  │
│           (unique per JSP)                   │
│                                              │
├─────────────────────────────────────────────┤
│              footer.jsp                      │
└─────────────────────────────────────────────┘
```

---

### 3.2 Login Page — `login.jsp`

**Purpose:** Single entry point. No navbar shown until authenticated.

```
┌────────────────────────────────────┐
│         🏫 College Portal          │
│                                    │
│  ┌──────────────────────────────┐  │
│  │         LOGIN                │  │
│  │                              │  │
│  │  Username: [____________]    │  │
│  │  Password: [____________]    │  │
│  │                              │  │
│  │  [!] Invalid username        │  │  ← error attribute set by LoginServlet
│  │      or password.            │  │
│  │                              │  │
│  │         [ Login ]            │  │
│  └──────────────────────────────┘  │
└────────────────────────────────────┘
```

**Form spec:**
```html
<form method="POST" action="/login">
  <input type="text"     name="username" required placeholder="Username" />
  <input type="password" name="password" required placeholder="Password" />
  <button type="submit">Login</button>
</form>
```

**States:**
- Default → empty form
- Error (`?error=1`) → red banner: "Invalid username or password."
- Locked (`?locked=1`) → amber banner: "Account locked. Try again after 15 minutes."
- Timeout (`?timeout=1`) → blue banner: "Your session has expired. Please log in again."

**Servlet flow:**
```
POST /login
  → LoginServlet.doPost()
      → hash password (SHA-256)
      → check lockout in DB
      → call RMI CollegeService.authenticate()
      → on success: session.setAttribute("role", ...) → redirect /dashboard
      → on failure: incrementFailedAttempts() → forward login.jsp?error=1
```

---

### 3.3 Student Dashboard — `student/dashboard.jsp`

**Purpose:** Landing page after student login. Snapshot of recent activity.

```
┌──────────────────────────────────────────────────────────┐
│  navbar.jsp                                               │
│  [⚠️ ALERT BANNER — appears here if admin sends alert]    │
├──────────────────────────────────────────────────────────┤
│  Welcome back, Rahul 👋                                   │
├──────────────┬────────────────────┬──────────────────────┤
│  📢 RECENT   │  📁 RECENT FILES   │  💬 ONLINE NOW       │
│  NOTICES     │                    │                       │
│  ─────────── │  ─────────────     │  ───────────          │
│  Exam on 5th │  CN_notes.pdf      │  • priya              │
│  Lab cancel  │  OS_slides.pptx    │  • amit               │
│  Fee due     │  DSA_assign.docx   │  • neha               │
│              │                    │                       │
│  [View All]  │  [View All]        │  [Open Chat]          │
└──────────────┴────────────────────┴──────────────────────┘
```

**Data sources:**
- Recent notices → `NoticeDAO.getRecentNotices(limit=3)`
- Recent files → `FileDAO.getRecentFiles(limit=3)`
- Online users → `ChatServer.activeClients.keySet()` (via `StatusServlet`)

**Empty state:** If no data yet, each card shows: *"Nothing here yet."*

---

### 3.4 Notice Board — `student/notices.jsp`

**Purpose:** Full list of admin-posted notices, newest first.

```
┌──────────────────────────────────────────────────────────┐
│  navbar.jsp                                               │
│  [⚠️ alert_banner.jsp]                                    │
├──────────────────────────────────────────────────────────┤
│  📢 Notice Board                                          │
│                                                           │
│  ┌────────────────────────────────────────────────────┐  │
│  │ 📌 Exam Schedule Update               5 Apr 2026   │  │
│  │    Mid-semester exams begin from 10th April.       │  │
│  │    Venue: Block B, Hall 1.             — Admin     │  │
│  └────────────────────────────────────────────────────┘  │
│  ┌────────────────────────────────────────────────────┐  │
│  │ 📌 Lab Cancelled                      4 Apr 2026   │  │
│  │    CN lab for Section A cancelled today.           │  │
│  └────────────────────────────────────────────────────┘  │
│                                                           │
│  [No more notices]                                        │
└──────────────────────────────────────────────────────────┘
```

**Multicast integration:**
- When a new notice is posted, `MulticastSender.broadcast("NEW_NOTICE:42")` fires
- `MulticastListenerThread` on client adds it to `NotificationQueue`
- A lightweight AJAX poll (`StatusServlet?type=notice`) checks the queue every 5s
- If a new notice ID is found, the page auto-refreshes (or shows: *"New notice available — [Refresh]"*)

**Empty state:**
```
📭  No notices have been posted yet.
    Check back later.
```

---

### 3.5 Resource Library — `student/resources.jsp`

**Purpose:** Browse and download all uploaded files.

```
┌──────────────────────────────────────────────────────────┐
│  navbar.jsp                                               │
│  [⚠️ alert_banner.jsp]                                    │
├──────────────────────────────────────────────────────────┤
│  📁 Resource Library              [ + Upload File ]       │
│                                                           │
│  ┌──────────┬────────────┬──────────┬────────┬─────────┐ │
│  │ File Name│ Subject    │ Uploaded │ Size   │ Action  │ │
│  ├──────────┼────────────┼──────────┼────────┼─────────┤ │
│  │CN_notes  │ Networks   │ rahul    │ 1.2 MB │[Download│ │
│  │.pdf      │            │ 3 Apr    │        │    ]    │ │
│  ├──────────┼────────────┼──────────┼────────┼─────────┤ │
│  │OS_slides │ OS         │ priya    │ 3.4 MB │[Download│ │
│  │.pptx     │            │ 2 Apr    │        │    ]    │ │
│  └──────────┴────────────┴──────────┴────────┴─────────┘ │
└──────────────────────────────────────────────────────────┘
```

**Download flow:**
```
[Download] button
  → GET /files?action=download&fileId=7
  → FileServlet.doGet()
      → FileDAO.getFile(7) → retrieve storedName
      → response.setHeader("Content-Disposition", "attachment; filename=...")
      → FileInputStream → response.getOutputStream()
```

**Empty state:**
```
📂  No files uploaded yet.
    Be the first to share something!   [ + Upload File ]
```

---

### 3.6 Upload File — `student/upload.jsp`

**Purpose:** Upload a single file with optional subject tag.

```
┌──────────────────────────────────────────────────────────┐
│  navbar.jsp                                               │
├──────────────────────────────────────────────────────────┤
│  📤 Upload Resource                                       │
│                                                           │
│  ┌────────────────────────────────────────────────────┐  │
│  │  Choose File:  [Browse...] [No file chosen]        │  │
│  │  Subject Tag:  [________________________]          │  │
│  │                                                    │  │
│  │  Allowed types: .pdf  .docx  .pptx  .zip           │  │
│  │  Max size: 10 MB                                   │  │
│  │                                                    │  │
│  │  [!] Error message appears here if upload fails    │  │
│  │                                                    │  │
│  │           [ Upload ]       [ Cancel ]              │  │
│  └────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────┘
```

**Form spec:**
```html
<form method="POST" action="/files" enctype="multipart/form-data">
  <input type="file"   name="file"       required />
  <input type="text"   name="subjectTag" placeholder="e.g. Computer Networks" />
  <button type="submit">Upload</button>
</form>
```

**Upload flow:**
```
POST /files (multipart)
  → FileServlet.doPost()
      → request.getPart("file")
      → validate size < 10MB and extension in whitelist
      → FileStorageUtil.saveFile() → FileOutputStream to /uploads/
      → FileDAO.insertMetadata(originalName, storedName, size, type, userId)
      → CollegeService.logEvent(userId, "FILE_UPLOAD", now)
      → redirect /files?success=1
```

**Error states:**
- File too large → `[!] File size exceeds the 10 MB limit.`
- Wrong extension → `[!] Only .pdf, .docx, .pptx, and .zip files are allowed.`
- Disk full → `[!] Storage unavailable. Contact admin.`

---

### 3.7 Chat Page — `student/chat.jsp`

**Purpose:** Real-time 1-to-1 and group messaging over TCP.

```
┌──────────────────────────────────────────────────────────┐
│  navbar.jsp                                               │
│  [⚠️ alert_banner.jsp]                                    │
├────────────────┬─────────────────────────────────────────┤
│  ONLINE (3)    │  💬 Chat with: priya                     │
│  ──────────    │  ──────────────────────────────────────  │
│  • priya  [→] │  [10:22] rahul:  Hey, notes ready?       │
│  • amit   [→] │  [10:23] priya:  Uploading now!          │
│  • neha   [→] │  [10:24] rahul:  Thanks!                 │
│                │                                          │
│  ──────────    │                                          │
│  [GROUP CHAT]  │                                          │
│                │                                          │
│                │  ┌───────────────────────────────────┐  │
│                │  │ Type a message...          [Send] │  │
│                │  └───────────────────────────────────┘  │
└────────────────┴─────────────────────────────────────────┘
```

**TCP integration:**
- On page load → `ChatServlet` opens TCP socket to `ChatServer:9100`, sends `LOGIN:username`
- [Send] button → AJAX POST to `ChatServlet` with `{to: "priya", message: "..."}` → forwarded over TCP
- Incoming messages → AJAX poll every 2s to `/chat?action=poll` → servlet reads from socket buffer → returns JSON
- Online user list → AJAX poll every 5s to `/status` → `StatusServlet` reads `ChatServer.activeClients`

**TCP down state:**
```
🔴 Chat server is currently offline.
   Messages cannot be sent at this time.
   [ Send ] button is disabled.
```

**Empty chat state (no messages yet):**
```
👋 Say hello to priya!
   No messages yet.
```

---

### 3.8 Admin Dashboard — `admin/dashboard.jsp`

**Purpose:** Admin home — quick view of system activity and shortcuts.

```
┌──────────────────────────────────────────────────────────┐
│  navbar.jsp (ADMIN)                                       │
├──────────────────────────────────────────────────────────┤
│  Admin Panel — Welcome, Admin 🛠️                          │
├──────────────┬────────────────────┬──────────────────────┤
│  📢 RECENT   │  🚨 SEND ALERT     │  📋 RECENT LOGS       │
│  NOTICES     │                    │                       │
│  ─────────── │  ─────────────     │  ───────────          │
│  Exam Sched. │  [Message______]   │  rahul LOGIN  10:01   │
│  Lab Cancel  │  [Send Alert Now]  │  priya UPLOAD 10:05   │
│  Fee Due     │                    │  admin NOTICE 10:10   │
│              │  Last sent: 09:45  │                       │
│  [Post New]  │                    │  [View All Logs]      │
└──────────────┴────────────────────┴──────────────────────┘
```

---

### 3.9 Post Notice — `admin/post_notice.jsp`

**Purpose:** Admin composes and broadcasts a notice via multicast.

```
┌──────────────────────────────────────────────────────────┐
│  navbar.jsp (ADMIN)                                       │
├──────────────────────────────────────────────────────────┤
│  📢 Post New Notice                                       │
│                                                           │
│  ┌────────────────────────────────────────────────────┐  │
│  │  Title:   [________________________________________│  │
│  │  Message: [________________________________________│  │
│  │           [________________________________________│  │
│  │           [________________________________________│  │
│  │                                                    │  │
│  │  ✅ Broadcast via Multicast to all students        │  │
│  │                                                    │  │
│  │        [ Post Notice ]     [ Cancel ]              │  │
│  └────────────────────────────────────────────────────┘  │
│                                                           │
│  ✅ Notice posted successfully! (on ?success=1)           │
└──────────────────────────────────────────────────────────┘
```

**Form spec:**
```html
<form method="POST" action="/notices">
  <input type="text"     name="title" required maxlength="200" />
  <textarea              name="body"  required rows="5"></textarea>
  <button type="submit">Post Notice</button>
</form>
```

**Post notice flow:**
```
POST /notices
  → NoticeServlet.doPost()
      → validate role = ADMIN
      → NoticeDAO.insertNotice(title, body, adminId)
      → MulticastSender.broadcast("NEW_NOTICE:" + noticeId)
      → CollegeService.logEvent(adminId, "POST_NOTICE", now)
      → redirect /admin/dashboard?success=1
```

---

### 3.10 Send Alert — `admin/send_alert.jsp`

**Purpose:** Admin sends an urgent message to all connected students via UDP multicast.

```
┌──────────────────────────────────────────────────────────┐
│  navbar.jsp (ADMIN)                                       │
├──────────────────────────────────────────────────────────┤
│  🚨 Send Urgent Alert                                     │
│                                                           │
│  ⚠️  This message will be sent instantly to all           │
│     connected students as a pop-up notification.          │
│                                                           │
│  ┌────────────────────────────────────────────────────┐  │
│  │  Alert Message:                                    │  │
│  │  [______________________________________________]  │  │
│  │                                                    │  │
│  │          [ Send Alert Now ]                        │  │
│  └────────────────────────────────────────────────────┘  │
│                                                           │
│  ✅ Alert sent at 10:22 AM. (on ?success=1)               │
│                                                           │
│  Previous Alerts                                          │
│  ─────────────────────────────────────────────────────   │
│  [10:01]  Lab cancelled for today — Admin                 │
│  [09:30]  Fee payment deadline today — Admin              │
└──────────────────────────────────────────────────────────┘
```

**UDP flow:**
```
POST /alert
  → AdminAlertServlet.doPost()
      → validate role = ADMIN
      → MulticastSender.broadcast("ALERT:" + alertMessage)
      → AlertDAO.insertAlert(alertMessage, adminId, now)
      → CollegeService.logEvent(adminId, "SEND_ALERT", now)
      → redirect /admin/send_alert?success=1
```

---

### 3.11 View Logs — `admin/view_logs.jsp`

**Purpose:** Admin audits all system events logged via RMI.

```
┌──────────────────────────────────────────────────────────┐
│  navbar.jsp (ADMIN)                                       │
├──────────────────────────────────────────────────────────┤
│  📋 System Activity Logs                                  │
│                                                           │
│  ┌──────────┬──────────┬──────────────┬────────────────┐ │
│  │ Log ID   │ User     │ Action       │ Timestamp      │ │
│  ├──────────┼──────────┼──────────────┼────────────────┤ │
│  │ 1042     │ rahul    │ LOGIN        │ 05 Apr 10:01   │ │
│  │ 1043     │ priya    │ FILE_UPLOAD  │ 05 Apr 10:05   │ │
│  │ 1044     │ admin    │ POST_NOTICE  │ 05 Apr 10:10   │ │
│  │ 1045     │ admin    │ SEND_ALERT   │ 05 Apr 10:22   │ │
│  └──────────┴──────────┴──────────────┴────────────────┘ │
└──────────────────────────────────────────────────────────┘
```

**Data flow:**
```
GET /logs
  → LogServlet.doGet()
      → validate role = ADMIN
      → LogDAO.getAllLogs()
      → set request attribute "logs"
      → forward to view_logs.jsp
```

**Empty state:**
```
📋 No activity logged yet.
```

---

### 3.12 Error Page — `common/error.jsp`

**Purpose:** Catches all 403, 404, 500 errors mapped in `web.xml`.

```
┌──────────────────────────────────────────────────────────┐
│  navbar.jsp (if session exists)                           │
├──────────────────────────────────────────────────────────┤
│                                                           │
│          ⚠️  Something went wrong                         │
│                                                           │
│     An unexpected error occurred. Please try again.      │
│     If the problem persists, contact admin.               │
│                                                           │
│            [ Go to Dashboard ]                            │
│                                                           │
└──────────────────────────────────────────────────────────┘
```

```jsp
<%-- common/error.jsp --%>
<h2>⚠️ Something went wrong</h2>
<p>${not empty errorMessage ? errorMessage : "An unexpected error occurred. Please try again."}</p>
<a href="${pageContext.request.contextPath}/dashboard" class="btn">Go to Dashboard</a>
```

---

## 4. Interaction Logic

### 4.1 Full Navigation Flow

```
Browser opens /
    │
    ▼
AuthFilter checks HttpSession
    │
    ├── No session ──────────────────────────▶ /login  (LoginServlet GET)
    │                                              │
    │                                         POST /login
    │                                              │
    │                              ┌───────────────┴────────────────┐
    │                              │ RMI authenticate()              │
    │                              │ → role = "STUDENT" or "ADMIN"  │
    │                              └───────────────┬────────────────┘
    │                                              │
    │                              ┌───────────────┴────────────────┐
    │                              │ success              failure    │
    │                              ▼                       ▼         │
    │                        session created        login.jsp?error=1│
    │                              │                                 │
    │                    ┌─────────┴──────────┐                      │
    │                    ▼                    ▼                      │
    │            /student/dashboard    /admin/dashboard              │
    │
    └── Session exists ──▶ Serve requested page
                                │
                                ▼
                        AuthFilter checks role for /admin/* paths
                                │
                        ┌───────┴───────┐
                        ▼               ▼
                    allowed          403 error.jsp
```

### 4.2 Session State Machine

```
[Not Logged In]
      │
      │  POST /login (valid)
      ▼
[Logged In — Student]  ←──────────────────┐
      │                                    │
      │  Any page action                   │ session still valid
      ▼                                    │
[Servlet Filter validates session] ────────┘
      │
      │  session.getAttribute("userId") == null
      │  OR session timed out (30 min)
      ▼
[Redirected to /login?timeout=1]
      │
      │  POST /logout
      ▼
[session.invalidate() → /login]
```

### 4.3 Redirect-After-POST Pattern

Every form submission follows the PRG (Post-Redirect-Get) pattern to prevent duplicate submissions on page refresh.

```
Student submits Upload form
    │
    ▼
POST /files  →  FileServlet.doPost()
                    │
                    ▼
              File saved to disk
              Metadata inserted to DB
                    │
                    ▼
              response.sendRedirect("/files?success=1")
                    │
                    ▼
GET /files   →  FileServlet.doGet() renders resources.jsp
                with ✅ success banner from query param
```

---

## 5. Networking Integration in UI

### 5.1 Where Each Network Technology Surfaces

| Technology | Triggered By | Visible To User As |
|---|---|---|
| **RMI** | Login form submit | Transparent — user just sees login succeed or fail |
| **JDBC** | Every page load | Transparent — data appears in tables/lists |
| **TCP Chat** | Opening `chat.jsp`, sending a message | Real-time messages appear in chat window |
| **UDP Multicast (Alert)** | Admin clicks "Send Alert Now" | Pop-up banner appears on all student sessions |
| **UDP Multicast (Notice)** | Admin posts a notice | "New notice available" prompt appears for students |
| **Java I/O (Upload)** | Student clicks "Upload" | Progress implicit; success/error message shown |
| **Java I/O (Download)** | Student clicks "Download" | Browser file download dialog opens |

### 5.2 Chat — TCP Lifecycle in the Browser

```
chat.jsp loads
    │
    ▼
AJAX: POST /chat?action=login  (sends LOGIN:username over TCP socket)
    │
    ▼
User selects recipient "priya" from online list
    │
    ▼
User types message, clicks [Send]
    │
    ▼
AJAX: POST /chat?action=send  {to: "priya", msg: "Hey!"}
    │
    ▼
ChatServlet writes "TO:priya:Hey!" over TCP socket to ChatServer
    │
    ▼
ChatServer's ClientHandler reads message, looks up priya's socket
Writes "FROM:rahul:Hey!" to priya's socket
    │
    ▼
AJAX poll on priya's chat.jsp: GET /chat?action=poll
ChatServlet reads from socket buffer, returns JSON {from: "rahul", msg: "Hey!"}
Message appended to priya's chat window
```

### 5.3 Alert — UDP Multicast Lifecycle in the Browser

```
Admin types "Lab cancelled" on send_alert.jsp
Clicks [Send Alert Now]
    │
    ▼
POST /alert → AdminAlertServlet
    │
    ▼
MulticastSender sends UDP DatagramPacket "ALERT:Lab cancelled"
to group 230.0.0.1:8888
    │
    ▼  (simultaneously on all connected student sessions)
MulticastListenerThread receives packet
Adds "ALERT:Lab cancelled" to NotificationQueue
    │
    ▼
alert_banner.jsp polls StatusServlet every 5s
StatusServlet reads from NotificationQueue
Returns JSON {alert: "Lab cancelled"}
    │
    ▼
JavaScript sets alert-banner div text and removes "hidden" class
Student sees: ⚠️ Lab cancelled
```

---

## 6. Empty States

| Page | Empty State Message |
|---|---|
| Notice Board | *"📭 No notices have been posted yet. Check back later."* |
| Resource Library | *"📂 No files uploaded yet. Be the first to share something!"* |
| Chat (no messages) | *"👋 No messages yet. Say hello!"* |
| Online Users (none) | *"😶 No other users are online right now."* |
| View Logs | *"📋 No activity has been logged yet."* |
| Admin Dashboard (no notices) | *"No notices posted yet."* |
| Send Alert (no history) | *"No alerts sent yet."* |

**Implementation pattern:**
```jsp
<c:choose>
  <c:when test="${empty notices}">
    <div class="empty-state">
      <p>📭 No notices have been posted yet.</p>
    </div>
  </c:when>
  <c:otherwise>
    <%-- render table --%>
  </c:otherwise>
</c:choose>
```

---

## 7. Error States

### 7.1 Login Errors

| Condition | Query Param | Message Shown |
|---|---|---|
| Wrong credentials | `?error=1` | ❌ Invalid username or password. |
| Account locked | `?locked=1` | 🔒 Account locked. Try again after 15 minutes. |
| Session expired | `?timeout=1` | 🕐 Your session has expired. Please log in again. |
| RMI server down | `?rmi=1` | ⚙️ Authentication service unavailable. Try again later. |

```jsp
<%-- login.jsp error display --%>
<c:if test="${param.error == 1}">
  <div class="error-banner">❌ Invalid username or password.</div>
</c:if>
<c:if test="${param.locked == 1}">
  <div class="warning-banner">🔒 Account locked for 15 minutes.</div>
</c:if>
<c:if test="${param.timeout == 1}">
  <div class="info-banner">🕐 Your session has expired. Please log in again.</div>
</c:if>
```

### 7.2 File Upload Errors

| Condition | Message |
|---|---|
| File > 10 MB | ❌ File size exceeds the 10 MB limit. |
| Wrong extension | ❌ Only .pdf, .docx, .pptx, and .zip files are allowed. |
| Disk full | ❌ Storage unavailable. Contact your admin. |
| No file selected | ❌ Please select a file before uploading. |

### 7.3 Network / Server Errors

| Condition | Where | Message |
|---|---|---|
| Chat server down | `chat.jsp` | 🔴 Chat server is offline. Messages are disabled. |
| Chat connection lost | `chat.jsp` | 🔄 Connection lost. Reconnecting... (3 retries) |
| RMI unavailable | Any Servlet | Caught → forward to `error.jsp` with safe message |
| JDBC failure | Any DAO | Caught → forward to `error.jsp` with HTTP 503 message |
| File not found | Download | HTTP 404 → `error.jsp`: "Resource not available." |
| Access denied | `AuthFilter` | HTTP 403 → `error.jsp`: "Access denied." |
| Server exception | Any Servlet | HTTP 500 → `error.jsp`: "An unexpected error occurred." |

### 7.4 Error Display Pattern (In-Page vs Full-Page)

| Error Severity | Display Method |
|---|---|
| Form validation error (e.g., wrong file type) | **Inline** — red message above the form on the same page |
| Network error (e.g., chat server down) | **Inline banner** — red strip on the affected page |
| Auth / permission error | **Full-page redirect** — `error.jsp` |
| Unhandled exception | **Full-page** — `error.jsp` via `web.xml` error mapping |

---

## 8. File & Data Handling

### 8.1 Upload Flow (End-to-End)

```
Student on upload.jsp
    │
    │  Selects file via <input type="file" name="file">
    │  Enters subject tag
    │  Clicks [Upload]
    ▼
POST /files (multipart/form-data)
    │
    ▼
FileServlet.doPost()
    ├── request.getPart("file") → Part object
    ├── read originalName from Part header
    ├── validate size ≤ 10 MB
    ├── validate extension in {pdf, docx, pptx, zip}
    │
    ├── generate storedName = baseName + "_" + currentTimeMillis + ext
    ├── FileStorageUtil.saveFile(part, storedName)
    │       └── new FileOutputStream("/uploads/" + storedName)
    │           → read part.getInputStream() in 4KB chunks
    │           → write to FileOutputStream
    │
    ├── FileDAO.insertMetadata(originalName, storedName, size, type, userId, subjectTag)
    ├── CollegeService.logEvent(userId, "FILE_UPLOAD", now)
    │
    └── response.sendRedirect("/files?success=1")
```

### 8.2 Download Flow (End-to-End)

```
Student clicks [Download] on resources.jsp
    │
GET /files?action=download&fileId=7
    │
    ▼
FileServlet.doGet()
    ├── read fileId from request param
    ├── FileDAO.getFile(7) → FileMetadata (storedName, originalName, size)
    ├── File file = new File("/uploads/" + storedName)
    ├── check file.exists() → if not, sendError(404)
    │
    ├── response.setHeader("Content-Disposition",
    │       "attachment; filename=\"" + originalName + "\"")
    ├── response.setContentLengthLong(file.length())
    │
    └── FileInputStream → 4KB buffer → response.getOutputStream()
            └── Browser receives file → "Save As" dialog shown
```

### 8.3 How Data Displays in Each Table

**Resource Library Table columns:**

| Column | Source | Notes |
|---|---|---|
| File Name | `files.original_name` | Show truncated if > 30 chars |
| Subject | `files.subject_tag` | Show "—" if null |
| Uploaded By | JOIN `users.username` | Always shown |
| Date | `files.uploaded_at` | Format: `DD MMM YYYY` |
| Size | `files.file_size` | Convert bytes → KB/MB display |
| Action | Computed | `<a href="/files?action=download&fileId=...">Download</a>` |

**Notice Board card fields:**

| Field | Source |
|---|---|
| Title | `notices.title` |
| Body | `notices.body` |
| Posted By | JOIN `users.full_name` |
| Date | `notices.created_at` |

---

## 9. CSS Structure (Minimal Stylesheet Reference)

No external CSS frameworks. One flat stylesheet: `css/style.css`.

```
/css/style.css
  ├── Reset & base font (Arial, 14px)
  ├── .navbar — flex row, dark background
  ├── .btn — padded button with border
  ├── .btn-primary — blue button (submit actions)
  ├── .btn-logout — red/outline (logout)
  ├── .card — white box with border, used for notices
  ├── .table — full-width, striped rows
  ├── .error-banner — red background, white text
  ├── .success-banner — green background, white text
  ├── .warning-banner — amber background
  ├── .info-banner — blue background
  ├── .empty-state — centered, gray, italic
  ├── .alert-banner — fixed top bar, high z-index, orange
  ├── .chat-window — scrollable message list
  ├── .online-list — left sidebar, fixed width
  └── .hidden — display: none
```

---

## 10. Quick-Reference: Page → Servlet → DAO → Network

| Page | Servlet Called | DAO Used | Network Used |
|---|---|---|---|
| `login.jsp` | `LoginServlet` | `UserDAO` | RMI `authenticate()` |
| `student/dashboard.jsp` | `DashboardServlet` | `NoticeDAO`, `FileDAO` | `StatusServlet` (AJAX) |
| `student/notices.jsp` | `NoticeServlet` | `NoticeDAO` | Multicast (receive) |
| `student/resources.jsp` | `FileServlet` | `FileDAO` | Java I/O (download) |
| `student/upload.jsp` | `FileServlet` | `FileDAO` | Java I/O (upload), RMI log |
| `student/chat.jsp` | `ChatServlet` | `MessageDAO` | TCP Socket |
| `admin/dashboard.jsp` | `DashboardServlet` | `NoticeDAO`, `LogDAO` | — |
| `admin/post_notice.jsp` | `NoticeServlet` | `NoticeDAO` | Multicast (send), RMI log |
| `admin/send_alert.jsp` | `AdminAlertServlet` | `AlertDAO` | UDP Multicast (send), RMI log |
| `admin/view_logs.jsp` | `LogServlet` | `LogDAO` | — |

---

*Confidential | Mini Project 2026 | UI/UX Design v1.0*
