# Technical Requirement Document
# College Communication & Resource Sharing System

> **Version:** 1.0 | **Type:** Mini Project | **Language:** Java (Servlet, JSP, JDBC, RMI, TCP/UDP, Multicast, Java I/O)

---

## 1. System Architecture Overview

### 1.1 High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                          CLIENT (Browser)                           │
│                    JSP Pages + HTML Forms                           │
└───────────────────────────┬─────────────────────────────────────────┘
                            │ HTTP Request/Response
┌───────────────────────────▼─────────────────────────────────────────┐
│                     WEB LAYER (Apache Tomcat)                       │
│          Servlets  ←→  HttpSession  ←→  Servlet Filters             │
└───────┬───────────────────┬─────────────────────┬───────────────────┘
        │                   │                     │
        │ RMI Call          │ JDBC                │ Java I/O
┌───────▼──────┐   ┌────────▼────────┐   ┌────────▼────────┐
│  RMI SERVER  │   │    DATABASE     │   │   FILE STORAGE  │
│  (port 1099) │   │  (MySQL/SQLite) │   │  /uploads dir   │
│  Auth + Log  │   │  JDBC via DAO   │   │  Java I/O APIs  │
└──────────────┘   └─────────────────┘   └─────────────────┘

        NETWORKING LAYER (runs as background processes)
┌─────────────────────────────────────────────────────────────────────┐
│   TCP ChatServer (port 9100)  │  UDP/Multicast (group 230.0.0.1)   │
│   Student ↔ Student chat      │  Admin alerts + Notice broadcast    │
└─────────────────────────────────────────────────────────────────────┘
```

### 1.2 Request Lifecycle (Standard Web Request)

```
Browser
  │── HTTP POST/GET ──▶ Servlet (doGet / doPost)
                           │── validate session (Servlet Filter)
                           │── call DAO (JDBC PreparedStatement)
                           │── call RMI (auth / log)
                           │── set request attributes
                           └── forward ──▶ JSP (renders HTML)
                                              └── HTTP Response ──▶ Browser
