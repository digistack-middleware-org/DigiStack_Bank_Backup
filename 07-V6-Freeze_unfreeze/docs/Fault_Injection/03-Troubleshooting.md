# Simple Lesson: The Node Agent Fault (INC-v6-001)

## 1. What Happened?

- process stopped: the **Node Agent** on node02.
- That one stop caused **all six symptoms**.
- Nothing else broke.

> **Real-life example:** Think of a shop manager (DMgr) who can only talk
> to the shop (node02) through a phone line (Node Agent). The phone line
> is dead. The is still open and selling, but the manager cannot
> call it.

---

## 2. What is a Node Agent?

It does three jobs:

- **Receives config updates** from DMgr and saves them on the node.
- **Forwards admin commands** (start/stop server, deploy apps) to the node.
- **Reports health status** back so the console shows the node as alive.

**It is the only bridge** between DMgr and the node.
No bridge = no remote control.

---

## 3. Why Each Symptom Happened

| Symptom | Simple Reason |
|---|---|
| Node shows grey "Unavailable" | DMgr pings the Node Agent. No answer = grey. |
| Sync fails | Sync pushes files through the Node Agent. No agent = push fails. |
| JVM change missing on node02 | DMgr saved it, but delivery needs the Node Agent. Never delivered |
| App still works | App server is a separate process, already running. It doesn't need the Node Agent to serve traffic. |
| Restart from console fails | Restart command must travel through the Node Agent. No path = error. |
| wsadmin throws exception | wsadmin talks to live MBeans via the Node Agent. No agent = no connection. |

---

## 4. Why This Fault is Dangerous

- **Users see nothing wrong.** The app keeps working.
- **Basic monitoring sees nothing wrong.** HTTP checks return 200.
- But the node is **administratively dark**:
  - Can't push config.
  - Can't restart the server remotely.
  - If the app crashes, you **cannot bring it back from the console**.

> **Real-life example:** The shop is open, but the manager can't call it.
> If the shop suddenly closes, no one can tell the staff to reopen.

---

## 5. How to Investigate (If You Didn Know the Answer)

### Step 1 — Is the Node Agent running?
```bash
ps -ef | grepagent
```
- No process found = root cause found. (10 seconds!)

### Step 2 — Why did it stop?
```bash
tail -100 /apps/IBM/WebSphere/AppServer/profiles/<node02-profile>/logs/nodeagent/SystemOut.log
```
- `ADMU3201I stopping` = clean manual stop.
- `ADMU0111E` or OOM errors = crash, different problem.

### Step 3 — Is the app server still alive?
```bash
ps -ef | grep server1
```
- Yes = proves app server and Node Agent are separate.

### Step 4 — Is traffic still flowing?
```bash
curl -o /dev/null -s -w "%{http_code}" http://192.168.10.20/digistack-bank/Home
```
- 200 = users unaffected.

---


