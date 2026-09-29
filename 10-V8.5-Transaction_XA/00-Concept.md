# XA Transactions & Two-Phase Commit on WebSphere Application Server — The Complete Beginner's Guide

**Author:** Ox Alpha — 25 years in WAS & banking systems

---

## Part 1: The Problem (Why Should You Care?)

### The Money Vanishing Story

Imagine a bank transfer:

1. **Debit** ₹1000 from Account A ✅
2. **Credit** ₹1000 to Account B ❌ *(server crashes!)*

**Result:** ₹1000 left A. Never arrived at B. **Money vanished.**

### Why Did This Happen?

- Each database was doing its **own** transaction.
- Step 1 already committed. **Committed = permanent.**
- Nothing can undo it after the crash.

> [!IMPORTANT]
> **Non-XA = each database commits alone. No teamwork. No safety net.**

---

## Part 2: The Solution — XA and 2PC

### What is XA?

- An **industry-standard protocol** for distributed transactions.
- One transaction spans **multiple databases**.
- Everyone succeeds **together** or fails **together**.

### The Two Roles

| Role | Who | Job |
|---|---|---|
| **Transaction Manager (TM)** | WAS itself | The boss. Coordinates everyone |
| **Resource Manager (RM)** | Each database | The worker. Does SQL, obeys the boss |

PostgreSQL becomes an RM via its driver: `PGXADataSource`.

### Two-Phase Commit (2PC) — The Protocol

#### Phase 1 — Prepare ("Do you agree?")

1. WAS asks every database: *"Can you commit? Get ready."*
2. Each DB writes changes to disk in a **prepared state** (survives a crash).
3. Each answers **YES** or **NO**.
4. Nothing final yet — just a **durable promise**.

#### Phase 2 — Commit / Rollback ("It's official")

- **ALL said YES** → WAS says `COMMIT` to everyone.
- **ANYONE said NO** → WAS says `ROLLBACK` to everyone.

> [!IMPORTANT]
> **All commit together, or none commit. No "half-done."**

### Worked Example

| Step | Action | Result |
|---|---|---|
| 1 | Prepare `DEBIT_DS` | "Yes, prepared" |
| 2 | Prepare `CREDIT_DS` | **"NO — out of disk space"** |
| 3 | WAS → `DEBIT_DS` | ****Result:** Money stays in Account A. Nothing lost. ✅

### What If WAS Crashes Mid-Protocol?

- Databases keep their **prepared records on disk**.
- WAS restarts → reads its transaction log (**tranlog**) → finishes the job (commit or rollback).
- This is why prepare records **MUST be durable** — that's the whole point.

---

## Part 3: EJB — Where the Transaction Lives

### What is an EJB?

- A **managed Java component** hosted by WAS.
- WAS controls its whole life: create, pool, destroy.

### `@Stateless` Bean

- Remembers **nothing** between calls.
- WAS keeps a **pool** of identical instances.
- Like taxis at a stand — any taxi works; none remembers your trip.

### The Key Idea

**The transaction boundary is the bean method.**

When a method is called:

1. WAS **starts a transaction** *before* it runs.
2. WAS **commits/rolls back** *after* it exits.
3. Your code does **nothing**. That's the beauty.

---

## Part 4: CMT — Container-Managed Transactions

### What is CMT?

The **container (WAS)** manages the transaction, not you:

```java
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public void transferFunds(...) { ... }
```

| Stage | WAS Action |
|---|---|
| **Before method** | Begin transaction |
| **During method** | Every DataSource touched gets enlisted as XA resource |
| **Clean exit** | Commit all via 2PC |
| **RuntimeException escapes** | Rollback all |

### Rules You MUST Obey

- ❌ Never call `connection.commit()`
- ❌ Never call `connection.rollback()`
- ❌ Never call `connection.setAutoCommit(...)`

> [!WARNING]
> **WAS owns the transaction. Touching it = grabbing the steering wheel while the driver drives.**

### The Exception Rule

| Exception | Default Behavior |
|---|---|
| **RuntimeException** | Automatic rollback |
| **Checked exception** | No rollback by default |

