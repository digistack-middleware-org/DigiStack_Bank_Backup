# WebSphere Transaction Service — Admin Console & wsadmin Baseline Walkthrough

## Overview

This document captures the **baseline inspection** of the Transaction Service configuration on both cluster members (`server1` and `server2`). No changes are made in this step — it is a **read-only audit** that establishes reference values for Sprint 4.

> [!NOTE]
> This step is inspection only. All values recorded here serve as the baseline. Modifications are performed in Sprint 4.

---

## Step 12 — Admin Console Walkthrough

### Navigation Path

| Click Path | Purpose |
|---|---|
| `Servers → Server Types → WebSphere application servers` | List all application servers |
| Click `<server1name>` | Open that server's settings |
| `Container Settings → Container Services → Transaction Service` | The service that manages transactions |
| `Transaction log directory` field | Shows where the tranlog lives |
| `Total transaction lifetime timeout` | Currently `120s` |
| `Maximum transaction timeout` | Currently `300s` |

### Baseline Values (Record These)

| Attribute | server1 | server2 |
|---|---|---|
| Total transaction lifetime timeout | `120` | `120` |
| Maximum transaction timeout | `300` | `300` |
| Transaction log directory | `USERINSTALLROOT/tranlog‘∣‘{USER_INSTALL_ROOT}/tranlog` | `USERI​NSTALLR​OOT/tranlog‘∣‘{USER_INSTALL_ROOT}/tranlog` |
| Enable logging for heuristic reporting | `false` | `false` |

> [!TIP]
> Record the values for **both** cluster members. In a cluster, both members must have **consistent** settings — mismatched timeouts cause confusing, inconsistent transaction behavior.

---

## Step 13 — The wsadmin Script Explained Line by Line

### Why Use wsadmin?

- The Admin Console shows **one server at a time**.
- A script checks **both servers in one pass**.
- Output is **repeatable** and serves as **audit evidence** — auditors favor scripts because they prove exactly what was checked.

### Script Flow

1. **Define both cluster members:**

   ```python
   CLUSTER_MEMBERS = {'devdsbinnode01': '..._server1', 'devdsbinnode02': '..._server2'}
   ```

   - Key = node name
   - Value = server name

2. **Loop through both servers** — one pass each.

3. **Build the server's config ID:**

   ```python
   AdminConfig.getid('/Node:.../Server:.../')
   ```

   - This is WebSphere's "address" of the server in its config repository.
   - If it returns **empty**, the server does not exist → the `WARNING` line fires and the loop continues to the next server (it does not crash).

4. **Find the TransactionService object:**

   ```python
   AdminConfig.list('TransactionService', serverId)
   ```

   - `TransactionService` is a child of the server config.
   - `.strip().splitlines()[0]` takes the first ID in case multiple are returned.

5. **Read four attributes** with `AdminConfig.showAttribute`:

   | Attribute | Expected Value |
   |---|---|
   | `totalTranLifetimeTimeout` | `120` |
   | `maximumTransactionTimeout` | `300` |
   | `transactionLogDirectory` | `${USER_INSTALL_ROOT}/tranlog` |
   | `enableLoggingForHeuristicReporting` | `false` |

6. **Print everything** — this output is your audit record.

7. **Physical file check (hint):**
   - The config says *where* the logs go; `ls -lh` on each machine shows *how big* they actually are.
   - The tranlog grows with transaction volume — worth monitoring on a busy payment system.

   ```bash
   ls -lh ${USER_INSTALL_ROOT}/tranlog
   ```

> [!IMPORTANT]
> This script is **read-only**. `showAttribute` reads; it never writes. Safe to run at any time. In a real bank, "read-only audit scripts" are exactly what change-management teams allow.

---

## Key Terms Cheat Sheet

| Term | Plain Meaning |
|---|---|
| Transaction | All-or-nothing unit of work |
| Tranlog | Notebook used for crash recovery |
| Total lifetime timeout | Max total time for one transaction (`120s`) |
| Maximum timeout | Absolute ceiling, even if the app asks for more (`300s`) |
| Recovery | Reading the notebook after a crash and finishing the work |
| Heuristic | Mixed/uncertain transaction outcome |
| `AdminConfig` | wsadmin command family for reading/changing config |
| `showAttribute` | Read one attribute — read-only |

---

## Why This Matters for a Bank (Real-World Framing)

- **Regulators/auditors ask:** *"If your app server crashes during a payment, how do you guarantee no money is lost?"*
  - **Answer:** tranlog + recovery.
- **Sprint 4 preview:**
  - Adjust the timeouts (e.g., longer lifetime for slow batch operations).
  - Possibly relocate the tranlog directory.
  - Today's step is your **baseline snapshot**.
- **Cluster rule:** Both members should match. Mismatched timeouts = confusing, inconsistent behavior.

---

## Summary

| Item | Status |
|---|---|
| Console inspection of Transaction Service | ✅ Completed |
| Baseline values recorded for both members | ✅ Recorded |
| wsadmin read-only audit script executed | ✅ Executed |
| Configuration changes | ❌ None (deferred to Sprint 4) |
