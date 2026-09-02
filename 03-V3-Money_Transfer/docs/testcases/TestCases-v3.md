# TestCases-v3.md
# DigiStack Bank — P01 Version 3
# Feature: Basic Transaction (Deposit & Withdraw)
# Test Execution Date: ___________
# Executed By: ___________
# WAS Version Confirmed: 9.0.5.28
# PostgreSQL Version Confirmed: 16
# EAR Deployed: digistack-bank-v3.ear

---

## Sign-Off Gate (TCS01 §2.7)

| Gate | Condition | Met? |
|---|---|---|
| 1 | All Critical test cases: Pass | |
| 2 | All High test cases: Pass | |
| 3 | No open Critical or High defects | |
| 4 | Regression Pack (v1+v2 Critical + High) re-run and passing | |
| 5 | Reviewer name and approved date recorded | |
| 6 | SetupDoc-v3.md complete and followed | |
| 7 | backupConfig baseline captured | |
| 8 | Smoke test passes | |

Reviewer: _________________     Approved Date: ___________

---

## Regression Pack — v1 + v2 Re-Run

All v1 and v2 Critical and High cases must pass on the v3 build.
Execute these before any v3-specific cases.

### v1 Regression (Critical)
| TC ID | Description | Status |
|---|---|---|
| TC-v1-01 | Admin Console reachable and login succeeds | |
| TC-v1-02 | server1 shows Started | |
| TC-v1-03 | Application shows Started (now digistack-bank-v3) | |
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
| TC-v2-14 | users table has hashed passwords (hash_len=64) | |

Note on TC-v1-03: application name is now `digistack-bank-v3`.

---

## v3 Smoke Test

| Check | Expected | Status |
|---|---|---|
| Admin Console reachable | Loads at port 9060 | |
| server1 Started | Green arrow | |
| digistack-bank-v3 Started | Green arrow | |
| Home page loads | HTTP 200, Database: Connected green | |
| Login succeeds | Dashboard renders | |
| Dashboard account card | Shows SAVINGS + masked number | |
| Deposit works | Success banner, balance updated | |
| Withdraw works | Success banner, balance updated | |
| Logout works | Redirected to Home | |

---

## v3 Test Cases

---

### TC-v3-01
| Field | Value |
|---|---|
| **ID** | TC-v3-01 |
| **Description** | accounts table exists with correct seed data |
| **Type** | Integration |
| **Priority** | Critical |
| **Steps** | 1. On dsb-db VM run: `psql -U digistack_app -d digistack_bank -h 127.0.0.1 -c "SELECT id, user_id, account_number, account_type, balance, is_frozen FROM accounts ORDER BY id;"` |
| **Expected Result** | Two rows: id=1 user_id=1 account_number=DSB0000000001 account_type=SAVINGS balance=<current> is_frozen=f. id=2 user_id=2 account_number=DSB0000000002 account_type=SAVINGS balance=<current> is_frozen=f. No error from psql. |
| **Actual Result** | |
| **Status** | |
| **Notes** | Balance may differ from seed value (50000.00) due to transactions performed during testing. |

---

### TC-v3-02
| Field | Value |
|---|---|
| **ID** | TC-v3-02 |
| **Description** | Foreign key constraint enforced — account cannot reference non-existent user |
| **Type** | Integration |
| **Priority** | Critical |
| **Steps** | 1. On dsb-db VM run: `psql -U digistack_app -d digistack_bank -h 127.0.0.1 -c "INSERT INTO accounts (user_id, account_number, account_type, balance) VALUES (999, 'DSB9999999999', 'SAVINGS', 100.00);"` |
| **Expected Result** | PostgreSQL returns: ERROR: insert or update on table "accounts" violates foreign key constraint "fk_accounts_user_id". DETAIL: Key (user_id)=(999) is not present in table "users". |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v3-03
| Field | Value |
|---|---|
| **ID** | TC-v3-03 |
| **Description** | Check constraint enforced — balance cannot go below zero at DB level |
| **Type** | Integration |
| **Priority** | Critical |
| **Steps** | 1. On dsb-db VM run: `psql -U digistack_app -d digistack_bank -h 127.0.0.1 -c "UPDATE accounts SET balance = -1.00 WHERE id = 1;"` |
| **Expected Result** | PostgreSQL returns: ERROR: new row for relation "accounts" violates check constraint "chk_accounts_balance". |
| **Actual Result** | |
| **Status** | |
| **Notes** | This confirms the DB-level safety net is in place independent of application logic. |

