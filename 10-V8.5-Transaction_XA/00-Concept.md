# XA Distributed Transactions with EJB and Two-Phase Commit on WebSphere Application Server

## 1. Overview

This document explains why using **two plain (non-XA) DataSources** is dangerous for multi-database operations, and how **XA DataSources + Two-Phase Commit (2PC) + Container-Managed Transactions (CMT)** on a `@Stateless` EJB guarantee data consistency.

The running example is a **bank funds transfer**:

1. Debit ₹1000 from **Account A** (`DEBIT_DS`)
2. Credit ₹1000 to **Account B** (`CREDIT_DS`)

---

## 2. The Problem: Why "Two DataSources" Is Dangerous

### 2.1 The Failure Scenario

With plain `PGConnectionPoolDataSource` (non-XA) connections:

| Step | Action | Result |
|------|--------|--------|
| 1 | Debit Account A on `DEBIT_DS` → commit | Committed. **Permanent.** |
| 2 | Credit Account B on `CREDIT_DS` → server crashes | **Never happens.** |

**Outcome:** ₹1000 left Account A and never arrived at Account B. The money vanished.

### 2.2 Root Cause

Each database manages its **own** transaction. Once the first one commits, nothing can undo it. This is **data inconsistency**.

> [!IMPORTANT]
> **Non-XA = each database commits alone. No teamwork.**

---

## 3. The Solution: XA and Two-Phase Commit (2PC)

### 3.1 What XA Means

- **XA** = an industry-standard protocol (X/Open) for **distributed transactions**.
- One transaction spans **multiple databases**.
- A single **coordinator** makes all databases **succeed together or fail together**.

### 3.2 The Two Roles

| Role | Who It Is | Job |
|------|-----------|-----|
| **Transaction Manager (TM)** | WAS itself | Coordinates everyone |
| **Resource Manager (RM)** | Each database | Executes SQL, obeys the TM |

> [!NOTE]
> `PGXADataSource` is the driver class that lets PostgreSQL act as a **Resource Manager**.

### 3.3 Two-Phase Commit — The Protocol

#### Phase 1 — Prepare ("Do you agree?")

- WAS asks **every** database: *"Can you commit? Get everything ready."*
- Each database:
  - Writes its changes to disk in a **prepared state** (durable — survives a crash).
  - Answers **YES** or **NO**.
- No changes are final yet — just a **promise**.

#### Phase 2 — Commit / Rollback ("I now pronounce you...")

- If **ALL** said YES → WAS tells everyone: **COMMIT**.
- If **ANYONE** said NO (or went silent) → WAS tells everyone: **ROLLBACK**.

### 3.4 The Golden Guarantee

> [!IMPORTANT]
> **All commit together, or none commit. There is no "half-done."**

### 3.5 Worked Example

| Step | Action | Result |
|------|--------|--------|
| 1 | Phase 1: `DEBIT_DS` | "Yes, prepared." |
| 2 | Phase 1: `CREDIT_DS` | "NO — out of disk space." |
| 3 | Phase 2: WAS → `DEBIT_DS` | **ROLLBACK** |

**Result:** ₹1000 stays in Account A. Nothing is lost. Consistency saved.

### 3.6 Failure During the Protocol

- If WAS crashes **after Phase 1**, databases keep their **prepared records**.
- On restart, WAS reads the **transaction log** and tells each DB to commit or roll back.
- This is why prepare records **must** be written to disk — that is the whole point.

---

## 4. EJB: The Component That Holds the Transaction

### 4.1 What Is an EJB?

- **Enterprise Java Bean** = a managed Java component hosted by WAS.
- WAS controls the entire lifecycle: **creation, pooling, destruction**.
- Your code is a guest in WAS's house — **WAS makes the rules**.

### 4.2 Stateless Session Bean (`@Stateless`)

- **Stateless** = the bean remembers nothing between calls.
- WAS keeps a **pool** of identical instances.
- Call arrives → WAS grabs one from the pool → method runs → bean returns to pool.
- Like taxis at a stand: any taxi will do; none remembers your trip.

> [!TIP]
> Stateless beans are fast and scalable — one bean pool serves thousands of users.

