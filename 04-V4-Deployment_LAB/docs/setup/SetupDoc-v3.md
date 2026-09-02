# SetupDoc-v3.md
# DigiStack Bank — P01 Version 3
# Title: Basic Transaction (Deposit & Withdraw)

---

## §1 Overview

Version 3 introduces the first financial transactions — Deposit and
Withdraw — using a full Controller → Service → DAO → DB enterprise
layering pattern. The `accounts` table is added to PostgreSQL with a
foreign key to `users`. Four new Java classes implement the layering:
`Account` (model), `AccountDao` (SQL only), `AccountService` (business
rules including overdraft rejection and frozen account enforcement), and
`AccountServlet` (controller, PRG pattern). A `BalanceJsonServlet`
returns balance as JSON for the Dashboard's AJAX balance toggle.
`DashboardServlet` is updated to load live account data on every visit.
`Dashboard.jsp` is retrofitted to show the real account number, account
type, and a frozen account banner. WAS ClassLoader policy is explicitly
configured: PARENT_FIRST class loader order, SINGLE WAR class loader
policy. The EAR is renamed to `digistack-bank-v3.ear`.

WebSphere topics: Enterprise application layering (Controller → Service
→ DAO → DB), ClassLoader policy (PARENT_FIRST / SINGLE), EAR redeploy
(version name change v2 → v3).

---

## §2 VM Setup

Same two VMs as v1 and v2. No new VMs powered on this version.

| VM | Role | IP | vCPU | RAM | Status |
|---|---|---|---|---|---|
| dsb-dmgr | Standalone WAS AppServer | 192.168.10.10 | 2 | 3 GB | Running |
| dsb-db | PostgreSQL 16 | 192.168.10.30 | 2 | 2 GB | Running |

Version-specific items:
- `V3__create_accounts.sql` migration added `accounts` table with
  foreign key to `users` and balance check constraint
- `digistack-bank-v2` uninstalled, `digistack-bank-v3` installed
- ClassLoader configured: PARENT_FIRST + SINGLE (Admin Console and
  wsadmin script `v3_set_classloader.py`)
- Development: Windows laptop (VSCode + Maven) — code written here,
  EAR built here, copied to dsb-dmgr via scp

---

## §3 Pre-Deployment Checklist

- [x] **01_Architecture diagram check** — opened `01_Architecture/README.md`.
      Triggers checked for v3:
      `06_Database_ER_Diagram.md` — triggers at v3 (`accounts` table
      added). Updated to show `accounts` table with FK to `users`.
      `03_Request_Flows.md` — extended to include Deposit/Withdraw
      flow (Browser → AccountServlet → AccountService → AccountDao →
      PostgreSQL → redirect → Account.jsp).
      `08_Deployment_Architecture.md` — updated for v3 EAR.
      All other diagrams: no new trigger at v3 — untouched.
- [x] Previous SetupDoc verified — SetupDoc-v2.md reviewed and
      confirmed complete before v3 work began
- [x] VM snapshot taken — dsb-dmgr and dsb-db snapshotted in VMware
      Workstation before Sprint 1 work began
- [x] Git branch created — `feature/v3-transactions`, created at
      Sprint 1 Step 9

---

## §4 Step-by-Step Configuration

### §4.1 WebSphere Admin Console Steps

1. Navigated to `http://192.168.10.10:9060/ibm/console`, logged in
   as `wasadmin`. Result: Welcome page loaded.

2. Applications → WebSphere enterprise applications →
   tick `digistack-bank-v2` → Stop → wait for red X.
   Result: digistack-bank-v2 stopped.

3. Tick `digistack-bank-v2` → Uninstall → OK → Save.
   Result: digistack-bank-v2 removed from application list.

4. Install → Local file system → Browse →
   `digistack-bank-v3.ear` → Next → defaults →
   WAR mapped to `server1`, virtual host `default_host` →
   Finish → Save.
   Result: `Application digistack-bank-v3 installed successfully.`