---

### TC-v3-04
| Field | Value |
|---|---|
| **ID** | TC-v3-04 |
| **Description** | Successful deposit — balance increases correctly |
| **Type** | Functional |
| **Priority** | Critical |
| **Steps** | 1. Record current balance from DB: `SELECT balance FROM accounts WHERE user_id = 1;` 2. Log in as customer1. 3. Navigate to Account page. 4. Enter deposit amount 5000. 5. Click Deposit. 6. View balance on Account page. 7. Verify balance in DB. |
| **Expected Result** | Green success banner shown: "Deposit Successful — ₹5000 has been deposited into your account successfully." Balance on page = previous balance + 5000. DB balance matches page balance exactly. |
| **Actual Result** | |
| **Status** | |
| **Notes** | Record the before and after balance in Actual Result. |

---

### TC-v3-05
| Field | Value |
|---|---|
| **ID** | TC-v3-05 |
| **Description** | Successful withdrawal — balance decreases correctly |
| **Type** | Functional |
| **Priority** | Critical |
| **Steps** | 1. Record current balance from DB. 2. Log in as customer1. 3. Navigate to Account page. 4. Enter withdrawal amount 2000. 5. Click Withdraw. 6. View balance. 7. Verify balance in DB. |
| **Expected Result** | Green success banner shown: "Withdrawal Successful — ₹2000 has been withdrawn from your account successfully." Balance on page = previous balance − 2000. DB balance matches exactly. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v3-06
| Field | Value |
|---|---|
| **ID** | TC-v3-06 |
| **Description** | Overdraft rejected — withdrawal exceeding balance fails |
| **Type** | Negative |
| **Priority** | Critical |
| **Steps** | 1. Record current balance from DB. 2. Log in as customer1. 3. Navigate to Account page. 4. Enter withdrawal amount 9999999. 5. Click Withdraw. 6. Observe result. 7. Verify balance in DB unchanged. |
| **Expected Result** | Red error banner: "Transaction Failed — Insufficient funds. Your current balance is ₹X and you requested ₹9,99,99,999.00." (or similar). Balance in DB is unchanged — no partial withdrawal occurred. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v3-07
| Field | Value |
|---|---|
| **ID** | TC-v3-07 |
| **Description** | Deposit and withdrawal confirmed in SystemOut.log |
| **Type** | Integration |
| **Priority** | Critical |
| **Steps** | 1. Perform one deposit and one withdrawal as customer1. 2. On dsb-dmgr VM run: `grep "AccountServlet" /opt/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/logs/server1/SystemOut.log \| tail -10` |
| **Expected Result** | Log contains: `AccountServlet: Deposit successful. userId=1 amount=<amount> newBalance=<balance>` AND `AccountServlet: Withdraw successful. userId=1 amount=<amount> newBalance=<balance>`. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v3-08
| Field | Value |
|---|---|
| **ID** | TC-v3-08 |
| **Description** | Dashboard account card shows live account data |
| **Type** | Functional |
| **Priority** | Critical |
| **Steps** | 1. Log in as customer1. 2. Observe the account card on the Dashboard. |
| **Expected Result** | Account card shows: account type = "SAVINGS Account", masked number = "**** **** 0001", balance hidden behind ••••••, View Balance button enabled. Not hardcoded — reads from DB via DashboardServlet → AccountService → AccountDao. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v3-09
| Field | Value |
|---|---|
| **ID** | TC-v3-09 |
| **Description** | Dashboard View Balance reveals live balance via AJAX |
| **Type** | Functional |
| **Priority** | Critical |
| **Steps** | 1. Log in as customer1. 2. Note current DB balance: `SELECT balance FROM accounts WHERE user_id = 1;` 3. On Dashboard click View Balance. 4. Observe the revealed balance. |
| **Expected Result** | Balance reveals without page reload. Value matches current DB balance formatted as ₹X,XX,XXX.XX. No "Available at v3" placeholder — that was v2's placeholder. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v3-10
| Field | Value |
|---|---|
| **ID** | TC-v3-10 |
| **Description** | ClassLoader configured correctly — PARENT_FIRST, SINGLE |
| **Type** | Functional |
| **Priority** | High |
| **Steps** | 1. Log in to Admin Console. 2. Navigate to Applications → WebSphere enterprise applications → digistack-bank-v3 → Class loading and update detection. 3. Read the settings. |
| **Expected Result** | Class loader order: "Classes loaded with parent class loader first (PARENT_FIRST)". WAR class loader policy: "Single class loader for application". |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v3-11
| Field | Value |
|---|---|
| **ID** | TC-v3-11 |
| **Description** | PRG pattern — F5 refresh after deposit does not re-submit |
| **Type** | Functional |
| **Priority** | High |
| **Steps** | 1. Log in as customer1. 2. Navigate to Account page. 3. Deposit ₹1,000. 4. Observe success banner and URL. 5. Press F5 (refresh) on the success page. 6. Observe result. 7. Check DB balance. |
| **Expected Result** | After step 4: URL contains `?result=success&action=deposit&amount=1000`. After F5 (step 5-6): page reloads showing the same success banner (GET request replayed) — the browser does NOT show a "resubmit form?" dialog. Balance in DB is NOT double-deposited — still the same value as after the first deposit. |
| **Actual Result** | |
| **Status** | |
| **Notes** | This verifies the POST/Redirect/GET pattern is working correctly. |

