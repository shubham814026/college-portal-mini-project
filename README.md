# College Communication and Resource Sharing System

Java web application using Servlet, JSP, JDBC, RMI, TCP sockets, UDP multicast, and Java I/O.

This guide is written for beginners and is intended to be enough to run the project from scratch on Windows.

## 1. What You Need

Required software:
- JDK 8 (recommended for this project)
- Apache Tomcat 9
- MySQL 8
- MySQL command-line client (comes with MySQL server install)

Required libraries in application:
- mysql-connector-j-8.x.x.jar
- jstl-1.2.jar

Recommended tools:
- VS Code

## 2. Verify Prerequisites

Open terminal and run:

```bat
java -version
javac -version
mysql --version
```

If any command is missing, install that component and restart terminal.

### 2.1 Your Current Detected Setup

- Tomcat: `C:\Tomcat` (Apache Tomcat/9.0.115)
- Java: `C:\Program Files\Java\jdk1.8.0_202`

Tomcat version check command:

```bat
cd C:\Tomcat\bin
version.bat
```

## 3. Project Structure

Main folders:
- src/com/college/servlets
- src/com/college/service
- src/com/college/dao
- src/com/college/models
- src/com/college/utils
- src/com/college/rmi
- src/com/college/chat
- src/com/college/network
- src/com/college/filter
- WebContent
- db
- scripts

Important files:
- db/schema.sql
- WebContent/WEB-INF/web.xml
- src/com/college/utils/DBConnection.java
- scripts/start-rmi.bat
- scripts/start-chat.bat

## 4. Database Setup

### 4.1 Create schema and seed data

Run in terminal:

```bat
mysql -u root -p
```

Then inside mysql prompt:

```sql
source C:/Users/Purvesh Rohit/OneDrive/Desktop/AJT/db/schema.sql;
```

This creates:
- college_db database
- users, notices, messages, files, alerts, logs tables
- default users

### 4.2 Default credentials from seed

- admin / admin123
- rahul / rahul123
- priya / priya123

## 5. JDBC Configuration

DB configuration is in src/com/college/utils/DBConnection.java.

Resolution order:
1. Environment variable
2. Java system property
3. Hardcoded local default fallback

Environment variables used:
- COLLEGE_DB_URL
- COLLEGE_DB_USER
- COLLEGE_DB_PASS

Optional set commands on Windows:

```bat
setx COLLEGE_DB_URL "jdbc:mysql://localhost:3306/college_db?useSSL=false&serverTimezone=UTC"
setx COLLEGE_DB_USER "root"
setx COLLEGE_DB_PASS "your_password"
```

Close and reopen terminal after setx.

## 6. Add Required JARs

Copy these into WebContent/WEB-INF/lib:
- mysql-connector-j-8.x.x.jar
- jstl-1.2.jar

If this folder does not exist, create it.

## 7. Deploy to Tomcat 9

You can deploy with VS Code or WAR.

### Option A: VS Code deployment (beginner friendly)

1. Install VS Code extensions:
- Extension Pack for Java
- Community Server Connectors (or Tomcat for Java)
2. Open this project folder in VS Code.
3. Register Tomcat 9 in VS Code (Server view) by pointing to your Tomcat installation folder, for example `C:\tomcat`.
4. Add/deploy this web app to the Tomcat server from Server view.
5. Start Tomcat from VS Code Server controls.

If VS Code asks for Tomcat home path, use:

```text
C:\Tomcat
```

### Option B: WAR deployment

1. Export project as WAR file.
2. Copy WAR to Tomcat webapps folder.
3. Start Tomcat.

Tomcat start command:

```bat
set "CATALINA_HOME=C:\Tomcat"
C:\Tomcat\bin\startup.bat
```

Tomcat stop command:

```bat
C:\Tomcat\bin\shutdown.bat
```

## 8. Start RMI and Chat Components

Start these from project root in separate terminals.

### 8.1 Start RMI server

```bat
scripts\start-rmi.bat
```

Or manual:

```bat
java com.college.rmi.RMIServer
```

Note: RMIServer creates the registry programmatically on port 1099.

### 8.2 Start Chat server

```bat
scripts\start-chat.bat
```

Or manual:

```bat
java com.college.chat.ChatServer
```

Chat server also starts status socket on port 9101.

## 9. Startup Order

Always follow this order:
1. MySQL service
2. Run db/schema.sql (first time, or after schema reset)
3. RMI server (1099)
4. Chat server (9100 and 9101)
5. Tomcat and web app

Reference: scripts/startup-order.md

## 10. Open Application

URL format:

http://localhost:8080/<app-context>/login

Common context values:
- AJT
- project name configured in VS Code server settings

Example:

http://localhost:8080/AJT/login

## 11. Ports Used

- Tomcat HTTP: 8080
- RMI: 1099
- Chat TCP: 9100
- Chat status: 9101
- Multicast: 230.0.0.1:8888

