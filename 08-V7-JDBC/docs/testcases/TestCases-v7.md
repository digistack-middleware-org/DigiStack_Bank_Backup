# TestCases-v7.md — DigiStack Bank P01 v7

Version: P01 v7 — JNDI DataSource Migration
Format: TCS01 condensed (one line per case, v5+ standard)
Tester: <your name>
Date: <test date>

Legend — Priority: C=Critical  H=High  M=Medium
Legend — Result:   P=Pass  F=Fail  S=Skip
All C and H cases must Pass before Sprint 7 sign-off.

---

## Section 1 — JDBC Provider (Sprint 1)

| ID | Priority | Test Case | Expected Result | Result | Notes |
|----|----------|-----------|-----------------|--------|-------|
| TC-v7-01 | C | Admin Console → Resources → JDBC → JDBC Providers → confirm PostgreSQL JDBC Provider exists at Cell scope | Provider listed, scope = Cell=devdsbincell01 | | |
| TC-v7-02 | C | Click PostgreSQL JDBC Provider → confirm Implementation class name | org.postgresql.ds.PGConnectionPoolDataSource | | |
| TC-v7-03 | C | Confirm Classpath field value in JDBC Provider | /apps/IBM/WebSphere/AppServer/lib/ext/jdbc/postgresql-42.7.3.jar | | |
| TC-v7-04 | H | On dsb-node02 — find /apps/IBM/WebSphere/AppServer -name "postgresql*.jar" | postgresql-42.7.3.jar found at identical path as dsb-dmgr | | |
| TC-v7-05 | H | grep ClassNotFoundException in nodeagent SystemOut.log on dsb-node02 after sync | No ClassNotFoundException output | | |

---

## Section 2 — JAAS Auth Alias and DataSource (Sprint 2)

| ID | Priority | Test Case | Expected Result | Result | Notes |
|----|----------|-----------|-----------------|--------|-------|
| TC-v7-06 | C | Admin Console → Security → Global security → J2C authentication data → confirm BankDS_Alias exists | BankDS_Alias listed with user digistack_app | | |
| TC-v7-07 | C | Admin Console → Resources → JDBC → Data sources → confirm DigiStack Bank DataSource exists with JNDI name jdbc/BankDS | DataSource listed, JNDI name = jdbc/BankDS | | |
| TC-v7-08 | C | Select DigiStack Bank DataSource → Test connection → devdsbinnode01 result | Test connection operation was successful on node01 | | |
| TC-v7-09 | C | Select DigiStack Bank DataSource → Test connection → devdsbinnode02 result | Test connection operation was successful on node02 | | |
| TC-v7-10 | H | grep -r "Wasadmin" resources.xml in DMgr cell config folder | No plaintext password in resources.xml — alias reference only | | |
| TC-v7-11 | H | Click DigiStack Bank DataSource → confirm component-managed and container-managed authentication aliases | Both show BankDS_Alias | | |

---

## Section 3 — Connection Pool Sizing and Validation (Sprint 3)

| ID | Priority | Test Case | Expected Result | Result | Notes |
|----|----------|-----------|-----------------|--------|-------|
| TC-v7-12 | C | DataSource → Connection pool properties → Minimum connections | 5 | | |
| TC-v7-13 | C | DataSource → Connection pool properties → Maximum connections | 20 | | |
| TC-v7-14 | C | Calculate total peak connections: 2 members × 20 max = 40. Headroom against max_connections=100 | 40 connections peak, 60 headroom — documented | | |
| TC-v7-15 | H | Confirm timeout values in connection pool properties | Connection timeout=180, Unused timeout=1800, Aged timeout=7200, Reap time=180 | | |
| TC-v7-16 | H | Confirm Purge policy in connection pool properties | EntirePool | | |
| TC-v7-17 | C | Confirm validation settings in connection pool properties | Pre-test connections=enabled, Validation query=SELECT 1 | | |
| TC-v7-18 | C | Run pg_terminate_backend(<pid>) on a WAS pooled connection PID from pg_stat_activity, then make an app request | App request succeeds with no user-visible error — pool rebuilt transparently | | |
| TC-v7-19 | H | After pool rebuild — SELECT pid FROM pg_stat_activity WHERE usename='digistack_app' | New PIDs present, killed PID gone — pool self-healed | | |

---

## Section 4 — JNDI Migration: Grep Verification (Sprint 4)

| ID | Priority | Test Case | Expected Result | Result | Notes |
|----|----------|-----------|-----------------|--------|-------|
| TC-v7-20 | C | grep -r "DriverManager" digistack-bank-web/src/main/java/ | No output except SeedUsers.java | | |
| TC-v7-21 | C | grep -r "192.168.10.30" digistack-bank-web/src/main/java/ | No output except SeedUsers.java | | |
| TC-v7-22 | C | grep -r "Wasadmin@951951" digistack-bank-web/src/main/java/ | No output at all — SeedUsers.java now reads from properties file | | |
| TC-v7-23 | C | grep -r "jdbc:postgresql" digistack-bank-web/src/main/java/ | No output except SeedUsers.java | | |
| TC-v7-24 | H | Confirm SeedUsers.java reads from config/db-local.properties at runtime | PROPS_FILE constant present, no static credential constants | | |
| TC-v7-25 | H | Confirm config/db-local.properties is listed in .gitignore | grep "db-local.properties" .gitignore returns a match | | |

---

## Section 5 — EAR Deployment and Transaction Boundary (Sprint 5)