---

### TC-v3-12
| Field | Value |
|---|---|
| **ID** | TC-v3-12 |
| **Description** | Zero amount deposit rejected |
| **Type** | Negative |
| **Priority** | High |
| **Steps** | 1. Log in as customer1. 2. Navigate to Account page. 3. Enter deposit amount 0. 4. Click Deposit. |
| **Expected Result** | Either: (a) HTML5 validation prevents submission ("Value must be greater than 0"), OR (b) Red error banner: "Amount must be greater than zero." Balance unchanged. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v3-13
| Field | Value |
|---|---|
| **ID** | TC-v3-13 |
| **Description** | Non-numeric amount rejected |
| **Type** | Negative |
| **Priority** | High |
| **Steps** | 1. Log in as customer1. 2. Open browser developer tools. 3. Temporarily change the amount input type from "number" to "text". 4. Enter "abc" in the deposit field. 5. Click Deposit. |
| **Expected Result** | Red error banner: "Invalid amount — please enter a valid number." Balance unchanged. No server exception or 500 error. |
| **Actual Result** | |
| **Status** | |
| **Notes** | Developer tools needed to bypass HTML5 number input validation. This tests the server-side validation in AccountService.parseAndValidateAmount(). |

---

### TC-v3-14
| Field | Value |
|---|---|
| **ID** | TC-v3-14 |
| **Description** | Quick amount buttons populate the form field correctly |
| **Type** | Functional |
| **Priority** | High |
| **Steps** | 1. Log in as customer1. 2. Navigate to Account page. 3. Click each quick amount button (₹500, ₹1,000, ₹5,000, ₹10,000) for Deposit. 4. Observe the amount field after each click. |
| **Expected Result** | Each click sets the amount field to the corresponding value (500, 1000, 5000, 10000). No page reload. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v3-15
| Field | Value |
|---|---|
| **ID** | TC-v3-15 |
| **Description** | Frozen account — deposit and withdraw disabled on Account page |
| **Type** | Negative |
| **Priority** | High |
| **Steps** | 1. On dsb-db VM run: `UPDATE accounts SET is_frozen = TRUE WHERE user_id = 1;` 2. Log in as customer1 (or refresh session). 3. Navigate to Account page. 4. Observe the Deposit and Withdraw buttons. 5. Attempt to submit the form anyway. 6. Restore: `UPDATE accounts SET is_frozen = FALSE WHERE user_id = 1;` |
| **Expected Result** | Deposit and Withdraw buttons are disabled (greyed out, not clickable). Amount input fields are disabled. Frozen chip shown on account card: "Account Frozen — Contact Support". Even if form is submitted manually (via dev tools), AccountService.deposit/withdraw throws IllegalStateException: "Your account is frozen." Red error banner shown. |
| **Actual Result** | |
| **Status** | |
| **Notes** | Restore is_frozen = FALSE after this test. |

