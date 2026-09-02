# TestCases-v4.md
# DigiStack Bank — P01 Version 4
# Feature: EAR Update, Rollback & Application Lifecycle
# Test Execution Date: ___________
# Executed By: ___________
# WAS Version Confirmed: 9.0.5.28
# PostgreSQL Version Confirmed: 16
# EAR Deployed: digistack-bank-v4.ear

---

## Sign-Off Gate (TCS01 §2.7)

| Gate | Condition | Met? |
|---|---|---|
| 1 | All Critical test cases: Pass | |
| 2 | All High test cases: Pass | |
| 3 | No open Critical or High defects | |
| 4 | Regression Pack (v1+v2+v3 Critical + High) re-run and passing | |
| 5 | Reviewer name and approved date recorded | |
| 6 | SetupDoc-v4.md complete and followed | |
| 7 | backupConfig baseline captured | |
| 8 | Smoke test passes | |

Reviewer: _________________     Approved Date: ___________

---

## Regression Pack — v1 + v2 + v3 Re-Run

### v1 Regression (Critical)
| TC ID | Description | Status |
|---|---|---|
| TC-v1-01 | Admin Console reachable and login succeeds | |
| TC-v1-02 | server1 shows Started | |
| TC-v1-03 | Application shows Started (now digistack-bank-v4) | |
| TC-v1-04 | Home page loads at full URL | |
| TC-v1-05 | Context root redirect works | |
| TC-v1-06 | Live DB read — system status from app_config | |
| TC-v1-07 | HomeServlet DB read confirmed in SystemOut.log | |
| TC-v1-08 | PostgreSQL running and seed data present | |

### v1 Regression (High)
| TC ID | Description | Status |
|---|---|---|
| TC-v1-09 | WAS profile uses correct naming standard | |
| TC-v1-10 | WAS logging configured — 50 MB / 3 files | |
| TC-v1-11 | Diagnostic trace level is *=info | |
| TC-v1-12 | PostgreSQL JDBC driver present in lib/ext/jdbc/ | |
| TC-v1-13 | EAR module mapped to server1 and default_host | |

### v2 Regression (Critical)
| TC ID | Description | Status |
|---|---|---|
| TC-v2-01 | Login page loads correctly | |
| TC-v2-02 | Customer login → Dashboard | |
| TC-v2-03 | Admin login → Dashboard with Admin label | |
| TC-v2-04 | Session guard — unauthenticated → Login | |
| TC-v2-05 | Logout destroys session | |
| TC-v2-06 | Wrong password rejected | |
| TC-v2-07 | Non-existent username rejected | |

### v2 Regression (High)
| TC ID | Description | Status |
|---|---|---|
| TC-v2-12 | Login confirmed in SystemOut.log | |
| TC-v2-13 | Logout confirmed in SystemOut.log | |
| TC-v2-14 | users table has hashed passwords | |

### v3 Regression (Critical)
| TC ID | Description | Status |
|---|---|---|
| TC-v3-01 | accounts table seed data correct | |
| TC-v3-02 | Foreign key constraint enforced | |
| TC-v3-03 | Check constraint — balance cannot go negative | |
| TC-v3-04 | Deposit — balance increases correctly | |
| TC-v3-05 | Withdraw — balance decreases correctly | |
| TC-v3-06 | Overdraft rejected | |
| TC-v3-07 | Transactions confirmed in SystemOut.log | |
| TC-v3-08 | Dashboard account card shows live data | |
| TC-v3-09 | Dashboard View Balance via AJAX | |

### v3 Regression (High)
| TC ID | Description | Status |
|---|---|---|
| TC-v3-10 | ClassLoader PARENT_FIRST + SINGLE confirmed | |
| TC-v3-15 | Frozen account blocks deposit/withdraw | |
| TC-v3-16 | Frozen banner on Dashboard | |
| TC-v3-18 | Account page session guard | |

Note on TC-v1-03: application name is now `digistack-bank-v4`.

---

## v4 Smoke Test

