# TestCases-v6.md — DigiStack Bank P01 v6

Version: P01 v6 — Application Administration
Format: TCS01 condensed (one line per case, v5+ standard)
Tester: <your name>
Date: <test date>

Legend — Priority: C=Critical  H=High  M=Medium
Legend — Result:   P=Pass  F=Fail  S=Skip
All C and H cases must Pass before Sprint 7 sign-off.

---

## Section 1 — Node Synchronization (Sprint 1)

| ID | Priority | Test Case | Expected Result | Result | Notes |
|----|----------|-----------|-----------------|--------|-------|
| TC-v6-01 | H | Trigger ordinary Node Sync via Admin Console | Success message, node status = Synchronized | | |
| TC-v6-02 | H | Manually edit variables.xml on node02, trigger ordinary sync | Drift comment still present after sync — not removed | | |
| TC-v6-03 | H | Trigger Full Resync on drifted node | Drift comment removed, variables.xml matches DMgr copy | | |
| TC-v6-04 | H | Trigger ordinary Node Sync via wsadmin AdminControl.invoke sync | Command completes with no error, node Synchronized | | |
| TC-v6-05 | M | Trigger Full Resync via wsadmin syncActiveModel | Command completes with no error | | |

---

## Section 2 — Database Schema (Sprint 2)

| ID | Priority | Test Case | Expected Result | Result | Notes |
|----|----------|-----------|-----------------|--------|-------|
| TC-v6-06 | C | Run \d accounts in psql | is_frozen column present, type boolean, not null, default false | | |
| TC-v6-07 | C | SELECT is_frozen FROM accounts for all rows | Every row returns f (false) — no account frozen by default | | |
| TC-v6-08 | H | V4__add_frozen_flag.sql rollback — check query returns 0 rows | No frozen accounts — safe to roll back schema if needed | | |

---

## Section 3 — Freeze/Unfreeze UI and Logic (Sprint 3)

| ID | Priority | Test Case | Expected Result | Result | Notes |
|----|----------|-----------|-----------------|--------|-------|
| TC-v6-09 | C | Login as customer1, navigate to /Account | Status shows Active, Freeze tile enabled, Unfreeze tile greyed out | | |
| TC-v6-10 | C | Click Freeze tile on Account page | /Freeze loads, shows current status Active, Confirm Freeze button present | | |
| TC-v6-11 | C | Click Confirm Freeze on Freeze page | Redirected to /Freeze?result=success, green success banner shown | | |
| TC-v6-12 | C | Check DB after freeze — SELECT is_frozen FROM accounts WHERE account_number='<n>' | Returns t (true) | | |
| TC-v6-13 | C | Navigate to /Account after freeze | Status shows Frozen, Deposit/Withdraw tiles greyed out, Unfreeze tile enabled | | |
| TC-v6-14 | C | Navigate directly to /Deposit while frozen | Amount field disabled, Deposit button disabled, yellow frozen warning banner shown | | |
| TC-v6-15 | C | POST directly to /Deposit with amount=500 while frozen (curl or browser DevTools) | Redirected to /Deposit?error=..., deposit rejected server-side, balance unchanged in DB | | |
| TC-v6-16 | C | Navigate directly to /Withdraw while frozen | Amount field disabled, Withdraw button disabled, yellow frozen warning banner shown | | |
| TC-v6-17 | C | POST directly to /Withdraw with amount=500 while frozen | Redirected to /Withdraw?error=..., withdrawal rejected server-side, balance unchanged in DB | | |
| TC-v6-18 | C | Click Freeze tile while account is already frozen | /Freeze loads, shows status Frozen, warning shown instead of Confirm Freeze form | | |
| TC-v6-19 | C | Navigate to /Unfreeze while frozen | Status Frozen shown, Confirm Unfreeze button present | | |
| TC-v6-20 | C | Click Confirm Unfreeze on Unfreeze page | Redirected to /Unfreeze?result=success, green success banner shown | | |
| TC-v6-21 | C | Check DB after unfreeze — SELECT is_frozen | Returns f (false) | | |
| TC-v6-22 | C | Navigate to /Account after unfreeze | Status shows Active, Deposit/Withdraw tiles re-enabled | | |
| TC-v6-23 | C | Deposit ₹100 after unfreeze | Deposit succeeds, balance increases by ₹100 — confirms full operation restored | | |
| TC-v6-24 | C | Withdraw ₹100 after unfreeze | Withdrawal succeeds, balance decreases by ₹100 | | |
| TC-v6-25 | H | Click Unfreeze tile when account is already active | /Unfreeze loads, shows info banner — account already active, no form shown | | |
| TC-v6-26 | M | Attempt freeze with no session (access /Freeze directly without login) | Redirected to /Login | | |
| TC-v6-27 | M | Attempt unfreeze with no session (access /Unfreeze directly without login) | Redirected to /Login | | |

---

## Section 4 — wsadmin Freeze/Unfreeze Script (Sprint 4)