---

### TC-v3-16
| Field | Value |
|---|---|
| **ID** | TC-v3-16 |
| **Description** | Frozen account — Dashboard shows frozen banner |
| **Type** | Functional |
| **Priority** | High |
| **Steps** | 1. Ensure is_frozen = TRUE for customer1 (from TC-v3-15 or set manually). 2. Log in as customer1. 3. Observe the Dashboard. 4. Restore is_frozen = FALSE after test. |
| **Expected Result** | Yellow frozen banner visible on Dashboard: "Your account is frozen — please contact support to restore access." View Balance button is disabled. After restoring is_frozen = FALSE and refreshing: banner disappears, View Balance button is active. |
| **Actual Result** | |
| **Status** | |
| **Notes** | Restore is_frozen = FALSE after this test. |

---

### TC-v3-17
| Field | Value |
|---|---|
| **ID** | TC-v3-17 |
| **Description** | Logging verification script confirms correct WAS log settings |
| **Type** | Functional |
| **Priority** | High |
| **Steps** | 1. On dsb-dmgr VM run the v3_verify_logging.py script via wsadmin. 2. Observe all output lines. |
| **Expected Result** | All checks show OK. No WARNING lines. Specifically: SystemOut rotation = 3 files / 50 MB, SystemErr rotation = 3 files / 50 MB, trace spec = *=info (both config and runtime), digistack-bank-v3 state = STARTED. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v3-18
| Field | Value |
|---|---|
| **ID** | TC-v3-18 |
| **Description** | Account page session guard — unauthenticated access redirects to Login |
| **Type** | Functional |
| **Priority** | High |
| **Steps** | 1. Ensure no active session (fresh browser or incognito). 2. Navigate directly to `http://192.168.10.10:9080/digistack-bank/Account`. |
| **Expected Result** | Browser redirected to `/Login`. Account page is not shown. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v3-19
| Field | Value |
|---|---|
| **ID** | TC-v3-19 |
| **Description** | BalanceJson endpoint returns correct JSON for authenticated user |
| **Type** | Integration |
| **Priority** | Medium |
| **Steps** | 1. Log in as customer1. 2. Navigate to `http://192.168.10.10:9080/digistack-bank/BalanceJson` directly in the browser. |
| **Expected Result** | Browser shows raw JSON: `{"balance":"₹XX,XXX.XX"}` where the value matches the current DB balance for customer1. Content-Type is application/json. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v3-20
| Field | Value |
|---|---|
| **ID** | TC-v3-20 |
| **Description** | BalanceJson returns null balance for unauthenticated request |
| **Type** | Negative |
| **Priority** | Medium |
| **Steps** | 1. Ensure no active session. 2. Navigate to `http://192.168.10.10:9080/digistack-bank/BalanceJson`. |
| **Expected Result** | Browser shows: `{"balance":null}`. No session created. No error page. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v3-21
| Field | Value |
|---|---|
| **ID** | TC-v3-21 |
| **Description** | Multiple deposits are cumulative — balance accumulates correctly |
| **Type** | Functional |
| **Priority** | Medium |
| **Steps** | 1. Record starting balance from DB. 2. Log in as customer1. 3. Deposit ₹1,000. 4. Deposit ₹2,000. 5. Deposit ₹3,000. 6. Check final balance in DB. |
| **Expected Result** | Final DB balance = starting balance + 1000 + 2000 + 3000. Three separate log lines in SystemOut.log. No rounding errors — balance is exact to 2 decimal places. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v3-22
| Field | Value |
|---|---|
| **ID** | TC-v3-22 |
| **Description** | Deposit and withdraw tiles on Dashboard are active links |
| **Type** | Functional |
| **Priority** | Medium |
| **Steps** | 1. Log in as customer1. 2. Observe the Quick Actions row on the Dashboard. 3. Click the Deposit tile. 4. Go back. 5. Click the Withdraw tile. |
| **Expected Result** | Both tiles are clickable links (not disabled). Both navigate to the Account page at `/digistack-bank/Account`. Neither tile shows "Coming soon" label. |
| **Actual Result** | |
| **Status** | |
| **Notes** | Transfer and Statement tiles remain disabled with "Coming soon" labels. |