| ID | Priority | Test Case | Expected Result | Result | Notes |
|----|----------|-----------|-----------------|--------|-------|
| TC-v7-26 | C | Run mvn clean package from project root | BUILD SUCCESS, digistack-bank-v7.ear present in digistack-bank-ear/target/ | | |
| TC-v7-27 | C | Navigate to http://192.168.10.20/digistack-bank/Home | Home page loads, dbConnStatus shows Connected — JNDI DataSource active | | |
| TC-v7-28 | C | Admin Console → Application status for both cluster members | Both show DigiStack Bank v7 — Started (green) | | |
| TC-v7-29 | C | Submit Withdraw for amount exceeding current balance | Error banner: Insufficient funds. Balance unchanged in DB (SELECT before and after confirm identical value) | | |
| TC-v7-30 | H | Check SystemOut.log after failed Withdraw | No SQL error stack trace — InsufficientFundsException caught cleanly at service layer, redirect issued | | |
| TC-v7-31 | H | Admin Console → Applications list → application display name | Shows DigiStack Bank v7 — not v6 or a generic name | | |

---

## Section 6 — Regression: All Existing Features via JNDI

| ID | Priority | Test Case | Expected Result | Result | Notes |
|----|----------|-----------|-----------------|--------|-------|
| TC-v7-32 | C | Navigate to /Home | Page loads, System Status and DB Connected shown — HomeServlet JNDI call succeeded | | |
| TC-v7-33 | C | Login as customer1 / Customer@123 | Redirected to /Dashboard, username in navbar — LoginServlet JNDI call succeeded | | |
| TC-v7-34 | C | Login with invalid password | Error message shown, no session created | | |
| TC-v7-35 | C | Access /Dashboard without session | Redirected to /Login | | |
| TC-v7-36 | C | Logout via navbar | Session invalidated, redirected to /Home | | |
| TC-v7-37 | C | Deposit ₹500 on active account via /Deposit | Success banner, balance increases by ₹500 — DepositService JNDI call succeeded | | |
| TC-v7-38 | C | Withdraw ₹200 on active account via /Withdraw | Success banner, balance decreases by ₹200 — WithdrawService JNDI call succeeded | | |
| TC-v7-39 | C | Withdraw amount exceeding balance | Error banner: Insufficient funds — balance unchanged in DB | | |
| TC-v7-40 | H | Freeze account via /Freeze → Confirm Freeze | Success banner, account status shows Frozen — FreezeService JNDI call succeeded | | |
| TC-v7-41 | H | Attempt Deposit while frozen | Form disabled in UI, server-side rejection confirms frozen check still enforced via JNDI connection | | |
| TC-v7-42 | H | Unfreeze via /Unfreeze → Confirm Unfreeze, then Deposit ₹100 | Unfreeze succeeds, Deposit succeeds — full round-trip confirmed via JNDI | | |
| TC-v7-43 | M | Run freezeAccount.py via wsadmin on seed account | Script still works — uses direct JDBC separately from application JNDI path, no conflict | | |

---

## Section 7 — Unit Tests (TEST01 — Gating)

| ID | Priority | Test Case | Expected Result | Result | Notes |
|----|----------|-----------|-----------------|--------|-------|
| TC-v7-44 | C | Run mvn test from project root | Tests run: 25, Failures: 0, Errors: 0, Skipped: 0 — BUILD SUCCESS | | |
| TC-v7-45 | C | DepositServiceTest — all 9 test methods pass | Green in test output — getConnection() override means JNDI path never reached in tests | | |
| TC-v7-46 | C | WithdrawServiceTest — all 8 test methods pass | Green in test output | | |
| TC-v7-47 | C | FreezeServiceTest — all 8 test methods pass | Green in test output | | |

---

## Sign-Off Summary

| Section | Total Cases | C Cases | H Cases | M Cases | All C+H Pass? |
|---------|-------------|---------|---------|---------|---------------|
| 1 — JDBC Provider | 5 | 3 | 2 | 0 | |
| 2 — JAAS Auth Alias + DataSource | 6 | 4 | 2 | 0 | |
| 3 — Connection Pool + Validation | 8 | 4 | 3 | 0 | |
| 4 — JNDI Migration Grep | 6 | 4 | 2 | 0 | |
| 5 — EAR + Transaction Boundary | 6 | 4 | 2 | 0 | |
| 6 — Regression | 12 | 8 | 3 | 1 | |
| 7 — Unit Tests | 4 | 4 | 0 | 0 | |
| **Total** | **47** | **31** | **14** | **1** | |

Sprint 7 sign-off is blocked until all 31 Critical + 14 High cases show P (Pass).
Unit tests (TC-v7-44 through TC-v7-47) are explicitly GATING per standing rule TEST01.

---

## Notes on Section 3 — Pool Sizing Traceability

The CAP01 §4 lab-adjusted formula for this project's topology:

  2 members × 20 max connections per member = 40 peak connections
  PostgreSQL max_connections = 100
  Headroom = 60 connections (for DBA tools, monitoring, admin psql sessions)

dsb-db VM spec: 2 vCPU / 2 GB RAM / PostgreSQL 16 dedicated host.
PostgreSQL 16 on 2 GB RAM: max_connections=100 is the established
safe ceiling (each idle connection ~5–10 MB shared memory overhead).
40 peak connections = 40% of capacity. Headroom is comfortable.

## Notes on Section 4 — SeedUsers Exception

SeedUsers.java intentionally retains DriverManager.getConnection()
because it runs on the dev laptop outside WAS — no JNDI context
is available. Credentials moved to config/db-local.properties
(gitignored). This is the correct pattern for a standalone utility.
All four grep checks (TC-v7-20 through TC-v7-23) confirm no
hardcoded credentials remain in any WAS-deployed class.