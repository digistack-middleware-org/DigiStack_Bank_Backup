# TestCases-v1.md
# DigiStack Bank — P01 Version 1
# Feature: Project Setup & Enterprise Architecture
# Test Execution Date: ___________
# Executed By: ___________
# WAS Version Confirmed: 9.0.5.28
# PostgreSQL Version Confirmed: 16

---

## Sign-Off Gate (TCS01 §2.7)

| Gate | Condition | Met? |
|---|---|---|
| 1 | All Critical test cases: Pass | |
| 2 | All High test cases: Pass | |
| 3 | No open Critical or High defects | |
| 4 | Regression Pack re-run and passing | |
| 5 | Reviewer name and approved date recorded | |
| 6 | SetupDoc-v1.md complete and followed | |
| 7 | backupConfig baseline captured | |
| 8 | Smoke test passes | |

Reviewer: _________________     Approved Date: ___________

---

## Regression Pack — v1

This is the first version. No prior regression cases exist.
The Critical and High cases below become the v1 Regression Pack
and must be re-run at every subsequent version sign-off.

---

## Test Cases

---

### TC-v1-01
| Field | Value |
|---|---|
| **ID** | TC-v1-01 |
| **Description** | Admin Console is reachable and login succeeds |
| **Type** | Smoke |
| **Priority** | Critical |
| **Steps** | 1. Open browser. 2. Navigate to `http://192.168.10.10:9060/ibm/console`. 3. Enter User ID `wasadmin` and correct password. 4. Click Log in. |
| **Expected Result** | Admin Console Welcome page loads. Left navigation panel visible with sections: Applications, Resources, Security, Servers. No error message shown. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v1-02
| Field | Value |
|---|---|
| **ID** | TC-v1-02 |
| **Description** | server1 shows Started status in Admin Console |
| **Type** | Smoke |
| **Priority** | Critical |
| **Steps** | 1. Log in to Admin Console. 2. Navigate to Servers → Server Types → WebSphere application servers. 3. Observe the Status column for server1. |
| **Expected Result** | server1 is listed. Status column shows a green arrow labelled Started. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v1-03
| Field | Value |
|---|---|
| **ID** | TC-v1-03 |
| **Description** | digistack-bank-v1 application shows Started in Admin Console |
| **Type** | Smoke |
| **Priority** | Critical |
| **Steps** | 1. Log in to Admin Console. 2. Navigate to Applications → Application Types → WebSphere enterprise applications. 3. Locate digistack-bank-v1 in the list. 4. Observe the Status column. |
| **Expected Result** | digistack-bank-v1 is listed. Status column shows a green arrow. Application name matches exactly: `digistack-bank-v1`. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v1-04
| Field | Value |
|---|---|
| **ID** | TC-v1-04 |
| **Description** | Home page loads at full URL path |
| **Type** | Functional |
| **Priority** | Critical |
| **Steps** | 1. Open browser. 2. Navigate to `http://192.168.10.10:9080/digistack-bank/Home`. 3. Observe the page. |
| **Expected Result** | DigiStack Bank Home page renders. Navy/gold design visible. Hero section visible with title "Banking Built for Your Future". No HTTP error code (404, 500, 503) shown. Page fully loads within 5 seconds. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v1-05
| Field | Value |
|---|---|
| **ID** | TC-v1-05 |
| **Description** | Context root redirect works — bare URL loads Home page |
| **Type** | Functional |
| **Priority** | Critical |
| **Steps** | 1. Open browser. 2. Navigate to `http://192.168.10.10:9080/digistack-bank/`. 3. Observe the page. |
| **Expected Result** | Same Home page loads as TC-v1-04. No redirect error. The welcome-file configuration in web.xml is correctly serving Home.jsp. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v1-06
| Field | Value |
|---|---|
| **ID** | TC-v1-06 |
| **Description** | Live database read — system status shows from app_config table |
| **Type** | Integration |
| **Priority** | Critical |
| **Steps** | 1. Navigate to `http://192.168.10.10:9080/digistack-bank/Home`. 2. Locate the status bar at the bottom of the hero section. 3. Read the System Status value. |
| **Expected Result** | Status bar shows: `System Status: All Systems Operational`. This value is read live from the `app_config` table in PostgreSQL — it is not hardcoded in the JSP. `Database: Connected` is shown in green. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v1-07
| Field | Value |
|---|---|
| **ID** | TC-v1-07 |
| **Description** | HomeServlet DB read confirmed in WAS SystemOut.log |
| **Type** | Integration |
| **Priority** | Critical |
| **Steps** | 1. On dsb-dmgr VM run: `grep "HomeServlet" /opt/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/logs/server1/SystemOut.log` 2. Load the Home page once if no log line appears yet, then re-run grep. 3. Observe the log output. |
| **Expected Result** | Log contains the line: `HomeServlet: DB read successful. bank.name=DigiStack Bank`. Log contains: `HomeServlet: PostgreSQL JDBC driver loaded successfully.`. No `HomeServlet: DB read FAILED` line present. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v1-08
| Field | Value |
|---|---|
| **ID** | TC-v1-08 |
| **Description** | PostgreSQL 16 running and app_config table contains seed data |
| **Type** | Integration |
| **Priority** | Critical |
| **Steps** | 1. On dsb-db VM run: `psql -U digistack_app -d digistack_bank -h 127.0.0.1 -c "SELECT config_key, config_value FROM app_config ORDER BY id;"` 2. Enter password when prompted. 3. Observe output. |
| **Expected Result** | Query returns exactly 2 rows: `bank.name = DigiStack Bank` and `system.status = All Systems Operational`. No error from psql. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v1-09
| Field | Value |
|---|---|
| **ID** | TC-v1-09 |
| **Description** | WAS profile uses correct naming standard |
| **Type** | Functional |
| **Priority** | High |
| **Steps** | 1. On dsb-dmgr VM run: `/opt/IBM/WebSphere/AppServer/bin/manageprofiles.sh -listProfiles` 2. In Admin Console navigate to System Administration → Cell to read cell name. 3. Navigate to System Administration → Nodes to read node name. |
| **Expected Result** | Profile name: `devdsbinappserver01`. Cell name: `devdsbincell01`. Node name: `devdsbinnode01`. Server name: `server1`. All match STD v1.10 naming convention exactly. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v1-10
| Field | Value |
|---|---|
| **ID** | TC-v1-10 |
| **Description** | WAS logging configured — SystemOut.log rotation is 50 MB / 3 files |
| **Type** | Functional |
| **Priority** | High |
| **Steps** | 1. Log in to Admin Console. 2. Navigate to Troubleshooting → Logs and Trace → server1 → JVM Logs. 3. Read the Maximum File Size and Maximum Number of Historical Files values for System.out and System.err. |
| **Expected Result** | System.out: Maximum File Size = 50, Maximum Number of Historical Files = 3. System.err: Maximum File Size = 50, Maximum Number of Historical Files = 3. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v1-11
| Field | Value |
|---|---|
| **ID** | TC-v1-11 |
| **Description** | Diagnostic trace level is set to *=info |
| **Type** | Functional |
| **Priority** | High |
| **Steps** | 1. Log in to Admin Console. 2. Navigate to Troubleshooting → Logs and Trace → server1 → Diagnostic Trace. 3. Click the Runtime tab. 4. Read the Trace Specification field. |
| **Expected Result** | Trace Specification field shows exactly: `*=info` |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v1-12
| Field | Value |
|---|---|
| **ID** | TC-v1-12 |
| **Description** | PostgreSQL JDBC driver is present in WAS lib/ext/jdbc/ |
| **Type** | Integration |
| **Priority** | High |
| **Steps** | 1. On dsb-dmgr VM run: `ls -lh /opt/IBM/WebSphere/AppServer/lib/ext/jdbc/postgresql-42.7.3.jar` |
| **Expected Result** | File is listed. Size is approximately 1 MB. No "No such file or directory" error. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v1-13
| Field | Value |
|---|---|
| **ID** | TC-v1-13 |
| **Description** | EAR module is correctly mapped to server1 and default_host |
| **Type** | Functional |
| **Priority** | High |
| **Steps** | 1. Log in to Admin Console. 2. Navigate to Applications → Application Types → WebSphere enterprise applications. 3. Click digistack-bank-v1. 4. Click Manage Modules. 5. Observe the Server column. 6. Go back and click Map virtual hosts for Web modules. 7. Observe the Virtual Host column. |
| **Expected Result** | Manage Modules shows WAR mapped to `server1`. Virtual host shows `default_host`. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v1-14
| Field | Value |
|---|---|
| **ID** | TC-v1-14 |
| **Description** | Non-existent URL returns 404 (not a server crash) |
| **Type** | Negative |
| **Priority** | Medium |
| **Steps** | 1. Open browser. 2. Navigate to `http://192.168.10.10:9080/digistack-bank/nonexistent`. 3. Observe the response. |
| **Expected Result** | Browser receives an HTTP 404 response. Server does not crash. Admin Console still shows server1 as Started after this request. |
| **Actual Result** | |
| **Status** | |
| **Notes** | Custom 404 page is not configured until v8 (IHS). Default WAS 404 response is acceptable at v1. |