| ID | Priority | Test Case | Expected Result | Result | Notes |
|----|----------|-----------|-----------------|--------|-------|
| TC-v6-28 | C | Run freezeAccount.py <account_number> freeze on active account | Script outputs SUCCESS: Account ... has been FREEZED, DB shows is_frozen=t | | |
| TC-v6-29 | C | Verify UI reflects wsadmin-set freeze state | Login, navigate to /Account — status shows Frozen without any UI action | | |
| TC-v6-30 | C | Run freezeAccount.py <account_number> freeze on already-frozen account | Script outputs NO ACTION: Account is already frozen | | |
| TC-v6-31 | C | Run freezeAccount.py <account_number> unfreeze on frozen account | Script outputs SUCCESS: Account ... has been UNFREEZED, DB shows is_frozen=f | | |
| TC-v6-32 | C | Verify UI reflects wsadmin-set unfreeze state | Navigate to /Account — status shows Active | | |
| TC-v6-33 | H | Run freezeAccount.py <account_number> unfreeze on already-active account | Script outputs NO ACTION: Account is already active (not frozen) | | |
| TC-v6-34 | H | Run freezeAccount.py with invalid action argument | Script outputs ERROR: action must be 'freeze' or 'unfreeze' | | |
| TC-v6-35 | H | Run freezeAccount.py with non-existent account number | Script outputs ERROR: No account found with account_number = <n> | | |

---

## Section 5 — EAR Deployment and Cluster Synchronization (Sprint 5)

| ID | Priority | Test Case | Expected Result | Result | Notes |
|----|----------|-----------|-----------------|--------|-------|
| TC-v6-36 | C | Run mvn clean package from project root | BUILD SUCCESS, digistack-bank-v6.ear present in digistack-bank-ear/target/ | | |
| TC-v6-37 | C | Access http://192.168.10.10:9080/digistack-bank/Home (member 1 direct) | Home page loads, footer shows v6 | | |
| TC-v6-38 | C | Access http://192.168.10.11:9080/digistack-bank/Home (member 2 direct) | Home page loads, footer shows v6 — identical to member 1 | | |
| TC-v6-39 | C | Access http://192.168.10.20/digistack-bank/Home via IHS | Home page loads via load balancer | | |
| TC-v6-40 | H | Admin Console — Application status for both cluster members | Both show Started (green icon) | | |
| TC-v6-41 | H | Admin Console — Node status post Full Resync | Both nodes show Synchronized | | |
| TC-v6-42 | H | application.xml display-name shows in Admin Console | Applications list shows DigiStack Bank v6 — not a generic name | | |

---

## Section 6 — Regression: Existing Features

| ID | Priority | Test Case | Expected Result | Result | Notes |
|----|----------|-----------|-----------------|--------|-------|
| TC-v6-43 | C | Navigate to /Home | Home page loads, System Status and DB Connected shown | | |
| TC-v6-44 | C | Login with valid credentials (customer1 / Customer@123) | Redirected to /Dashboard, username shown in navbar | | |
| TC-v6-45 | C | Login with invalid password | Error message shown, no session created | | |
| TC-v6-46 | C | Access /Dashboard without session | Redirected to /Login | | |
| TC-v6-47 | C | Logout via navbar button | Session invalidated, redirected to /Home | | |
| TC-v6-48 | H | Dashboard account card shows masked account number | **** **** <last4> format displayed | | |
| TC-v6-49 | H | Dashboard View Balance toggle fetches live balance via /BalanceJson | Balance amount shown on click, dots shown on second click | | |
| TC-v6-50 | H | Navigate to /Deposit, enter ₹500, submit on active account | Success banner, balance increases by ₹500 | | |
| TC-v6-51 | H | Navigate to /Withdraw, enter ₹200, submit on active account with sufficient funds | Success banner, balance decreases by ₹200 | | |
| TC-v6-52 | H | Withdraw amount exceeding balance | Error banner: Insufficient funds, balance unchanged | | |
| TC-v6-53 | M | Deposit with zero amount | Error banner: Amount must be greater than zero | | |
| TC-v6-54 | M | Deposit with negative amount | Error banner: Amount must be greater than zero | | |
| TC-v6-55 | M | Deposit with non-numeric amount | Error banner: Invalid amount | | |

---

## Section 7 — Unit Tests (TEST01 — Sprint 6 mandatory)

| ID | Priority | Test Case | Expected Result | Result | Notes |
|----|----------|-----------|-----------------|--------|-------|
| TC-v6-56 | C | Run mvn test from project root | Tests run: 25, Failures: 0, Errors: 0, Skipped: 0 — BUILD SUCCESS | | |
| TC-v6-57 | C | DepositServiceTest — all 9 test methods pass | Green in test output | | |
| TC-v6-58 | C | WithdrawServiceTest — all 8 test methods pass | Green in test output | | |
| TC-v6-59 | C | FreezeServiceTest — all 8 test methods pass | Green in test output | | |

---