Check ports on Windows:

```bat
netstat -ano | findstr :8080
netstat -ano | findstr :1099
netstat -ano | findstr :9100
netstat -ano | findstr :9101
```

## 12. File Upload Storage

Upload path resolution:
1. Servlet container uploads path
2. Fallback to project working directory uploads folder

Ensure uploads folder exists and is writable:

```bat
mkdir uploads
```

Upload constraints:
- max size 10 MB
- allowed extensions: pdf, docx, pptx, zip

## 13. Common Errors and Fixes

### 13.1 Port already in use
Symptom: BindException for 8080, 1099, 9100, or 9101.

Fix:
1. Find process using port with netstat command above.
2. Stop conflicting process.
3. Restart service.

### 13.2 JDBC connection failure
Symptom: login or data pages fail with DB errors.

Fix:
1. Ensure MySQL service is running.
2. Ensure schema.sql executed successfully.
3. Verify DB URL, user, password in environment or DBConnection fallback.
4. Ensure mysql-connector JAR exists in WebContent/WEB-INF/lib.

### 13.3 Servlet not found (404)
Symptom: URL returns 404 for servlet endpoints.

Fix:
1. Verify app context path.
2. Redeploy app.
3. Restart Tomcat.
4. Confirm servlet class annotations are compiled and deployed.

### 13.4 ClassNotFoundException for MySQL driver or JSTL tags
Symptom: com.mysql.cj.jdbc.Driver not found, or JSTL tag errors.

Fix:
1. Add mysql-connector and jstl JARs to WebContent/WEB-INF/lib.
2. Clean redeploy app.

### 13.5 Chat offline status in UI
Symptom: message send returns chat-offline.

Fix:
1. Start ChatServer.
2. Verify ports 9100 and 9101.

## 14. Verification Checklist

Run these checks after startup:

1. Authentication
- student login works
- admin login works
- invalid login shows safe message

2. Notices
- admin posts notice
- student sees notice list updated

3. Files
- valid upload works
- invalid type and oversize are blocked
- download works for existing file

4. Alerts
- admin sends alert
- student sees alert banner through status poll

5. Chat
- online users endpoint returns list
- message send works when chat server is up
- chat-offline shown when server is down

6. Logs
- login/logout and key actions appear in logs view

Detailed test list: docs/INTEGRATION_TEST_CHECKLIST.md

## 15. Security and Runtime Notes

- Session timeout is 30 minutes.
- Unauthorized admin access returns 403.
- Error pages for 403/404/500 are configured in WebContent/WEB-INF/web.xml.
- Request correlation ID is returned as X-Request-Id on protected endpoints for easier tracing.

## 16. Useful References

- docs/BACKEND_INTEGRATION_PLAN.md
- docs/IMPLEMENTATION_ROADMAP_FINAL.md
- docs/INTEGRATION_TEST_CHECKLIST.md
- docs/RISK_REGISTER.md
- scripts/startup-order.md

## 17. Quick Start (Copy-Paste Commands)

Run these in separate terminals where needed.

### Terminal 1: Database schema (first run or reset)

```bat
mysql -u root -p
source C:/Users/Purvesh Rohit/OneDrive/Desktop/AJT/db/schema.sql;
```

### Terminal 2: Start RMI server

```bat
cd C:\Users\Purvesh Rohit\OneDrive\Desktop\AJT
scripts\start-rmi.bat
```

### Terminal 3: Start chat server

```bat
cd C:\Users\Purvesh Rohit\OneDrive\Desktop\AJT
scripts\start-chat.bat
```

### Terminal 4: Start Tomcat

You may need to set `CATALINA_HOME` if Tomcat cannot find its home directory:

```bat
set "CATALINA_HOME=C:\Tomcat"
C:\Tomcat\bin\startup.bat
```

Open browser:

```text
http://localhost:8080/AJT/login
```

If `AJT` context does not work, check your deployed context name in VS Code Server view.

## 18. One-Command Redeploy (Recommended)

If you edit Java code and want to redeploy quickly, run:

```bat
cd C:\Users\Purvesh Rohit\OneDrive\Desktop\AJT
scripts\deploy-tomcat.bat
```

What this script does:
- compiles all Java sources
- copies `WebContent` into `C:\Tomcat\webapps\AJT`
- copies compiled classes into `C:\Tomcat\webapps\AJT\WEB-INF\classes`
- restarts Tomcat

## 19. Community Server Connectors with JDK 17

If Community Server Connectors fails on JDK 8, open VS Code with JDK 17:

```bat
cd C:\Users\Purvesh Rohit\OneDrive\Desktop\AJT
scripts\start-vscode-jdk17.bat
```

To verify the launcher configuration only:

```bat
scripts\start-vscode-jdk17.bat --check
```

Then in VS Code, create/register Tomcat server from Server view.