### 4.3 The Key Idea

> **The transaction boundary is the bean method.**

When a method on the EJB is called, WAS:

1. Starts a transaction **before** the method runs.
2. Commits or rolls back **after** the method exits.

Your code does nothing — that's the beauty.

---

## 5. CMT: Container-Managed Transactions

### 5.1 What Is CMT?

The **container (WAS)** manages the transaction — not your code.

```java
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public void transferFunds(...) { ... }
```

WAS reads the annotation and does:

| Stage | WAS Action |
|-------|-----------|
| **Before method** | Begin a transaction |
| **During method** | Every DataSource touched gets **enlisted** as an XA resource |
| **Clean exit** | Commit all via **2PC** |
| **RuntimeException escapes** | Mark transaction **rollback-only** → Rollback all |

### 5.2 What You Must NOT Do in CMT

- ❌ Never call `connection.commit()`
- ❌ Never call `connection.rollback()`
- ❌ Never call `connection.setAutoCommit(...)`

> [!WARNING]
> WAS owns the transaction. Touching it is like driving the taxi while the driver drives.

### 5.3 The Exception Rule

| Exception Type | Default Behavior |
|----------------|------------------|
| `RuntimeException` out of the method | **Automatic rollback** of everything |
| Checked exception (e.g., `Exception`) | **No rollback** by default |

> [!TIP]
> Design business failures as runtime exceptions:

```java
public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(String message) {
        super(message);
    }
}
```

### 5.4 Why `REQUIRED`already exists**, join it.
- If not, **start one**.
- Safest, most common choice — the default for EJBs anyway.

---

## 6. Why Servlet → EJB (and Not Servlet Alone)

### 6.1 Option A: Servlet Does Everything (BMT — Avoid)

A plain Servlet cannot use CMT. It would need manual **BMT** (Bean-Managed Transactions):

```java
UserTransaction ut = ...;
ut.begin();
try {
   // debit
   // credit
   ut.commit();
} catch (Exception e) {
   ut.rollback();
}
```

**Drawbacks of BMT:**

- Verbose.
- Easy to forget `rollback()` in some code path.
- Easy to leak a transaction on an exception.
- One missed `try` block = **inconsistent money**.

### 6.2 Option B: Servlet Calls an EJB (Recommended)

- **Servlet** = thin entry point. Receives the request, calls the bean, shows the result.
- **EJB method** = the transaction boundary. One annotation does all the work.

```java
// In the Servlet — injection, same as the DataSource:
@EJB
private FundsTransferBean transferBean;
```

> [!NOTE]
> WAS injects the bean at deployment time — the same mechanism as `@Resource` for the DataSource. Because `web.xml` has `metadata-complete=false`, annotation scanning is active — **no XML entry needed** for `@EJB`.

> **Servlet = traffic controller. EJB = transaction boss.**

### 6.3 The Flow, End to End

```text
Browser
   ↓ HTTP POST /transfer
Servlet (receives amount, accounts)
   ↓ @EJB call
FundsTransferBean.transferFunds()   ← WAS starts TX here (CMT)
   ├── getConnection(DEBIT_DS)  → enrolled as XA resource
   │     debit Account A (no commit!)
   ├── getConnection(CREDIT_DS) → enrolled as XA resource
   │     credit Account B (no commit!)
   ↓ method returns cleanly
WAS runs 2PC:
   Phase 1 → both DBs "prepare" (write durable records)
   Phase 2 → both COMMIT   (or both ROLLBACK)
   ↓
Servlet shows "Transfer Successful"
```

> [!IMPORTANT]
> Inside the method, there is **no commit anywhere**. Commits happen **after** the method exits, coordinated by WAS.

---

## 7. `DEBIT_DS` and `CREDIT_DS`: Same DB, Two Resources

### 7.1 The Honest Truth

Both DataSources point to the same `digistack_bank` PostgreSQL instance. In a real bank, they would be **two different databases** (e.g., a debit ledger DB and a credit ledger DB).

### 7.2 Why This Is Still a Valid Exercise

