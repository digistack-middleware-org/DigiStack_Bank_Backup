# Test Cases — Version 2: Login & Session

**Part:** P01 — Foundation
**Prerequisite versions' regression pack:** TestCases-v1.md

## Test Case Table

| ID | Description | Type | Priority | Steps | Expected Result | Actual Result | Status | Notes |
|---|---|---|---|---|---|---|---|---|
| TC-v2-01 | users table created with seed user | Functional | Critical | Run V2__create_users.sql, SELECT * | One row: testuser with real bcrypt hash | As expected | Pass | |
| TC-v2-02 | Login page renders | Functional | Critical | Browse to /digistack-bank/login | Login form displays, banking-style UI | As expected | Pass | |
| TC-v2-03 | Invalid credentials rejected | Negative | Critical | Submit wrong password | Stays on Login page, shows error message, no session created | As expected | Pass | |
| TC-v2-04 | Valid credentials succeed | Functional | Critical | Submit testuser / Password123! | Redirects to /home, session created | As expected | Pass | |
| TC-v2-05 | Session attribute (username, lastLogin) set correctly | Functional | Critical | Log in, view Home page | "Logged in as testuser" and Last login timestamp shown | As expected | Pass | |
| TC-v2-06 | SQL injection resistance | Negative | Critical | Attempt ' OR '1'='1 style input in username field | Login rejected, no unexpected data returned (PreparedStatement in use) | As expected | Pass | |
| TC-v2-07 | Logout destroys session | Functional | Critical | Click Logout, then revisit /home directly | Home page shows logged-out state, no session data displayed | As expected | Pass | |
| TC-v2-08 | v1 regression — Home page DB read | Regression | Critical | Revisit /digistack-bank/ (unauthenticated) | Live DB welcome message still displays correctly | As expected | Pass | |
| TC-v2-09 | Redeploy over v1 succeeds cleanly | Functional | High | Update application with v2 EAR, Stop/Start | Clean start, no SEVERE errors in SystemOut.log | As expected | Pass | |
| TC-v2-10 | backupConfig baseline captured (v2) | Functional | High | Run backupConfig.sh | Archive file created in /backup | As expected | Pass | |

## Sign-off

| Item | Status |
|---|---|
| All Critical cases passed | Yes |
| All High cases passed | Yes |
| No open Critical/High defects | Yes |
| Regression subset re-run (v1) | Yes — TC-v2-08 |
| Reviewed by | Venkatesh |
| Approved date | 2026-08-11 |