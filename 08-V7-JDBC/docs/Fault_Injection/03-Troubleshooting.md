# 🔍 INC-v7-001 — Root Cause Analysis & Remediation

> **One misconfigured field in one WAS configuration object explains
> every single symptom in the ticket.** The database is healthy, the
> application is deployed, the cluster is intact — none of that matters
> because WAS cannot authenticate to PostgreSQL.

---

## 🎯 Root Cause

**The JAAS Auth Alias `BankDS_Alias` contains the wrong password.**

---

## 🔗 Why One Wrong Password Causes All Seven Symptoms

### The Causal Chain

When your application calls:

```java
InitialContext.lookup("jdbc/BankDS")
ds.getConnection()
```

WAS does **not** pass credentials from your Java code — it reads them
from `BankDS_Alias` and uses them to authenticate to PostgreSQL when
opening a pooled connection. If those credentials are wrong, WAS cannot
open **any** connection at all. Every single feature that touches the
database fails immediately.

### Symptom-by-Symptom Explanation

| # | Symptom | Why the wrong alias password causes it |
|---|---------|----------------------------------------|
| 1 | Home page — DB Error, Status Unavailable | `HomeServlet.doGet()` calls `getConnection()` → JNDI lookup returns DataSource → `ds.getConnection()` attempts to borrow a pool connection → WAS tries to authenticate to PostgreSQL with `WrongPassword@999` → PostgreSQL rejects with `FATAL: password authentication failed` → `SQLException` caught → `dbConnStatus` stays "Error", default values used |
| 2 | Login — "A system error occurred" | `LoginServlet.doPost()` calls `getConnection()` → same authentication failure → `SQLException` caught in the outer try-catch → generic error message forwarded to `Login.jsp` → user cannot log in |
| 3 | Application shows Started in Admin Console | The application server JVM started cleanly — the EAR deployed fine, servlets initialized fine. WAS only attempts to authenticate to PostgreSQL when a connection is **actually requested**, not at startup. The green icon reflects JVM health, **not** DataSource health |
| 4 | Test Connection returns password authentication failed | Direct proof. WAS's Test Connection button attempts exactly what the application does — authenticate with `BankDS_Alias` credentials. PostgreSQL correctly rejects `WrongPassword@999` and returns SQLSTATE `28000` (invalid authorization specification) |
| 5 | PostgreSQL is healthy and accepts correct credentials | Correct — the database is not the problem. It is behaving exactly as it should: accepting correct credentials, rejecting wrong ones. The problem is entirely in WAS's credential store |
| 6 | Node Agents synchronized, topology intact | Node Sync has nothing to do with DataSource credential validity. The alias configuration was synchronized to both nodes correctly — **both nodes now have the wrong password synchronized to them** |
| 7 | `StaleConnectionException` in SystemOut.log | When WAS starts the application servers, it tries to establish the minimum pool connections (min=5 per member). Each attempt fails with an authentication error. WAS logs these as `StaleConnectionException` — a misleading label, but it is the WAS pool manager's way of saying "I tried to open this connection and it failed" |

### ⚠️ The Most Deceptive Aspect of This Fault

**Symptom 3 is the trap.**

- The Admin Console shows **green** — the application is "Started."
- A junior administrator looking at that screen concludes:
  *"WAS is fine, the problem must be in the database or the network."*
- But the database **IS fine** (Symptom 5).

This contradiction — **green application, broken database connectivity,
healthy database** — points precisely at the authentication layer
between them: **the JAAS Auth Alias.**

---

## 📖 What "Credential Management Tasks" Means in the Incident Ticket

This is the real-world scenario:

1. A DBA rotated the `digistack_app` PostgreSQL password as part of a
   **security compliance exercise**.
2. They updated the password in PostgreSQL directly.
3. They **did not tell the WAS administrator**.
4. The WAS administrator did **not** update `BankDS_Alias`.
5. The next application server restart loaded the stale credentials
   from the alias into the pool — and the pool could not authenticate.

> 💡 This is one of the **most common DataSource failures** in
> enterprise WAS environments. The fix is not technical complexity —
> it is **one field in one form**. But finding that field requires
> understanding the credential flow, which is exactly what v7 was
> designed to teach.

---

## 🕵️ Investigation Steps (What You Would Do If You Didn't Know)

### Step 1 — Read the exact SQL State from the Test Connection error

```
DSRA0010E: SQL State = 28000
```

SQLSTATE `28000` is the PostgreSQL code for **invalid authorization
specification** — wrong username or password.

This immediately tells you the problem is:

| SQL State / Error | Meaning |
|---|---|
| `28000` ✅ | **Authentication** — wrong username/password (our case) |
| `08001` | Connectivity — cannot reach the database |
| `3D000` | Missing database |
| `ClassNotFoundException` | Driver problem (not a SQL State at all) |

### Step 2 — Confirm PostgreSQL is reachable and the account exists

```bash
psql -h 192.168.10.30 -U digistack_app -d digistack_bank
```

Enter the known-correct password.

- ✅ If this succeeds — PostgreSQL is healthy and the user exists.
- The problem is not in PostgreSQL.
- The problem is in **what WAS is sending as the password**.

### Step 3 — Identify what WAS is sending

WAS sends the credentials from `BankDS_Alias`.

- There is no way to see the decrypted password from `security.xml`
  directly — WAS encrypts it.
- But you do **not** need to see it.
- The Test Connection result **proves** the wrong password is being sent.
- The alias is the **only place** WAS reads DataSource credentials from.

### Step 4 — Check the alias

```
Admin Console → Security → Global security
→ J2C authentication data → BankDS_Alias → open it
```

- You cannot see the stored password (it is masked).
- But you **can re-enter** the correct one.

---
