# Ox Alpha — Sprint 8: Node Synchronization & Federation (Simple English Lesson)

---

## 1. What is WAS ND Cell?

- **ND** = Network Deployment.
- A **cell** is a group of servers managed from one place.
- The boss is the **Deployment Manager (DMgr)** — usually on `dsb-dmgr`.
- The workers are **managed nodes** — like `dsb-node02`.

**Real-life example:**
Think of a company. Head office (DMgr) sends policies. Branch offices (nodes) follow them.

---

## 2. What is Federation?

- **Federation** = joining a node to a cell.
- You run `addNode` on the node.
- The node registers with the DMgr.
- After that, the DMgr controls the node.

What happens during federation:
- A copy of the cell config comes to the node.
- The node gets a **node agent**.
- The DMgr now "owns" the node.

**Real-life example:**
A franchise store joining a big brand. It now follows head office rules.

---

## 3. What is a Node Agent?

- One **nodeagent** runs per node.
- It is the **middleman** between DMgr and the app servers on that node.
- Jobs:
  - Pass config changes from DMgr to app servers.
  - Start/stop app servers when DMgr asks.
  - Run periodic **node sync**.
  - Report node health to DMgr.

**Real-life example:**
The branch manager. Head office emails him. He tells the staff. If he quits, staff keep working — but nobody gets new instructions.

---

## 4. What is Node Synchronization?

- The DMgr keeps the **master config** (master repository).
- Each node keeps a **local copy**.
- **Sync** = copying changes from master to the node's local copy.

Three ways sync happens:
- **Automatic** — node agent pulls every few minutes (default config).
- **Manual** — Console → System administration → Nodes → Synchronize.
- **Command** — `syncNode.sh` (run when node agent is down).

Direction matters:
- Sync flows **DMgr → node** (config).
- Logs/files flow **node → DMgr** (for viewing in console).

---

## 5. What Happens When a Node Agent is Down?

This is the key lesson of this sprint:

- App servers on the node **keep running**. ✅
- Applicationskeep serving users**. ✅
- BUT:
  - New config changes **do not reach** the node. ❌
  - DMgr **cannot start/stop** app servers on it. ❌
  - Console shows the node as **not synchronized**. ❌

**The trap:** Everything *looks* fine from the user side. Nobody notices until a change is needed — or a restart fails.

**Real-life example:**
The branch manager is on leave. Staff keep selling. But no new price list arrives. Orders sent to head office go unanswered.

---

## 6. Signs of a Down Node Agent (Your Diagnosis Clues)

In Phase 2, watch for these:

- Console: node shows **"not synchronized"** or **unknown status**.
- Click **Synchronize** → error like:
  - "Could not connect to node agent"
  - ADMU or sync exception mentioning the node.
- `serverStatus.sh -all` on the node: nodeagent missing.
- DMgr `SystemOut.log`: failed connection to node agent port (default **9101** — ORB bootstrap).
- Deployments or restarts on that node fail, but users see nothing wrong.

---

## 7. How to Fix It (For After Your Diagnosis)

```bash
cd /apps/IBM/WebSphere/AppServer/profiles/<node02-profile>/bin
./startNode.sh
```