- WAS does **not** care that they point to the same DB.
- It sees **two separate XA resources** and runs the full 2PC protocol on both.
- You can:

| Experiment | What You Observe |
|------------|------------------|
| Watch prepare/commit records | PostgreSQL logs and WAS transaction logs (`transaction.log`, trace on `TransactionImpl`) |
| Force a mid-transfer failure (`RuntimeException` after the debit) | **Both** resources roll back |
| Verify the outcome | No orphaned rows, no vanished money |

> [!NOTE]
> Same DB with two XA DataSources is a **teaching rig**. Production-grade XA across two real DBs behaves the same way — that is the point of learning it here.

---

## 8. Memory Cards

1. **Non-XA**: each DB commits alone → partial failure = inconsistent data.
2. **XA**: WAS is the Transaction Manager; DBs are Resource Managers.
3. **2PC**: Phase 1 = prepare (durable promise). Phase 2 = commit all or rollback all.
4. **Stateless EJB**: pooled, no memory, any instance serves any call.
5. **CMT**: annotation on the method; WAS begins/commits/rolls back — never your code.
6. **RuntimeException** out of an EJB method = rollback of all enrolled resources.
7. **Servlet can't do CMT** — it would need manual BMT (verbose, risky).
8. **`@EJB` injection** works because `metadata-complete=false` enables annotation scanning.
9. **Two DataSources, same DB**: still two XA resources; full 2PC runs; perfect for observing the protocol.

---

## 9. One-Sentence Summary

> Put an `@Stateless` EJB with `@TransactionAttribute(REQUIRED)` between the Servlet and the two XA DataSources, let WAS run 2PC, and a failure anywhere rolls back everywhere — **the money is never lost**.

---
# Proving XA Rollback Works — The Negative Control Method

> [!NOTE]
> **Author:** Ox Alpha — 25 years in WAS & banking systems
> **Audience:** Java EE / WebSphere developers validating distributed transactions

---

## 1. The Big Picture (Why Are We Even Doing This?)

Consider a bank transfer composed of two steps:

1** Account A by $100.
2. **Credit** Account B by $100.

If Step 1 succeeds and Step 2 fails, the customer loses $100. That is a disaster in banking.

**XA transactions** exist to prevent this. XA is a protocol that lets multiple databases participate in a single transaction. If anything fails, *everything* rolls back.

---

## 2. The Problem: One Test Is Not Enough

Suppose you run a single test:

1. Debit runs.
2. Credit fails.
3. You check the database.
4. The debit is gone — rolled back. ✅

Sounds like a win. But a sceptic (auditor, senior dev, examiner) will ask:

> "Maybe the debit just hadn't been saved yet when you looked. Timing, not transactions."

This is the key doubt. A single test cannot rule it out.

---

## 3. The Fix: The Negative Control

A **negative control** is a scientific idea:

> Run the same experiment *without* the thing you're testing, to prove the difference comes from that thing.

Our version:

| Test | Connection Type | Autocommit | Result |
|---|---|---|---|
| **Test A (XA)** | XA DataSource | Managed by transaction | Debit rolled back ✅ |
| **Test B (Negative Control)** | Plain (non-XA) DataSource | Autocommit ON | Debit stays permanently ❌ |

Side by side, they prove:

- Same SQL. Same failure point. Same database.
- The **only** difference is the transaction protocol.
- Therefore, the rollback must be caused by XA — **not timing**.

That is a complete proof.

---

## 4. Building the Negative Control: The Pieces

### 4.1 `@TransactionAttribute(NOT_SUPPORTED)` — The EJB Annotation

**What it does:**

- Tells WAS: *"Suspend any active transaction before this method runs. Resume it after."*
- A Servlet has no active transaction anyway.
- So the practical effect is simple:

> [!TIP]
> **`NOT_SUPPORTED` = "WAS, keep your transactions away from me."**

**Why we need it:**

- Connections from a non-XA DataSource inside this method are not enlisted in any transaction.
- They behave as plain autocommit JDBC connections.
- Perfect for our negative control.

### 4.2 `conn.setAutoCommit(true)` — Plain JDBC Behaviour

**Default JDBC rule:**