```

### 1.3 Server Startup Sequence

```
1. Start MySQL / SQLite database
2. Start RMI Server  →  binds CollegeService on port 1099
3. Start TCP ChatServer  →  listens on port 9100
4. Start Multicast Listener threads on clients  →  group 230.0.0.1:8888
5. Deploy WAR to Tomcat  →  web application ready on port 8080
```

---

## 2. Module Breakdown

### 2.1 Authentication Module
- **Technology:** Servlet + JSP + RMI + JDBC
- **Responsibility:** Validate credentials, create sessions, enforce role-based access
- **Components:** `LoginServlet`, `LogoutServlet`, `AuthFilter`, `UserDAO`, `CollegeService` (RMI)
- **Flow:** JSP form → `LoginServlet` → RMI `authenticate()` → JDBC user lookup → `HttpSession` creation

### 2.2 Notice Management Module
- **Technology:** Servlet + JSP + JDBC + Multicast (UDP)
- **Responsibility:** Admin creates notices; students view them; multicast pushes live updates
- **Components:** `NoticeServlet`, `NoticeDAO`, `MulticastSender`, `MulticastListenerThread`
- **Flow:** Admin JSP → `NoticeServlet` → `NoticeDAO.insert()` → `MulticastSender.broadcast()`

### 2.3 File Upload/Download Module
- **Technology:** Servlet + JSP + JDBC + Java I/O
- **Responsibility:** Accept multipart file uploads, store to disk, serve downloads
- **Components:** `FileServlet`, `FileDAO`, `FileStorageUtil`
- **Flow:** Upload JSP → `FileServlet` (multipart parse) → `FileOutputStream` to disk → `FileDAO.insertMetadata()`

### 2.4 Chat Module (TCP)
- **Technology:** TCP Sockets + Servlet + JSP
- **Responsibility:** Real-time student messaging (1-to-1 and group)
- **Components:** `ChatServer` (standalone), `ClientHandler` (thread per client), `MessageDAO`, `ChatServlet` (proxy)
- **Flow:** Chat JSP → `ChatServlet` → TCP socket to `ChatServer:9100` → relay to recipient

### 2.5 Alert System (UDP + Multicast)
- **Technology:** UDP DatagramSocket + MulticastSocket
- **Responsibility:** Admin sends urgent one-way alerts to all online students instantly
- **Components:** `AdminAlertServlet`, `AlertDAO`, `MulticastSender`, `MulticastListenerThread`
- **Flow:** Admin JSP → `AdminAlertServlet` → `DatagramSocket.send()` to multicast group → client listener triggers JS pop-up

### 2.6 Logging Module (RMI)
- **Technology:** Java RMI + JDBC
- **Responsibility:** Central, remote logging of all key events (login, logout, file upload, alerts)
- **Components:** `CollegeService` (Remote Interface), `CollegeServiceImpl`, `LogDAO`
- **Flow:** Any Servlet → `CollegeService.logEvent()` (RMI call) → `LogDAO.insert()` → logs table

---

## 3. Screen-to-Servlet Mapping

| JSP Page | Servlet | Method | Purpose |
|---|---|---|---|
| `login.jsp` | `LoginServlet` | POST | Submit credentials for authentication |
| `login.jsp` (redirect) | `LoginServlet` | GET | Show login page |
| `student/dashboard.jsp` | `DashboardServlet` | GET | Load student home page |
| `student/notices.jsp` | `NoticeServlet` | GET | Fetch and display all notices |
| `student/resources.jsp` | `FileServlet` | GET | List all uploaded files |
| `student/upload.jsp` | `FileServlet` | POST | Handle multipart file upload |
| `student/chat.jsp` | `ChatServlet` | GET/POST | Chat page; proxy to TCP server |
| `admin/dashboard.jsp` | `DashboardServlet` | GET | Load admin home page |
| `admin/post_notice.jsp` | `NoticeServlet` | POST | Admin submits new notice |
| `admin/send_alert.jsp` | `AdminAlertServlet` | POST | Admin sends UDP alert |
| `admin/view_logs.jsp` | `LogServlet` | GET | Fetch and display system logs |
| `common/logout.jsp` | `LogoutServlet` | GET | Invalidate session, redirect |
| `common/error.jsp` | — | — | Generic error display page |
| `student/online_users.jsp` | `StatusServlet` | GET | AJAX poll for online user list |

### 3.1 Request/Response Flow Example — Post Notice

```
admin/post_notice.jsp
  │── POST /NoticeServlet ──▶ NoticeServlet.doPost()
                                │── validate session role = "ADMIN"
                                │── read title, body from request params
                                │── NoticeDAO.insertNotice(title, body, adminId)
                                │── MulticastSender.broadcast("NEW_NOTICE:" + noticeId)
                                │── CollegeService.logEvent(adminId, "POST_NOTICE", now)
                                └── response.sendRedirect("admin/dashboard.jsp?success=1")
