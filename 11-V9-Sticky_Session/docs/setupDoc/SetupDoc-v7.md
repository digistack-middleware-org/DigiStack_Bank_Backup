# SetupDoc-v7.md — DigiStack Bank P01 v7

Version:  P01 v7 — JNDI DataSource Migration
Date:     <completion date>
Author:   <your name>
Status:   SIGNED OFF

---

## Purpose

This document captures the complete setup, configuration, and
verification steps performed for DigiStack Bank P01 v7.
Following this document start-to-finish on a clean environment
must reproduce a working v7 deployment — including the WAS
JDBC Provider, JAAS Auth Alias, DataSource, connection pool,
and pool validation configuration.

---

## Environment

| Component | Value |
|-----------|-------|
| WAS ND version | 9.0.5.28 |
| IHS version | 9.0.5.28 |
| PostgreSQL | 16 |
| OS | RHEL 8 |
| Java | IBM JDK 8 (bundled with WAS) |
| PostgreSQL JDBC driver | postgresql-42.7.3.jar |

## VM Topology

| VM | IP | Role | State |
|----|-----|------|-------|
| dsb-dmgr | 192.168.10.10 | DMgr + node devdsbinnode01 | ON |
| dsb-node02 | 192.168.10.11 | Cluster member 2 | ON |
| dsb-ihs | 192.168.10.20 | IBM HTTP Server (load balancer) | ON |
| dsb-db | 192.168.10.30 | PostgreSQL 16 | ON |

## WAS Cell Topology

| Item | Value |
|------|-------|
| Cell name | devdsbincell01 (confirm from your env) |
| DMgr profile | devdsbindmgr01 |
| WAS install path | /apps/IBM/WebSphere/AppServer/ |
| DMgr profile path | /apps/IBM/WebSphere/AppServer/profiles/devdsbindmgr01/ |
| Cluster name | confirm from your env |

---

## Pre-requisites

- P01 v6 signed off and deployed (digistack-bank-v6.ear running)
- All VMs powered on and reachable
- PostgreSQL JDBC driver JAR present at IDENTICAL path on BOTH nodes:
  /apps/IBM/WebSphere/AppServer/lib/ext/jdbc/postgresql-42.7.3.jar
- WAS Admin Console reachable at https://192.168.10.10:9043/ibm/console
- PostgreSQL digistack_bank database running on dsb-db (192.168.10.30)
- PostgreSQL user: digistack_app / Wasadmin@951951
- Java and Maven installed on development laptop (Windows)

---

## Part 1 — Sprint 1: Create PostgreSQL JDBC Provider

### Pre-flight: Confirm driver JAR on both nodes

```bash
# On dsb-dmgr
find /apps/IBM/WebSphere/AppServer -name "postgresql*.jar" 2>/dev/null

# On dsb-node02
find /apps/IBM/WebSphere/AppServer -name "postgresql*.jar" 2>/dev/null
```
Expected: `/apps/IBM/WebSphere/AppServer/lib/ext/jdbc/postgresql-42.7.3.jar`
on both nodes. If missing on node02 — scp from dsb-dmgr.

### Admin Console steps

1. Resources → JDBC → JDBC Providers
2. Scope: Cell=devdsbincell01
3. New → Database type: User-defined
4. Implementation class name:
   `org.postgresql.ds.PGConnectionPoolDataSource`
5. Name: `PostgreSQL JDBC Provider`
6. Description:
   `PostgreSQL 16 JDBC Provider for DigiStack Bank.
   Driver: postgresql-42.7.3.jar. Added at P01 v7.`
7. Classpath:
   `/apps/IBM/WebSphere/AppServer/lib/ext/jdbc/postgresql-42.7.3.jar`
8. Finish → Save

### wsadmin equivalent

```jython
cellId = AdminConfig.getid('/Cell:devdsbincell01/')
jdbcAttrs = [
    ['name',                   'PostgreSQL JDBC Provider'],
    ['description',            'PostgreSQL 16 JDBC Provider for DigiStack Bank. Driver: postgresql-42.7.3.jar. Added at P01 v7.'],
    ['implementationClassName', 'org.postgresql.ds.PGConnectionPoolDataSource'],
    ['classpath',              '/apps/IBM/WebSphere/AppServer/lib/ext/jdbc/postgresql-42.7.3.jar']
]
AdminConfig.create('JDBCProvider', cellId, jdbcAttrs)
AdminConfig.save()
```

