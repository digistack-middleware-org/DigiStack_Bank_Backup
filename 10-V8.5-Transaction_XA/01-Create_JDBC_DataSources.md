# WebSphere Application Server — PostgreSQL XA DataSource Configuration Guide

This document describes how to configure two XA-capable PostgreSQL DataSources (`jdbc/DebitDS` and `jdbc/CreditDS`) on WebSphere Application Server (WAS) at Cell scope, and propagate the configuration to all cluster nodes.

---

## Overview

| Item | Value |
|---|---|
| Database | PostgreSQL (`digistack_bank`) |
| DB Host / Port | `192.168.10.30:5432` |
| Driver JAR | `postgresql-42.7.3.jar` (in `lib/ext`) |
| Implementation Class | `org.postgresql.xa.PGXADataSource` |
| Auth Alias | `devdsbincell01/BankDS_Alias` |
| DataSources | `jdbc/DebitDS`, `jdbc/CreditDS` |
| Cell | `devdsbincell01` |

---

## Step 1 — Locate the Driver JAR on the Server

Run on `dsb-dmgr`:

```bash
ls /apps/IBM/SharedLibs/postgresql/postgresql-42.7.3.jar
```

- `SharedLibs` is a directory WAS automatically adds to the classpath at startup.
- Copy the exact full path — you will paste it into the Provider config.

> [!IMPORTANT]
> If the path is wrong, WAS cannot load the driver, and every connection test fails.

---

## Step 2 — Create the JDBC Provider

**Console path:** `Resources → JDBC → JDBC Providers` → set `Scope = Cell=devdsbincell01` → **New**.

| Field | Value | Why |
|---|---|---|
| Database type | `User-defined` | WAS has no built-in PostgreSQL template |
| Implementation class name | `org.postgresql.xa.PGXADataSource` | XA-capable class from the PostgreSQL driver |
| Name | `XA LAB JDBC` | Human-friendly label |

On the next page, set the class path:

```text
/apps/IBM/SharedLibs/postgresql/postgresql-42.7.3.jar
```

(Use your actual filename from Step 1.)

Then: **Next → Finish → Save**.

> [!TIP]
> **Key lesson — the implementation class decides everything:**
> - `org.postgresql.ds.PGConnectionPoolDataSource` → normal connections (no XA)
> - `org.postgresql.xa.PGXADataSource` → XA connections (two-phase commit)
>
> If you pick the wrong class here, you cannot fix it later by editing the DataSource — you must fix the Provider.

> [!NOTE]
> In WAS, **Save** writes to the master config repository. Nothing is live on the nodes yet — that comes in the sync step (Step 7).

---

## Step 3 — Create the DataSource

**Console path:** `Resources → JDBC → Data sources` → `Scope = Cell=devdsbincell01` → **New**.

| Field | Value | Why |
|---|---|---|
| Data source name | `DigiStack Debit XA DataSource` | Display label only |
| JNDI name | `jdbc/DebitDS` | Critical — app code looks it up; must match exactly (case-sensitive) |

### Select JDBC provider page

- Choose **"Select an existing JDBC provider"**
- Pick `XA LAB JDBC`

### Database specific properties page

- **Leave the URL blank.** `PGXADataSource` does not use a URL string — it uses separate JavaBean properties (Step 5).

### Setup security aliases page

| Field | Value |
|---|---|
| Component-managed auth alias | `devdsbincell01/BankDS_Alias` |
| Container-managed auth alias | `devdsbincell01/BankDS_Alias` |
| XA recovery auth alias | `devdsbincell01/BankDS_Alias` |

> [!TIP]
> **Key lesson — auth aliases:**
> The auth alias is a stored username/password (JAAS alias). Using it means:
> - No credentials in your application code
> - Central password management — change it in one place, everywhere updates

**Finish → Save.**

---

## Step 4 — Repeat for the Second DataSource

Same procedure, only two values change:

| Field | Value |
|---|---|
| Data source name | `DigiStack Credit XA DataSource` |
| JNDI name | `jdbc/CreditDS` |

Everything else is identical: same Provider, same alias, same scope.

> [!NOTE]
> **Why two DataSources to the same database?**
> They are separate connection pools. The application's transaction manager enlists both in a single XA transaction so the debit + credit commit together or roll back together.

---

## Step 5 — Add Connection Properties (Custom Properties)

