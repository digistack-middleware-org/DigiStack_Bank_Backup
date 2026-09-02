# SetupDoc-v4.md
# DigiStack Bank — P01 Version 4
# Title: EAR Update, Rollback & Application Lifecycle

---

## §1 Overview

Version 4 introduces zero new banking functionality — the only visible
application change is a version label in the footer of Home.jsp,
Dashboard.jsp, Account.jsp, and Login.jsp. The entire version is a
100% WebSphere administration practice sprint focused on the
Application Lifecycle: Update Application (replacing EAR code without
changing the registered application name), Stop/Start/Restart at both
the application and server level, Uninstall+Install (used when the
application name changes, v3→v4), and a deliberate, real rollback to
the prior version's EAR followed by a verified forward redeploy. The
rollback exercise proved that a code-only rollback never touches the
database — account balances survived rollback and forward redeploy
identically.

WebSphere topics: Update Application, Application Lifecycle
(Started/Stopped states, application-level vs server-level stop),
Deployment Targets, Rollback (redeploy prior EAR pattern).

---

## §2 VM Setup

Same two VMs as v1, v2, v3. No new VMs powered on this version.

| VM | Role | IP | vCPU | RAM | Status |
|---|---|---|---|---|---|
| dsb-dmgr | Standalone WAS AppServer | 192.168.10.10 | 2 | 3 GB | Running |
| dsb-db | PostgreSQL 16 | 192.168.10.30 | 2 | 2 GB | Running |

Version-specific items:
- No database migration this version — zero schema changes
- `digistack-bank-v3` uninstalled, `digistack-bank-v4` installed
- A rollback copy of the v3 EAR (`digistack-bank-v3-rollback-copy.ear`)
  was preserved specifically for the Sprint 4 rollback exercise
- ClassLoader configuration (PARENT_FIRST/SINGLE) re-applied under the
  new `digistack-bank-v4` application name
- Development: Windows laptop (VSCode + Maven) — code written here,
  EAR built here, copied to dsb-dmgr via scp

---

## §3 Pre-Deployment Checklist

- [x] **01_Architecture diagram check** — opened `01_Architecture/README.md`.
      No diagram triggers met at v4 — no new VM, no schema change, no
      new request flow, no new cluster/MQ/security component. All
      diagrams remain untouched at their v3 state, correctly reflecting
      that v4 is a pure administration-practice version.
- [x] Previous SetupDoc verified — SetupDoc-v3.md reviewed and confirmed
      complete before v4 work began
- [x] VM snapshot taken — dsb-dmgr and dsb-db snapshotted in VMware
      Workstation before Sprint 1 work began
- [x] Git branch created — `feature/v4-lifecycle`, created at Sprint 1
      Step 5

---

## §4 Step-by-Step Configuration

### §4.1 WebSphere Admin Console Steps

1. Navigated to `http://192.168.10.10:9060/ibm/console`, logged in
   as `wasadmin`.

2. **Update path (Sprint 2):** Applications → digistack-bank-v3 →
   Update → Replace the entire application → Browse →
   digistack-bank-v4.ear → Next → Next → Finish → Save → Stop → Start.
   Result: `Application digistack-bank-v3 updated successfully.`
   Footer confirmed showing v4 label — application name unchanged.

3. **Application Stop/Start observation (Sprint 3):**
   Tick digistack-bank-v3 → Stop → confirmed Home page returns error
   while Admin Console remains reachable. Tick → Start → confirmed
   Home page reachable again.

4. **Uninstall + Install (Sprint 3):** Tick digistack-bank-v3 → Stop →
   Uninstall → OK → Save. Install → Local file system → Browse →
   digistack-bank-v4.ear → Next → defaults → Finish → Save → Start.
   Result: application registered as `digistack-bank-v4`.

5. **ClassLoader (Sprint 3):** Applications → digistack-bank-v4 →
   Class loading and update detection → Class loader order:
   PARENT_FIRST, WAR class loader policy: Single → OK → Save.