5. Tick `digistack-bank-v3` → Start.
   Result: green arrow ▶.

6. Applications → digistack-bank-v3 →
   Class loading and update detection:
   - Class loader order: PARENT_FIRST
   - WAR class loader policy: Single class loader for application
   → OK → Save.
   Result: ClassLoader policy saved.

7. (Mid-sprint update path): Applications → digistack-bank-v3 →
   Update → Replace entire application → browse new EAR →
   Next → Finish → Save → Stop → Start.
   Used during Sprint 4 redeploy and Sprint 5 formal cycle.

### §4.2 wsadmin / Command-Line Steps

1. Deployment script — `scripts/v3_deploy.py` run via:

wsadmin.sh -lang jython -username wasadmin -password <redacted>
-f /tmp/v3_deploy.py

   Result: `=== Deployment complete. ===`
   digistack-bank-v2 uninstalled, digistack-bank-v3 installed
   and started, state: STARTED.

2. ClassLoader configuration — `scripts/v3_set_classloader.py`:

wsadmin.sh -lang jython -username wasadmin -password <redacted>
-f /tmp/v3_set_classloader.py

   Result: `=== ClassLoader configuration complete. ===`
   PARENT_FIRST and SINGLE confirmed set.

3. Logging verification — `scripts/v3_verify_logging.py`:

wsadmin.sh -lang jython -username wasadmin -password <redacted>
-f /tmp/v3_verify_logging.py

   Result: `=== Logging verification complete. ===`
   All rotation settings OK (50 MB / 3 files).
   Trace spec *=info confirmed at both config and runtime level.

4. backupConfig:

./backupConfig.sh
/opt/backups/was-config/devdsbinappserver01_v3_backup.zip

   Result: `ADMU5002I: XXX files successfully backed up`

### §4.3 Database Changes

Migration executed on dsb-db:

psql -U digistack_app -d digistack_bank -h 127.0.0.1
-f /tmp/V3__create_accounts.sql


Output:

CREATE TABLE
ALTER TABLE
ALTER TABLE
ALTER TABLE
ALTER TABLE
CREATE INDEX
INSERT 0 1
INSERT 0 1


Rollback script (not executed, on file):
`db/rollback/V3__rollback_accounts.sql`

Verification:
```sql
SELECT id, user_id, account_number, account_type,
       balance, is_frozen
FROM accounts ORDER BY id;
```
Result: 2 rows — DSB0000000001 (user 1, SAVINGS),
DSB0000000002 (user 2, SAVINGS). Both is_frozen=false.

Foreign key confirmed:
```sql
INSERT INTO accounts (user_id, ...) VALUES (999, ...);
-- ERROR: violates foreign key constraint fk_accounts_user_id
```

Balance check constraint confirmed:
```sql
UPDATE accounts SET balance = -1.00 WHERE id = 1;
-- ERROR: violates check constraint chk_accounts_balance
```

### §4.4 Application Deployment

Build command (run on Windows laptop):

cd C:\Projects\digistack-bank-parent
mvn clean package

Result: `BUILD SUCCESS`, produces
`digistack-bank-ear\target\digistack-bank-v3.ear`.

WAR contents confirmed:

jar tf digistack-bank-web\target\digistack-bank-web-1.0.war

Result: 12 class files confirmed including Account, AccountDao,
AccountService, InsufficientFundsException, AccountServlet,
BalanceJsonServlet.

Hand-off to WAS VM:

scp digistack-bank-v3.ear root@192.168.10.10:/tmp/
scp scripts/v3_deploy.py root@192.168.10.10:/tmp/
scp scripts/v3_set_classloader.py root@192.168.10.10:/tmp/


Deployed via Admin Console (§4.1 steps 2–5) and via
`v3_deploy.py` (§4.2 step 1) — both paths confirmed working.
ClassLoader re-applied after fresh install via
`v3_set_classloader.py`. Server restarted to activate
ClassLoader settings.

