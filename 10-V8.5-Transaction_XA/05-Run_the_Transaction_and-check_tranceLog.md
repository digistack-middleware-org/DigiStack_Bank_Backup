# XA Distributed Transactions & Two-Phase Commit (2PC) in WebSphere Application Server

**Audience:** WebSphere administrators and developers
**Scope:** Theory of ACID/XA/2PC, commit-style comparison (0PC / 1PC / 2PC), and hands-on trace verification on WebSphere Application Server (WAS)
**Environment reference:** WAS cell with node `devdsbinnode01`, DMGR host `192.168.10.10`, app `digistack-bank`

---

## 1. Purpose

This document explains how WebSphere Application Server (WAS) coordinates **distributed transactions** across multiple databases using the **XA protocol** and **Two-Phase Commit (2PC)**, and provides a repeatable procedure to capture and verify 2PC evidence in the server trace log.

---

## 2. Background Concepts

### 2.1 The Atomicity Problem

A bank transfer from Account 1001 to Account 1002 consists of two operations:

1. **Debit** Rs.500 from Account 1001
2. **Credit** Rs.500 into Account 1002

If a crash, network failure, or bug interrupts the sequence:

| Failure Scenario | Result |
|---|---|
| Debit OK, credit FAILED | Rs.500 vanishes |
| Credit OK, debit FAILED | Rs.500 created from nothing |

A **transaction** guarantees that both steps succeed or **both fail** — never half-done.

### 2.2 ACID Properties

| Letter | Property | Meaning |
|---|---|---|
| **A** | Atomicity | All operations succeed, or none do |
| **C** | Consistency | System invariants are never violated |
| **I** | Isolation | Concurrent transactions never observe partial state |
| **D** | Durability | Once committed, data survives crashes |

### 2.3 Distributed Transactions

When the debit and credit target **two different databases**, each database can manage its own local transactions, but **neither can coordinate with the other**. Work spanning more than one resource requires an external coordinator.

### 2.4 The Players

| Term | Identity | Role |
|---|---|---|
| **TM** — Transaction Manager | WAS itself | Coordinates the transaction (the "referee") |
| **RM** — Resource Manager | Each database | Executes the actual work |
| **XAResource** | Java handle per RM | Channel the TM uses to issue XA commands to each RM |
| **Tranlog** | WAS recovery log on disk | Persistent record of the transaction decision |

```
        [ WAS = TM (coordinator) ]
          /            \
   XAResource      XAResource
        /                  \
 [DB #1 = RM]         [DB #2 = RM]
  (debit DS)           (credit DS)
```

### 2.5 What Is XA?

**XA** is the industry-standard protocol defining the handshake between the Transaction Manager and Resource Managers.

| TM Command | RM Response | Meaning |
|---|---|---|
| `prepare` | `XA_OK` (0) | "Ready — my work is durably stored." |
| `prepare` | Error code | "I cannot proceed — abort." |
| `commit` | Done | "Change is permanent." |
| `rollback` | Undone | "All changes erased." |

> [!NOTE]
> Without a common protocol such as XA, databases from different vendors (DB2, PostgreSQL, Oracle, etc.) could never participate in a single atomic transaction.

---

## 3. Two-Phase Commit (2PC)

### 3.1 Phase 1 — Prepare (the vote)

The TM asks every participating resource: *"Are you ready to commit?"*

- Each RM answers **Yes (`XA_OK`)** or **No**.
- Nothing is final yet — this is only a vote.

### 3.2 Phase 2 — Commit (the action)

- **All resources voted Yes** → TM issues `commit` to each. Irreversible.
- **Any resource voted No** → TM issues `rollback` to all.

> [!IMPORTANT]
> Once Phase 2 begins, there is no undo. Phase 1 exists as the safety check before the point of no return.

### 3.3 Crash Safety and the Tranlog

If WAS crashes **between Phase 1 and Phase 2**:

1. On restart, WAS reads the **tranlog**.
2. It finds the recorded decision (e.g., "commit").
3. It replays and completes the outcome — no resource is left half-committed.

> [!TIP]
> Analogy: the officiant writes "WEDDING APPROVED" in the official register *before* announcing it. If the officiant faints mid-announcement, the register proves what was decided. The tranlog must always be written **before** Phase 2 begins.

