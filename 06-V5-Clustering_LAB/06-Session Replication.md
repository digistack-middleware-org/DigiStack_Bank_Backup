# Sprint 5 — Session Replication + Failover Proof 🏦

> **Goal:** Configure memory-to-memory session replication on the cluster and prove with a live failover test — log in, kill one cluster member mid-session, confirm the session and a deposit both survive on the surviving member.

---

## The Real Bank Story 🏦

Imagine you walk into an HDFC Bank branch to deposit ₹5,000. You're at the counter (Teller #1), half-way through the transaction. Suddenly — power goes out at Teller #1's desk.

### ❌ Bad bank (no session replication)

- Teller #1's system dies → your details are lost
- Security guard says: *"Sorry, please go back outside, take a token again, stand in line, and start from scratch."*
- You're furious. Worse — did your money go through or not? Even the bank isn't sure.

### ✅ Good bank (with session replication)

- Teller #1 dies mid-transaction
- Teller #2 (sitting next to him) **already has a photocopy of your file** — updated every second
- Teller #2 says: *"Sir, I have your details. Please continue — deposit ₹5,000, done!"*
- You never even noticed anything happened.

**That's exactly what Sprint 5 does.**

| Bank | Our Lab |
|---|---|
| Teller #1 | `server1` on `devdsbinnode01` |
| Teller #2 | `server1` on `devdsbinnode02` |
| Photocopying | Memory-to-memory replication |

---

## Why This Is the MOST Important Test

- **Load balancing** = two tellers share customers.
- **HA (High Availability)** = if one teller dies, the customer doesn't even blink.

If your session dies mid-deposit, the transaction state is ambiguous — money debited? Not debited? Banks can't afford "maybe." This test proves the cluster is **genuinely HA**, not just load-balanced.

---

## Concept — The 3 Ways WAS Can Save Your "Photocopy"

| Mode | Bank Analogy | Verdict |
|---|---|---|
| **None** | No photocopies. Teller dies = start over | ❌ Not HA |
| **Memory-to-memory** | Tellers photocopy files to each other instantly | ✅ Fastest, no extra dependency — **we use this** |
| **Database** | Every file also stored in the bank's locker room | Survives full restart, but locker room gets crowded (DB load) |

Memory-to-memory uses WAS's built-in **DRS (Data Replication Service)** — think of it as an internal courier running between the two tellers, constantly syncing files.

---

## Step 1 — Configure It: Admin Console (GUI) Method 🖥️

> **Think of it as:** The branch manager setting the office rule: *"All tellers must photocopy every customer file to the other teller."*

