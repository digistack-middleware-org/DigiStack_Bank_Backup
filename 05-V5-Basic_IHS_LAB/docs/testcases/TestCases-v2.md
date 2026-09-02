# TestCases-v2.md
# DigiStack Bank — P01 Version 2
# Feature: Login & Session
# Test Execution Date: ___________
# Executed By: ___________
# WAS Version Confirmed: 9.0.5.28
# PostgreSQL Version Confirmed: 16
# EAR Deployed: digistack-bank-v2.ear

---

## Sign-Off Gate (TCS01 §2.7)

| Gate | Condition | Met? |
|---|---|---|
| 1 | All Critical test cases: Pass | |
| 2 | All High test cases: Pass | |
| 3 | No open Critical or High defects | |
| 4 | Regression Pack (v1 Critical + High) re-run and passing | |
| 5 | Reviewer name and approved date recorded | |
| 6 | SetupDoc-v2.md complete and followed | |
| 7 | backupConfig baseline captured | |
| 8 | Smoke test passes | |

Reviewer: _________________     Approved Date: ___________

---

## Regression Pack — v1 Re-Run

All v1 Critical and High cases must pass on the v2 build before
sign-off is granted. Execute these first, before any v2-specific cases.

| TC ID | Description | Priority | Status |
|---|---|---|---|
| TC-v1-01 | Admin Console reachable and login succeeds | Critical | |
| TC-v1-02 | server1 shows Started | Critical | |
| TC-v1-03 | Application shows Started (now digistack-bank-v2) | Critical | |
| TC-v1-04 | Home page loads at full URL | Critical | |
| TC-v1-05 | Context root redirect works | Critical | |
| TC-v1-06 | Live DB read — system status from app_config | Critical | |
| TC-v1-07 | HomeServlet DB read confirmed in SystemOut.log | Critical | |
| TC-v1-08 | PostgreSQL running and seed data present | Critical | |
| TC-v1-09 | WAS profile uses correct naming standard | High | |
| TC-v1-10 | WAS logging configured — 50 MB / 3 files | High | |
| TC-v1-11 | Diagnostic trace level is *=info | High | |
| TC-v1-12 | PostgreSQL JDBC driver present in lib/ext/jdbc/ | High | |
| TC-v1-13 | EAR module mapped to server1 and default_host | High | |

Note on TC-v1-03: the application name is now `digistack-bank-v2`
not `digistack-bank-v1`. Confirm the new name shows Started.

---

## v2 Smoke Test

| Check | Expected | Status |
|---|---|---|
| Admin Console reachable | Loads at port 9060 | |
| server1 Started | Green arrow in Admin Console | |
| digistack-bank-v2 Started | Green arrow in Admin Console | |
| Home page loads | HTTP 200 at /digistack-bank/Home | |
| Login page loads | HTTP 200 at /digistack-bank/Login | |
| Login succeeds | Dashboard renders after correct credentials | |
| Logout works | Redirected to Home, session destroyed | |

---

## v2 Test Cases

---