---

## 4. The Six Trace Markers

A successful 2PC transaction appears in `trace.log` in this exact order:

| # | Marker | Trace Content | Meaning |
|---|---|---|---|
| 1 | **BEGIN** | `TranManagerImpl ... transaction begin` | Transaction starts |
| 2 | **ENLIST ×2** | `XAResource enlist` (twice) | Each DB signs up (automatic on `getConnection()`) |
| 3 | **PREP ×2** | `XAResource.prepare → XA_OK` (twice) | Phase 1: both DBs vote yes |
| 4 | **LOG** | `tranlog / TransactionLog / PartnerLog` | Decision written to disk |
| 5 | **COMMIT ×2** | `XAResource.commit` (twice) | Phase 2: commit issued to each DB |
| 6 | **DONE** | `transaction complete / committed` | Transaction finished |

**Memory aid:**

```
BEGIN → ENLIST → PREP → LOG → COMMIT → DONE
  (1)     (2)     (2)   (1)    (2)     (1)
```

---

## 5. Commit Styles: 0PC vs 1PC vs 2PC

### 5.1 0PC — Non-XA Resource ("Referee was never invited")

A transaction using only a **non-XA DataSource** (e.g., `PGConnectionPoolDataSource`):

- The database never signed the XA contract — it cannot answer `prepare`/`commit`.
- WAS does not enlist it.
- The JDBC driver performs its own plain `commit()`, invisible to XA tracing.

**Trace signature:** no `prepare`, no `commit`, no tranlog record.

### 5.2 1PC — Single Resource

If a transaction uses **only one resource**, no vote is needed:

- One `XAResource.enlist`
- One `XAResource.commit`
- No `prepare`, no tranlog decision record

### 5.3 LAO — Last Agent Optimization

When a transaction mixes **one XA + one non-XA** resource:

- WAS commits the **non-XA resource last, directly** (1PC behavior).
- The XA resource receives the full protocol.

> [!NOTE]
> LAO is faster but slightly less safe: a failure at exactly the wrong moment on the non-XA resource can cause divergent outcomes. Hence "optimization."

### 5.4 2PC — Full Protocol

Two or more **XA DataSources** in one transaction → WAS performs full 2PC, always.

> [!NOTE]
> Even if both DataSources point at the same physical database, two separate connections = two resources = 2PC.

### 5.5 Master Comparison Table

| Scenario | Resources | Prepare? | Tranlog Decision? | Commit Style |
|---|---|---|---|---|
| **0PC** (Deposit) | 1 non-XA | ❌ | ❌ | DB's own `commit()` |
| **1PC / LAO** | 1 XA (or 1 XA + 1 non-XA last) | ❌ | ❌ | Single `XAResource.commit` |
| **2PC** (Transfer) | 2+ XA | ✅ | ✅ | `prepare` → decision → `commit` |

---

## 6. Hands-On Verification Procedure

### 6.1 Target the Correct Server

| Port | Role | Behavior |
|---|---|---|
| **80** (IHS) | Front door with load balancing | Request may land on any cluster member |
| **9080** | Member 1's direct HTTP port | Bypasses IHS — hits member 1 only |

> [!IMPORTANT]
> Always use port **9080** for tests. Via IHS (port 80), the request may be routed to a different server and your `trace.log` will remain empty.

Test URLs:

```
http://192.168.10.10:9080/digistack-bank/Deposit
http://192.168.10.10:9080/digistack-bank/Transfer
```

### 6.2 Capture a Baseline Line Count

SSH to the DMGR host and record the current trace log length:

```bash
ssh wasadmin@192.168.10.10

wc -l /apps/IBM/WebSphere/AppServer/profiles/devdsbinnode01/logs/<server1name>/trace.log
```

Example output:

```
4821  .../trace.log
```

Record `4821` as the baseline.

> [!NOTE]
> If `trace.log` does not exist yet (normal on a fresh setup), the file is created on first trace output. Verify with:
>
> ```bash
> ls -la /apps/IBM/WebSphere/AppServer/profiles/devdsbinnode01/logs/<server1name>/
> ```
>
> - File absent → baseline = `0`
> - File present → use the `wc -l` value

