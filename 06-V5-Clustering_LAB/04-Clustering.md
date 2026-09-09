# P01 v5 — Sprint 3: Create the AppCluster + Cluster Members

> **Sprint Goal:** Create `devdsbinappcluster01` with two cluster members — one on
> `devdsbinnode01` (dsb-dmgr) and one on `devdsbinnode02` — both started and confirmed
> running via Admin Console and wsadmin.

---

## 🎯 What are we doing in this sprint?

We now have a boss (DMgr) and two workers (nodes). This sprint we **group the workers
into a team** called a **cluster** — `devdsbinappcluster01` — and turn the team **on**.

After this sprint: one team name, two identical workers behind it, both running. 🏗️👷👷

---

## 📚 First, Understand 2 Simple Ideas

### 👥 What is a Cluster?

**A cluster = a named team of identical servers running the same app.**

Think of a **bank with two identical teller windows**:

- Customers don't care which window serves them — either one works.
- If one window closes, the other keeps serving (**failover**).
- The queue is shared — work is spread between both (**load balancing**).

**The magic part:** When you deploy an app to the *cluster*, WAS automatically copies it
to **every member**. You deploy once — both servers get it. The cluster acts like
**one single target**.

### 🏷️ Naming Note

Both members are named `server1` — but they live on **different nodes**, so there's no
confusion. It's like two branches of a shop both having an employee named "Alex" —
they work in different buildings. (The project naming standard allows this one exception.)

---

## Step 1 — Create the Cluster by Clicking (GUI Method)

**Like filling a form in the boss's office:**

1. Open DMgr console → log in as `wasadmin`.
2. Go to **Servers → Clusters → WebSphere application server clusters** → **New**.
3. **Wizard page 1:** Name it `devdsbinappcluster01` → Next.
4. **Wizard page 2 — add member #1:**
   - Member name: `server1`
   - Node: `devdsbinnode01`
   - Click **Add Member** ✅
5. **Add member #2:**
   - Member name: `server1`
   - Node: `devdsbinnode02`
   - Click **Add Member** ✅

✅ **Expected table:**

```
server1    devdsbinnode01
server1    devdsbinnode02
```

6. **Page 3:** Check the summary → **Finish**.
7. Click **Save** in the banner (very important — saving writes the change to the master config).
8. Go back to the clusters list.

✅ **Expected:** `devdsbinappcluster01` is listed with a **red X** — correct!
It exists but isn't started yet. (A team roster exists, but nobody is at their desk yet.)

---

## Step 2 — The Same Thing via Script (wsadmin Method)

**Why show both ways?** In real companies, admins prefer **scripts** — they can run over
SSH, be repeated, and be automated. The GUI is good for learning; scripts are good for work.

⚠️ **Note:** The cluster already exists from Step 1. The script is **smart** — it first
*checks* if the cluster exists:

- **If it exists** → it skips creation and just **reads and prints the member list**
  (safe verification mode).
- **If it doesn't exist** → it creates the cluster + member 1, then adds member 2, then saves.

**How the script works, in plain words:**

1. Ask: *"Does a cluster named devdsbinappcluster01 exist?"* (`AdminConfig.getid`)
2. If yes → list its members and stop.
3. If no → `AdminTask.createCluster()` makes the cluster **and the first member** in one call.
4. `AdminTask.createClusterMember()` adds the **second member**.
5. `AdminConfig.save()` → writes it to the master rulebook.
6. Count the members — should be **2**. Print ✅ or ⚠️.

**Run it:**

```bash
scp C:\Projects\digistack-bank-parent\scripts\v5_create_cluster.py root@192.168.10.10:/tmp/

cd /apps/IBM/WebSphere/AppServer/profiles/devdsbindmgr01/bin/
./wsadmin.sh -lang jython -username wasadmin -password <pwd> -f /tmp/v5_create_cluster.py
```

✅ **Expected:**

```
Cluster 'devdsbinappcluster01' already exists... Skipping creation.
Current members: 2
  server1 on devdsbinnode01
  server1 on devdsbinnode02
```

---

## Step 3 — Push the New Rulebook to Both Nodes

The cluster config currently exists **only in the DMgr's master copy**. The nodes don't
know about their new team membership yet. So sync:

**Way A (Console):** **System Administration → Nodes** → tick both → **Full Resynchronize**.

✅ **Expected:** Both show **Synchronized**.

**Way B (Script):** Re-run the sync script from Sprint 2:

```bash
./wsadmin.sh -lang jython -username wasadmin -password <pwd> -f /tmp/v5_sync_nodes.py
```

✅ **Expected:** Both nodes: **Synchronized successfully.**

---

## Step 4 — Start the Cluster (Open Both Teller Windows)

### Way A — Console (Easy)

**Servers → Clusters** → tick `devdsbinappcluster01` → **Start**.

> 💡 One click starts BOTH members — that's cluster power. Takes 1–2 minutes.

Verify: **Servers → Server Types → WebSphere application servers**

✅ **Expected — both green ▶:**

```
server1    devdsbinnode01    Started
server1    devdsbinnode02    Started
```

### Way B — Script (`v5_start_cluster.py`)

**How it works, in plain words:**

1. **Find the cluster's live "handle" (MBean)** — an important distinction:
   - `AdminConfig` = the **stored rulebook** (XML files on disk)
   - `AdminControl` = the **live, running system** (like a TV remote)
   - To *start* something, you need the **live remote**, not the paper file.
2. If not found → error message reminding you: **Node Agents must be running first**
   (the foremen!).
3. **Check current state** — if already `websphere.cluster.running`, skip
   (safe re-runnable script).
4. Otherwise → issue `start`, wait 60 seconds.
5. **Check each member** and print its state.

**Run it:**

```bash
scp C:\Projects\digistack-bank-parent\scripts\v5_start_cluster.py root@192.168.10.10:/tmp/

./wsadmin.sh -lang jython -username wasadmin -password <pwd> -f /tmp/v5_start_cluster.py
```

✅ **Expected:**

```
server1 on devdsbinnode01: STARTED
server1 on devdsbinnode02: STARTED
```

---

## Step 5 — Write Down Each Member's Port Number 📝

**Why?** Even though both members are named `server1`, they're **different servers on
different machines**, and WAS gives them **different HTTP ports** so their "doors" don't
clash (a **port offset** — like apartment 101 vs 201).

**How to check:**

1. Console → **Servers → Server Types → WebSphere application servers**
2. Click `server1` on **devdsbinnode01** → **Ports** → note **WC_defaulthost**
   (usually **9080**)
3. Click `server1` on **devdsbinnode02** → **Ports** → note **WC_defaulthost**
   (maybe **9081** or other)

📋 **Write these two numbers down** — Sprint 5 uses them to test hitting each member
directly and prove **failover** works.

---