### TC-v2-01
| Field | Value |
|---|---|
| **ID** | TC-v2-01 |
| **Description** | Login page loads correctly at /Login |
| **Type** | Smoke |
| **Priority** | Critical |
| **Steps** | 1. Open browser. 2. Navigate to `http://192.168.10.10:9080/digistack-bank/Login`. 3. Observe the page. |
| **Expected Result** | Login page renders with DigiStack Bank navbar, "Welcome Back" card, Username and Password fields, Sign In button. No error message shown. No HTTP error code. Page loads within 3 seconds. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v2-02
| Field | Value |
|---|---|
| **ID** | TC-v2-02 |
| **Description** | Successful login with customer credentials creates session and redirects to Dashboard |
| **Type** | Functional |
| **Priority** | Critical |
| **Steps** | 1. Navigate to `/Login`. 2. Enter Username: `customer1`, Password: `Customer@123`. 3. Click Sign In. 4. Observe the result. |
| **Expected Result** | Browser redirects to `/Dashboard`. Dashboard renders with greeting ("Good Morning/Afternoon/Evening, Ravi Kumar!"), last login shown, role shown as Customer, email shown as `ravi.kumar@digistack.cloud`. No error shown. HTTP 200 on Dashboard. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v2-03
| Field | Value |
|---|---|
| **ID** | TC-v2-03 |
| **Description** | Successful login with administrator credentials creates session and redirects to Dashboard |
| **Type** | Functional |
| **Priority** | Critical |
| **Steps** | 1. Navigate to `/Login`. 2. Enter Username: `admin1`, Password: `Admin@123`. 3. Click Sign In. 4. Observe the result. |
| **Expected Result** | Browser redirects to `/Dashboard`. Dashboard renders with greeting ("Good Morning/Afternoon/Evening, Admin User!"). Navbar shows `admin1` with `[Admin]` label in gold. Role panel shows Administrator with gold shield icon. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v2-04
| Field | Value |
|---|---|
| **ID** | TC-v2-04 |
| **Description** | Session guard — unauthenticated access to Dashboard redirects to Login |
| **Type** | Functional |
| **Priority** | Critical |
| **Steps** | 1. Ensure no active session (open fresh browser or use private/incognito window). 2. Navigate directly to `http://192.168.10.10:9080/digistack-bank/Dashboard`. 3. Observe the result. |
| **Expected Result** | Browser is redirected to `/Login`. Dashboard is NOT shown. No session is created by this redirect. Login page renders normally. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v2-05
| Field | Value |
|---|---|
| **ID** | TC-v2-05 |
| **Description** | Logout invalidates session — Dashboard inaccessible after logout |
| **Type** | Functional |
| **Priority** | Critical |
| **Steps** | 1. Log in as `customer1`. 2. Confirm Dashboard loads. 3. Click Logout. 4. Confirm redirect to Home page. 5. Navigate directly to `/Dashboard`. 6. Observe the result. |
| **Expected Result** | Step 4: redirected to `/Home`. Step 5-6: redirected to `/Login` — Dashboard is not accessible. Session is fully destroyed. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v2-06
| Field | Value |
|---|---|
| **ID** | TC-v2-06 |
| **Description** | Wrong password rejected with generic error message |
| **Type** | Negative |
| **Priority** | Critical |
| **Steps** | 1. Navigate to `/Login`. 2. Enter Username: `customer1`, Password: `wrongpassword`. 3. Click Sign In. 4. Observe the result. |
| **Expected Result** | Login page reloads with red error box showing: "Invalid username or password. Please try again." URL stays at `/Login`. No session created. No redirect to Dashboard. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v2-07
| Field | Value |
|---|---|
| **ID** | TC-v2-07 |
| **Description** | Non-existent username rejected with generic error message |
| **Type** | Negative |
| **Priority** | Critical |
| **Steps** | 1. Navigate to `/Login`. 2. Enter Username: `doesnotexist`, Password: `anypassword`. 3. Click Sign In. 4. Observe the result. |
| **Expected Result** | Login page reloads with red error box showing: "Invalid username or password. Please try again." The error message does NOT say "username not found" — it is the same generic message as a wrong password, preventing username enumeration. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v2-08
| Field | Value |
|---|---|
| **ID** | TC-v2-08 |
| **Description** | Empty username and password fields rejected |
| **Type** | Negative |
| **Priority** | High |
| **Steps** | 1. Navigate to `/Login`. 2. Leave both fields empty. 3. Click Sign In. 4. Observe the result. |
| **Expected Result** | Either: (a) browser HTML5 validation prevents form submission and shows "Please fill in this field" on the username input, OR (b) LoginServlet returns "Username and password are required." error. In neither case is a session created or a redirect to Dashboard issued. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v2-09
| Field | Value |
|---|---|
| **ID** | TC-v2-09 |
| **Description** | Last login timestamp — first login shows "First login" |
| **Type** | Functional |
| **Priority** | High |
| **Steps** | 1. On dsb-db VM run: `UPDATE users SET last_login = NULL WHERE username = 'customer1';` to reset the timestamp. 2. Log in as `customer1`. 3. Observe the last login bar on the Dashboard. |
| **Expected Result** | Last login bar shows: `Last login: First login`. Confirms the null-check in LoginServlet is working correctly. |
| **Actual Result** | |
| **Status** | |
| **Notes** | Reset last_login to NULL before this test. After this test the next login will show a real timestamp. |

