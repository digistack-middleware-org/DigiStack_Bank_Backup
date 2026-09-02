# SetupDoc-v2.md
# DigiStack Bank — P01 Version 2
# Title: Login & Session

---

## §1 Overview

Version 2 adds authentication and HTTP session management to the
DigiStack Bank application. A `users` table is created in PostgreSQL
with salted SHA-256 password hashing. Three new servlets are introduced:
`LoginServlet` (credential validation, session creation),
`DashboardServlet` (session guard, post-login landing page),
and `LogoutServlet` (session invalidation). The Dashboard shows a
time-of-day greeting, last login timestamp (security signal), account
summary placeholder, and quick action tiles (all disabled until v3).
The EAR is renamed from `digistack-bank-v1.ear` to
`digistack-bank-v2.ear` per the project naming standard. The v1
application (`digistack-bank-v1`) is uninstalled and replaced by
`digistack-bank-v2` — context root `/digistack-bank` is unchanged.

WebSphere topic: JVM/Application startup behaviour, HTTP session
creation, session-scoped logs, EAR redeploy (v2 over v1 — different
application name, same context root).

---

## §2 VM Setup

Same two VMs as v1. No new VMs powered on this version.

| VM | Role | IP | vCPU | RAM | Status |
|---|---|---|---|---|---|
| dsb-dmgr | Standalone WAS AppServer | 192.168.10.10 | 2 | 3 GB | Running |
| dsb-db | PostgreSQL 16 | 192.168.10.30 | 2 | 2 GB | Running |

Version-specific items:
- `V2__create_users.sql` migration added `users` table to `digistack_bank`
- `SeedUsers.java` utility run on Windows laptop to set correct
  SHA-256 password hashes for both seed users
- `digistack-bank-v1` uninstalled from WAS; `digistack-bank-v2`
  installed in its place
- Development: Windows laptop (VSCode + Maven) — code written here,
  EAR built here, copied to dsb-dmgr via scp for deployment

---

## §3 Pre-Deployment Checklist

- [x] **01_Architecture diagram check** — opened `01_Architecture/README.md`.
      Triggers checked for v2:
      `06_Database_ER_Diagram.md` — triggers at v2 (`users` table added).
      Updated to show `users` table alongside `app_config`.
      `03_Request_Flows.md` — extended to include Login flow
      (Browser → LoginServlet → PostgreSQL → Dashboard redirect).
      All other diagrams: no new trigger at v2 — untouched.
- [x] Previous SetupDoc verified — SetupDoc-v1.md reviewed and confirmed
      complete before v2 work began
- [x] VM snapshot taken — dsb-dmgr and dsb-db snapshotted in VMware
      Workstation before Sprint 1 work began
- [x] Git branch created — `feature/v2-login`, created at Sprint 1 Step 8

---

## §4 Step-by-Step Configuration

### §4.1 WebSphere Admin Console Steps

1. Navigated to `http://192.168.10.10:9060/ibm/console`, logged in
   as `wasadmin`. Result: Welcome page loaded.

2. Applications → Application Types → WebSphere enterprise applications
   → tick `digistack-bank-v1` → Stop.
   Result: status changed to red X (Stopped).

3. Tick `digistack-bank-v1` → Uninstall → OK → Save.
   Result: `digistack-bank-v1` removed from application list.

4. Install → Local file system → Browse →
   `digistack-bank-v2.ear` → Next → Next (defaults) →
   Confirm WAR mapped to `server1`, virtual host `default_host` →
   Finish → Save.
   Result: `Application digistack-bank-v2 installed successfully.`

5. Tick `digistack-bank-v2` → Start.
   Result: green arrow ▶ next to `digistack-bank-v2`.

6. Verified Manage Modules: WAR mapped to `server1`,
   virtual host `default_host`. Context root: `/digistack-bank`.

### §4.2 wsadmin / Command-Line Steps

1. Deployment script — `scripts/v2_deploy.py` run via:

wsadmin.sh -lang jython -username wasadmin -password <redacted>
-f /tmp/v2_deploy.py

   Result: `=== Deployment complete. ===`
   Old application (`digistack-bank-v1`) uninstalled,
   new application (`digistack-bank-v2`) installed and started,
   application state: STARTED.

2. backupConfig:

./backupConfig.sh
/opt/backups/was-config/devdsbinappserver01_v2_backup.zip

   Result: `ADMU5002I: XXX files successfully backed up`

### §4.3 Database Changes

Migration executed on dsb-db:

psql -U digistack_app -d digistack_bank -h 127.0.0.1
-f /tmp/V2__create_users.sql


Output:

CREATE TABLE
ALTER TABLE
ALTER TABLE
ALTER TABLE
CREATE INDEX
INSERT 0 1
INSERT 0 1


Password hashes set by running SeedUsers utility on Windows laptop:

