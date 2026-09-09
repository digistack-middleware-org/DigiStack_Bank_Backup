# TestCases-v4.5.md
# DigiStack Bank — P01 Version 4.5
# Feature: Basic IHS Standalone Era
# Test Execution Date: ___________
# Executed By: ___________
# WAS Version Confirmed: 9.0.5.28
# IHS Version Confirmed: 9.0.5.28
# EAR Deployed: digistack-bank-v4.ear (unchanged from v4)

---

## Sign-Off Gate (TCS01 §2.7)

| Gate | Condition | Met? |
|---|---|---|
| 1 | All Critical test cases: Pass | |
| 2 | All High test cases: Pass | |
| 3 | No open Critical or High defects | |
| 4 | Regression Pack (v1+v2+v3+v4 Critical + High) re-run and passing | |
| 5 | Reviewer name and approved date recorded | |
| 6 | SetupDoc-v4.5.md complete and followed | |
| 7 | backupConfig baseline captured | |
| 8 | Smoke test passes | |

Reviewer: _________________     Approved Date: ___________

---

## Regression Pack — v1 + v2 + v3 + v4 Re-Run

### v1 Regression (Critical)
| TC ID | Description | Status |
|---|---|---|
| TC-v1-01 | Admin Console reachable and login succeeds | |
| TC-v1-02 | server1 shows Started | |
| TC-v1-03 | Application shows Started (digistack-bank-v4) | |
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

### v4 Regression (Critical)
| TC ID | Description | Status |
|---|---|---|
| TC-v4-01 | Update Application preserves app name | |
| TC-v4-02 | Application Stop does not affect server/Admin Console | |
| TC-v4-03 | Server Stop takes down everything | |

### v4 Regression (High)
| TC ID | Description | Status |
|---|---|---|
| TC-v4-09 | Uninstall+Install changes registered app name | |
| TC-v4-12 | WSVR log messages confirm lifecycle transitions | |