New files in this version:
- `src/main/webapp/Account.jsp` — deposit/withdraw page
- `src/main/java/.../servlet/AccountServlet.java`
- `src/main/java/.../servlet/BalanceJsonServlet.java`
- `src/main/java/.../model/Account.java`
- `src/main/java/.../dao/AccountDao.java`
- `src/main/java/.../service/AccountService.java`
- `src/main/java/.../exception/InsufficientFundsException.java`
- `db/migrations/V3__create_accounts.sql`
- `db/rollback/V3__rollback_accounts.sql`
- `scripts/v3_deploy.py`
- `scripts/v3_set_classloader.py`
- `scripts/v3_verify_logging.py`

Updated files in this version:
- `src/main/webapp/Dashboard.jsp` — live account card, frozen
  banner, AJAX balance toggle, Deposit/Withdraw tiles active
- `src/main/java/.../servlet/DashboardServlet.java` — account
  data loaded via AccountService on every Dashboard visit

---

## §5 Verification Steps

See `TestCases-v3.md` for full detail. Summary:
- v1 Regression Pack (13 cases): all Pass
- v2 Regression Pack (10 cases): all Pass
- 9/9 Critical v3 cases: Pass
- 9/9 High v3 cases: Pass
- 4/4 Medium v3 cases: Pass
- 2/2 Low v3 cases: Pass
- Grand total: 47/47 cases Pass

---

## §6 Rollback Procedure

**Option A — VM Snapshot Restore (fastest):**
1. VMware Workstation → dsb-dmgr → Snapshot → Revert to
   pre-v3 snapshot.
2. Repeat for dsb-db if database rollback needed.

**Option B — Manual Undo:**
1. Stop and uninstall `digistack-bank-v3` via Admin Console.
2. Restore WAS config from v2 backup:

./stopServer.sh server1 -username wasadmin -password <redacted>
./restoreConfig.sh
/opt/backups/was-config/devdsbinappserver01_v2_backup.zip
./startServer.sh server1

3. Reinstall `digistack-bank-v2.ear` via Admin Console.
4. Roll back the database:

psql -U digistack_app -d digistack_bank -h 127.0.0.1
-f db/rollback/V3__rollback_accounts.sql

   Result: `DROP TABLE` — accounts table removed.
5. Verify: `\dt` in psql shows only `app_config` and `users` —
   no `accounts` table.

---

## §7 Known Issues / Troubleshooting

| Issue | Cause | Resolution |
|---|---|---|
| Dashboard balance shows "View in Account" instead of amount | `/BalanceJson` returned null — DB connection failed or session expired | Confirm PostgreSQL running on dsb-db, confirm session still active, check SystemOut.log for BalanceJsonServlet errors |
| ClassLoader settings reset after fresh install | AdminApp.install() resets ClassLoader to WAS defaults | Always re-run `v3_set_classloader.py` after any fresh install (not needed for updates) |
| Deposit/Withdraw buttons disabled even when account not frozen | JSP compiled ClassLoader issue — old version of Account.class loaded | Stop/Start the application via Admin Console to force class reload |

**Known Technical Debt (carried forward):**
- Direct JDBC with hardcoded credentials in AccountService,
  DashboardServlet — replaced at v7 with JNDI DataSource
  (jdbc/BankDS)
- App-layer authentication only — WAS security roles at v10
- No transaction history table yet — Deposit/Withdraw amounts
  are not persisted beyond the balance update (deferred)

---

## §8 Sign-off Table

| Item | Status |
|---|---|
| Setup completed | ✅ |
| Verification passed | ✅ (47/47 test cases — see TestCases-v3.md) |
| Documentation reviewed | ✅ |
| backupConfig baseline captured | ✅ — `devdsbinappserver01_v3_backup.zip` |
| Smoke test passed | ✅ — 9/9 checks |
| Reviewed by | _________________ |
| Approved date | _________________ |

---

*This is SetupDoc-v3.md. Companion: TestCases-v3.md (test detail),
FaultDrill-v3.md (Sprint 8, non-gating).*