- With autocommit ON, every `executeUpdate()` commits immediately.
- No pending transaction. No rollback possible.

> [!NOTE]
> **Key sentence to remember:**
> *The moment `executeUpdate()` returns for the debit, that change is permanent — even if the JVM crashes a millisecond later.*

**Why this matters:**

This is exactly what we want in the negative control — proof that a debit **can survive a failure when XA is absent**.

### 4.3 `simulateCreditFail` Flag — Injecting Failure Cleanly

**The idea:**

- Don't "break" the credit account in the form (clumsy, easy to forget).
- Use a simple boolean flag instead:

```java
// Flow inside FundsTransferBean.transfer()

// 1. Run DEBIT SQL          ← executes fine
jdbcTemplateDebit.update("UPDATE account SET balance = balance - 100 WHERE id = 'A'");

// 2. Controlled failure point
if (simulateCreditFail) {
    throw new FundsTransferException(); // RuntimeException
}

// 3. Run CREDIT SQL         ← never reached
jdbcTemplateCredit.update("UPDATE account SET balance = balance + 100 WHERE id = 'B'");
```

**Why `RuntimeException`?**

- Under CMT (Container-Managed Transactions), a `RuntimeException` tells WAS:
  *"Mark the transaction rollback-only."*

**Result in the XA test:**

1. WAS marks the transaction **rollback-only**.
2. Both XA connections are rolled back.
3. The debit SQL physically ran — but it's rolled back as if it never happened.

> [!TIP]
> **Why a flag is better than corrupting data:**
>
> - One checkbox toggles failure on/off.
> - Same code path, same SQL, same timing.
> - Cleaner comparison between Test A and Test B.

---

## 5. The Full Test Scenario (Step by Step)

### Test A — XA Rollback (the positive proof)

1. Servlet calls `FundsTransferBean.transfer()` (CMT = `REQUIRED`).
2. Bean gets XA connections to two databases.
3. Both connections enlist in one WAS transaction.
4. Debit SQL runs.
5. `simulateCreditFail = true` → `RuntimeException` thrown.
6. WAS marks transaction **rollback-only**.
7. WAS rolls back **both** XA connections.
8. Check database: debit gone, credit absent. ✅

### Test B — Negative Control (the disproof of "timing")

1. Servlet calls a method annotated `@TransactionAttribute(NOT_SUPPORTED)`.
2. Bean gets a **non-XA** connection.
3. `conn.setAutoCommit(true)`.
4. Debit SQL runs → **committed instantly**.
5. `simulateCreditFail = true` → `RuntimeException` thrown.
6. Credit never runs.
7. Check database: debit is **still there, permanently**. ❌

---

## 6. Reading the Results

| Question | Test A (XA) | Test B (Non-XA) |
|---|---|---|
| Did the debit SQL run? | Yes | Yes |
| Did the credit fail? | Yes | Yes |
| Is the debit in the DB afterwards? | **No — rolled back** | **Yes — committed instantly** |
| Why? | XA transaction protocol | Autocommit, no transaction |

**Conclusion a sceptic must accept:**

- Both tests ran the same debit, at the same point, **before** the failure.
- Only the XA test removed it afterwards.
- Therefore XA **did** roll back a fully-executed SQL statement.
- The *"it just hadn't happened yet"* objection is dead.

---

## 7. Memory Summary (Cheat Sheet)

- **Negative control** = same test without XA, to rule out "timing" doubts.
- **`NOT_SUPPORTED`** = suspend transactions → non-XA connection behaves as plain autocommit JDBC.
- **Autocommit ON** = every statement commits instantly; rollback impossible.
- **`simulateCreditFail` flag** = clean, controlled failure injection after debit, before credit.
- **`RuntimeException` under CMT `REQUIRED`** = WAS marks rollback-only → both XA branches roll back.
- **Side-by-side results** = proof that the difference is the transaction protocol, not timing.

---
# Sprint 3: Seeing the 2PC Protocol with Your Own Eyes