### Verify

```bash
grep -i "ClassNotFoundException" \
  /apps/IBM/WebSphere/AppServer/profiles/devdsbindmgr01/logs/nodeagent/SystemOut.log
```
Expected: no output.

---

## Part 2 — Sprint 2: JAAS Auth Alias and DataSource

### Create JAAS Auth Alias

**Admin Console:**
1. Security → Global security → Java Authentication and Authorization
   Service → J2C authentication data → New
2. Alias: `BankDS_Alias`
3. User ID: `digistack_app`
4. Password: `Wasadmin@951951`
5. Description:
   `PostgreSQL digistack_bank credentials for jdbc/BankDS.
   Added at P01 v7.`
6. OK → Save

**wsadmin:**
```jython
AdminTask.createAuthDataEntry(
    '[-alias BankDS_Alias '
    '-user digistack_app '
    '-password Wasadmin@951951 '
    '-description "PostgreSQL digistack_bank credentials for jdbc/BankDS. Added at P01 v7."]')
AdminConfig.save()
```

### Create DataSource

**Admin Console:**
1. Resources → JDBC → Data sources
2. Scope: Cell=devdsbincell01
3. New
4. Data source name: `DigiStack Bank DataSource`
5. JNDI name: `jdbc/BankDS`
6. Select existing JDBC provider: `PostgreSQL JDBC Provider`
7. URL: `jdbc:postgresql://192.168.10.30:5432/digistack_bank`
8. Data store helper: `com.ibm.websphere.rsadapter.GenericDataStoreHelper`
9. Component-managed auth alias: `BankDS_Alias`
10. Container-managed auth alias: `BankDS_Alias`
11. Finish → Save

**wsadmin:**
```jython
jdbcProvider = AdminConfig.getid(
    '/Cell:devdsbincell01/JDBCProvider:PostgreSQL JDBC Provider/')
dsAttrs = [
    ['name',              'DigiStack Bank DataSource'],
    ['jndiName',          'jdbc/BankDS'],
    ['authDataAlias',     'BankDS_Alias'],
    ['xaRecoveryAuthAlias','BankDS_Alias']
]
ds = AdminConfig.create('DataSource', jdbcProvider, dsAttrs)
propSet = AdminConfig.create('J2EEResourcePropertySet', ds, [])
AdminConfig.create('J2EEResourceProperty', propSet,
    [['name','URL'],
     ['value','jdbc:postgresql://192.168.10.30:5432/digistack_bank'],
     ['type','java.lang.String']])
AdminConfig.create('J2EEResourceProperty', propSet,
    [['name','dataStoreHelperClassName'],
     ['value','com.ibm.websphere.rsadapter.GenericDataStoreHelper'],
     ['type','java.lang.String']])
AdminConfig.save()
```

### Test Connection

Admin Console → Data sources → select DigiStack Bank DataSource
→ Test connection

Expected:

The test connection operation for data source DigiStack Bank DataSource
on server server1 at node devdsbinnode01 was successful.
The test connection operation for data source DigiStack Bank DataSource
on server server1 at node devdsbinnode02 was successful.


### Verify no plaintext credentials

```bash
grep -r "Wasadmin\|digistack_app" \
  /apps/IBM/WebSphere/AppServer/profiles/devdsbindmgr01/config/cells/devdsbincell01/resources.xml
```
Expected: no output.

---

## Part 3 — Sprint 3: Connection Pool Sizing and Validation

### Pool sizing math (CAP01 §4 lab-adjusted)

2 cluster members × 20 max connections per member = 40 peak connections
PostgreSQL max_connections = 100
Headroom = 60 connections (DBA tools, monitoring, admin sessions)
dsb-db: 2 vCPU / 2 GB RAM / PostgreSQL 16 dedicated — max_connections=100 safe


### Admin Console steps

1. Resources → JDBC → Data sources → DigiStack Bank DataSource
2. Connection pool properties:

| Property | Value |
|----------|-------|
| Minimum connections | 5 |
| Maximum connections | 20 |
| Connection timeout | 180 |
| Unused timeout | 1800 |
| Aged timeout | 7200 |
| Reap time | 180 |
| Purge policy | EntirePool |
| Pre-test connections | Enabled |
| Validation query | SELECT 1 |

3. OK → Save

### wsadmin equivalent

```jython
ds = AdminConfig.getid(
    '/Cell:devdsbincell01/JDBCProvider:PostgreSQL JDBC Provider/'
    'DataSource:DigiStack Bank DataSource/')
pool = AdminConfig.list('ConnectionPool', ds)
AdminConfig.modify(pool, [
    ['minConnections',    5],
    ['maxConnections',    20],
    ['connectionTimeout', 180],
    ['unusedTimeout',     1800],
    ['agedTimeout',       7200],
    ['reapTime',          180],
    ['purgePolicy',       'EntirePool'],
    ['validateNewConnection', 'true'],
    ['preTestConnections', 'true'],
    ['connectionValidationMethod', 'SQL_QUERY'],
    ['validationQuery',   'SELECT 1']
])
AdminConfig.save()
```

### Pool validation proof

```sql
-- Find WAS pooled connection PIDs
SELECT pid, client_addr, state
FROM pg_stat_activity
WHERE usename = 'digistack_app';

-- Kill one
SELECT pg_terminate_backend(<pid>);

-- Make an app request via browser (/Dashboard)
-- Pool self-heals — user sees no error

-- Confirm fresh PIDs
SELECT pid, client_addr FROM pg_stat_activity
WHERE usename = 'digistack_app';
```
Expected: killed PID gone, new PIDs present, app request succeeded.

### Restart application servers

Pool changes require app server restart to take effect.
Admin Console → Servers → WebSphere application servers
→ select both → Stop → Start.

---

## Part 4 — Sprint 4: JNDI Migration

### What changed

All seven classes migrated from `DriverManager.getConnection()` to
JNDI lookup of `jdbc/BankDS`:

| Class | v1–v6 | v7 |
|-------|-------|-----|
| AccountService | DriverManager | InitialContext.lookup("jdbc/BankDS") |
| DepositService | DriverManager | InitialContext.lookup("jdbc/BankDS") |
| WithdrawService | DriverManager | InitialContext.lookup("jdbc/BankDS") |
| FreezeService | DriverManager | InitialContext.lookup("jdbc/BankDS") |
| HomeServlet | DriverManager | InitialContext.lookup("jdbc/BankDS") |
| LoginServlet | DriverManager | InitialContext.lookup("jdbc/BankDS") |
| DashboardServlet | init() removed | No driver registration needed |

### Grep verification (mandatory)

Run from project root — all must return no output except SeedUsers.java:

```bash
grep -r "DriverManager" digistack-bank-web/src/main/java/
grep -r "192.168.10.30"  digistack-bank-web/src/main/java/
grep -r "Wasadmin@951951" digistack-bank-web/src/main/java/
grep -r "jdbc:postgresql" digistack-bank-web/src/main/java/
```

### SeedUsers.java exception (intentional)

SeedUsers.java retains direct JDBC — it is a standalone utility
run on the dev laptop outside WAS. JNDI is not available outside
a WAS JVM context. Credentials moved to gitignored
`config/db-local.properties`. No hardcoded credentials remain
in any WAS-deployed class.

---

## Part 5 — Sprint 5: EAR Packaging and Deployment

### EAR version changes

- `digistack-bank-ear/pom.xml` → `<finalName>digistack-bank-v7</finalName>`
- `application.xml` → `<display-name>DigiStack Bank v7</display-name>`

### Build

```bash
cd digistack-bank
mvn clean package
```
Expected: `digistack-bank-ear/target/digistack-bank-v7.ear` produced.

### Deploy — Admin Console

1. Applications → WebSphere enterprise applications
2. Stop DigiStack Bank v6
3. Select → Update → Replace entire application
4. Browse to digistack-bank-v7.ear → Next through wizard
5. Map to cluster → Finish → Save
6. Start DigiStack Bank v7

### Deploy — wsadmin

