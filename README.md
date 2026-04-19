# College Communication and Resource Sharing System

A modern, decoupled web application featuring a **React Single Page Application (SPA)** on the frontend and a robust **Java Servlet RESTful API** on the backend. It also incorporates Java RMI, TCP sockets, and UDP multicast for specialized communication tasks.

This guide provides step-by-step instructions for setting up and running the project from scratch on Windows.

---

## 1. Tech Stack Overview

### Frontend
- **Framework:** React 18
- **Build Tool:** Vite
- **Styling:** Tailwind CSS (Vanilla CSS structure with tokens)
- **Routing:** React Router DOM

### Backend
- **Language:** Java 8 (Servlets & JDBC)
- **Server:** Apache Tomcat 9
- **Database:** MySQL 8
- **Networking:** 
  - RMI (Remote Method Invocation)
  - TCP Sockets (Chat & Status Server)
  - UDP Multicast (Alerts/Notifications)

---

## 2. Verify Prerequisites

Ensure you have the following installed on your machine:
- **Node.js** (v18+) & **npm**
- **Java JDK 8** (Required for RMI and Servlet compatibility)
- **Apache Tomcat 9**
- **MySQL 8**

Open your terminal and verify:
```bat
java -version
javac -version
mysql --version
node -v
npm -v
```

---

## 3. Database Setup

1. Open your terminal and connect to MySQL:
   ```bat
   mysql -u root -p
   ```
   *(Enter your password when prompted, usually `1507` or `root`)*

2. Run the database seed script to initialize the schema and populate dummy data:
   ```sql
   source E:/college-portal-mini-project/db/seed_v3.sql;
   ```
   *Note: Adjust the path if your project is located elsewhere.*

**Default Credentials:**
- `admin` / `admin123` (Role: ADMIN)
- `faculty` / `faculty123` (Role: FACULTY)
- `rahul` / `rahul123` (Role: STUDENT)
- `priya` / `priya123` (Role: STUDENT)

---

## 4. Backend Setup & Deployment (Tomcat)

### 4.1 Database Credentials
The database configuration is located in `src/com/college/utils/DBConnection.java`. 
It defaults to `root` and `1507`. If your password differs, either update the file or set environment variables:
```bat
setx COLLEGE_DB_URL "jdbc:mysql://localhost:3306/college_db?useSSL=false&serverTimezone=UTC"
setx COLLEGE_DB_USER "root"
setx COLLEGE_DB_PASS "your_password"
```

### 4.2 Required Libraries
Ensure the following `.jar` files exist in `WebContent/WEB-INF/lib`:
- `mysql-connector-j-8.x.x.jar`
- `jstl-1.2.jar` (Legacy support)

### 4.3 Deploying to Tomcat
We have included an automated deployment script that compiles the Java classes and copies them to Tomcat.

1. Ensure Tomcat is stopped.
2. In the project root terminal, run:
   ```bat
   scripts\deploy-tomcat.bat
   ```
   *This script uses your `CATALINA_HOME` environment variable to locate Tomcat.*

The backend REST API will now be running at `http://localhost:8080/AJT/api/`.

---

## 5. Background Services (RMI & Chat)

The application relies on specialized Java networking servers for chat and remote methods. Start these in separate terminal windows:

### 5.1 RMI Server
```bat
scripts\start-rmi.bat
```
*(Runs on port 1099)*

### 5.2 Chat Server
```bat
scripts\start-chat.bat
```
*(Runs on ports 9100 for messages and 9101 for status)*

---

## 6. Frontend Setup & Execution (React/Vite)

The frontend is completely decoupled from Tomcat during development. 

1. Open a new terminal and navigate to the frontend directory:
   ```bat
   cd frontend
   ```
2. Install Node dependencies:
   ```bat
   npm install
   ```
3. Start the Vite development server:
   ```bat
   npm run dev
   ```

**The application is now accessible at:** [http://localhost:5173](http://localhost:5173)

*(Note: Vite is configured to automatically proxy API requests to `http://localhost:8080/AJT` to avoid CORS issues).*

---

## 7. Features Overview

- **Authentication:** Stateless cookie-based session management.
- **Resource Library:** Hierarchical file sharing (Branch -> Year -> Sem -> Subject).
  - Admins/Faculty can directly upload and delete files.
  - Student uploads require Faculty/Admin approval.
  - Universal File Preview capability built directly into the UI.
- **Notice Boards:** Global and Group-specific announcements.
- **Live Chat:** Direct messaging and Group chats utilizing TCP sockets.
- **Admin Dashboard:** System logs and system-wide Alert broadcasting.

---

## 8. Common Troubleshooting

- **405 Method Not Allowed on API requests:** This usually means the Java class wasn't recompiled. Stop Tomcat, run `Remove-Item -Recurse -Force "build\classes"` (in PowerShell) to clear the cache, and re-run `scripts\deploy-tomcat.bat`.
- **"Chat Offline" in UI:** Ensure `scripts\start-chat.bat` is running in the background.
- **Vite Proxy Errors (502 Bad Gateway):** Make sure Tomcat is actively running on port 8080 and the backend has been successfully deployed.
- **Port Conflicts:** If ports `8080`, `1099`, `5173`, `9100`, or `9101` are blocked, use `netstat -ano | findstr :<port>` to find the conflicting PID and terminate it.