| Check | Expected | Status |
|---|---|---|
| Admin Console reachable | Loads at port 9060 | |
| server1 Started | Green arrow | |
| digistack-bank-v4 Started | Green arrow | |
| Home page loads | HTTP 200, footer shows v4 label | |
| Login succeeds | Dashboard renders | |
| Deposit works | Success banner, balance updated | |
| ClassLoader correct | PARENT_FIRST + SINGLE confirmed | |
| Logout works | Redirected to Home | |

---

## v4 Test Cases

---

### TC-v4-01
| Field | Value |
|---|---|
| **ID** | TC-v4-01 |
| **Description** | Update Application replaces code without changing application name |
| **Type** | Functional |
| **Priority** | Critical |
| **Steps** | 1. Note current application name in Admin Console (digistack-bank-v4). 2. Navigate to Applications → digistack-bank-v4 → Update → Replace entire application. 3. Browse and select a different EAR build. 4. Complete the wizard. 5. Observe application name after update. |
| **Expected Result** | After Update completes, the application name remains exactly `digistack-bank-v4` — Update never changes the registered application name, only the underlying code. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v4-02
| Field | Value |
|---|---|
| **ID** | TC-v4-02 |
| **Description** | Application-level Stop does not affect the WAS server or Admin Console |
| **Type** | Functional |
| **Priority** | Critical |
| **Steps** | 1. Confirm Home page loads normally. 2. Stop digistack-bank-v4 via Admin Console. 3. Attempt to load `/digistack-bank/Home`. 4. Attempt to load Admin Console at port 9060. 5. Start digistack-bank-v4 again. |
| **Expected Result** | Step 3: Home page returns an error (404 or unavailable) — application is down. Step 4: Admin Console loads normally — the server1 JVM process is unaffected by stopping one application. Step 5: Home page reachable again after Start. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v4-03
| Field | Value |
|---|---|
| **ID** | TC-v4-03 |
| **Description** | Server-level Stop takes down both the application AND the Admin Console |
| **Type** | Functional |
| **Priority** | Critical |
| **Steps** | 1. On dsb-dmgr VM run `./stopServer.sh server1 -username wasadmin -password <pwd>`. 2. Attempt to load Admin Console. 3. Attempt to load Home page. 4. Run `ps -ef \| grep java \| grep server1`. 5. Run `./startServer.sh server1` to restore. |
| **Expected Result** | Step 2 and 3: both connections fail (connection refused/timeout) — nothing is listening on either port. Step 4: no matching Java process found for server1. Step 5: server restarts and both Admin Console and Home page become reachable again within 60-120 seconds. |
| **Actual Result** | |
| **Status** | |
| **Notes** | This confirms the critical distinction between application-level and server-level Stop. |

---