```

---

## 4. Servlet Responsibilities

### `LoginServlet`
- **URL Pattern:** `/login`
- **doGet:** Forward to `login.jsp`
- **doPost:**
  1. Read `username`, `password` from request
  2. Check failed attempt count from session/DB — if ≥ 3, reject with lockout message
  3. Call `CollegeService.authenticate(username, password)` via RMI
  4. On success: create `HttpSession`, set `userId`, `username`, `role` attributes
  5. Redirect to `/student/dashboard` or `/admin/dashboard` based on role
  6. On failure: increment counter, forward to `login.jsp` with error attribute

### `LogoutServlet`
- **URL Pattern:** `/logout`
- **doGet:**
  1. Call `CollegeService.logEvent(userId, "LOGOUT", now)` via RMI
  2. Call `session.invalidate()`
  3. Redirect to `/login`

### `NoticeServlet`
- **URL Pattern:** `/notices`
- **doGet:** Call `NoticeDAO.getAllNotices()`, set as request attribute, forward to `notices.jsp`
- **doPost (Admin only):**
  1. Read `title`, `body` from request
  2. Call `NoticeDAO.insertNotice(title, body, adminId)`
  3. Call `MulticastSender.broadcast(noticeId)`
  4. Call `CollegeService.logEvent(adminId, "POST_NOTICE", now)`
  5. Redirect to admin dashboard

### `FileServlet`
- **URL Pattern:** `/files`
- **doGet (list):** Call `FileDAO.getAllFiles()`, forward to `resources.jsp`
- **doGet (download):** Read `fileId` param → `FileDAO.getFile(fileId)` → stream bytes via `FileInputStream`
- **doPost (upload):**
  1. Parse multipart request using `request.getPart("file")`
  2. Validate extension and size (< 10 MB)
  3. Generate unique file name (original name + timestamp)
  4. Write to disk using `FileStorageUtil.save(part, fileName)`
  5. Call `FileDAO.insertMetadata(fileName, originalName, size, uploaderId)`
  6. Call `CollegeService.logEvent(userId, "FILE_UPLOAD", now)`

### `ChatServlet`
- **URL Pattern:** `/chat`
- **doGet:** Forward to `chat.jsp` with online users list from `StatusServlet`
- **doPost (send message):**
    1. Open TCP socket to `ChatServer:9100`
  2. Write message as `"TO:recipient:message"` over socket output stream
  3. Call `MessageDAO.saveMessage(senderId, receiverId, content, now)`

### `AdminAlertServlet`
- **URL Pattern:** `/alert`
- **doPost:**
  1. Validate session role = `"ADMIN"`
  2. Read `alertMessage` from request
  3. Call `MulticastSender.sendAlert(alertMessage)`
  4. Call `AlertDAO.insertAlert(alertMessage, adminId, now)`
  5. Call `CollegeService.logEvent(adminId, "SEND_ALERT", now)`

### `StatusServlet`
- **URL Pattern:** `/status`
- **doGet:** Return online user list from `ChatServer`'s active socket map as JSON string (for AJAX poll)

### `LogServlet`
- **URL Pattern:** `/logs`
- **doGet (Admin only):** Call `LogDAO.getAllLogs()`, forward to `admin/view_logs.jsp`

### `DashboardServlet`
- **URL Pattern:** `/dashboard`
- **doGet:** Check session role, forward to appropriate dashboard JSP

### `AuthFilter` (Servlet Filter)
- **Intercepts:** All URLs except `/login` and `/error`
- **Logic:**
  1. Check if `HttpSession` exists and has `userId` attribute
  2. If not → redirect to `/login`
  3. For admin URLs (`/admin/*`) → check role = `"ADMIN"`, else → send HTTP 403

---

## 5. Database Schema

### 5.1 `users` Table
```sql
CREATE TABLE users (
    user_id       INT PRIMARY KEY AUTO_INCREMENT,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(256) NOT NULL,          -- SHA-256 hashed
    role          ENUM('STUDENT', 'ADMIN') NOT NULL,
    full_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(100),
    failed_attempts INT DEFAULT 0,
    locked_until  DATETIME DEFAULT NULL,
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

### 5.2 `notices` Table
```sql
CREATE TABLE notices (
    notice_id   INT PRIMARY KEY AUTO_INCREMENT,
    title       VARCHAR(200) NOT NULL,
    body        TEXT         NOT NULL,
    posted_by   INT          NOT NULL,            -- FK → users.user_id
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_active   BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (posted_by) REFERENCES users(user_id)
);
```

### 5.3 `messages` Table
```sql
CREATE TABLE messages (
    message_id  INT PRIMARY KEY AUTO_INCREMENT,
    sender_id   INT          NOT NULL,            -- FK → users.user_id
    receiver_id INT          DEFAULT NULL,        -- NULL = group message
    room        VARCHAR(50)  DEFAULT 'general',
    content     TEXT         NOT NULL,
    sent_at     DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id)   REFERENCES users(user_id),
    FOREIGN KEY (receiver_id) REFERENCES users(user_id)
);
```

### 5.4 `files` Table
```sql
CREATE TABLE files (
    file_id       INT PRIMARY KEY AUTO_INCREMENT,
    original_name VARCHAR(255) NOT NULL,
    stored_name   VARCHAR(255) NOT NULL UNIQUE,   -- name on disk (with timestamp)
    file_size     BIGINT       NOT NULL,           -- bytes
    file_type     VARCHAR(20)  NOT NULL,           -- pdf, docx, etc.
    uploaded_by   INT          NOT NULL,           -- FK → users.user_id
    subject_tag   VARCHAR(100) DEFAULT NULL,
    uploaded_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (uploaded_by) REFERENCES users(user_id)
);
```

### 5.5 `alerts` Table
```sql
CREATE TABLE alerts (
    alert_id  INT PRIMARY KEY AUTO_INCREMENT,
    message   TEXT     NOT NULL,
    sent_by   INT      NOT NULL,                  -- FK → users.user_id
    sent_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sent_by) REFERENCES users(user_id)
);
```

### 5.6 `logs` Table
```sql
CREATE TABLE logs (
    log_id     INT PRIMARY KEY AUTO_INCREMENT,
    user_id    INT          NOT NULL,
    action     VARCHAR(100) NOT NULL,             -- LOGIN, LOGOUT, FILE_UPLOAD, etc.
    ip_address VARCHAR(45)  DEFAULT NULL,
    logged_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);
```

---

## 6. JDBC Design

### 6.1 DAO Pattern

Each database table has its own DAO class. No SQL is written outside a DAO.

```
com.college.dao
├── UserDAO.java
├── NoticeDAO.java
├── MessageDAO.java
├── FileDAO.java
├── AlertDAO.java
└── LogDAO.java
```

### 6.2 `DBConnection.java` — Connection Utility

```java
// com/college/util/DBConnection.java
public class DBConnection {
    private static final String URL  = "jdbc:mysql://localhost:3306/college_db";
    private static final String USER = "root";
    private static final String PASS = "password";

    static {
        try { Class.forName("com.mysql.cj.jdbc.Driver"); }
        catch (ClassNotFoundException e) { throw new RuntimeException("Driver not found", e); }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
```

### 6.3 Sample DAO — `UserDAO.java`

```java
public class UserDAO {

    // Returns role string ("STUDENT"/"ADMIN") or null if invalid
    public String authenticate(String username, String passwordHash) {
        String sql = "SELECT role FROM users WHERE username = ? AND password_hash = ? " +
                     "AND (locked_until IS NULL OR locked_until < NOW())";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("role");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void incrementFailedAttempts(String username) {
        String sql = "UPDATE users SET failed_attempts = failed_attempts + 1, " +
                     "locked_until = CASE WHEN failed_attempts >= 2 " +
                     "THEN DATE_ADD(NOW(), INTERVAL 15 MINUTE) ELSE locked_until END " +
                     "WHERE username = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void resetFailedAttempts(String username) {
        String sql = "UPDATE users SET failed_attempts = 0, locked_until = NULL WHERE username = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
```

### 6.4 PreparedStatement Rules

- **Always use `PreparedStatement`** — never `Statement` with string concatenation.
- **Always close** `Connection`, `PreparedStatement`, and `ResultSet` in `finally` blocks or use try-with-resources.
- **Never store plain-text passwords** — hash with `SHA-256` before storing or comparing.

```java
// Password hashing utility
public static String hashPassword(String plain) throws NoSuchAlgorithmException {
    MessageDigest md = MessageDigest.getInstance("SHA-256");
    byte[] hash = md.digest(plain.getBytes(StandardCharsets.UTF_8));
    StringBuilder sb = new StringBuilder();
    for (byte b : hash) sb.append(String.format("%02x", b));
    return sb.toString();
}
```

---

## 7. RMI Design

### 7.1 Remote Interface

```java
// com/college/rmi/CollegeService.java
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.Timestamp;

public interface CollegeService extends Remote {

    // Returns role ("STUDENT" / "ADMIN") or null if invalid
    String authenticate(String username, String passwordHash) throws RemoteException;

    // Logs any system event centrally
    void logEvent(int userId, String action, Timestamp time) throws RemoteException;
}
```

### 7.2 Server Implementation

```java
// com/college/rmi/CollegeServiceImpl.java
import java.rmi.server.UnicastRemoteObject;
import java.sql.Timestamp;

public class CollegeServiceImpl extends UnicastRemoteObject implements CollegeService {

    private final UserDAO userDAO = new UserDAO();
    private final LogDAO  logDAO  = new LogDAO();

    public CollegeServiceImpl() throws RemoteException { super(); }

    @Override
    public String authenticate(String username, String passwordHash) throws RemoteException {
        return userDAO.authenticate(username, passwordHash);
    }

    @Override
    public void logEvent(int userId, String action, Timestamp time) throws RemoteException {
        logDAO.insertLog(userId, action, time);
    }
}
```

### 7.3 RMI Server Startup

```java
// com/college/rmi/RMIServer.java
public class RMIServer {
    public static void main(String[] args) throws Exception {
        CollegeService service = new CollegeServiceImpl();
        java.rmi.registry.LocateRegistry.createRegistry(1099);
        java.rmi.Naming.rebind("rmi://localhost/CollegeService", service);
        System.out.println("RMI Server running on port 1099...");
    }
}
```

### 7.4 RMI Client Usage (inside a Servlet)

```java
// Inside LoginServlet.doPost()
try {
    CollegeService service =
        (CollegeService) Naming.lookup("rmi://localhost/CollegeService");
    String role = service.authenticate(username, hashedPassword);
    if (role != null) {
        // create session
    }
} catch (RemoteException | NotBoundException e) {
    request.setAttribute("error", "Authentication service unavailable.");
    request.getRequestDispatcher("/login.jsp").forward(request, response);
}
```

---

## 8. Networking Design

### 8.1 TCP Chat Server

```
Architecture: One server process, one thread per connected client

ChatServer (port 9100)
├── ServerSocket.accept() loop (main thread)
└── ClientHandler (new Thread per client)
    ├── reads "TO:username:message" from socket
    ├── looks up recipient socket in activeClients map
    └── writes message to recipient's socket output stream
```

```java
// com/college/chat/ChatServer.java  (simplified)
public class ChatServer {
    static Map<String, Socket> activeClients = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(9100);
        System.out.println("Chat server listening on port 9100...");
        while (true) {
            Socket client = server.accept();
            new Thread(new ClientHandler(client)).start();
        }
    }
}

// com/college/chat/ClientHandler.java  (simplified)
public class ClientHandler implements Runnable {
    private final Socket socket;
    // constructor...

    public void run() {
        try (BufferedReader in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter    out = new PrintWriter(socket.getOutputStream(), true)) {

            String username = in.readLine();           // first line = "LOGIN:username"
            ChatServer.activeClients.put(username, socket);

            String line;
            while ((line = in.readLine()) != null) {
                // Format: "TO:recipientUsername:messageText"
                String[] parts = line.split(":", 3);
                if (parts[0].equals("TO") && ChatServer.activeClients.containsKey(parts[1])) {
                    Socket recipientSocket = ChatServer.activeClients.get(parts[1]);
                    PrintWriter recipientOut = new PrintWriter(recipientSocket.getOutputStream(), true);
                    recipientOut.println("FROM:" + username + ":" + parts[2]);
                }
            }
        } catch (IOException e) {
            // handle disconnect
        } finally {
            ChatServer.activeClients.values().remove(socket);
        }
    }
}
```

### 8.2 UDP Multicast — Admin Alerts & Notice Broadcast

```
Multicast Group IP : 230.0.0.1
Multicast Port     : 8888
Protocol           : UDP (unreliable by design — fire and forget)
```

```java
// com/college/network/MulticastSender.java
public class MulticastSender {
    private static final String GROUP = "230.0.0.1";
    private static final int    PORT  = 8888;

    public static void broadcast(String message) {
        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress group = InetAddress.getByName(GROUP);
            byte[] buf = message.getBytes(StandardCharsets.UTF_8);
            DatagramPacket packet = new DatagramPacket(buf, buf.length, group, PORT);
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();   // log but do not crash — UDP is best-effort
        }
    }
}
```

```java
// com/college/network/MulticastListenerThread.java
// Started as a daemon thread when Tomcat context initializes (via ServletContextListener)
public class MulticastListenerThread implements Runnable {
    private static final String GROUP = "230.0.0.1";
    private static final int    PORT  = 8888;

    public void run() {
        try (MulticastSocket socket = new MulticastSocket(PORT)) {
            InetAddress group = InetAddress.getByName(GROUP);
            socket.joinGroup(group);
            byte[] buf = new byte[1024];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                // Store latest message in a shared queue / ServletContext attribute
                // JSP polls this via AJAX to show pop-up
                NotificationQueue.add(message);
            }
        } catch (IOException e) { e.printStackTrace(); }
    }
}
```

### 8.3 Protocol Message Formats

| Channel | Format | Example |
|---|---|---|
| TCP Chat | `LOGIN:username` | `LOGIN:rahul` |
| TCP Chat | `TO:recipient:message` | `TO:priya:Hey, notes ready?` |
| TCP Chat | `TO:GROUP:message` | `TO:GROUP:Exam postponed` |
| Multicast Notice | `NEW_NOTICE:noticeId` | `NEW_NOTICE:42` |
| Multicast Alert | `ALERT:message` | `ALERT:Lab cancelled today` |

---

## 9. File Handling

### 9.1 Storage Structure

```
/webapps/college/
└── uploads/
    ├── notes/
    │   ├── CN_notes_1711012345678.pdf
    │   └── OS_slides_1711098765432.pptx
    └── resources/
        └── DSA_assignment_1711011112222.docx
```

### 9.2 `FileStorageUtil.java`

```java
// com/college/util/FileStorageUtil.java
public class FileStorageUtil {

    // Absolute path to uploads directory — set once from ServletContext
    private static String BASE_PATH;

    public static void init(String uploadPath) {
        BASE_PATH = uploadPath;
        new File(BASE_PATH).mkdirs();   // create directory if absent
    }

    // Saves uploaded Part to disk, returns the stored file name
    public static String saveFile(Part filePart, String originalName) throws IOException {
        String extension   = originalName.substring(originalName.lastIndexOf('.'));
        String baseName    = originalName.substring(0, originalName.lastIndexOf('.'));
        String storedName  = baseName + "_" + System.currentTimeMillis() + extension;
        String fullPath    = BASE_PATH + File.separator + storedName;

        try (InputStream in  = filePart.getInputStream();
             FileOutputStream out = new FileOutputStream(fullPath)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
        return storedName;
    }

    // Streams file bytes to servlet response
    public static void streamFile(String storedName, HttpServletResponse response) throws IOException {
        File file = new File(BASE_PATH + File.separator + storedName);
        if (!file.exists()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Resource not available.");
            return;
        }
        response.setHeader("Content-Disposition", "attachment; filename=\"" + storedName + "\"");
        response.setContentLengthLong(file.length());
        try (FileInputStream in = new FileInputStream(file);
             OutputStream out   = response.getOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) out.write(buffer, 0, bytesRead);
        }
    }
}
```

### 9.3 Upload Validation (inside `FileServlet`)

```java
private static final long MAX_FILE_SIZE = 10 * 1024 * 1024L;  // 10 MB
private static final Set<String> ALLOWED_EXTENSIONS =
    new HashSet<>(Arrays.asList("pdf", "docx", "pptx", "zip"));

private boolean isValid(Part part, String originalName, HttpServletRequest request) {
    if (part.getSize() > MAX_FILE_SIZE) {
        request.setAttribute("error", "File size exceeds the 10 MB limit.");
        return false;
    }
    String ext = originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase();
    if (!ALLOWED_EXTENSIONS.contains(ext)) {
        request.setAttribute("error", "Only .pdf, .docx, .pptx, and .zip files are allowed.");
        return false;
    }
    return true;
}
```

---

## 10. Session Management

### 10.1 Session Creation (on successful login)

```java
// Inside LoginServlet.doPost() — after RMI confirms valid credentials
HttpSession session = request.getSession(true);
session.setAttribute("userId",   user.getUserId());
session.setAttribute("username", user.getUsername());
session.setAttribute("role",     role);             // "STUDENT" or "ADMIN"
session.setMaxInactiveInterval(30 * 60);            // 30-minute timeout
```

### 10.2 `AuthFilter.java` — Protects All Pages

```java
// com/college/filter/AuthFilter.java
@WebFilter("/*")
public class AuthFilter implements Filter {

    private static final Set<String> PUBLIC_PATHS =
        new HashSet<>(Arrays.asList("/login", "/error"));

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest  request  = (HttpServletRequest)  req;
        HttpServletResponse response = (HttpServletResponse) res;

        String path = request.getServletPath();

        if (PUBLIC_PATHS.contains(path) || path.startsWith("/css") || path.startsWith("/js")) {
            chain.doFilter(req, res);
            return;
        }

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // Admin-only paths
        if (path.startsWith("/admin")) {
            String role = (String) session.getAttribute("role");
            if (!"ADMIN".equals(role)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied.");
                return;
            }
        }

        chain.doFilter(req, res);
    }
}
```

### 10.3 Session Attributes Reference

| Attribute | Type | Value |
|---|---|---|
| `userId` | `Integer` | DB primary key of logged-in user |
| `username` | `String` | Login username |
| `role` | `String` | `"STUDENT"` or `"ADMIN"` |

---

## 11. Error Handling

### 11.1 Error Categories and Responses

| Error Type | Where It Occurs | Handling Strategy |
|---|---|---|
| Invalid login | `LoginServlet` | Forward to `login.jsp` with `error` attribute |
| Account locked | `LoginServlet` | Forward to `login.jsp` with lockout message |
| RMI unavailable | Any Servlet calling RMI | Catch `RemoteException`; show "Service unavailable" |
| JDBC failure | Any DAO method | Catch `SQLException`; log it; return null/empty; show HTTP 503 |
| File too large | `FileServlet` | Set error attribute; re-forward to upload page |
| Invalid file type | `FileServlet` | Set error attribute; re-forward to upload page |
| File not found (download) | `FileServlet` | `response.sendError(404, "Resource not available")` |
| Disk full | `FileStorageUtil` | Catch `IOException`; delete partial file; return HTTP 500 |
| TCP chat server down | `ChatServlet` | Catch `ConnectException`; set `chatOffline = true`; disable send button in JSP |
| UDP send failure | `MulticastSender` | Catch and log `IOException`; do not crash — alert is still logged to DB |
| Session expired | `AuthFilter` | Redirect to `/login` with `?timeout=1`; JSP shows "Session expired" message |
| Unauthorized access | `AuthFilter` | `response.sendError(403, "Access denied")` |
| Generic server error | Any Servlet | Catch `Exception`; forward to `error.jsp` with safe message |

### 11.2 `error.jsp` — Generic Error Page

```jsp
<%-- common/error.jsp --%>
<h2>Something went wrong</h2>
<p>${not empty errorMessage ? errorMessage : "An unexpected error occurred. Please try again."}</p>
<a href="${pageContext.request.contextPath}/dashboard">Go to Dashboard</a>
```

### 11.3 `web.xml` Error Page Mapping

```xml
<!-- web.xml -->
<error-page>
    <error-code>403</error-code>
    <location>/common/error.jsp</location>
</error-page>
<error-page>
    <error-code>404</error-code>
    <location>/common/error.jsp</location>
</error-page>
<error-page>
    <error-code>500</error-code>
    <location>/common/error.jsp</location>
</error-page>
<session-config>
    <session-timeout>30</session-timeout>
</session-config>
```

---

## 12. Scalability & Modularity

### 12.1 Package Structure

```
src/
└── com/college/
    ├── dao/
    │   ├── UserDAO.java
    │   ├── NoticeDAO.java
    │   ├── MessageDAO.java
    │   ├── FileDAO.java
    │   ├── AlertDAO.java
    │   └── LogDAO.java
    │
    ├── rmi/
    │   ├── CollegeService.java        ← Remote interface
    │   ├── CollegeServiceImpl.java    ← Implementation
    │   └── RMIServer.java             ← Startup class
    │
    ├── servlet/
    │   ├── LoginServlet.java
    │   ├── LogoutServlet.java
    │   ├── NoticeServlet.java
    │   ├── FileServlet.java
    │   ├── ChatServlet.java
    │   ├── AdminAlertServlet.java
    │   ├── DashboardServlet.java
    │   ├── StatusServlet.java
    │   └── LogServlet.java
    │
    ├── filter/
    │   └── AuthFilter.java
    │
    ├── chat/
    │   ├── ChatServer.java            ← Standalone TCP server
    │   └── ClientHandler.java        ← Per-client thread
    │
    ├── network/
    │   ├── MulticastSender.java
    │   └── MulticastListenerThread.java
    │
    ├── util/
    │   ├── DBConnection.java
    │   ├── FileStorageUtil.java
    │   └── PasswordUtil.java          ← SHA-256 hashing
    │
    └── model/
        ├── User.java
        ├── Notice.java
        ├── Message.java
        ├── FileMetadata.java
        └── Log.java

WebContent/
├── WEB-INF/
│   └── web.xml
├── login.jsp
├── common/
│   └── error.jsp
├── student/
│   ├── dashboard.jsp
│   ├── notices.jsp
│   ├── resources.jsp
│   ├── upload.jsp
│   ├── chat.jsp
│   └── online_users.jsp
└── admin/
    ├── dashboard.jsp
    ├── post_notice.jsp
    ├── send_alert.jsp
    └── view_logs.jsp
```

### 12.2 Separation of Concerns

| Layer | Responsibility | Contains |
|---|---|---|
| **Model** | Plain data objects only — no logic | `User.java`, `Notice.java`, etc. |
| **DAO** | All SQL — nothing else | `UserDAO`, `NoticeDAO`, etc. |
| **Servlet** | HTTP logic — read params, call DAO/RMI, set attributes, redirect | All `*Servlet.java` files |
| **JSP** | Display only — no business logic, no SQL | All `.jsp` files |
| **Util** | Shared helpers with no layer dependency | `DBConnection`, `FileStorageUtil`, `PasswordUtil` |
| **Network** | Socket communication only | `ChatServer`, `MulticastSender`, etc. |
| **RMI** | Remote services only — auth and logging | `CollegeService`, `CollegeServiceImpl` |

### 12.3 Key Design Decisions for Mini Project

| Decision | Reason |
|---|---|
| One `DBConnection.getConnection()` per request (no pool) | Simple; adequate for LAN demo with low concurrency |
| `ConcurrentHashMap` for active chat clients | Thread-safe without needing external libraries |
| DB is source of truth for notices (multicast is a push-only hint) | Multicast is UDP — unreliable; DB guarantees consistency |
| `SHA-256` for passwords (not BCrypt) | Available in standard Java SDK — no extra dependency |
| Multicast listener started via `ServletContextListener` | Ensures it starts with Tomcat and shuts down cleanly |
| All Servlet responses use redirect-after-POST | Prevents duplicate form submission on browser refresh |

---

## Appendix — Technology-to-Exam-Answer Map

| If asked about... | Point to... |
|---|---|
| How authentication works | `LoginServlet` → RMI `authenticate()` → `UserDAO` → `HttpSession` |
| How RMI is used | `CollegeService` interface, `CollegeServiceImpl`, `RMIServer` startup |
| How TCP sockets are used | `ChatServer` + `ClientHandler` thread per client |
| How UDP is used | `AdminAlertServlet` → `MulticastSender.broadcast()` → `DatagramSocket` |
| How Multicast works | `MulticastSender` sends to group `230.0.0.1:8888`; `MulticastListenerThread` on each client receives |
| How JDBC is used | DAO pattern + `PreparedStatement` + try-with-resources in every DAO method |
| How files are handled | `FileServlet` → `FileStorageUtil.saveFile()` → `FileOutputStream` to disk |
| How sessions work | `HttpSession` set in `LoginServlet`; enforced by `AuthFilter` on every request |
| What prevents SQL injection | `PreparedStatement` — never raw `Statement` with string concat |
| How errors are handled | Try-catch in Servlets + DAOs; forward to `error.jsp`; `web.xml` error mappings |

---

*Confidential | Mini Project 2026 | TRD v1.0*