---

### TC-v2-10
| Field | Value |
|---|---|
| **ID** | TC-v2-10 |
| **Description** | Last login timestamp — second login shows real timestamp from first login |
| **Type** | Functional |
| **Priority** | High |
| **Steps** | 1. Log in as `customer1` (first login — shows "First login"). 2. Logout. 3. Log in again as `customer1`. 4. Observe the last login bar. |
| **Expected Result** | Last login bar shows a real formatted timestamp (e.g. "29 Aug 2026, 10:45 AM") matching the time of the first login. Confirms LoginServlet reads and then updates last_login correctly. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v2-11
| Field | Value |
|---|---|
| **ID** | TC-v2-11 |
| **Description** | Balance toggle — view/hide works without page reload |
| **Type** | Functional |
| **Priority** | High |
| **Steps** | 1. Log in as `customer1`. 2. On Dashboard, observe the account card shows `••••••` (hidden balance). 3. Click View Balance. 4. Observe. 5. Click Hide Balance. 6. Observe. |
| **Expected Result** | Step 3-4: `••••••` disappears, "Available at v3" text appears in gold. Button changes to "Hide Balance". No page reload — URL stays at `/Dashboard`. Step 5-6: `••••••` returns. Button returns to "View Balance". No page reload. |
| **Actual Result** | |
| **Status** | |
| **Notes** | Live balance wired at v3. |

---

### TC-v2-12
| Field | Value |
|---|---|
| **ID** | TC-v2-12 |
| **Description** | Login confirmed in SystemOut.log with correct user and role |
| **Type** | Integration |
| **Priority** | High |
| **Steps** | 1. Log in as `customer1`. 2. On dsb-dmgr VM run: `grep "LoginServlet" /apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/logs/server1/SystemOut.log \| tail -5` |
| **Expected Result** | Log contains: `LoginServlet: Login successful for user: customer1 role: CUSTOMER`. No `Login failed` line for this attempt. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v2-13
| Field | Value |
|---|---|
| **ID** | TC-v2-13 |
| **Description** | Logout confirmed in SystemOut.log |
| **Type** | Integration |
| **Priority** | High |
| **Steps** | 1. Log in as `customer1`. 2. Click Logout. 3. On dsb-dmgr VM run: `grep "LogoutServlet" /apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/logs/server1/SystemOut.log \| tail -5` |
| **Expected Result** | Log contains: `LogoutServlet: Session invalidated for user: customer1`. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v2-14
| Field | Value |
|---|---|
| **ID** | TC-v2-14 |
| **Description** | users table contains correct seed data with hashed passwords |
| **Type** | Integration |
| **Priority** | High |
| **Steps** | 1. On dsb-db VM run: `psql -U digistack_app -d digistack_bank -h 127.0.0.1 -c "SELECT username, role, full_name, email, is_active, LENGTH(password_hash) AS hash_len FROM users ORDER BY id;"` |
| **Expected Result** | Two rows returned. customer1: role=CUSTOMER, full_name=Ravi Kumar, is_active=t, hash_len=64. admin1: role=ADMINISTRATOR, full_name=Admin User, is_active=t, hash_len=64. No PLACEHOLDER text in any hash column. |
| **Actual Result** | |
| **Status** | |
| **Notes** | hash_len=64 confirms SHA-256 hex output (64 hex characters). |

---