### TC-v4-04
| Field | Value |
|---|---|
| **ID** | TC-v4-04 |
| **Description** | Deliberate rollback to v3 code succeeds and is visibly verifiable |
| **Type** | Functional |
| **Priority** | Critical |
| **Steps** | 1. Confirm Home page footer shows "v4 — Application Lifecycle". 2. Perform Update on digistack-bank-v4, replacing content with digistack-bank-v3-rollback-copy.ear. 3. Stop and Start the application. 4. Reload Home page and read footer. |
| **Expected Result** | Footer now shows "v1 — Foundation" (the pre-v4 label baked into the rollback EAR) — NOT "v4 — Application Lifecycle". This is visible, unambiguous proof that older code is now running. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v4-05
| Field | Value |
|---|---|
| **ID** | TC-v4-05 |
| **Description** | Rollback does not affect database data |
| **Type** | Integration |
| **Priority** | Critical |
| **Steps** | 1. Record account balance from DB before rollback: `SELECT balance FROM accounts WHERE user_id = 1;` 2. Perform the rollback (TC-v4-04). 3. Query balance again immediately after rollback completes. |
| **Expected Result** | Balance recorded in step 3 is IDENTICAL to step 1 — down to the cent. Rollback is a code-only operation; it never touches the database. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v4-06
| Field | Value |
|---|---|
| **ID** | TC-v4-06 |
| **Description** | Application remains fully functional on rolled-back code |
| **Type** | Functional |
| **Priority** | Critical |
| **Steps** | 1. With v3 code running (post-rollback), log in as customer1. 2. Navigate to Account page. 3. Perform a deposit of ₹100. 4. Observe result. |
| **Expected Result** | Deposit succeeds normally with a green success banner — the rolled-back v3 code is functionally complete and correct, differing from v4 only in the cosmetic footer label. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v4-07
| Field | Value |
|---|---|
| **ID** | TC-v4-07 |
| **Description** | Forward redeploy after rollback restores v4 code correctly |
| **Type** | Functional |
| **Priority** | Critical |
| **Steps** | 1. With v3 code running (post-rollback), perform Update again with digistack-bank-v4.ear. 2. Stop and Start. 3. Reload Home page footer. 4. Verify balance in DB includes the TC-v4-06 test deposit. |
| **Expected Result** | Footer shows "v4 — Application Lifecycle" again. Balance in DB reflects the ₹100 deposit from TC-v4-06 — confirming the forward redeploy also did not touch the database, and the deposit performed on rolled-back code persisted correctly through the redeploy. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v4-08
| Field | Value |
|---|---|
| **ID** | TC-v4-08 |
| **Description** | wsadmin v4_rollback.py script performs rollback correctly |
| **Type** | Integration |
| **Priority** | High |
| **Steps** | 1. Confirm v4 code running (footer shows v4 label). 2. On dsb-dmgr VM run `v4_rollback.py` via wsadmin. 3. Observe script output. 4. Reload Home page footer. |
| **Expected Result** | Script output ends with "=== Rollback complete. ===". Footer shows "v1 — Foundation" after the script runs — confirming the scripted rollback path works identically to the GUI path. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v4-09
| Field | Value |
|---|---|
| **ID** | TC-v4-09 |
| **Description** | Uninstall + Install correctly changes the registered application name |
| **Type** | Functional |
| **Priority** | High |
| **Steps** | 1. On dsb-dmgr VM run `v4_deploy.py` via wsadmin (with digistack-bank-v3 currently registered). 2. Observe script output. 3. Check Admin Console application list. |
| **Expected Result** | Script confirms `digistack-bank-v3` uninstalled and `digistack-bank-v4` installed and started. Admin Console shows ONLY `digistack-bank-v4` in the list — `digistack-bank-v3` is completely absent, not just stopped. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v4-10
| Field | Value |
|---|---|
| **ID** | TC-v4-10 |
| **Description** | ClassLoader settings must be re-applied after a fresh Install (but not after Update) |
| **Type** | Functional |
| **Priority** | High |
| **Steps** | 1. Note ClassLoader settings before an Uninstall+Install cycle (should be PARENT_FIRST/SINGLE from prior configuration). 2. Perform Uninstall+Install via v4_deploy.py. 3. Check ClassLoader settings immediately after, WITHOUT re-running v4_set_classloader.py. 4. Re-run v4_set_classloader.py. 5. Check settings again. |
| **Expected Result** | Step 3: ClassLoader settings have reverted to WAS defaults (likely PARENT_FIRST but WAR class loader policy may show MULTIPLE or a different default — a fresh Install does not carry forward prior custom configuration). Step 5: after re-running the script, PARENT_FIRST and SINGLE are confirmed correctly set again. |
| **Actual Result** | |
| **Status** | |
| **Notes** | This confirms Update preserves custom config while Install (fresh) does not. |

---

### TC-v4-11
| Field | Value |
|---|---|
| **ID** | TC-v4-11 |
| **Description** | Update path (not fresh Install) preserves ClassLoader settings |
| **Type** | Functional |
| **Priority** | High |
| **Steps** | 1. With digistack-bank-v4 installed and ClassLoader set to PARENT_FIRST/SINGLE, perform an Update (not Uninstall+Install) — replace the EAR content only. 2. Check ClassLoader settings immediately after the Update, without re-running any ClassLoader script. |
| **Expected Result** | ClassLoader settings remain PARENT_FIRST/SINGLE after an Update — Update preserves the existing application's configuration (server mapping, virtual host, ClassLoader policy), unlike a fresh Install which resets to WAS defaults. |
| **Actual Result** | |
| **Status** | |
| **Notes** | This is the contrast case to TC-v4-10. |

---