Note on regression: All v4.5 browser-based regression cases
must be executed via IHS port 80 (http://192.168.10.20/...) —
NOT via WAS direct port 9080. This confirms IHS is genuinely
in the path for all existing flows, not just the Home page.

---

## v4.5 Smoke Test

| Check | Expected | Status |
|---|---|---|
| IHS process running on dsb-ihs | `ps aux \| grep httpd` shows process | |
| IHS default page | `http://192.168.10.20` loads | |
| webserver1 in Admin Console | Listed under Servers → Web Servers | |
| plugin-cfg.xml on dsb-dmgr | File exists at config/cells/.../webserver1/ | |
| plugin-cfg.xml on dsb-ihs | File exists at Plugins/config/webserver1/ | |
| Home page via IHS | `http://192.168.10.20/digistack-bank/Home` loads | |
| Database Connected | Status bar shows green via IHS path | |
| Login via IHS | Login succeeds via IHS port 80 | |

---

## v4.5 Test Cases

---

### TC-v4.5-01
| Field | Value |
|---|---|
| **ID** | TC-v4.5-01 |
| **Description** | IHS 9.0.5.28 installed and running on dsb-ihs |
| **Type** | Integration |
| **Priority** | Critical |
| **Steps** | 1. On dsb-ihs VM run: `/apps/IBM/HTTPServer/bin/httpd -v` 2. Run: `ps aux \| grep httpd \| grep -v grep` |
| **Expected Result** | Version output shows `IBM_HTTP_Server/9.0.5.28`. At least one httpd process listed in ps output. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v4.5-02
| Field | Value |
|---|---|
| **ID** | TC-v4.5-02 |
| **Description** | IHS default page reachable on port 80 from Windows browser |
| **Type** | Smoke |
| **Priority** | Critical |
| **Steps** | 1. On Windows browser navigate to `http://192.168.10.20` (no path). 2. Observe the page. |
| **Expected Result** | IBM HTTP Server default page loads. "It works!" or IBM branded welcome page shown. No connection error. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v4.5-03
| Field | Value |
|---|---|
| **ID** | TC-v4.5-03 |
| **Description** | webserver1 Web Server Definition exists in WAS Admin Console |
| **Type** | Functional |
| **Priority** | Critical |
| **Steps** | 1. Log in to Admin Console. 2. Navigate to Servers → Server Types → Web Servers. 3. Observe the table. |
| **Expected Result** | `webserver1` is listed. Type shows IBM HTTP Server. No error. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v4.5-04
| Field | Value |
|---|---|
| **ID** | TC-v4.5-04 |
| **Description** | webserver1 definition has correct hostname and port |
| **Type** | Functional |
| **Priority** | Critical |
| **Steps** | 1. Admin Console → Servers → Web Servers → click `webserver1`. 2. Read the General Properties. |
| **Expected Result** | Host name: `192.168.10.20`. Web server port: `80`. Type: IBM HTTP Server. Web server config file: `/apps/IBM/HTTPServer/conf/httpd.conf`. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v4.5-05
| Field | Value |
|---|---|
| **ID** | TC-v4.5-05 |
| **Description** | plugin-cfg.xml exists on dsb-dmgr at the correct path |
| **Type** | Integration |
| **Priority** | Critical |
| **Steps** | 1. On dsb-dmgr VM run: `ls -lh /apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/config/cells/devdsbincell01/nodes/devdsbinnode01/servers/webserver1/plugin-cfg.xml` |
| **Expected Result** | File listed with non-zero size (typically 10-50 KB). No "No such file or directory" error. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v4.5-06
| Field | Value |
|---|---|
| **ID** | TC-v4.5-06 |
| **Description** | plugin-cfg.xml contains correct WAS server reference (port 9080) |
| **Type** | Integration |
| **Priority** | Critical |
| **Steps** | 1. On dsb-dmgr VM run: `grep "9080" /apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/config/cells/devdsbincell01/nodes/devdsbinnode01/servers/webserver1/plugin-cfg.xml` |
| **Expected Result** | At least one line referencing port 9080 — confirming the plugin routes to the WAS AppServer HTTP port. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v4.5-07
| Field | Value |
|---|---|
| **ID** | TC-v4.5-07 |
| **Description** | plugin-cfg.xml contains the /digistack-bank context root |
| **Type** | Integration |
| **Priority** | Critical |
| **Steps** | 1. On dsb-dmgr VM run: `grep "digistack-bank" /apps/.../servers/webserver1/plugin-cfg.xml` |
| **Expected Result** | At least one line referencing `/digistack-bank` — confirming requests for the DigiStack Bank application are routed to WAS. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v4.5-08
| Field | Value |
|---|---|
| **ID** | TC-v4.5-08 |
| **Description** | plugin-cfg.xml propagated to dsb-ihs |
| **Type** | Integration |
| **Priority** | Critical |
| **Steps** | 1. On dsb-ihs VM run: `ls -lh /apps/IBM/WebSphere/Plugins/config/webserver1/plugin-cfg.xml` 2. Run: `grep "9080" /apps/IBM/WebSphere/Plugins/config/webserver1/plugin-cfg.xml` |
| **Expected Result** | File exists on dsb-ihs with non-zero size. grep returns port 9080 reference — confirming the file on dsb-ihs is identical in content to the one on dsb-dmgr. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v4.5-09
| Field | Value |
|---|---|
| **ID** | TC-v4.5-09 |
| **Description** | httpd.conf contains WAS plugin LoadModule directive |
| **Type** | Integration |
| **Priority** | Critical |
| **Steps** | 1. On dsb-ihs VM run: `grep "LoadModule was_ap22_module" /apps/IBM/HTTPServer/conf/httpd.conf` |
| **Expected Result** | One line returned referencing `mod_was_ap22_http.so` at the correct plugin installation path. No error. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v4.5-10
| Field | Value |
|---|---|
| **ID** | TC-v4.5-10 |
| **Description** | httpd.conf contains WebSpherePluginConfig directive |
| **Type** | Integration |
| **Priority** | Critical |
| **Steps** | 1. On dsb-ihs VM run: `grep "WebSpherePluginConfig" /apps/IBM/HTTPServer/conf/httpd.conf` |
| **Expected Result** | One line returned referencing the plugin-cfg.xml path at `/apps/IBM/WebSphere/Plugins/config/webserver1/plugin-cfg.xml`. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v4.5-11
| Field | Value |
|---|---|
| **ID** | TC-v4.5-11 |
| **Description** | Home page loads via IHS port 80 — full Browser→IHS→WAS→DB path |
| **Type** | Functional |
| **Priority** | Critical |
| **Steps** | 1. On Windows browser navigate to `http://192.168.10.20/digistack-bank/Home`. 2. Observe the page and footer. 3. Observe the status bar. |
| **Expected Result** | DigiStack Bank Home page renders fully. Footer shows `v4 — Application Lifecycle`. Status bar shows `Database: Connected` in green. No HTTP error. Page loads via port 80 — not port 9080. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v4.5-12
| Field | Value |
|---|---|
| **ID** | TC-v4.5-12 |
| **Description** | IHS access_log records requests for the Home page |
| **Type** | Integration |
| **Priority** | Critical |
| **Steps** | 1. Navigate to `http://192.168.10.20/digistack-bank/Home`. 2. On dsb-ihs VM run: `tail -5 /apps/IBM/HTTPServer/logs/access_log` |
| **Expected Result** | access_log contains a line with the Windows browser IP, "GET /digistack-bank/Home HTTP/1.1", and HTTP status 200. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v4.5-13
| Field | Value |
|---|---|
| **ID** | TC-v4.5-13 |
| **Description** | WAS SystemOut.log records HomeServlet execution after IHS request |
| **Type** | Integration |
| **Priority** | Critical |
| **Steps** | 1. Navigate to `http://192.168.10.20/digistack-bank/Home`. 2. On dsb-dmgr VM run: `grep "HomeServlet" .../SystemOut.log \| tail -3` |
| **Expected Result** | Log contains `HomeServlet: DB read successful. bank.name=DigiStack Bank` timestamped after the IHS request was made. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v4.5-14
| Field | Value |
|---|---|
| **ID** | TC-v4.5-14 |
| **Description** | Stopping IHS causes connection error on port 80 — proves IHS is the real front door |
| **Type** | Functional |
| **Priority** | Critical |
| **Steps** | 1. Confirm Home page loads via `http://192.168.10.20/digistack-bank/Home`. 2. On dsb-ihs VM run: `apachectl stop`. 3. Navigate to `http://192.168.10.20/digistack-bank/Home`. 4. Navigate to `http://192.168.10.10:9080/digistack-bank/Home`. 5. Restart IHS: `apachectl start`. 6. Confirm `http://192.168.10.20/digistack-bank/Home` loads again. |
| **Expected Result** | Step 3: connection refused/error — nothing listening on port 80. Step 4: Home page loads normally — WAS unaffected by IHS stop. Step 6: Home page loads via IHS again after restart. |
| **Actual Result** | |
| **Status** | |
| **Notes** | Always restart IHS at step 5 before proceeding to next test. |

---

### TC-v4.5-15
| Field | Value |
|---|---|
| **ID** | TC-v4.5-15 |
| **Description** | Full login→Dashboard→Deposit→Logout flow works via IHS |
| **Type** | Functional |
| **Priority** | High |
| **Steps** | 1. Navigate to `http://192.168.10.20/digistack-bank/Login`. 2. Log in as customer1 / Customer@123. 3. Confirm Dashboard loads. 4. Navigate to Account page, deposit ₹100. 5. Confirm success banner. 6. Click Logout. 7. Confirm redirect to Home page via IHS. |
| **Expected Result** | All steps succeed via IHS port 80. Session cookie (`JSESSIONID`) handled transparently through the reverse proxy. Balance updated in DB. Logout redirects to `http://192.168.10.20/digistack-bank/Home`. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v4.5-16
| Field | Value |
|---|---|
| **ID** | TC-v4.5-16 |
| **Description** | AJAX balance reveal works via IHS |
| **Type** | Functional |
| **Priority** | High |
| **Steps** | 1. Log in via IHS. 2. On Dashboard click View Balance. 3. Observe result. |
| **Expected Result** | Balance reveals without page reload — the AJAX call to `/BalanceJson` routes correctly through IHS plugin to WAS. Balance matches current DB value. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v4.5-17
| Field | Value |
|---|---|
| **ID** | TC-v4.5-17 |
| **Description** | Plugin log file exists and shows routing activity |
| **Type** | Integration |
| **Priority** | High |
| **Steps** | 1. On dsb-ihs VM run: `ls -lh /apps/IBM/WebSphere/Plugins/logs/webserver1/http_plugin.log` 2. Run: `tail -20 /apps/IBM/WebSphere/Plugins/logs/webserver1/http_plugin.log` |
| **Expected Result** | File exists with non-zero size. Log contains entries referencing plugin initialisation and digistack-bank routing. No ERROR lines. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v4.5-18
| Field | Value |
|---|---|
| **ID** | TC-v4.5-18 |
| **Description** | v4_5_verify_flow.py script confirms all WAS-side checks pass |
| **Type** | Integration |
| **Priority** | High |
| **Steps** | 1. On dsb-dmgr VM run `v4_5_verify_flow.py` via wsadmin. 2. Observe output for each numbered check. |
| **Expected Result** | Checks [1] through [4] all show OK. Check [5] shows OK or acceptable INFO (admin daemon). Check [6] shows INFO (use grep separately). No FAIL lines. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v4.5-19
| Field | Value |
|---|---|
| **ID** | TC-v4.5-19 |
| **Description** | IHS configured to start automatically on boot |
| **Type** | Functional |
| **Priority** | High |
| **Steps** | 1. On dsb-ihs VM run: `systemctl is-enabled ihs` |
| **Expected Result** | Output shows `enabled` — IHS will start automatically when the dsb-ihs VM is rebooted. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v4.5-20
| Field | Value |
|---|---|
| **ID** | TC-v4.5-20 |
| **Description** | Direct WAS access (port 9080) and IHS access (port 80) produce identical pages |
| **Type** | Functional |
| **Priority** | Medium |
| **Steps** | 1. Open two browser tabs. 2. Tab 1: `http://192.168.10.20/digistack-bank/Home` (IHS). 3. Tab 2: `http://192.168.10.10:9080/digistack-bank/Home` (WAS direct). 4. Compare both pages. |
| **Expected Result** | Both tabs show identical page content, footer, status bar. IHS is acting as a transparent reverse proxy — no visible difference to the user. |
| **Actual Result** | |
| **Status** | |
| **Notes** | Port 9080 direct access will be restricted at v8. Both paths work at v4.5 by design for comparison purposes. |

---

### TC-v4.5-21
| Field | Value |
|---|---|
| **ID** | TC-v4.5-21 |
| **Description** | httpd.conf syntax is valid — configtest passes |
| **Type** | Integration |
| **Priority** | Medium |
| **Steps** | 1. On dsb-ihs VM run: `/apps/IBM/HTTPServer/bin/apachectl configtest` |
| **Expected Result** | Output shows exactly: `Syntax OK` — no error, no warning. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v4.5-22
| Field | Value |
|---|---|
| **ID** | TC-v4.5-22 |
| **Description** | dsb-ihs VM network — correct static IP and reachable from dsb-dmgr |
| **Type** | Integration |
| **Priority** | Medium |
| **Steps** | 1. On dsb-ihs VM run: `ip addr show \| grep "192.168.10.20"` 2. On dsb-dmgr VM run: `ping -c 3 192.168.10.20` |
| **Expected Result** | Step 1: IP `192.168.10.20` shown. Step 2: `3 packets transmitted, 3 received, 0% packet loss`. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v4.5-23
| Field | Value |
|---|---|
| **ID** | TC-v4.5-23 |
| **Description** | IHS error_log shows no ERROR or FAILED lines after restart |
| **Type** | Functional |
| **Priority** | Medium |
| **Steps** | 1. On dsb-ihs VM run: `grep -i "ERROR\|FAILED" /apps/IBM/HTTPServer/logs/error_log \| tail -10` |
| **Expected Result** | No ERROR or FAILED lines related to the WAS plugin or IHS configuration. IHS startup messages (notice level) are acceptable. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v4.5-24
| Field | Value |
|---|---|
| **ID** | TC-v4.5-24 |
| **Description** | Technical debt acknowledged — plugin-cfg.xml points at standalone AppServer only |
| **Type** | Functional |
| **Priority** | Low |
| **Steps** | 1. Review `docs/technical-debt/v4_5_technical_debt.md`. 2. Confirm all five technical debt items are listed. |
| **Expected Result** | File exists. All five items present: standalone-only plugin config (v8), HTTP only (v11), no custom error pages (v8), no static assets at IHS (v8), port 9080 not firewalled (v8). |
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
| Critical | 14 | | | |
| High | 5 | | | |
| Medium | 4 | | | |
| Low | 1 | | | |
| **v4.5 Subtotal** | **24** | | | |
| **v1 Regression** | **13** | | | |
| **v2 Regression** | **10** | | | |
| **v3 Regression** | **13** | | | |
| **v4 Regression** | **8** | | | |
| **Grand Total** | **68** | | | |

---

## Regression Pack — Forward Reference

The following v4.5 test cases are added to the Regression Pack.
Re-run all of these (plus v1, v2, v3, v4 Regression Packs) at
every subsequent version sign-off:

| TC ID | Description | Priority |
|---|---|---|
| TC-v4.5-01 | IHS 9.0.5.28 installed and running | Critical |
| TC-v4.5-03 | webserver1 Web Server Definition exists | Critical |
| TC-v4.5-05 | plugin-cfg.xml exists on dsb-dmgr | Critical |
| TC-v4.5-08 | plugin-cfg.xml propagated to dsb-ihs | Critical |
| TC-v4.5-09 | httpd.conf LoadModule directive present | Critical |
| TC-v4.5-10 | httpd.conf WebSpherePluginConfig directive present | Critical |
| TC-v4.5-11 | Home page loads via IHS port 80 | Critical |
| TC-v4.5-12 | IHS access_log records requests | Critical |
| TC-v4.5-14 | IHS stop causes port 80 error — IHS is real front door | Critical |