**Design tip:** Make business failures runtime exceptions:

```java
public class InsufficientFundsException extends RuntimeException { ... }
```

### Why `REQUIRED`?

- If a transaction already exists, **join it**.
- If not, **start one**.
- Safest choice — it's the default anyway.

---

## Part 5: Servlet → EJB (Not Servlet Alone)

### Servlet Alone = Manual BMT (Avoid)

```java
ut.begin();
try { /* work */ ut.commit(); }
catch (Exception e) { ut.rollback(); }
```

**Problems:** verbose, easy to forget rollback, easy to leak transactions. One mistake = inconsistent money.

### Servlet + EJB (Recommended)

- **Servlet** = thin entry point (traffic controller).
- **EJB** = transaction boss.

```java
@EJB
private FundsTransferBean transferBean;
```

No XML needed — `metadata-complete=false` enables annotation scanning.

### Full Flow

```
Browser → Servlet → EJB.transferFunds()  ← WAS starts TX
   ├── DEBIT_DS: debit A  (no commit!)
   ├── CREDIT_DS: credit B (no commit!)
   ↓ method exits cleanly
WAS runs 2PC: prepare both → commit both (or rollback both)
   ↓
"Transfer Successful"
```

> [!IMPORTANT]
> **No commit anywhere inside the method. Commits happen AFTER, coordinated by WAS.**

---

## Part 6: The Negative Control — Proving XA Really Works

### The Doubt

One test isn't enough. A sceptic says:

> "Maybe the debit just hadn't saved yet. That's timing, not transactions."

### The Fix: Run the Same Test WITHOUT XA

| Test | Connection | Autocommit | Result |
|---|---|---|---|
| **Test A (XA)** | XA DataSource | Managed by WAS | Debit rolled back ✅ |
| **Test B (Control)** | Plain DataSource | Autocommit ON | Debit stays permanently ❌ |

Same SQL. Same failure point. Only difference = transaction protocol.

### How Test B Works

- Method annotated `@TransactionAttribute(NOT_SUPPORTED)` → WAS **suspends** transactions.
- Non-XA connection + `setAutoCommit(true)` → every statement commits instantly.
- Failure after debit → debit survives. **Proof:** XA was what rolled it back in Test A.

### Clean Failure Injection

Use a **flag**, not corrupted data:

```java
// 1. Debit SQL runs fine
// 2. if (simulateCreditFail) throw new FundsTransferException(); // RuntimeException
// 3. Credit SQL never reached
```

One checkbox toggles failure. Same code path both tests. Clean comparison.

---

## Part 7: Watching 2PC With Your Own Eyes (Trace Logs)

### The Two Log Files

| File | What | Always On? |
|---|---|---|
| `SystemOut.log` | Short summaries (`WTRN` messages) | Yes |
| `trace.log` | Every internal decision, line by line | No — enable when debugging |

### The Trace String

```
*=info:com.ibm.ws.Transaction*=all
```

- `*=info` → everything else stays quiet.
- `com.ibm.ws.Transaction*=all` → transaction manager logs everything.

> [!TIP]
> **Analogy:** Keep the building quiet, but mic up the — Phase 1 vote request
3. **Vote** — `XA_OK` = yes
4. **Decision record** — written to tranlog
5. **Commit/Rollback** — Phase 2

### 1PC vs 2PC

| | 1PC (Last Agent) | 2PC |
|---|---|---|
| **Resources** | Exactly one | Two or more |
| **Prepare phase** | Skipped | Always done |
| **Speed** | Faster | Slower (more round trips) |

- One resource → no vote needed, just commit. *(Like choosing lunch alone.)*
- Two resources (our `DEBIT_DS` + `CREDIT_DS`) → full 2PC every time.

### The Tranlog — Insurance Policy

- **Durable files** written by WAS after all votes, **before any commit**.
- Crash after decision record? On restart, WAS reads it and re-delivers `Commit`.
- Without it: databases stuck **in-doubt** → corruption.

> [!TIP]
> **Analogy:** Pilot writes the flight plan before takeoff. Ground control reads it if contact is lost.

### The IHS Problem