java -cp "digistack-bank-web\target\classes;C:\Tools\postgresql-42.7.3.jar"
com.digistack.bank.util.SeedUsers


Output:

Connected to digistack_bank on dsb-db.
Updated customer1 with correct password hash.
Updated admin1 with correct password hash.
Seed complete. Both users ready for login.


Rollback script (not executed, on file):
`db/rollback/V2__rollback_users.sql`

Verification:

SELECT username, role, is_active, LENGTH(password_hash)
FROM users;

Result: 2 rows, both is_active=true, hash length=64 (SHA-256 hex).

### §4.4 Application Deployment

Build command (run on Windows laptop):

cd C:\Projects\digistack-bank-parent
mvn clean package

Result: `BUILD SUCCESS`, produces
`digistack-bank-ear\target\digistack-bank-v2.ear`.

Hand-off to WAS VM:

scp digistack-bank-v2.ear root@192.168.10.10:/tmp/
scp scripts/v2_deploy.py root@192.168.10.10:/tmp/


Deployed via Admin Console (§4.1 steps 2–5) first to prove GUI path,
then re-deployed via `v2_deploy.py` (§4.2 step 1) to prove scripted
path — both confirmed working.

New files in this version:
- `src/main/webapp/Login.jsp` — branded login page
- `src/main/webapp/Dashboard.jsp` — post-login dashboard
- `src/main/java/.../LoginServlet.java` — credential validation
- `src/main/java/.../DashboardServlet.java` — session guard + display
- `src/main/java/.../LogoutServlet.java` — session invalidation
- `src/main/java/.../util/PasswordUtil.java` — SHA-256 hashing
- `src/main/java/.../util/SeedUsers.java` — one-time seed utility
- `db/migrations/V2__create_users.sql` — users table
- `db/rollback/V2__rollback_users.sql` — rollback script
- `scripts/v2_deploy.py` — wsadmin deployment script

---

## §5 Verification Steps

See `TestCases-v2.md` for full detail. Summary:
- v1 Regression Pack (13 cases): all Pass
- 7/7 Critical v2 cases: Pass
- 7/7 High v2 cases: Pass
- 4/4 Medium v2 cases: Pass
- 2/2 Low v2 cases: Pass
- Grand total: 33/33 cases Pass

---

## §6 Rollback Procedure

**Option A — VM Snapshot Restore (fastest):**
1. VMware Workstation → dsb-dmgr → Snapshot → Revert to pre-v2 snapshot.
2. Repeat for dsb-db if database rollback also needed.

**Option B — Manual Undo:**
1. Stop and uninstall `digistack-bank-v2` via Admin Console.
2. Restore WAS config from v1 backup:

./stopServer.sh server1 -username wasadmin -password <redacted>
./restoreConfig.sh
/opt/backups/was-config/devdsbinappserver01_v1_backup.zip
./startServer.sh server1

3. Reinstall `digistack-bank-v1.ear` via Admin Console.
4. Roll back the database:

psql -U digistack_app -d digistack_bank -h 127.0.0.1
-f db/rollback/V2__rollback_users.sql

   Result: `DROP TABLE` — users table removed.
5. Verify: `\dt` in psql shows only `app_config`, no `users` table.

---

## §7 Known Issues / Troubleshooting

| Issue | Cause | Resolution |
|---|---|---|
| SeedUsers fails with `Connection refused` | dsb-db VM not powered on, or port 5432 not open | Confirm `systemctl status postgresql-16` on dsb-db, confirm firewall rule for 5432 |
| Login always fails even with correct password | SeedUsers not run after V2 migration — password_hash still PLACEHOLDER | Run SeedUsers utility again, verify hash length = 64 in DB |
| Dashboard shows blank email | email attribute not set in DashboardServlet | Confirmed fixed in Sprint 4 — DashboardServlet reads email from session |

**Known Technical Debt (carried forward):**
- Direct JDBC with hardcoded credentials in LoginServlet,
  DashboardServlet — replaced at v7 with JNDI DataSource (jdbc/BankDS)
- App-layer authentication only (username/password check in servlet) —
  no WAS security roles yet. WAS security roles introduced at v10
  (Administrative Security topic)

---

## §8 Sign-off Table

| Item | Status |
|---|---|
| Setup completed | ✅ |
| Verification passed | ✅ (33/33 test cases — see TestCases-v2.md) |
| Documentation reviewed | ✅ |
| backupConfig baseline captured | ✅ — `devdsbinappserver01_v2_backup.zip` |
| Smoke test passed | ✅ — 7/7 checks |
| Reviewed by | _________________ |
| Approved date | _________________ |

---

*This is SetupDoc-v2.md. Companion: TestCases-v2.md (test detail),
FaultDrill-v2.md (Sprint 8, non-gating).*
