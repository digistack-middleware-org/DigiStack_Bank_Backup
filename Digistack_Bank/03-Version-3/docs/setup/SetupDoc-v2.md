# Setup Documentation — Version 2: Login & Session

**Part:** P01 — Foundation
**Prerequisite versions completed:** v1
**Estimated setup time:** 2-3 hours

---

## 1. Overview
Adds authentication (Login/Logout) and HTTP session management on top of
v1's foundation. Introduces users table, bcrypt password hashing,
session-scoped "last login" display.

## 2. VM Setup
No new VM/package changes — reuses dsb-dmgr (WAS) and dsb-db (PostgreSQL)
from v1. Reference SOE01, no restatement needed.

## 3. Pre-Deployment Checklist
- [x] v1 SetupDoc completed and verified
- [x] VM snapshot taken (pre-v2)
- [x] Git branch feature/v2-login-session created from develop

## 4. Step-by-Step Configuration

### 4.1 WebSphere Admin Console Steps
1. No new WebSphere config objects required for v2 (reuses existing
   profile, shared library, application definition from v1)
2. Updated existing digistack-bank-v1 application via Update > Replace
   entire application, using the newly-built EAR containing Login/Logout
   servlets and updated Home.jsp
3. Stopped and restarted the application to force running code refresh
4. Observed startup log sequence in SystemOut.log — clean start, no
   SEVERE/WARNING entries

### 4.2 wsadmin / Command-Line Steps
AdminApp.update(...) with -operation update, followed by
AdminControl.invoke(...) stop/start — used as the Method 2 equivalent
of the GUI Update Application steps above.

### 4.3 Database Changes
Migration: V2__create_users.sql (creates users table: id, username,
password_hash; seeds one test user)
Rollback: V2__create_users_rollback.sql (DROP TABLE users)

Real bcrypt hash generated via a temporary throwaway HashGenerator.java
utility class (jBCrypt library), run once via `mvn exec:java`, then
deleted after use. Test credentials: username testuser, password
Password123! (hash stored in users.password_hash, never plaintext).

### 4.4 Application Deployment
Same Maven multi-module project as v1, digistack-bank-web module
extended with LoginServlet.java, LogoutServlet.java, Login.jsp; jBCrypt
0.4 added as a compile-scope dependency (bundled in WAR, unlike the
provided-scope Servlet API/JDBC driver). Build command unchanged:
`mvn clean package` at digistack-bank-parent level.

## 5. Verification Steps
See TestCases-v2.md — 10/10 Critical+High test cases pass, including a
SQL injection resistance negative test and full session
lifecycle (login → session data visible → logout → session destroyed).

## 6. Rollback Procedure
- VM snapshot restore (pre-v2 snapshot), or:
- Redeploy v1's EAR via Update Application (Replace entire application),
  or:
- Run V2__create_users_rollback.sql if a schema-only rollback is needed

## 7. Known Issues / Troubleshooting
- Same hardcoded-credential technical debt as v1 (DB_PASSWORD constant
  in LoginServlet.java, matching HomeServlet.java) — scheduled for
  proper resolution at Version 7 (JNDI DataSource + JAAS Auth Alias).
- PreparedStatement used throughout for all user-input SQL (login
  lookup) — deliberate SQL injection prevention, worth noting as the
  correct pattern to replicate in future versions handling user input.

## 8. Sign-off
- [x] Setup completed successfully
- [x] All verification steps passed (10/10 Critical+High test cases)
- [x] Documentation reviewed for accuracy