Because `PGXADataSource` is a JavaBean, WAS passes settings to its setter methods. Do this via **Custom properties** on each DataSource.

**Path:** Open each DataSource → `Custom properties` (under *Additional Properties*) → **New** for each:

| # | Name | Value | Type |
|---|---|---|---|
| 1 | `serverName` | `192.168.10.30` | `java.lang.String` |
| 2 | `portNumber` | `5432` | `java.lang.Integer` |
| 3 | `databaseName` | `digistack_bank` | `java.lang.String` |

These map directly to driver calls:

```java
setServerName("192.168.10.30");
setPortNumber(5432);
setDatabaseName("digistack_bank");
```

### Common mistakes

- Typo in the property name (e.g., `servername` — be consistent)
- Setting `portNumber` as `String` → connection test fails
- Forgetting to click **Save** after adding properties

> [!NOTE]
> Repeat for **both** DataSources — same values, since both point to the same PostgreSQL instance.

---

## Step 6 — Test Connection

**Console path:** `Data sources (Cell scope)` →

1. Check the box next to the DataSource
2. Click **Test connection**

### What WAS does behind the scenes

1. Loads the driver JAR from the Provider's class path
2. Instantiates `PGXADataSource`
3. Applies your custom properties
4. Connects to PostgreSQL using the auth alias credentials

A green banner = the entire chain works.

### If the test fails, check in this order

1. Custom properties present and spelled correctly?
2. `portNumber` type = `Integer`?
3. Auth alias name correct (format: `cell/aliasname`)?
4. Is PostgreSQL reachable from the WAS box?

```bash
ping 192.168.10.30
telnet 192.168.10.30 5432
```

Test **both** DataSources.

---

## Step 7 — Sync Nodes

### The concept

- The console writes to the Deployment Manager's **master repository** (on `dsb-dmgr` only).
- Each node (`node01`, `node02`) keeps its own local copy of configuration.
- The node agent on each node pulls updates from the master during synchronization.

**Console path:** `System administration → Nodes` → check both nodes → **Full Resync**.

> [!WARNING]
> **What happens without sync:**
> - The DataSource config exists only on the DMgr.
> - Cluster members never see it.
> - The app fails at runtime with:
>
> ```text
> javax.naming.NameNotFoundException: jdbc/DebitDS
> ```

> [!IMPORTANT]
> **Golden rule:** Every console change → resync affected nodes. Until sync completes, the change exists in only one place.

---

## Configuration Summary Diagram

```text
postgresql-42.7.3.jar  (the driver, on disk)
        │
        ▼
PostgreSQL XA JDBC Provider  (Provider: JAR path + class org.postgresql.xa.PGXADataSource)
        │
        ├──► DigiStack Debit XA DataSource   (JNDI: jdbc/DebitDS)
        │        • serverName 192.168.10.30
        │        • portNumber 5432
        │        • databaseName digistack_bank
        │        • auth: BankDS_Alias
        │
        └──► DigiStack Credit XA DataSource (JNDI: jdbc/CreditDS)
                 • same properties
                 • same auth alias

Cell scope   → visible to whole cluster
Full Resync  → pushed to node01 + node02
```

---

## Quick Recap Checklist

- [ ] Know the exact JAR filename (Step 1)
- [ ] XA Provider created with the right class (`PGXADataSource`)
- [ ] Provider class path points to a real file
- [ ] DataSource JNDI names match what the app expects
- [ ] Correct Provider selected for each DataSource
- [ ] All three auth alias fields set
- [ ] All three custom properties added to both DataSources, with correct types
- [ ] Connection tested green on both
- [ ] Full Resync done on both nodes

---
# Changes in DB server

Check the Transaction allowed
```
sudo -u postgres psql -d digistack_bank -c \
"SHOW max_prepared_transactions;"
```
output
```
 max_prepared_transactions
---------------------------
 0
```
Means 0 Transactions Allow, we need to change these Number to 10

```
sudo -u postgres psql -d digistack_bank -c \
"ALTER SYSTEM SET max_prepared_transactions = 10;"
```
Check the Transaction allowed
```
sudo -u postgres psql -d digistack_bank -c \
"SHOW max_prepared_transactions;"
```
output
```
 max_prepared_transactions
---------------------------
 10
```

## Restart the Postgresql Server
```
systemctl restart postgresql-16.service
```