---

### TC-v1-15
| Field | Value |
|---|---|
| **ID** | TC-v1-15 |
| **Description** | Home page renders correctly when DB is unreachable (graceful fallback) |
| **Type** | Negative |
| **Priority** | Medium |
| **Steps** | 1. On dsb-db VM temporarily stop PostgreSQL: `systemctl stop postgresql-16`. 2. On browser navigate to `http://192.168.10.10:9080/digistack-bank/Home`. 3. Observe the page. 4. Restart PostgreSQL after test: `systemctl start postgresql-16`. |
| **Expected Result** | Home page still loads (no 500 error crash). Status bar shows `Database: Error` in red. `System Status: Status Unavailable` shown. WAS server remains up. HomeServlet's catch block handled the SQLException gracefully. |
| **Actual Result** | |
| **Status** | |
| **Notes** | Confirm PostgreSQL is restarted after this test before proceeding. |

---

### TC-v1-16
| Field | Value |
|---|---|
| **ID** | TC-v1-16 |
| **Description** | Page title reflects live bank name from database |
| **Type** | Functional |
| **Priority** | Low |
| **Steps** | 1. Navigate to `http://192.168.10.10:9080/digistack-bank/Home`. 2. Read the browser tab title. |
| **Expected Result** | Browser tab shows: `DigiStack Bank — Your Trusted Banking Partner`. The `DigiStack Bank` portion is the live value from `app_config.bank_name`, rendered via `${bankName}` EL expression in Home.jsp. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

## Defect Log

| Defect ID | TC Ref | Priority | Description | Status | Resolution |
|---|---|---|---|---|---|
| | | | | | |

*Add rows here as defects are found during test execution.*

---

## Test Summary

| Priority | Total | Pass | Fail | Blocked |
|---|---|---|---|---|
| Critical | 8 | | | |
| High | 5 | | | |
| Medium | 2 | | | |
| Low | 1 | | | |
| **Total** | **16** | | | |

---

## Regression Pack — Forward Reference

The following test cases from this version form the v1 Regression Pack.
Re-run all of these at every subsequent version sign-off:

| TC ID | Description | Priority |
|---|---|---|
| TC-v1-01 | Admin Console reachable and login succeeds | Critical |
| TC-v1-02 | server1 shows Started | Critical |
| TC-v1-03 | digistack-bank-v1 application shows Started | Critical |
| TC-v1-04 | Home page loads at full URL | Critical |
| TC-v1-05 | Context root redirect works | Critical |
| TC-v1-06 | Live DB read — system status from app_config | Critical |
| TC-v1-07 | HomeServlet DB read confirmed in SystemOut.log | Critical |
| TC-v1-08 | PostgreSQL running and seed data present | Critical |