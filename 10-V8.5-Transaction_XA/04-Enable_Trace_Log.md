# WebSphere Application Server — Two-Phase Commit (2PC), XA Transactions & Transaction Log

> **Audience:** Developers and administrators working with WebSphere Application Server (WAS) and multi-database transactions.
> **Scope:** XA coordination, the full 2PC sequence, failure & recovery behavior, trace analysis, and Transaction Service configuration.

---

## 1. The Full 2PC Sequence

When a transaction spans two XA data sources (e.g., a bank transfer debiting one database and crediting another), WAS coordinates a **two-phase commit**. The runtime sequence observed in trace is:

| Step | Event | Meaning |
|------|-------|---------|
| 1 | `transaction begin` | WAS starts the transaction |
| 2 | `XAResource enlist (DEBIT_DS)` | Connection 1 joins the transaction |
| 3 | `XAResource enlist (CREDIT_DS)` | Connection 2 joins the transaction |
| 4 | `XAResource.prepare (DEBIT_DS)` → `XA_OK (0)` | Resource votes "yes" |
| 5 | `XAResource.prepare (CREDIT_DS)` → `XA_OK (0)` | Resource votes "yes" |
| 6 | `tranlog write` | The commit decision is recorded on disk |
| 7 | `XAResource.commit (DEBIT_DS)` | Changes become permanent |
| 8 | `XAResource.commit (CREDIT_DS)` | Changes become permanent |
| 9 | `transaction complete` | Transaction ends |

> [!TIP]
> Memorize this order:
> `BEGIN → ENLIST ×2 → PREPARE ×2 → TRANLOG → COMMIT ×2 → COMPLETE`

---

## 2. Failure Scenarios: What If Someone Votes No, or Crashes?

| Scenario | What WAS Does | Outcome |
|----------|---------------|---------|
| Any resource votes `XA_ABORT` | WAS rolls back **all** resources | Nothing changes — atomicity preserved |
| WAS crashes **before** writing the tranlog decision | On restart: no decision found → rollback everything | Safe |
| WAS crashes **after** writing the decision, before Phase 2 finishes | On restart: reads tranlog → sees `COMMIT` decision → replays Phase 2 commits | Safe |
| A database is **in-doubt** (was prepared, never received Phase 2) | It holds pending data locked until WAS recovery contacts it | Resolved by recovery machinery |

> [!NOTE]
> This recovery machinery is exactly why the `TransactionLog` and `PartnerLog` files exist.

---

## 3. 2PC vs 1PC vs No Protocol

| Scenario | What Happens | Trace Evidence |
|----------|--------------|----------------|
| **Two XA DataSources** (e.g., transfer) | Full 2PC | `enlist ×2`, `prepare ×2`, `tranlog`, `commit ×2` |
| **One XA DataSource only** | 1PC (Last Agent Optimization) — with only one resource there is nothing to coordinate, so WAS skips `prepare` | `enlist ×1`, `commit ×1`, no `prepare`, no `tranlog` |
| **Non-XA DataSource** (e.g., Deposit) | Plain JDBC — the database commits itself; WAS is not involved | No XA lines at all in trace |

### Key Concept — XA vs Non-XA DataSource

- An **XA DataSource** is configured so the driver supports joining WAS-coordinated transactions.
- A **non-XA DataSource** (e.g., `jdbc/BankDS` using `PGConnectionPoolDataSource`) **cannot join** WAS transactions. The connection simply calls its own `commit()` internally.
- Non-XA is fine for **single-database** operations. It is **insufficient** when two databases must agree atomically.

---

## 4. Proving It with Trace

### 4.1 What Is Trace?

WAS logs normally show errors and warnings. **Trace** is a much deeper microscope — it logs internal component activity. Enable it with a trace specification:

```properties
*=info:com.ibm.ws.Transaction*=all
```

Reading it left to right:

- `*=info` → everything logs at the normal `info` level
- `:com.ibm.ws.Transaction*=all` → the transaction component logs **everything** (all levels)

### 4.2 Runtime vs Configuration Tab

| Tab | Effect | Survives Restart? |
|-----|--------|-------------------|
| **Runtime** | Immediate on the live server | ❌ No |
| **Configuration** | Only after next restart | ✅ Yes |

> [!TIP]
> This lab uses **Runtime** — instant, no restart, nothing saved. Leaving full trace on permanently would fill your disk; that is why trace is turned off afterward (Step 9 of the lab).