> [!NOTE]
> This guide is part of a hands-on WAS (WebSphere Application Server) learning series. Sprint 2 proved rollback worked by inspecting the database. Sprint 3 goes deeper: we make the **mechanism** visible by reading WAS transaction trace logs.

---

## 1. Purpose of This Sprint

- Sprint 2 showed the **result** of rollback by looking at the database.
- Sprint 3 answers the question: **"What exactly does WAS do, step by step, when it commits or rolls back a transaction?"**
- The answer lives inside WAS's **trace logs**. We enable tracing, run the transfer scenario, and read the protocol like a story.

> [!TIP]
> Analogy: Sprint 2 showed you the car arrived. Sprint 3 installs a dashcam so you can watch the entire journey.

---

## 2. WAS Log Files — The Two Documents Every Server Keeps

Every WAS server process writes two log files:

| File | Contents | Always On? |
|------|----------|------------|
| `SystemOut.log` | Short informational messages (prefix codes like `WTRN`, `WSVR`) | Yes |
| `trace.log` | Extremely detailed, line-by-line internal activity | No — off by default |

- **`SystemOut.log`** — the receptionist's summary: *"Transaction committed."* Done.
- **`trace.log`** — the engineer's notebook: every single decision, written down.

### Why trace is off by default

- It is very verbose.
- It adds slight overhead.
- You enable it only when debugging.

---

## 3. The Trace Specification String

Apply this trace string to a server:

```properties
*=info:com.ibm.ws.Transaction*=all
```

The string has two parts, separated by a colon:

| Part | Meaning |
|------|---------|
| `*=info` | `*` = all loggers; `=info` = log everything else at normal info level |
| `com.ibm.ws.Transaction*=all` | The Transaction Manager logger family; `*` wildcard includes sub-loggers; `=all` = log every severity (`finest`, `finer`, `fine`, `config`, `info`, `warning`, `severe`) |

**Result:** every internal transaction decision is written to `trace.log`, while the rest of the system stays quiet.

> [!TIP]
> Plain English: *"Keep the whole building quiet at normal volume, but put a microphone in the transaction manager's office and record every word."*

---

## 4. What the Transaction Manager Writes

With tracing enabled, you will see the Transaction Manager record each protocol step:

1. **Enlist** — a resource (database connection) joins the transaction.
2. **Prepare** — Phase 1 vote request sent; the resource votes.
3. **Vote** — the resource's answer (`XA_OK` = yes, I can commit).
4. **Decision record** — WAS writes the final decision to the tranlog.
5. **Commit / Rollback** — Phase 2 instructions sent to every resource.

You will literally read the protocol execution, line by line. No more guessing.

---

## 5. 1PC vs 2PC — The Critical Difference

### Comparison

| Aspect | 1PC (Last Agent Optimisation) | 2PC (Two-Phase Commit) |
|--------|-------------------------------|------------------------|
| Trigger condition | Exactly **one** XA resource enlisted | **Two or more** XA resources enlisted |
| Prepare phase | Skipped entirely | Always performed |
| Prepare record in tranlog | None | Yes |
| Speed | Faster | Slower (more round trips) |
| Coordination needed | None — nothing to coordinate | Full coordination across resources |

### 1PC — One-Phase Commit (Last Agent Optimisation)

- Only one XA resource is in the transaction.
- WAS skips **Prepare** and sends **Commit** directly.
- No prepare record is written to the tranlog.
- Faster — but only safe with one resource.

> [!TIP]
> Analogy: You're the only person deciding where to eat lunch. You just decide. No vote needed.

### 2PC — Two-Phase Commit

- Triggered when **two or more** XA resources are enlisted — exactly what `FundsTransferBean` does.

The protocol:

1. **Phase 1 — Prepare:** WAS sends `Prepare` to every resource.
2. **Vote:** Each resource replies. All must vote `XA_OK` (*yes, I can commit*).
3. **Decision record:** Only after all yes-votes, WAS writes a durable decision record to the tranlog.
4. **Phase 2 — Commit:** WAS sends `Commit` to all resources.

> [!TIP]
> Analogy: A wedding. Phase 1: the officiant asks both the bride and groom *"do you agree?"* Both must say yes. Only then is the marriage certificate (decision record) signed. Phase 2: everyone is told *"it's done."*