```jython
AdminApp.update(
    'DigiStack Bank v6',
    'app',
    '[-operation update '
    '-contents /home/<user>/digistack-bank-v7.ear '
    '-usedefaultbindings]')
AdminConfig.save()
```

### Post-deployment verification

| URL | Expected |
|-----|----------|
| http://192.168.10.10:9080/digistack-bank/Home | v7 footer, DB Connected |
| http://192.168.10.11:9080/digistack-bank/Home | identical to member 1 |
| http://192.168.10.20/digistack-bank/Home | loads via IHS |

### Transaction boundary traceability note

**Current state (v7):**
All operations are single-statement local transactions with
`autoCommit=true`. PostgreSQL provides implicit atomicity.
No explicit `conn.commit()` or `conn.rollback()` in application code.
This is correct and complete for all current operations
(each touches one row in one table on one DataSource).

**Rollback proof (Sprint 5):**
Over-limit Withdraw submitted → `InsufficientFundsException` thrown
before SQL UPDATE → balance unchanged (confirmed by SELECT before
and after).

**Future transaction boundary design points:**

| Version | What | Why |
|---------|------|-----|
| P02 v15 | Fund Transfer — two account rows | `setAutoCommit(false)` / explicit commit + rollback |
| P03 v23/v25 | PostgreSQL + Oracle 21c XE | Saga pattern — avoids 2PC/XA entirely |

---

## Part 6 — Sprint 6: Test Cases and Unit Tests

### Manual test cases
- File: `docs/testing/TestCases-v7.md`
- 47 test cases across 7 sections
- 31 Critical + 14 High cases all passed before sign-off

### Unit tests (TEST01 — gating)

```bash
mvn test
```
Expected:

Tests run: 25, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS

No new Service classes introduced in v7.
Existing test subclasses override `getConnection()` — JNDI
path never reached in tests. All 25 tests pass unchanged.

---

## Part 7 — Sprint 7: backupConfig

### Command

```bash
cd /apps/IBM/WebSphere/AppServer/profiles/devdsbindmgr01/bin
./backupConfig.sh \
  /apps/IBM/WebSphere/AppServer/profiles/devdsbindmgr01/config-backups/digistack-bank-v7-signoff.zip \
  -nostop
```

### Backup file

/apps/IBM/WebSphere/AppServer/profiles/devdsbindmgr01/
config-backups/digistack-bank-v7-signoff.zip


### Restore command (reference only — do not run unless restoring)

```bash
./restoreConfig.sh \
  /apps/IBM/WebSphere/AppServer/profiles/devdsbindmgr01/config-backups/digistack-bank-v7-signoff.zip
```

---

## Known Technical Debt (carried into v8)

| Item | Detail |
|------|--------|
| freezeAccount.py still uses direct JDBC | wsadmin script cannot use JNDI DataSource — credential lookup improvement via AdminTask deferred |
| No role gating on Freeze/Unfreeze | Any logged-in user can freeze/unfreeze. Role-based protection deferred to v10 |
| No audit log table | No immutable transaction history. Balance mutations applied directly to accounts.balance |
| Explicit transaction boundaries | Not needed at v7 (single-statement ops). Required at P02 v15 (Fund Transfer — two accounts) |
| JSP scriptlets | Raw scriptlet tags in JSPs — out of scope for this learning project |

---

## Sprint 7 Sign-Off Checklist

- [ ] backupConfig captured: `digistack-bank-v7-signoff.zip` confirmed present
- [ ] Smoke Test 1 passed: Home loads via IHS, DB Connected shown
- [ ] Smoke Test 2 passed: Login succeeds for customer1
- [ ] Smoke Test 3 passed: Deposit ₹500 succeeds via JNDI
- [ ] Smoke Test 4 passed: Withdraw ₹100 succeeds via JNDI
- [ ] Smoke Test 5 passed: Over-limit Withdraw rejected, balance unchanged
- [ ] SetupDoc-v7.md complete and accurate
- [ ] TestCases-v7.md: all 31 Critical + 14 High cases marked Pass
- [ ] mvn test: BUILD SUCCESS, 0 failures (TEST01 gating satisfied)
- [ ] Footer updated to v7 across all JSPs
- [ ] Transaction-boundary traceability note in SetupDoc-v7.md

**Version 7 is signed off when all boxes above are checked.**