IHS routes requests round-robin — you won't know which member your transaction hit.

**Fix:** bypass IHS, hit members directly:

| Member | Address |
|---|---|
| Member 1 | `192.168.10.10:9080` |
| Member 2 | `192.168.10.11:9081` |

> [!TIP]
> Don't call the switchboard — dial the employee's direct line.

---

## Part 8: Transaction Timeouts & Heuristic Outcomes

### The Three Timeouts

| Timeout | Property | Default | What It Does |
|---|---|---|---|
| **Total transaction lifetime** | `totalTranLifetimeTimeout` | 120 s | Wall-clock limit; fires → rollback-only + `RollbackException` |
| **Maximum transaction** | `maximumTransactionTimeout` | 300 s | Hard cap; silently clamps bigger `setTransactionTimeout(N)` values |
| **Client inactivity** | `clientInactivityTimeout` | — | Remote (IIOP) clients only; not triggered by local calls |

### Programmatic Timeout (BMT in Servlet)

```java
UserTransaction ut = (UserTransaction) new InitialContext()
    .lookup("java:comp/UserTransaction");
ut.setTransactionTimeout(30);  // MUST come before begin()
ut.begin();
// work...
ut.commit();
```

**Rules:**

- `setTransactionTimeout(N)` **before** `begin()`.
- Applies to **next transaction only**, on that thread.
- Silently clamped to `maximumTransactionTimeout`.
- No restart needed.

### Key WTRN Codes

| Code | Meaning |
|---|---|
| `WTRN0006E` | Transaction timed out → rolled back |
| `WTRN0014W` | Marked rollback-only (precursor to 0006E) |
| `WTRN0024W` | Heuristic outcome |
| `WTRN0025W` | XA resource completed heuristically |
| `WTRN0032W` | Heuristic logged — manual fix needed |

### Heuristic Outcome — The One Thing 2PC Can't Fix

A rare inconsistency in the tiny window between Phase 1 and Phase 2:

1. Both DBs vote **YES**. WAS writes the commit decision to tranlog.
2. WAS commits `DEBIT_DS`. ✅
3. **Before reaching `CREDIT_DS`:** WAS crashes — or a DBA manually commits/rolls back the prepared transaction.
4. WAS recovers, tries to commit `CREDIT_DS` → PostgreSQL says `XAER_NOTA` ("no such transaction").
5. Two databases now disagree. WAS **can't fix it automatically**.

**Consequences:**

- Logged as **heuristic**; a human must reconcile data, then forget the transaction.

> [!WARNING]
> **Heuristics = possible data inconsistency. Always reconcile manually before forgetting.**

### Why You Can't Easily Catch This Window

- Phase 1 → Phase 2 happens in **milliseconds**, right after the EJB method exits.
- To catch it: trace enabled, tailing live, kill command pre-staged.

> [!TIP]
> Set up all tooling **BEFORE** triggering the transaction. The window is too fast to react.

---

## Part 9: Memory Cards — The 10 Things to Never Forget

1. **Non-XA** = each DB commits alone → partial failure = lost money.
2. **XA** = WAS is the TM; databases are RMs.
3. **2PC** = Phase 1 prepare (durable promise) → Phase 2 commit all or rollback all.
4. **Stateless EJB** = pooled, no memory, any instance serves any call.
5. **CMT** = WAS begins/commits/rolls back. Never touch the connection's transaction.
6. **RuntimeException** out of an EJB method = rollback of everything enrolled.
7. **Negative control** = same test without XA → kills the "it was just timing" objection.
8. **Trace string** = `*=info:com.ibm.ws.Transaction*=all`; bypass IHS: direct `9080/9081`.
9. **1 resource = 1PC** (fast, no prepare). **2+ resources = 2PC** (vote, decide, commit).
10. **Tranlog before commit** = crash-safe recovery. **Heuristic** = the rare failure only humans can fix.

---

## One-Sentence Summary

> Put an `@Stateless` EJB with `@TransactionAttribute(REQUIRED)` between the Servlet and two XA DataSources, let WAS run 2PC, and a failure anywhere rolls back everywhere — the money is never lost.