### TC-v2-15
| Field | Value |
|---|---|
| **ID** | TC-v2-15 |
| **Description** | Time-of-day greeting is correct for current server time |
| **Type** | Functional |
| **Priority** | Medium |
| **Steps** | 1. Note the current time on the WAS server: run `date` on dsb-dmgr VM. 2. Log in as `customer1`. 3. Read the greeting on the Dashboard. |
| **Expected Result** | If server hour is 5–11: "Good Morning". If 12–16: "Good Afternoon". If 17–23 or 0–4: "Good Evening". Greeting matches server time, not browser time. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v2-16
| Field | Value |
|---|---|
| **ID** | TC-v2-16 |
| **Description** | Home page Login button navigates to Login page |
| **Type** | Functional |
| **Priority** | Medium |
| **Steps** | 1. Navigate to `/Home`. 2. Click "Login to NetBanking" button in the hero section. 3. Observe the result. |
| **Expected Result** | Browser navigates to `/Login`. Login page renders correctly. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v2-17
| Field | Value |
|---|---|
| **ID** | TC-v2-17 |
| **Description** | Already logged-in user visiting /Login is redirected to Dashboard |
| **Type** | Functional |
| **Priority** | Medium |
| **Steps** | 1. Log in as `customer1`. 2. While session is active, navigate directly to `http://192.168.10.10:9080/digistack-bank/Login`. 3. Observe the result. |
| **Expected Result** | Browser is redirected to `/Dashboard`. Login page is NOT shown to an already-authenticated user. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v2-18
| Field | Value |
|---|---|
| **ID** | TC-v2-18 |
| **Description** | Inactive account cannot log in |
| **Type** | Negative |
| **Priority** | Medium |
| **Steps** | 1. On dsb-db VM run: `UPDATE users SET is_active = false WHERE username = 'customer1';` 2. Attempt login as `customer1` with `Customer@123`. 3. Observe result. 4. After test, restore: `UPDATE users SET is_active = true WHERE username = 'customer1';` |
| **Expected Result** | Login page shows: "Your account is inactive. Please contact support." No session created. No redirect to Dashboard. |
| **Actual Result** | |
| **Status** | |
| **Notes** | Restore is_active = true after this test. |

---

### TC-v2-19
| Field | Value |
|---|---|
| **ID** | TC-v2-19 |
| **Description** | Coming soon items on Dashboard and sidebar are disabled and not clickable |
| **Type** | Functional |
| **Priority** | Low |
| **Steps** | 1. Log in as `customer1`. 2. On Dashboard attempt to click Transfer tile, Statement tile, Cards sidebar item, Loans sidebar item. 3. Observe each. |
| **Expected Result** | All disabled tiles and sidebar items are non-clickable (pointer-events: none). Each shows its "coming soon — vNN" label. No navigation occurs when clicking them. |
| **Actual Result** | |
| **Status** | |
| **Notes** | |

---

### TC-v2-20
| Field | Value |
|---|---|
| **ID** | TC-v2-20 |
| **Description** | Forgot Password and Unlock User links on Login page are disabled |
| **Type** | Functional |
| **Priority** | Low |
| **Steps** | 1. Navigate to `/Login`. 2. Attempt to click "Forgot Password?" link. 3. Attempt to click "Unlock User" link. 4. Observe each. |
| **Expected Result** | Neither link is clickable — both have `cursor: not-allowed` styling. No navigation occurs. Both show "coming soon" labels. |
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
| High | 7 | | | |
| Medium | 4 | | | |
| Low | 2 | | | |
| **v2 Subtotal** | **20** | | | |
| **v1 Regression** | **13** | | | |
| **Grand Total** | **33** | | | |

---

## Regression Pack — Forward Reference

The following v2 test cases are added to the Regression Pack.
Re-run all of these (plus v1 Regression Pack) at every subsequent
version sign-off:

| TC ID | Description | Priority |
|---|---|---|
| TC-v2-01 | Login page loads | Critical |
| TC-v2-02 | Customer login → Dashboard | Critical |
| TC-v2-03 | Admin login → Dashboard with Admin label | Critical |
| TC-v2-04 | Session guard — unauthenticated → Login | Critical |
| TC-v2-05 | Logout destroys session | Critical |
| TC-v2-06 | Wrong password rejected | Critical |
| TC-v2-07 | Non-existent username rejected | Critical |
| TC-v2-12 | Login confirmed in SystemOut.log | High |
| TC-v2-13 | Logout confirmed in SystemOut.log | High |
| TC-v2-14 | users table has hashed passwords | High |