6. **Rollback (Sprint 4):** Applications → digistack-bank-v4 →
   Update → Replace entire application → Browse →
   digistack-bank-v3-rollback-copy.ear → Next → Finish → Save →
   Stop → Start. Result: footer reverted to "v1 — Foundation" —
   rollback visibly confirmed.

7. **Forward redeploy (Sprint 4/5):** Applications → digistack-bank-v4
   → Update → Replace entire application → Browse →
   digistack-bank-v4.ear → Next → Finish → Save → Stop → Start.
   Result: footer restored to "v4 — Application Lifecycle".

### §4.2 wsadmin / Command-Line Steps

1. Update script — `scripts/v4_update.py`:

wsadmin.sh -lang jython -username wasadmin -password <redacted>
-f /tmp/v4_update.py

   Result: `=== Update demonstration complete. ===`
   Confirmed AdminApp.update() replaces code, preserves app name.

2. Deployment script — `scripts/v4_deploy.py`:

wsadmin.sh -lang jython -username wasadmin -password <redacted>
-f /tmp/v4_deploy.py

   Result: `=== Deployment complete. ===`
   digistack-bank-v3 uninstalled, digistack-bank-v4 installed
   and started. Lesson learned: script's hardcoded OLD_APP_NAME
   requires manual state verification before re-running against
   an already-v4-named application (documented in Sprint 5 §4.4).

3. ClassLoader script — `scripts/v4_set_classloader.py`:

wsadmin.sh -lang jython -username wasadmin -password <redacted>
-f /tmp/v4_set_classloader.py

   Result: `=== ClassLoader configuration complete. ===`

4. Rollback script — `scripts/v4_rollback.py`:

wsadmin.sh -lang jython -username wasadmin -password <redacted>
-f /tmp/v4_rollback.py

   Result: `=== Rollback complete. ===`
   Footer confirmed reverted to v1 label after script execution.

5. Server-level stop/start/restart (Sprint 3):

./stopServer.sh server1 -username wasadmin -password <redacted>
./startServer.sh server1

   Result: confirmed both Admin Console and application unreachable
   during full server stop; `ps -ef | grep java | grep server1`
   returned no process. Restart via chained
   `stopServer.sh && startServer.sh` confirmed working.

6. backupConfig:

./backupConfig.sh
/apps/backups/was-config/devdsbinappserver01_v4_backup.zip

   Result: `ADMU5002I: XXX files successfully backed up`

### §4.3 Database Changes

**No database migration this version.** Zero schema changes.
`accounts`, `users`, and `app_config` tables are unchanged from v3.

Rollback exercise (Sprint 4) explicitly verified database
immutability across code deployments:
```sql
SELECT id, balance FROM accounts WHERE user_id = 1;
```
Balance recorded before rollback, confirmed identical immediately
after rollback, confirmed correctly reflecting a test deposit made
on rolled-back code after the subsequent forward redeploy — proving
no code deployment path (Update, rollback, or fresh Install) ever
touches application data.

### §4.4 Application Deployment

Build command (run on Windows laptop):

cd C:\Projects\digistack-bank-parent
mvn clean package

Result: `BUILD SUCCESS`, produces
`digistack-bank-ear\target\digistack-bank-v4.ear`.

A rollback copy of the v3 EAR was preserved BEFORE building v4:

copy digistack-bank-v3.ear digistack-bank-v3-rollback-copy.ear

This copy was essential — it captured the pre-v4-footer-edit state
used throughout Sprint 4's rollback exercise.

Hand-off to WAS VM:

scp digistack-bank-v4.ear root@192.168.10.10:/tmp/
scp digistack-bank-v3-rollback-copy.ear root@192.168.10.10:/tmp/
scp scripts/v4_deploy.py root@192.168.10.10:/tmp/
scp scripts/v4_update.py root@192.168.10.10:/tmp/
scp scripts/v4_rollback.py root@192.168.10.10:/tmp/
scp scripts/v4_set_classloader.py root@192.168.10.10:/tmp/


