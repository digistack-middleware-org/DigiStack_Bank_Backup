# JDBC Provider for PostgreSQL
---

## 1. The Problem (How it was before)

Before v7, your Java code looked like this:

```
DriverManager.getConnection(
    "jdbc:postgresql://192.168.10.30:5432/digistack_bank",
    "digistack_app",
    "Wasadmin@951951");
```

Three big problems:

- **Hardcoded URL** → If the database moves to a new IP, you must **change code and redeploy** the whole application.
- **Hardcoded password** → The password sits in the source code AND inside the compiled `.class` in the EAR. Anyone who opens the EAR file can read it.
- **No connection pooling** → Every single request:
  - Opens a new TCP connection to PostgreSQL
  - Does the handshake and login
  - Uses it once
  - Closes it

**Real-life example:** Imagine hiring a new taxi driver, signing his contract, and firing him — for every single trip. Expensive and slow. Pooling keeps drivers (connections) ready and reuses them.

---

## 2. The Solution: JDBC Provider + DataSource (Two Layers)

WAS fixes this in **two steps** (two sprints):

| Layer | Sprint | What it does | Simple meaning |
|---|---|---|---|
| **JDBC Provider** | Sprint 1 (this one) Tells WAS **which driver to use and where the JAR is** | "WAS, here is the PostgreSQL driver software" |
| **DataSource + JAAS Alias** | Sprint 2 | A **named, pooled** connection in JNDI | "W, here is how to connect, with what password, keeping N connections ready" |

**Real-life example:**
- JDBC Provider = installing the printer driver on your laptop
- DataSource = the printer shortcut you click to actually print

You **cannot** create a DataSource without a Provider. Provider comes first. That's why this is Sprint 1.

---

## 3. What is a JDBC Driver?

- A **JAR file** (like `postgresql-42.x.x.jar`) that contains code.
- This code knows **how to talk to PostgreSQL** — its language, its protocol.
- Without the driver, WAS has no idea how to speak to PostgreSQL.

**Real-life example:** The driver is like a translator between Java and PostgreSQL. WAS speaks Java. PostgreSQL speaks its own protocol. The driver translates.

** job in Sprint 1:** Tell WAS:
1. "Use the PostgreSQL driver" and
2. "The JAR is at this path" (on **both nodes**)

---

## 4. What is JNDI? (Quick intro — full use comes in Sprint 2)

- **JNDI = Java Naming and Directory Interface**
- It's a **phone book / registry** inside WAS.
- Your code looks up objects **by name** instead of building them.

**Old way:** "Connect to this IP with this password."
**JNDI way:** "Look up `jdbc/BankDS` and give me a pooled connection."

- WAS keeps the registry.
- Your application code **never sees the credentials**.
- Passwords change? Update it in WAS admin — **no code change, no redeploy**.

---

## 5. Scope: Cell vs Node Server

WAS ND has a hierarchy:

```
Cell (devdsbincell01)          <- the whole managed group
 |-- Node (devdsbinnode01)     <- one host
 |-- Node (devdsbinnode02)     <- another host
 |      `-- Server (your app server)  <- one JVM