### Why this matters for us

- `DEBIT_DS` and `CREDIT_DS` are two **separately-enlisted** XA connections.
- Two resources → WAS uses **full 2PC every time**. No shortcuts.

---

## 6. The Transaction Log (Tranlog) — The Insurance Policy

- The tranlog is a pair of **durable files** written by WAS's Transaction Manager.
- **When is it written?** After all Phase 1 votes are `XA_OK`, **before** any Phase 2 Commit is sent.

### Why this ordering saves you

Imagine the server crashes right after writing the decision record but before sending Commit:

1. Server restarts.
2. WAS reads the tranlog.
3. It finds the decision record: *"this transaction was committed."*
4. WAS **re-delivers** Commit to all participants.
5. Databases end up **consistent**.

> [!WARNING]
> Without the tranlog, WAS would have no idea what the outcome was. Databases would be stuck **in-doubt** — one committed, one didn't, and nobody knows which. The tranlog is the difference between *"crash and recover"* and *"crash and corrupt."*

> [!TIP]
> Analogy: The pilot writes the flight plan before takeoff. If the plane loses contact, ground control reads the plan and knows exactly what was supposed to happen.

---

## 7. The IHS Problem — Which Server Did My Request Hit?

- IHS (the web server in front of WAS) routes requests **round-robin** across both cluster members.
- Problem: your trace could be on either member — you won't know which.
- Solution: **bypass IHS** and hit each member's own HTTP port directly.

| Member | Host | Direct Port |
|--------|------|-------------|
| Member 1 (`dsb-dmgr/node01`) | `192.168.10.10` | `9080` |
| Member 2 (`dsb-node02/node02`) | `192.168.10.11` | `9081` |

- Want trace on **member 1**? Send your transfer request to `192.168.10.10:9080`.
- Want trace on **member 2**? Use `192.168.10.11:9081`.
- IHS never sees the request, so there is **no ambiguity** about where the trace lives.

> [!TIP]
> Analogy: Don't call the company switchboard (IHS) — dial the employee's direct line.

---

## 8. Memory Hooks — Remember These Five Things

1. **Two logs, two jobs:** `SystemOut.log` = summary. `trace.log` = full story.
2. **Trace string:** `*=info:com.ibm.ws.Transaction*=all` — quiet everywhere, loud in the transaction manager.
3. **1 resource = 1PC** (fast, no prepare). **2+ resources = 2PC** (prepare, vote, decide, commit).
4. **Decision record before commit** — the tranlog is written before Phase 2, so a crash can be replayed safely.
5. **Bypass IHS to target a member:** `9080` = member 1, `9081` = member 2.
---
# WAS Transaction Timeout Types & Heuristic Outcomes (NDS01 Rule 3)

## Overview

This document describes the three WebSphere Application Server (WAS) transaction timeout types, how programmatic timeouts work in Bean-Managed Transactions (BMT), the key WTRN log codes, and the mechanics of a heuristic outcome.

---

## Transaction Timeout Types

### 1. Total Transaction Lifetime Timeout (`totalTranLifetimeTimeout`)

The maximum wall-clock time from when a transaction begins to when it must complete — either committed or rolled back.

- **Default:** `120` seconds
- This is the **primary server-level guard** against runaway transactions.
- When it fires, WAS marks the transaction **rollback-only** and throws `RollbackException` to the application code at the next commit attempt.
- The `WTRN` message is written to `SystemOut.log`.

### 2. Maximum Transaction Timeout (`maximumTransactionTimeout`)

A hard ceiling that caps any programmatic timeout.

- **Default:** `300` seconds
- When code calls `UserTransaction.setTransactionTimeout(N)`, WAS **silently clamps** `N` to this value if `N` is larger.
- Prevents application developers from setting arbitrarily long timeouts that bypass the server-level guard.

### 3. Client Inactivity Timeout (`clientInactivityTimeout`)

The maximum time a transaction opened by a **remote client** (over IIOP — the inter-process protocol WAS uses for EJB remote calls) can remain inactive before WAS rolls it back.