### 6.3 Run the Test

Execute the test in the browser (Deposit or Transfer page).

### 6.4 Extract Only New Lines

Read only lines **after** the baseline:

```bash
tail -n +<baseline+1> /apps/IBM/WebSphere/AppServer/profiles/devdsbinnode01/logs/<server1name>/trace.log
```

| Command Piece | Meaning |
|---|---|
| `tail -n +4822` | Print from line 4822 onward (only fresh lines) |
| `grep -E "..."` | Keep only transaction-related lines |
| `--color` | Highlight matches |
| `-A2` | Show 2 lines after each match (return codes often live on the next line) |
| `> /tmp/file.txt` | Save evidence to a file |

Full evidence-capture command:

```bash
tail -n +4822 /apps/IBM/WebSphere/AppServer/profiles/devdsbinnode01/logs/<server1name>/trace.log \
  | grep -E -A2 "XAResource\.(prepare|commit)|XA_OK|prepare returned|commit returned|tranlog|TransactionLog|PartnerLog|enlist|tran.*begin|tran.*complet" \
  > /tmp/xa-2pc-trace-capture.txt

cat /tmp/xa-2pc-trace-capture.txt
```

### 6.5 Test A — Deposit (expect silence)

1. Capture baseline line count.
2. Browse to `http://192.168.10.10:9080/digistack-bank/Deposit`.
3. Deposit any amount into any account.
4. Grep the new lines for `XAResource|prepare|commit|enlist`.

**Expected result: EMPTY output.**

> [!TIP]
> Empty grep output is a **positive result** here — it proves a single non-XA connection performed its own commit (0PC). Silence = proof.

### 6.6 Test B — Transfer (expect the full drumbeat)

Same procedure on the Transfer page. Expected sequence:

```
begin → enlist (debit) → enlist (credit) →
prepare (XA_OK) → prepare (XA_OK) →
tranlog written →
commit (debit) → commit (credit) →
transaction complete
```

Browser output:

```
XA Transfer complete. Rs.500.00 moved...
```

### 6.7 Test C — Failure Path (failure checkbox TICKED)

The credit step deliberately fails. One resource votes **no** in Phase 1 → WAS rolls back **both** sides.

Expected trace content: `XAResource.rollback` (instead of `commit`). No tranlog commit decision. Browser shows a failure message.

**Verification:** check account balances after the failed transfer. The debit database and credit database are both untouched — Rs.500 remains in Account 1001. Nothing created, nothing destroyed. **Atomicity demonstrated live.**

---

## 7. Key Takeaways

1. **XA** = the handshake contract between WAS and a database.
2. **2PC** = vote first (`prepare`), act second (`commit`). Required when 2+ resources could disagree.
3. **1PC** = one resource, one shot, no vote.
4. **LAO** = one XA + one non-XA committed last — a 1PC shortcut.
5. **0PC / non-XA** = the referee was never invited; the DB commits by itself.
6. **Tranlog** = the coordinator's notebook; written only when a real 2PC decision occurs.
7. **Empty grep output is a positive result** — it proves no XA ran.

---

## 8. Exit Criteria Checklist

### Setup Phase

- [ ] SSH to `dsb-dmgr` (192.168.10.10) as `wasadmin` succeeded
- [ ] Baseline line count captured (or 0 if `trace.log` absent)
- [ ] Baseline number recorded
- [ ] URL uses port **9080** (direct to member 1), not port 80

### Test Phase

- [ ] Deposit test run → grep output empty (0PC confirmed)
- [ ] Transfer test run → all 6 drumbeat markers seen in order
- [ ] Failure-checkbox test run → `XAR esource.rollback` seen, balances unchanged
- [ ] Evidence saved to /tmp/xa-2pc-trace-capture.txt
- [ ] File contents reviewed with cat

### Understanding Phase
- [ ] Can explain prepare/commit using the wedding analogy without notes
- [ ] Can recite the drumbeat: BEGIN → ENLIST → PREP → LOG → COMMIT → DONE
- [ ] Can explain why the tranlog must be written before Phase 2
