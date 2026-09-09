# Node Federation — Both Nodes Join the Cell

---

## 🎯 What are we doing in this sprint?

Last sprint we built the **boss (DMgr)** and a **new worker machine (dsb-node02)**.
But right now, they don't know each other!

This sprint's job:

1. Make **both worker nodes join the company** (the cell) — this is called **Federation**.
2. Make sure everyone follows the boss's rulebook (**Synchronization**).
3. Turn on the **messenger/bridge on each machine** (the **Node Agent**).

After this sprint, the DMgr is the **boss of both nodes**. 🧠👷👷

---

## 📚 First, Understand 3 Simple Ideas

### 🤝 What is Federation?

**Federation = a standalone worker joining the boss's company.**

Think of it like a **freelancer joining a company**:

- The company gives the freelancer the **employee handbook** (copies the master config to the node).
- The freelancer **registers with HR** (registers with the DMgr).
- From now on, the **boss's rulebook is the only one that matters** — the freelancer's old
  personal rulebook (its own standalone Admin Console on port 9060) **stops working**.
  You manage everything from the boss's office (DMgr console) now.

### ⚖️ What is Synchronization?

The DMgr keeps the **master rulebook**. Synchronization = the DMgr **pushing the latest
version of the rulebook** to every node, so everyone is following the same rules.
Like the head office emailing every branch the updated company policy.

### 📡 What is a Node Agent?

A Node Agent is a **background helper process** running on each node — like a **site foreman**:

- It **listens to the boss** (receives config updates from the DMgr).
- It **starts/stops workers** when the boss says so.
- **Without the foreman, the boss cannot manage anything on that machine.**

---

## Step 1 — Federate Node 1 (the AppServer on dsb-dmgr)

### Step 1.1 — Stop the Standalone Server First

A worker must **stop working alone** before joining the company. So stop `server1`:

```bash
cd /apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/bin/
./stopServer.sh server1 -username wasadmin -password <YourPassword>
```

✅ **Expected:**

```
ADMU4000I: Server server1 stop completed.
```

### Step 1.2 — Run the Join Command (addNode)

```bash
./addNode.sh 192.168.10.10 8879 -username wasadmin -password <YourPassword>
```

**Simple meaning:**

| Part | Meaning |
|---|---|
| `addNode.sh` | "I want to join a cell" |
| `192.168.10.10` | Address of the boss (DMgr) |
| `8879` | The boss's **SOAP door** — the specific "door" the boss uses to talk to nodes |
| `-username / -password` | Login to prove you're allowed to join |

> 💡 **What's SOAP?** Just a way for programs to talk to each other over the network
> (like a formal phone line). 8879 is the DMgr's default SOAP port.

> 💡 **Tip:** If unsure of the port, check the boss's config file with the
> `grep SOAP_CONNECTOR_ADDRESS` command — it will tell you the number (usually 8879).

```
grep -i "SOAP_CONNECTOR_ADDRESS" \
  /apps/IBM/WebSphere/AppServer/profiles/devdsbindmgr01/config/cells/devdsbincell01/nodes/devdsbindmgrnode01/serverindex.xml
```

✅ **Expected:**

```
ADMU0003I: Node devdsbinnode01 has been successfully federated.
```

### Step 1.3 — Verify in the Boss's Console

Open the **DMgr console** → **System Administration → Nodes**

✅ **Expected:** `devdsbinnode01` is listed.

> ⚠️ Remember: use the **DMgr console**, not the old standalone console — the old one is retired now.

---

## Step 2 — Federate Node 2 (dsb-node02)

### Step 2.2 — Join the Cell

```bash
cd /apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver02/bin/

./addNode.sh 192.168.10.10 8879 -username wasadmin -password <YourPassword>
```

✅ **Expected:**

```
ADMU0003I: Node devdsbinnode02 has been successfully federated.
```

### Step 2.3 — Verify

DMgr console → **System Administration → Nodes**

✅ **Expected — both listed:**

```
devdsbinnode01    Started / Synchronized
devdsbinnode02    Started / Synchronized
```

---

## Step 3 — Synchronize Both Nodes (Push the Rulebook)

You can sync **two ways** — both do the same thing:

### Way A — Click in the Admin Console (Easy Way)

**System Administration → Nodes** → tick both nodes → click **Full Resynchronize**.

✅ **Expected:** Both show **Synchronized**.

### Way B — Run a Script with wsadmin (Automation Way)

> 💡 **What is wsadmin?** It's WAS's command-line tool that lets you control WAS
> with scripts instead of clicking. We use a Jython (Python-like) script: `v5_sync_nodes.py`.

**How the script works, in plain words:**

1. Ask WAS: *"Give me all the NodeSync helpers"* (`AdminControl.queryNames('type=NodeSync,*')`).
2. For each node found:
   - Print its name.
   - Call its `sync()` action — "please pull the latest rulebook."
   - Print ✅ if it worked, ❌ if not.

**Copy it to the boss machine and run it:**

```bash
scp C:\Projects\digistack-bank-parent\scripts\v5_sync_nodes.py root@192.168.10.10:/tmp/

cd /apps/IBM/WebSphere/AppServer/profiles/devdsbindmgr01/bin/
./wsadmin.sh -lang jython -username wasadmin -password <pwd> -f /tmp/v5_sync_nodes.py
```

⚠️ **Important:** Cluster operations via wsadmin must be run from the **DMgr profile's
bin folder** — because the DMgr is the boss, and the boss is the one who manages nodes.

✅ **Expected:**

```
Found 2 node(s) to sync.
-> devdsbinnode01: Synchronized successfully.
-> devdsbinnode02: Synchronized successfully.
```

---

## Step 4 — Start the Node Agents (Hire the Foremen)

### Step 4.1 — On dsb-dmgr (Node 01)

```bash
cd /apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/bin/
./startNode.sh
```

✅ **Expected:**

```
ADMU3000I: Server nodeagent open for e-business
```

### Step 4.2 — On dsb-node02 (Node 02)

```bash
cd /apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver02/bin/
./startNode.sh
```

✅ **Expected:** Same message.

### Step 4.3 — Verify

DMgr console → **System Administration → Node agents**

✅ **Expected:** Both node agents show a **green arrow ▶ (Started)**.

Now the DMgr can **remotely start/stop servers** on both machines — the foremen are on duty.

---