- **Not triggered** by local EJB calls.
- Included here for completeness; the lab test uses the total lifetime timeout.

### Comparison Table

| Timeout | Property | Default | Scope | Triggered By |
|---|---|---|---|---|
| Total transaction lifetime | `totalTranLifetimeTimeout` | 120 s | Server-wide | Any transaction exceeding wall-clock limit |
| Maximum transaction | `maximumTransactionTimeout` | 300 s | Server-wide cap | Programmatic `setTransactionTimeout()` exceeding cap |
| Client inactivity | `clientInactivityTimeout` | — | Remote (IIOP) clients | Inactivity on remotely-initiated transactions |

---

## How Programmatic Timeout Works

Inside a Servlet, you can obtain a `javax.transaction.UserTransaction` from JNDI and manage a transaction manually — this is called **BMT (Bean-Managed Transaction)** in a Servlet context.

```java
javax.transaction.UserTransaction ut =
    (javax.transaction.UserTransaction) new InitialContext()
        .lookup("java:comp/UserTransaction");

ut.setTransactionTimeout(30); // must precede begin()
ut.begin();
// ... work ...
ut.commit();
```

### Key Rules

- `ut.setTransactionTimeout(N)` must be called **before** `ut.begin()`.
- The timeout applies to the **next transaction only**, scoped to that thread.
- It **cannot exceed** `maximumTransactionTimeout` — larger values are silently clamped.
- **No server restart is needed** — it applies per-transaction at runtime.

> [!NOTE]
> If the total lifetime timeout fires before your programmatic timeout, WAS marks the transaction rollback-only and `ut.commit()` throws `javax.transaction.RollbackException`.

---

## Key WTRN Codes

| Code | Meaning |
|---|---|
| `WTRN0006E` | Transaction timed out and was rolled back |
| `WTRN0014W` | Transaction marked rollback-only (precursor to `WTRN0006E`) |
| `WTRN0024W` | Transaction completed with a heuristic outcome |
| `WTRN0025W` | XA resource completed heuristically (one side committed or rolled back independently) |
| `WTRN0032W` | Heuristic outcome logged — manual resolution required |

---

## Heuristic Outcome Explained

A **heuristic outcome** is a consistency failure that can occur in the window between **Phase 1 (Prepare)** and **Phase 2 (Commit/Rollback)** of the two-phase commit protocol.

### Failure Sequence

1. **Phase 1 completes** — both XA resources vote `XA_OK` (prepared). WAS writes its commit decision to the tranlog.
2. **Phase 2 begins** — WAS sends `XAResource.commit()` to resource 1 (`DEBIT_DS`). It commits.
3. **The window** — before WAS can reach resource 2 (`CREDIT_DS`), one of the following occurs:
   - The server is killed, **or**
   - The DBA manually commits or rolls back the prepared transaction directly in the database.
4. **Recovery attempt** — WAS restarts, reads the tranlog, determines the decision was `Commit`, and tries to commit resource 2. But the database reports the transaction is already gone (PostgreSQL returns `XAER_NOTA` — "no such transaction").
5. **Inconsistency** — WAS cannot achieve a globally consistent outcome: `DEBIT_DS` committed, `CREDIT_DS` either also committed or was rolled back by human intervention, and the two sides may now disagree.

### Consequences

- WAS logs the heuristic outcome and requires a **human administrator** to acknowledge it (**forget**) and manually fix any data inconsistency.
- It is **not automatically recoverable**.

> [!WARNING]
> Heuristic outcomes indicate possible data inconsistency between resources. Always reconcile the participating databases manually before forgetting the heuristic transaction.

---

## Why Phase 1 → Phase 2 Timing Is Tight

WAS runs Phase 1 and Phase 2 inside its own commit code path, **immediately after the EJB method exits**. This happens in **milliseconds** under normal conditions.

### To Catch This Window Manually, You Need:

- Transaction trace enabled
- The trace output tailed live
- The `kill` command pre-staged

> [!TIP]
> Pre-stage all tooling (trace tail + kill command) **before** triggering the transaction. The prepare-to-commit window is far too short to set up tooling reactively.
 