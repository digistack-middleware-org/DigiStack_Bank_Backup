# Test Cases — Version 3: Basic Transaction (Deposit & Withdraw)

**Part:** P01 — Foundation
**Prerequisite versions' regression pack:** TestCases-v1.md, TestCases-v2.md

## Test Case Table

| ID | Description | Type | Priority | Steps | Expected Result | Actual Result | Status | Notes |
|---|---|---|---|---|---|---|---|---|
| TC-v3-01 | accounts table created with seed account | Functional | Critical | Run V3__create_accounts.sql, SELECT * | One row, user_id linked to testuser, balance 1000.00 | As expected | Pass | |
| TC-v3-02 | Balance displays correctly | Functional | Critical | Log in, click My Account | Shows Current Balance ₹1000.00 | As expected | Pass | |
| TC-v3-03 | Deposit increases balance | Functional | Critical | Deposit ₹200 | Balance becomes ₹1200.00, success message shown | As expected | Pass | |
| TC-v3-04 | Withdrawal decreases balance | Functional | Critical | Withdraw ₹300 (sufficient funds) | Balance decreases correctly, success message shown | As expected | Pass | |
| TC-v3-05 | Overdraft rejected | Negative | Critical | Withdraw more than current balance | Rejected, clear failure message, balance unchanged | As expected | Pass | |
| TC-v3-06 | Negative/zero deposit rejected | Negative | High | Attempt deposit of 0 or negative amount | Rejected at Service layer | Not yet executed | Blocked | Recommend running before final sign-off |
| TC-v3-07 | Unauthenticated access blocked | Negative | Critical | Log out, browse directly to /account | Redirected to /login, no balance/account data exposed | Not yet executed | Blocked | Recommend running before final sign-off |
| TC-v3-08 | v1/v2 regression | Regression | Critical | Home page DB read, Login/Logout flow | Both still work correctly | As expected | Pass | Verified during this session's testing |
| TC-v3-09 | ClassLoader / redeploy behavior understood | Functional | Medium | Stop/Start required to reflect updated code after Update Application | Confirmed via live debugging this sprint | As expected | Pass | See SetupDoc-v3.md §7 |
| TC-v3-10 | backupConfig baseline captured (v3) | Functional | High | Run backupConfig.sh | Archive created in /backup | As expected | Pass | |

## Sign-off

| Item | Status |
|---|---|
| All Critical cases passed | Pending TC-v3-07 |
| All High cases passed | Pending TC-v3-06 |
| No open Critical/High defects | Pending both above |
| Regression subset re-run | Yes — TC-v3-08 |
| Reviewed by | Venkatesh |
| Approved date | Pending |