```

| Scope | Applies to | When to use |
||---||
| **Cell** | Every node + every server in the cell | Shared infrastructure — **always right for clusters** |
| **Node** | One node only | Rare cases, node-specific stuff |
| **Server** | One server only | Almost never for JDBC |

**Why Cell scope?- Create it **once**, both nodes **inherit it automatically**.
- Node scope = create and maintain it **twice** (once per node).
- Double maintenance = double chance of mistakes.

**Real-life example:** Cell scope is like posting one company-wide memo. Node scope is like emailing the same memo separately to each office — someone will get a different version.

**Rule to remember:** *Shared across the cluster? Use Cell scope.*

---
# Transactions — Explained Simply

*(By Ox Alpha)*

## 1. What Is a Transaction?

- A **transaction = a unit of work**.
- One rule: **all of it happens, or none of it happens.**
- No half-done states allowed.

**Real-life example:**
- ATM transfer of $50 to a friend:
  - Step 1: money leaves your account.
  - Step 2: money enters theirs.
- If the machine crashes between steps → your money must **not vanish**.
- Bank promise: **both steps or nothing.**

---

## 2. The Bank Withdrawal Pattern (2 Steps)

1. **Check** the balance (enough money?).
2. **Deduct** the balance.

- If step 2 fails after step 1 passed → balance must stay **unchanged**.
- This is **"rollback" — undo everything**.

**Real-life example:**
- Vending machine eats your coin but no snack drops → machine must give the coin back.

---

## 3. What Is autoCommit?

- Your app gets a pooled connection from `jdbc/BankDS`.
- WAS gives it with **autoCommit = true**.
- Meaning:
  - **Every SQL statement = its own tiny transaction.**
  - Success → **committed instantly**.
  - Failure → **rolled back instantly**.
- You write **no commit/rollback code**. It's automatic.

**Real-life example:**
- Vending machine: press one button → one snack drops. Each press is complete on its own. No "half a snack" possible.

---

## 4. Why autoCommit=true Is Safe in v7 (Current Version)

Every operation does **exactly**:

| Operation | Touches |
|-----------|---------|
| Deposit | 1 row in `accounts` |
| Withdraw | 1 row in `accounts` |
| Freeze | 1 row in `accounts` |
| Unfreeze | 1 row in `accounts` |

- **One statement + one row + one DataSource.**
- PostgreSQL guarantees: a **single statement is atomic by itself**.
- So: 1 statement + autoCommit = **1 complete transaction**.
- **Result: v7 is safe with zero extra code.**

### Rollback Proof (Sprint 5 test)
- Deliberately try an over-limit Withdraw.
- Exception thrown **before** the SQL ran.
- SELECT before and after → balance **unchanged**. ✅

---

## 5. When autoCommit STOPS Being Enough

### P02 v15 — Fund Transfer
- One user action touches **TWO accounts**:
  1. Debit the source.
  2. Credit the destination.
- With autoCommit=true, each statement commits **separately**.
- **Danger:**
  - Debit succeeds, credit fails → money **disappears**.
  - Credit succeeds, debit fails → money **created from nothing**. 😱

### The Fix: Explicit Transaction Boundaries

```java
conn.setAutoCommit(false);   // take manual control
// ... do BOTH updates ...
conn.commit();               // both succeed → save both
conn.rollback();             // anything fails → undo both
```

**Real-life example:**
- Paying at a shop by card: the **charge** and the **receipt** must both happen.
- If only the charge happens → you paid and got nothing.

---

## 6.03 — Two Databases (PostgreSQL + Oracle)

- Core Banking Migration writes to:
  - PostgreSQL (legacy system)
  - Oracle 21c XE (new CBS)
- **Two DataSources = distributed transaction territory.**

---

## 7. What Is 2PC / XA?

- **2PC = Two-Phase Commit.**
- **XA = the Java/JTA interface for it.**

How it works:
1. A **coordinator** asks all databases: "PREPARE — can you commit?"
2. If **all say yes** → coordinator says: "COMMIT — all at once."
3. If **anyone says no** → everyone aborts.

**Real-life example:**
- A wedding: coordinator asks both bride and groom "Do you?" — only if **both say yes** does anyone get "married."

### Why P03 AVOIDS 2PC:
1. Not all DataSources support XA.
2. Locks are held across **network round-trips** → latency risk.
3. Coordinator crash mid-commit → everyone **stuck waiting** (blocking).
4. Better option exists: **Saga pattern**.

---

## 8. The Saga Pattern (The Chosen Way)

- Instead of one big transaction → a **chain of small local transactions**.
- Each step has:
  - **Idempotency key** → safe to retry, never applies twice.
  - **Compensating transaction** → an "undo" step if a later step fails.

**Real-life example:**
- Booking a flight + hotel:
  - Book flight ✅ → hotel fails ❌ → **cancel the flight** (compensation).
  - You never end up with a flight and no hotel, unpaid.

---

## 9. Key Rules to Remember

- ✅ v7: one statement, one row, one DataSource → **autoCommit=true is enough**.
- ⚠️ v15 (Transfer): two rows in one action → **need setAutoCommit(false) + commit/rollback**.
- 🚫 P03 (two databases): **skip 2PC**, use **Saga + compensating transactions**.
- 📌 This traceability note is the **anchor** for all future transaction decisions.

**One-line summary:**
> *Small work = auto commit. Big work = manual commit. Cross-system work = Saga, not 2PC.*