### TC-v4-12
| Field | Value |
|---|---|
| **ID** | TC-v4-12 |
| **Description** | WSVR log messages confirm application lifecycle transitions |
| **Type** | Integration |
| **Priority** | High |
| **Steps** | 1. Perform a Stop then Start of digistack-bank-v4 via Admin Console. 2. On dsb-dmgr VM run: `grep -E "WSVR0221I\|WSVR0220I" SystemOut.log \| tail -4` |
| **Expected Result** | Log contains a WSVR0221I (stopped) line followed by a WSVR0220I (started) line, both referencing `digistack-bank-v4`, both timestamped consistent with when the Stop/Start actions were performed. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v4-13
| Field | Value |
|---|---|
| **ID** | TC-v4-13 |
| **Description** | installedApps directory reflects the currently deployed code |
| **Type** | Integration |
| **Priority** | Medium |
| **Steps** | 1. With v4 code running, on dsb-dmgr VM run: `grep "v4" /apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/installedApps/devdsbincell01/digistack-bank-v4.ear/digistack-bank-web-1.0.war/Home.jsp` |
| **Expected Result** | grep finds the line containing "v4 — Application Lifecycle" inside the physically expanded EAR directory on disk — confirming the installedApps directory is the actual source WAS serves requests from. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v4-14
| Field | Value |
|---|---|
| **ID** | TC-v4-14 |
| **Description** | Restart (chained stop && start) restores full functionality |
| **Type** | Functional |
| **Priority** | Medium |
| **Steps** | 1. On dsb-dmgr VM run: `./stopServer.sh server1 -username wasadmin -password <pwd> && ./startServer.sh server1` 2. Wait for completion. 3. Load Admin Console. 4. Load Home page. 5. Log in and confirm Dashboard loads. |
| **Expected Result** | Both stop and start complete without error in sequence. Admin Console and Home page both reachable after restart. Login and Dashboard function normally — no residual state issues from the restart. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v4-15
| Field | Value |
|---|---|
| **ID** | TC-v4-15 |
| **Description** | Home, Dashboard, Login, and Account pages all show consistent v4 footer label |
| **Type** | Functional |
| **Priority** | Low |
| **Steps** | 1. With v4 code running, visit Home page and check footer. 2. Log in, check Dashboard footer. 3. Navigate to Account page, check footer. 4. Log out, check Login page footer. |
| **Expected Result** | Home footer: "v4 — Application Lifecycle". Dashboard, Account, Login footers all show "v4" consistently. No page shows a stale v1/v2/v3 label. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v4-16
| Field | Value |
|---|---|
| **ID** | TC-v4-16 |
| **Description** | backupConfig baseline captured for v4 alongside v1, v2, v3 |
| **Type** | Integration |
| **Priority** | Low |
| **Steps** | 1. On dsb-dmgr VM run: `ls -lh /apps/backups/was-config/` |
| **Expected Result** | Four backup ZIP files present: `devdsbinappserver01_v1_backup.zip`, `_v2_`, `_v3_`, `_v4_` — none overwritten, all with non-zero sizes and increasing timestamps. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

## Defect Log

| Defect ID | TC Ref | Priority | Description | Status | Resolution |
|---|---|---|---|---|---|
| | | | | | |

---

## Test Summary

| Priority | Total | Pass | Fail | Blocked |
|---|---|---|---|---|
| Critical | 7 | | | |
| High | 5 | | | |
| Medium | 2 | | | |
| Low | 2 | | | |
| **v4 Subtotal** | **16** | | | |
| **v1 Regression** | **13** | | | |
| **v2 Regression** | **10** | | | |
| **v3 Regression** | **13** | | | |
| **Grand Total** | **52** | | | |

---

## Regression Pack — Forward Reference

The following v4 test cases are added to the Regression Pack.
Re-run all of these (plus v1, v2, v3 Regression Packs) at every
subsequent version sign-off:

| TC ID | Description | Priority |
|---|---|---|
| TC-v4-01 | Update Application preserves app name | Critical |
| TC-v4-02 | Application Stop does not affect server/Admin Console | Critical |
| TC-v4-03 | Server Stop takes down everything | Critical |
| TC-v4-09 | Uninstall+Install changes registered app name correctly | High |
| TC-v4-12 | WSVR log messages confirm lifecycle transitions | High |