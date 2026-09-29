# Sprint 2 — WAS Deployment + XA Transactions (digistack-bank)

## 1. Overview

This sprint deploys the **digistack-bank** application to WebSphere Application Server (WAS) and demonstrates the difference between **XA (coordinated) transactions** and **Non-XA (uncoordinated) transactions** using a bank transfer scenario.

### The Problem

A bank transfer involves two steps:

1. Debit Rs.500 from Account A
2. Credit Rs.500 to Account B

If step 1 succeeds and step 2 fails, money vanishes. A **transaction** guarantees both steps succeed together, or both fail together (all-or-nothing).

### XA vs Non-XA Comparison

| Type | Meaning | Behavior when credit fails |
|------|---------|---------------------------|
| **XA** (real transaction) | WAS coordinates both databases | Debit is rolled back — balances unchanged ✅ |
| **Non-XA** (no coordination) | Each DB does its own thing | Debit is committed anyway — money "lost" ❌ |

> [!TIP]
> **Analogy:** XA is a wedding planner — if the caterer cancels, the whole wedding is cancelled. Non-XA is no planner — the caterer cancels, but the photographer still shows up and gets paid. Chaos.

## 2. Environment

| Machine | IP | Role |
|---------|-----|------|
| `dsb-dmgr` | 192.168.10.10 | WAS Deployment Manager. Admin Console lives here. |
| `dsb-db` | — | PostgreSQL database. Balances live here. |
| Web server (IHS) | 192.168.10.20 | The URL users hit. Sends requests to WAS. |

### Key Terms

- **Cell** — the whole WAS kingdom (`devdsbincell01`)
- **Cluster** — a group of identical app servers (`devdsbinappcluster01`) for load sharing
- **Node** — one machine's WAS agent (`devdsbinnode01`, `devdsbinnode02`)
- **EAR file** — the packaged application: `digistack-bank-v8.5.ear`

## 3. Step 11 — Deploy the New App (Admin Console)

Deployment flow: **Stop → Uninstall → Install → Start → Sync**.

### 3.1 Remove the Old Version

1. Open a browser → `http://192.168.10.10:9060/ibm/console`
2. Log in as `wasadmin`.
3. Navigate to **Applications → Application Types → WebSphere enterprise applications**.
4. Find `digistack-bank-v8`.
5. Tick the checkbox → click **Stop**. Wait until status says **Stopped**.
6. Tick again → click **Uninstall** → **OK**.
7. Click **Save** (yellow banner).

> [!NOTE]
> **Why stop first?** You cannot remove an app that is running — like removing an engine while the car is on. **Save** commits your changes to WAS's config.

### 3.2 Install the New Version

1. In the left menu → click **Install**.
2. Choose **Remote file system** — the EAR sits on the server (`/tmp/digistack-bank-v8.5.ear`), not on your laptop. WAS fetches it remotely.
3. Browse → select the EAR → **OK** → **Next**.
4. Choose **Fast Path** — "use sensible defaults, let me change what matters."
5. **Step 2 — Map modules to servers** (the important step):
   - Set the WAR module's target to:

     ```text
     WebSphere:cell=devdsbincell01,cluster=devdsbinappcluster01
     ```

   - Translation: "run this app on the cluster, not on one random server." Cluster = both nodes run it = high availability.
6. Click **Next** through the rest → **Finish** → **Save**.

### 3.3 Start the App

1. In the app list, tick `digistack-bank-v8.5` → click **Start**.
2. Refresh the page.

✅ **Expected:** green arrow = running.

### 3.4 Sync Nodes

The Deployment Manager holds the master config; the actual servers (node01, node02) are separate machines. **Sync = copying the plan to the workers.**

1. Left menu: **System administration → Nodes**
2. Tick both nodes → **Full Resync** → both show **Synchronized** ✅

> [!TIP]
> **Analogy:** Head office updates the rulebook; each branch must receive the new copy. Resync = sending copies.

## 4. Step 11b — Automated Deployment (wsadmin)

`wsadmin` is WAS's command-line tool. A Jython script performs the same console steps, repeatable and documented.

### Script Logic (Jython)

```python
# 1. Find the old app, stop it, uninstall it
AdminControl.invoke(..., 'stop')     # stop app
AdminApp.uninstall(OLD_APP)          # remove app
AdminConfig.save()                   # save config

# 2. Install new EAR straight onto the CLUSTER
AdminApp.install(EAR_PATH, '-appname ... -cluster ...')

# 3. Start it
AdminControl.invoke(..., 'start')

# 4. Sync both nodes
AdminControl.invoke('WebSphere:type=NodeSync,node=...', 'sync')
```

### Rules

- Run it **instead of** the console steps — never both.
- Replace `<your-password>` with the actual password.
- Port **8879** = the SOAP admin port (the "phone line" wsadmin uses to talk to the DMgr).

