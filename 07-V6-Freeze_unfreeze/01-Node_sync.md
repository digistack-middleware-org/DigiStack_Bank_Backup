# Node Sync vs. Full Resync — Sprint 1 Deep Dive

---

## 1. What is a WebSphere Cell?

Think of a **cell** like a company.

- **DMgr (Deployment Manager)** = Head office
  - Keeps the **master copy** of all configuration
  - Lives on `dsb-dmgr` at
    `/apps/IBM/WebSphere/AppServer/profiles/devdsbindmgr01/config/`
- **Nodes** = Branch offices
  - Each federated node keeps a **local copy** of the config it needs
  - Servers actually **run** using this local copy, not the DMgr copy directly

> **Real-life example:** Head office writes a new policy. Branch employees keep
> working from old printed copies until someone delivers the new policy to them.

---

## 2. What is Node Sync?

The **Node Agent** is a small background process on each node — it is the
delivery driver.

- Every **60 seconds** (default), it asks the DMgr:
  > "Has anything changed for me?"
- If yes → it downloads **only the changed files**
- This is **Node Sync**

### Why it matters

- You change a setting in the Admin Console
- It saves to the DMgr **immediately**
- But the running server **doesn't see it yet**
- Only after Node Sync does the change take effect

> **Real-life example:** You send an email update, but your teammate only
> checks email once a minute. Until they check, they don't know.

---

## 3. Node Sync vs. Full Resync

|              | Node Sync                    | Full Resync                                     |
| ------------ | ---------------------------- | ----------------------------------------------- |
| **What**     | Copies only changed files    | Wipes local config, copies **everything** fresh |
| **When**     | Auto every 60 sec, or manual | Manual only — never automatic                   |
| **Speed**    | Fast (seconds)               | Slow (can take minutes)                         |
| **Use when** | Normal daily changes         | Node config corrupted, drifted, or untrusted    |

> **Real-life example:**
>
> - Node Sync = getting a new page for your book
> - Full Resync = throwing away the book and getting a brand new copy

---

## 4. Exercise — Step by Step

### Step 1: Check the environment

- [x] DMgr running on `dsb-dmgr` 
- [x] `dsb-node02` federated to DMgr

---

### Step 2: Check the Node Agent

No agent = no sync.

```bash
ps -ef | grep nodeagent
```

- Java process visible → running ✅
- Nothing → start it:

```bash
cd /apps/IBM/WebSphere/AppServer/profiles/<node-profile>/bin
./startNode.sh
```

Look for:

```text
ADMU3000I: Server nodeagent open for e-business; process id is <number>
```

---

### Step 3: Check the network

```bash
ping -c 3 192.168.10.10
```

- 3 packets received, 0% packet loss = ✅

---

### Step 4: Normal Node Sync (GUI)

1. Browser → `https://192.168.10.10:9043/ibm/console`
2. **System administration → Nodes**
3. Confirm Node Status = "Synchronized"
4. Select the `dsb-node02` node's checkbox
5. Click **Synchronize** (NOT "Full Resynchronize")
6. Green success message = done ✅

---

### Step 5: Node Sync via wsadmin (Jython)

Real admins use **wsadmin** — it's scriptable and scales.

```bash
cd /apps/IBM/WebSphere/AppServer/profiles/devdsbindmgr01/bin
./wsadmin.sh -lang jython
```

At the `wsadmin>` prompt:

```python
print AdminTask.listNodes()
AdminControl.invoke(AdminControl.completeObjectName('type=NodeSync,node=<NODE_NAME>,*'), 'sync')
quit()
```

> ⚠️ Use the **exact node name** from `listNodes()`
> (e.g. `devdsbinnode01Node01`), not the hostname.

---

### Step 6: Deliberately create drift

**Drift** = hand-editing node config files, bypassing the DMgr.
Bad practice — but happens in real ops teams.

On `dsb-node02`:

```bash
cd .../config/cells/<cell>/nodes/<node>
cp variables.xml variables.xml.bak     # always backup!
nano variables.xml
```

Add just before the closing `</xmi:XMI>` tag:

```xml
<!-- DRIFT-TEST-v6-Sprint1 -->
```

Save (Ctrl+O, Enter, Ctrl+X).
Now the node's copy differs from the DMgr's master copy.

---

### Step 7: Prove normal sync does NOT fix drift ⭐

**This is the key lesson of Sprint 1.**

- Run a normal **Synchronize** again (GUI or wsadmin)
- It reports success
- Check the file:

```bash
grep "DRIFT-TEST" .../nodes/<node>/variables.xml
```

- The comment is **still there** ✅ (drift survives)

**Why?** Normal sync only pulls **DMgr → node** changes.
The DMgr didn't change, so nothing is synced. Local junk is ignored.

> **Real-life example:** The delivery driver only brings new pages.
> He never checks if your book has graffiti in it.

---

### Step 8: Fix drift with Full Resync

**GUI:**

1. System administration → Nodes
2. Select the node
3. Click **Full Resynchronize**

**wsadmin:**

```python
AdminControl.invoke(AdminControl.completeObjectName('type=NodeSync,node=<NODE_NAME>,*'), 'syncActiveModel')
```

- `sync` = normal sync
- `syncActiveModel` = **full** resync (wipe and recopy everything)

Takes longer — it recopies the entire node config tree.

---

### Step 9: Verify + clean up

```bash
grep "DRIFT-TEST" .../nodes/<node>/variables.xml
```

- No output = drift gone ✅

Remove the backup:

```bash
rm .../nodes/<node>/variables.xml.bak
```
---
