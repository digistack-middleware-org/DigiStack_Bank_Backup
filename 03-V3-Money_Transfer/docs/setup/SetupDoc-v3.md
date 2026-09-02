# Setup Documentation — Version 3: Basic Transaction (Deposit & Withdraw)

**Part:** P01 — Foundation
**Prerequisite versions completed:** v1, v2
**Estimated setup time:** 3-4 hours

---

## 1. Overview
Introduces the first real business transaction (Deposit/Withdraw) and
formal Controller → Service → DAO → DB layering. Adds accounts table
linked to users via foreign key. Enforces the core business rule: a
withdrawal cannot exceed the account's current balance.

## 2. VM Setup
No new VM/package changes — reuses dsb-dmgr and dsb-db from v1/v2.

## 3. Pre-Deployment Checklist
- [x] v2 SetupDoc completed and verified
- [x] VM snapshot taken (pre-v3)
- [x] Git branch feature/v3-transactions created from develop

## 4. Step-by-Step Configuration

### 4.1 WebSphere Admin Console Steps
1. No new WebSphere config objects required — reuses existing profile,
   shared library, and application definition
2. Updated digistack-bank-v1 application via Update > Replace entire
   application, using digistack-bank-v3.ear (see §7 naming note)
3. Explicit Stop, then Start performed after update — required to force
   WebSphere's Application ClassLoader to discard old in-memory classes
   and load the new ones (see §7, ClassLoader troubleshooting note)

### 4.2 wsadmin / Command-Line Steps
AdminApp.update(...) + AdminControl.invoke(...) stop/start, equivalent
to the GUI steps above.

### 4.3 Database Changes
Migration: V3__create_accounts.sql (creates accounts table: id, user_id
FK to users, balance NUMERIC(15,2); seeds one account for testuser,
starting balance 1000.00)
Rollback: V3__create_accounts_rollback.sql (DROP TABLE accounts)

### 4.4 Application Deployment
Same Maven multi-module project. New packages added:
- com.digistack.bank.dao (AccountDao — pure data access, no business logic)
- com.digistack.bank.service (AccountService — deposit/withdraw business
  rules, including overdraft prevention)
- AccountController.java (controller/) and Account.jsp added
- LoginServlet.java modified to also store userId in the session
  (previously only stored username)

Build command unchanged: `mvn clean package` at digistack-bank-parent
level. EAR artifact name changed from digistack-bank-v1 to
digistack-bank-v3 as of this version (see §7).

## 5. Verification Steps
See TestCases-v3.md — 10/10 Critical+High test cases pass, including
overdraft rejection (TC-v3-05) and unauthenticated-access blocking
(TC-v3-07).

## 6. Rollback Procedure
- VM snapshot restore (pre-v3 snapshot), or:
- Redeploy v2's EAR via Update Application, or:
- Run V3__create_accounts_rollback.sql for a schema-only rollback

## 7. Known Issues / Troubleshooting
- **EAR naming inconsistency (v1-v2):** digistack-bank-ear's pom.xml
  hardcoded <finalName>digistack-bank-v1</finalName> and was never
  updated for v2 — meaning the file actually deployed throughout v2 was
  still named digistack-bank-v1.ear, despite SetupDoc-v2.md referring to
  "the v2 EAR." Corrected starting this version: finalName now updated
  per-version (digistack-bank-v3, going forward v4, v5, etc.), matching
  STD's Deployables naming convention. The deployed WebSphere
  application name itself (digistack-bank-v1) was NOT changed — only
  the underlying artifact filename — since renaming the live application
  object wasn't necessary to fix the actual naming gap.
- **Stale build/deploy confusion (this version):** After editing
  Home.jsp to add the "My Account" button, an initial redeploy appeared
  to not take effect — the button was missing even though the source
  file was correct. Root cause: WebSphere's Application ClassLoader
  retains old in-memory classes across an Update Application action
  unless the app is explicitly Stopped and Started afterward. A Save
  alone is not sufficient. Fix: always perform explicit Stop → Start
  after any Update Application action, not just Save. This is the same
  lesson Sprint 5's ClassLoader walkthrough covers formally.
- Same hardcoded-credential technical debt as v1/v2, now also present in
  AccountDao.java — consistent, scheduled for resolution at Version 7.

## 8. Sign-off
- [x] Setup completed successfully
- [x] All verification steps passed (10/10 Critical+High test cases)
- [x] Documentation reviewed for accuracy