### 4.3 The Baseline Trick

Trace output is huge. To read only the fresh output from **your** test, record the line count before running, then read only from that line onward:

```bash
wc -l trace.log        # note the number, e.g. 4821

# ... run the test ...

tail -n +4822 trace.log | grep -E "enlist|prepare|commit|tranlog"
```

- `tail -n +4822` means *"start from line 4822."*
- `grep -E` filters for the XA keywords.
- This isolates your test's 2PC sequence.

### 4.4 Targeting Member 1 Directly

A cluster has two members. If you go through IHS (port 80), the load balancer might send your request to **member 2** — and you would be reading the wrong `trace.log`!

**Fix:** bypass IHS and go directly to member 1's HTTP port (`9080`). Now you know which server processed the request.

> [!TIP]
> **Memory hook:** Port 80 = front door (IHS, unpredictable). Port 9080 = employee entrance (straight to one server, predictable).

---

## 5. The Tranlog — WAS's Insurance Policy

### 5.1 Where Is It?

The tranlog lives **on each node, under the profile** — not on the DMgr. Each server has its own pair of files:

```text
<profile>/tranlog/<cell>/<node>/<server>/TransactionLog
<profile>/tranlog/<cell>/<node>/<server>/PartnerLog
```

### 5.2 What Each File Does

| File | Contents | Purpose |
|------|----------|---------|
| `TransactionLog` | The commit/rollback decisions | Crash recovery: *"what must I finish?"* |
| `PartnerLog` | The list of Xids (participants) per transaction | Recovery: *"which databases must I contact?"* |

> [!NOTE]
> Both files are **binary** — do not try to read them with `cat`. Just verify they exist and check their sizes:

```bash
ls -lh <profile>/tranlog/<cell>/<node>/<server>/
```

### 5.3 Key Facts to Remember

- **Default size:** 512 KB per file.
- The tranlog write is **synchronous** — WAS blocks until the decision is safely on disk. Slow disk = slow commits.
- **Production advice:** put the tranlog on its own fast disk, separate from the OS and application data.
- **Sizing formula:** `peak concurrent transactions × avg record size × safety factor`.
- The directory is configured per server under **Transaction Service** settings and is readable via the wsadmin attribute `transactionLogDirectory`.

### 5.4 Timeout Values (Configured in Sprint 4)

| Setting | Default | Meaning |
|---------|---------|---------|
| `totalTranLifetimeTimeout` | 120 s | Maximum total lifespan of any transaction |
| `maximumTransactionTimeout` | 300 s | Maximum timeout for any transaction |

> [!NOTE]
> **Why it matters:** a transaction that hangs (e.g., waiting on a slow database) must eventually be killed, or locks pile up and everything freezes.

---

# Enabling WebSphere XA Transaction Trace (Runtime Only)

> [!NOTE]
> **Audience:** WAS 8.5.x administrators in the NDS01 lab environment.
> **Goal:** Enable `com.ibm.ws.Transaction` diagnostic trace on **all cluster members** at **runtime** — no restart, no persistent config change.

---

## 1. Overview

| Item | Value |
|---|---|
| Product | IBM WebSphere Application Server 8.5 (ND) |
| Trace string | `*=info:com.ibm.ws.Transaction*=all` |
| Change type | Runtime only (`AdminControl`) |
| Restart required | ❌ No |
| Output | `<profile>/logs/<server-name>/trace.log` |
| DMGR host / port | `192.168.10.10:8879` (SOAP) |

### Trace String Explained

```
*=info:com.ibm.ws.Transaction*=all
```

- `*=info` → keep **normal `info` level everywhere** (default behavior).
- `com.ibm.ws.Transaction*=all` → **full detail** on all transaction components.

> [!TIP]
> Think of trace as a camera on a component: normal footage everywhere, close-up on transactions.

---

## 2. Two Ways to Do It

| | Way A — Admin Console | Way B — wsadmin Script |
|---|---|---|
| Mode | GUI, point and click | Automated Jython script |
| Best for | One-off, learning | Repeatable, auditable, both members at once |
| Risk of typos | Higher | Low |
| Change control | Manual evidence | Script is the audit trail |

---

## 3. Way A — Admin Console (GUI)

Repeat for **each member**:

1. Open browser → `http://192.168.10.10:9060/ibm/console`
2. Log in with WAS admin credentials.
3. Left menu: **Troubleshooting → Logs and trace**.
4. Click the server name (e.g. `devdsbinappcluster01_server1`).
5. Click **Diagnostic Trace Service**.
6. ⚠️ Click the **Runtime** tab (NOT **Configuration**).
7. Clear the field, type:

   ```text
   *=info:com.ibm.ws.Transaction*=all
   ```

8. Click **Apply**.

**Expected result:** a green confirmation banner. No restart. Trace starts writing immediately.

### Where the Output Goes

```text
<profile>/logs/<server-name>/trace.log
```

| Member | Path |
|---|---|
| Member 1 | `.../profiles/devdsbinnode01/logs/devdsbinappcluster01_server1/trace.log` |
| Member 2 | `.../profiles/devdsbinnode02/logs/devdsbinappcluster01_server2/trace.log` |

> [!WARNING]
> If you change the **Configuration** tab instead of **Runtime**, the trace becomes permanent, survives restart, and floods disk.

---

## 4. Way B — wsadmin Script (Automation)

### Why Bother with a Script?

- No clicking around, no typos.
- Repeatable — one command traces **both members**.
- In banking environments (change-controlled), scripted actions are **auditable and consistent**.

### Script Logic (Plain English)

1. Define the trace string and the list of members (node → server).
2. For each member:
   - **Ask:** "Is this server alive?" → `AdminControl.queryNames(...)` looks for its `TraceService` MBean.
     - An *MBean* is like a remote control handle for a running server. If the server is stopped, the handle doesn't exist → the script warns you and moves on.
   - **Set:** `AdminControl.setAttribute(...)` flips the trace switch at runtime.
3. Print confirmation and the `trace.log` locations.

> [!IMPORTANT]
> The script does **NOT** call `AdminConfig.save()`.

| API | Purpose | Persists? |
|---|---|---|
| `AdminControl` | Talk to **running** servers (runtime) | ❌ Lost on restart |
| `AdminConfig` | Edit **stored** configuration | ✅ Survives restart |

This script deliberately uses **only `AdminControl`** → runtime only.

---

## 5. How to Run the Script

```bash
/apps/IBM/WebSphere/AppServer/bin/wsadmin.sh \
  -lang jython \
  -conntype SOAP \
  -host 192.168.10.10 \
  -port 8879 \
  -user wasadmin \
  -password <your-password> \
  -f wsadmin-scripts/enable-xa-trace-v8.5.py
```

| Flag | Meaning |
|---|---|
| `-lang jython` | Script is written in Jython (Python for Java) |
| `-conntype SOAP -host -port` | Connect to the Deployment Manager's admin port `8879` over SOAP |
| `-f` | Run this script file |

---

## 6. Rules This Lab Teaches (NDS01 Rule 7 Spirit)

- ❌ Never trace at `all` level permanently — disk fills up, performance suffers.
- ✅ Always trace **all members of a cluster**, not just one.
- ✅ Runtime first; persist only if justified — keep changes reversible.
- ✅ Know which server you're changing — always confirm names first.
- ✅ Watch for **stopped servers** — the script warns you; a stopped server can't accept a trace change.

---

## 7. What Happens After — and How to Undo

### After Enabling

1. Tail the log:

   ```bash
   tail -f .../trace.log
   ```

2. Reproduce the failing transaction.
3. Look for entries with component `com.ibm.ws.Transaction` — prepare/commit/rollback decisions, XA resource interactions.

### Switch Trace OFF (Runtime)

Same steps (console or script), but set the spec back to:

```text
*=info
```

> [!WARNING]
> If you ever need it to survive restart (the commented-out code block): it uses `AdminConfig.modify` on `startupTraceSpecification`, then `AdminConfig.save()`.
> **Do not use in the lab** — it permanently writes massive logs.

---

## 8. Memory Card — One-Liners

- 📷 **Trace** = camera on a component.
- ⏱️ **Runtime** = now; **Configuration** = after restart.
- 🔍 `*=info:com.ibm.ws.Transaction*=all` = "normal everywhere, full detail on transactions."
- 🖥️ **Both members or nothing.**
- 📄 `trace.log` lives in the server's logs directory.
- 🔄 No restart needed for Runtime changes.
- ↩️ Undo = set spec back to `*=info`.