1. Open **DMgr Admin Console** (the branch manager's head office desk)
2. Go to: **Servers → Clusters → WebSphere application server clusters** → click `devdsbinappcluster01`
3. Click **Session management** → tick **[x] Override session management**
   - *Meaning: "This rule applies to ALL tellers in the branch, not individually."*
4. Click **Distributed environment settings** → select **Memory-to-memory replication**
5. Set **Replication mode = Both client and server**
   - *Meaning: "Teller #1 sends copies to Teller #2 AND accepts copies from Teller #2."* Both directions = full backup either way.
6. **Save** the config

### Ripple Start — The Smart Restart 🔄

> **Analogy:** You don't close the whole bank to change the photocopier. You upgrade Teller #1 while Teller #2 serves customers, then swap. One teller is ALWAYS working = **zero downtime**.

- Tick cluster → **Ripple Start** → wait 2–3 min → both members show green ▶

---

## Step 2 — Same Thing via wsadmin (Scripting) Method ⌨️

> **Think of it as:** Instead of walking to each teller and explaining the rule, the manager sends a **written memo** (the Python script) that both tellers follow automatically.

### 2.1 — Create the script

Create `C:\Projects\digistack-bank-parent\scripts\v5_session_replication.py` and paste the script content (see repo: `scripts/v5_session_replication.py`).

### 2.2 — Copy and run from DMgr profile bin

```bash
scp C:\Projects\digistack-bank-parent\scripts\v5_session_replication.py root@192.168.10.10:/tmp/

cd /apps/IBM/WebSphere/AppServer/profiles/devdsbindmgr01/bin/
./wsadmin.sh -lang jython \
    -username wasadmin \
    -password <YourPassword> \
    -f /tmp/v5_session_replication.py
```

### What the script does (in plain English)

1. Finds the cluster *(walks to the branch)*
2. Loops through each member *(visits Teller #1, then Teller #2)*
3. Updates each one's `SessionManager` — *"start photocopying your sessions"*
4. Sets `DistributedSessionConfig: replicationType=BOTH` — *"copy in both directions"*
5. `AdminConfig.save()` — files the memo officially

### Expected output

```
Configuring: server1 on devdsbinnode01
  DistributedSessionConfig: replicationType=BOTH (memory-to-memory).
Configuring: server1 on devdsbinnode02
  DistributedSessionConfig: replicationType=BOTH (memory-to-memory).
Configuration saved.
```

> 💡 **Why both methods?** Console = good for learning/one-time. Script = good because it's repeatable — if you rebuild the environment tomorrow, run one script instead of 20 clicks.

---

## Step 3 — Confirm Session Replication in Admin Console ✅

Navigate to:

```
Servers → Clusters → devdsbinappcluster01 →
Session management → Distributed environment settings
```

**Expected result:** *Memory-to-memory replication* selected, replication mode shows *Both client and server*.

---

## Step 4 — THE FAILOVER TEST (The Main Event) 🎬

> ⚠️ **This is the whole point of Sprint 5.** Follow in order — move quickly once the server dies.

### 4.1 — Log in

Open `http://192.168.10.10:9080/digistack-bank/Home` and log in as `customer1`

> → You're now **"at Teller #1's counter."**

### 4.2 — Check balance (on `dsb-db`)

```bash
psql -U digistack_app -d digistack_bank -h 127.0.0.1 \
  -c "SELECT balance FROM accounts WHERE user_id = 1;"
```

> Note it. Say it's **₹20,000**. This is your **"before" photo**.

### 4.3 — Prepare the deposit (but DON'T submit)

Open the Account page, type **5000** in the deposit field.

> → You've handed Teller #1 the cash, he's counting...

### 4.4 — KILL Teller #1

```bash
cd /apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/bin/
./stopServer.sh server1 -username wasadmin -password <pwd>
```

Wait for:

```
ADMU4000I: Server server1 stop completed.
```

### 4.5 — IMMEDIATELY click Deposit

| Outcome | Meaning |
|---|---|
| ✅ **Best case:** Green success banner | Teller #2 took over so smoothly you saw nothing |
| ✅ **Acceptable:** Brief error, refresh, still logged in | Routing took a second to switch — session survived |
| ❌ **FAILURE:** Thrown back to Login page | Your "photocopy" never existed = replication failed |

### 4.6 — Verify the money arrived

```bash
psql -U digistack_app -d digistack_bank -h 127.0.0.1 \
  -c "SELECT balance FROM accounts WHERE user_id = 1;"
```

> Expected: **₹20,000 → ₹25,000**. The deposit survived the crash.

### 4.7 — Prove it was Teller #2 who did it (on `dsb-node02`)

```bash
grep "AccountServlet" \
  /apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver02/logs/server1/SystemOut.log \
  | tail -5
```

Expected:

```
AccountServlet: Deposit successful. userId=1 amount=5000 newBalance=₹25,000.00
```

> This line in **node02's** log = the surviving member handled it. **Case closed.** 🔒

### 4.8 — Bring Teller #1 back from lunch

```bash
cd /apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/bin/
./startServer.sh server1
```

Wait for:

```
ADMU3000I: Server server1 open for e-business
```

---

## Step 5 — Post-Recovery Health Check 🩺

- [ ] **Console:** Cluster green, both members **Started** — *both tellers back at their desks*
- [ ] **Applications → digistack-bank-v5** → Started
- [ ] **Manage Modules** → shows `cluster=devdsbinappcluster01` — *app still deployed to the whole branch, not one teller*
- [ ] Log in again, balance shows **₹25,000**, do a small **₹100 deposit** — *confirming normal banking has resumed on both tellers*

---