---

### TC-v3-23
| Field | Value |
|---|---|
| **ID** | TC-v3-23 |
| **Description** | Account page breadcrumb and Back link work correctly |
| **Type** | Functional |
| **Priority** | Low |
| **Steps** | 1. Log in as customer1. 2. Navigate to Account page. 3. Click the "Dashboard" breadcrumb at the top. 4. Go back to Account page. 5. Click "Back to Dashboard" link. |
| **Expected Result** | Both the breadcrumb and the Back link navigate to `/Dashboard`. Dashboard loads with session intact. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v3-24
| Field | Value |
|---|---|
| **ID** | TC-v3-24 |
| **Description** | Balance formatted with Indian number system (₹ symbol, commas) |
| **Type** | Functional |
| **Priority** | Low |
| **Steps** | 1. Log in as customer1. 2. Navigate to Account page. 3. Click View Balance. 4. Observe the balance format. |
| **Expected Result** | Balance displays with ₹ prefix and Indian number formatting. Example: 50000.00 displays as "₹50,000.00". 100000.00 displays as "₹1,00,000.00" (Indian lakh formatting). Always shows exactly 2 decimal places. |
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
| Critical | 9 | | | |
| High | 9 | | | |
| Medium | 4 | | | |
| Low | 2 | | | |
| **v3 Subtotal** | **24** | | | |
| **v1 Regression** | **13** | | | |
| **v2 Regression** | **10** | | | |
| **Grand Total** | **47** | | | |

---

## Regression Pack — Forward Reference

The following v3 test cases are added to the Regression Pack.
Re-run all of these (plus v1 and v2 Regression Packs) at every
subsequent version sign-off:

| TC ID | Description | Priority |
|---|---|---|
| TC-v3-01 | accounts table seed data correct | Critical |
| TC-v3-02 | Foreign key constraint enforced | Critical |
| TC-v3-03 | Check constraint — balance cannot go negative | Critical |
| TC-v3-04 | Deposit — balance increases correctly | Critical |
| TC-v3-05 | Withdraw — balance decreases correctly | Critical |
| TC-v3-06 | Overdraft rejected | Critical |
| TC-v3-07 | Transactions confirmed in SystemOut.log | Critical |
| TC-v3-08 | Dashboard account card shows live data | Critical |
| TC-v3-09 | Dashboard View Balance via AJAX | Critical |
| TC-v3-10 | ClassLoader PARENT_FIRST + SINGLE confirmed | High |
| TC-v3-15 | Frozen account blocks deposit/withdraw | High |
| TC-v3-16 | Frozen banner on Dashboard | High |
| TC-v3-18 | Account page session guard | High |