Only code files changed this version (footer labels):
- `src/main/webapp/Home.jsp`
- `src/main/webapp/Dashboard.jsp`
- `src/main/webapp/Account.jsp`
- `src/main/webapp/Login.jsp`

New scripts this version:
- `scripts/v4_update.py` — Update path demonstration
- `scripts/v4_deploy.py` — Uninstall+Install to register v4 name
- `scripts/v4_rollback.py` — scripted rollback to v3 code
- `scripts/v4_set_classloader.py` — ClassLoader re-application under
  the v4 application name

No new Java classes, no new JSPs, no new database tables — by design,
per the v4 roadmap entry: "100% admin-practice sprint, no feature work."

---

## §5 Verification Steps

See `TestCases-v4.md` for full detail. Summary:
- v1 Regression Pack (13 cases): all Pass
- v2 Regression Pack (10 cases): all Pass
- v3 Regression Pack (13 cases): all Pass
- 7/7 Critical v4 cases: Pass
- 5/5 High v4 cases: Pass
- 2/2 Medium v4 cases: Pass
- 2/2 Low v4 cases: Pass
- Grand total: 52/52 cases Pass

---

## §6 Rollback Procedure

**This version's Sprint 4 already demonstrated the primary rollback
pattern live.** Documented here as the standing procedure:

**Option A — VM Snapshot Restore (fastest, full rollback):**
1. VMware Workstation → dsb-dmgr → Snapshot → Revert to pre-v4
   snapshot.

**Option B — Code-only Redeploy Rollback (surgical, DB-safe):**
1. Applications → digistack-bank-v4 → Update → Replace entire
   application → Browse → prior version's EAR → Next → Finish →
   Save → Stop → Start.
2. Confirm the footer label reflects the older version.
3. Confirm database is unaffected — this pattern never touches data.
4. This is the pattern used throughout Sprint 4 and is now this
   project's standard rollback method for any version where only
   code (not schema) needs reverting.

**Option C — Full WAS Config Rollback (for WAS config changes):**
1. Stop and uninstall the current application.
2. `./restoreConfig.sh /apps/backups/was-config/<prior-version>_backup.zip`
3. Reinstall the prior version's EAR.
4. Used when a WAS configuration change (not just app code) caused
   the problem — e.g. a bad ClassLoader setting or resource
   definition.

---

## §7 Known Issues / Troubleshooting

| Issue | Cause | Resolution |
|---|---|---|
| v4_deploy.py fails with "application already exists" | Script hardcodes OLD_APP_NAME=digistack-bank-v3; if the app is already registered as digistack-bank-v4, the uninstall check is skipped and install collides with the existing v4 registration | Manually confirm/uninstall current application state via Admin Console before re-running the script against an already-renamed application |
| ClassLoader reverts to defaults unexpectedly | A fresh Install (via Uninstall+Install) resets ClassLoader settings; an Update does not | Always re-run the ClassLoader script after any Uninstall+Install cycle; not required after a plain Update |
| Footer label shows stale version after deployment | Browser cache serving an old cached page | Hard refresh (Ctrl+F5) or clear browser cache |

**Known Technical Debt (carried forward):**
- Direct JDBC with hardcoded credentials — replaced at v7 with JNDI
  DataSource (jdbc/BankDS)
- App-layer authentication only — WAS security roles at v10
- Deployment scripts hardcode prior application names rather than
  querying current state dynamically — acceptable for this lab-scale
  project, but flagged as a fragility that a real CI/CD pipeline
  would need to address with dynamic state detection

---

## §8 Sign-off Table

| Item | Status |
|---|---|
| Setup completed | ✅ |
| Verification passed | ✅ (52/52 test cases — see TestCases-v4.md) |
| Documentation reviewed | ✅ |
| backupConfig baseline captured | ✅ — `devdsbinappserver01_v4_backup.zip` |
| Smoke test passed | ✅ — 8/8 checks |
| Reviewed by | _________________ |
| Approved date | _________________ |

---

*This is SetupDoc-v4.md. Companion: TestCases-v4.md (test detail),
FaultDrill-v4.md (Sprint 8, non-gating).*