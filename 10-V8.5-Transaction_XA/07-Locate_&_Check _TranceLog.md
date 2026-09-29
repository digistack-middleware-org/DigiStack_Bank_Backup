# WebSphere Application Server — Transaction Log (tranlog) Reference Guide

A practical guide to understanding, locating, and verifying the WAS transaction log (`tranlog`) in a 2-node cluster environment.

---

## 1. Overview

The transaction log (`tranlog`) is WebSphere Application Server's "notebook" of in-flight transactions. When a transaction spans multiple resources (e.g., two databases), WAS records its decisions here so it can recover correctly after a crash.

Without a valid `tranlog`, in-doubt transactions cannot be resolved — resulting in stuck locks, held resources, and failed recoveries.

---

## 2. Where Does the tranlog Live?

> [!IMPORTANT]
> **Rule #1:** The `tranlog` lives on the machine where the **application server runs** — **NOT** on the Deployment Manager (DMgr).

### Why?

- The **application server** is the component actually executing transactions.
- It must write its transaction notebook to its **local disk** (fast, always available).
- The **DMgr** only manages the cell — it does not run your transactions.

### Example Layout (2-Node Cluster)

| Machine      | Role                          | tranlog Path                                                                    |
|--------------|-------------------------------|----------------------------------------------------------------------------------|
| `dsb-dmgr`   | DMgr + member 1 (`node01`)    | `/apps/IBM/WebSphere/AppServer/profiles/devdsbinnode01/tranlog/`                 |
| `dsb-node02` | member 2 (`node02`)           | `/apps/IBM/WebSphere/AppServer/profiles/devdsbinnode02/tranlog/`                 |

**Key points:**

- There is **one tranlog directory per node profile**.
- Check **each machine separately**.
- `dsb-dmgr` also hosts an application server (member 1), so it has a tranlog too.
- `node02`'s tranlog exists only on `dsb-node02` — you must **SSH there** to see it.

---

## 3. Directory Structure — How to Read the Path

```text
/apps/IBM/WebSphere/AppServer/profiles/devdsbinnode01/tranlog/
    └── devdsbincell01/          ← cell name
        └── devdsbinnode01/      ← node name
            └── <server1name>/   ← server name
                ├── TransactionLog
                └── PartnerLog
```

> [!TIP]
> **Memory trick:** **Cell → Node → Server.** The path mirrors the WAS topology, top-down.

### File Descriptions

| File             | Purpose                                                                 |
|------------------|-------------------------------------------------------------------------|
| `TransactionLog` | Records WAS's votes/decisions for each transaction (commit/rollback).   |
| `PartnerLog`     | Records which resource managers (e.g., databases) were involved.        |

---

## 4. The Commands — What Each One Does

### 4.1 Check the Directory Exists

```bash
ls -lh /apps/IBM/WebSphere/AppServer/profiles/devdsbinnode01/tranlog/
```

- `ls -lh` = list files, human-readable sizes, long format.
- **Expected output:** a folder named after your cell (e.g., `devdsbincell01`).
- Permissions should show the owner as `wasadmin` — the WAS runtime user.

### 4.2 Find the Actual Files

```bash
find /apps/IBM/WebSphere/AppServer/profiles/devdsbinnode01/tranlog/ -type f
```

- `-type f` = show only **files**, not folders.
- This walks the entire `Cell/Node/Server` tree and prints full paths to `TransactionLog` and `PartnerLog`.

### 4.3 Check node02 — You Must Go There

```bash
ssh wasadmin@192.168.10.11
find /apps/IBM/WebSphere/AppServer/profiles/devdsbinnode02/tranlog/ -type f
```

> [!NOTE]
> You **cannot** see node02's tranlog files from the DMgr machine. They reside on physically separate disks.

### 4.4 Check the File Sizes

```bash
ls -lh /apps/IBM/WebSphere/AppServer/profiles/devdsbinnode01/<server1name>/
```

**Expected output:**

```text
-rw-r--r--. 1 wasadmin wasadmin 512K ... TransactionLog
-rw-r--r--. 1 wasadmin wasadmin 512K ... PartnerLog
```

---

## 5. Why 512 KB?

| Property            | Detail                                                            |
|---------------------|-------------------------------------------------------------------|
| Default size        | **512 KB** per file (WAS default)                                 |
| Allocation          | Files are **pre-allocated** — size stays fixed                    |
| Reuse behavior      | WAS reuses space internally; size does not grow with activity     |
| Content format      | **Binary** — managed 100% internally by WAS                       |

> [!WARNING]
> **Never** `cat`, `vi`, or edit these files. They are binary and WAS-managed. Corrupting them will **break crash recovery**.

> [!TIP]
> Don't panic if you see 512K even on a busy system — that is **normal**.

---

## 6. Real-Life Failure Story — Why This Matters

### Scenario

1. Your app **debits Account A** (Database 1 says "yes").
2. **Server power failure** — before Account B is credited.
3. Database 1 has the money held and locked — the transaction is now **in-doubt**.
4. The server restarts.
5. WAS reads the **TransactionLog**: *"I voted to commit this Xid."*
6. WAS reads the **PartnerLog**: *"Database 1 and Database 2 were involved."*
7. WAS **replays Phase 2** → tells both databases: **Commit**.
8. Money moves correctly. **The customer never notices the crash.**

### Outcome Without a tranlog

| With tranlog                        | Without tranlog                   |
|-------------------------------------|-----------------------------------|
| Automatic recovery on restart       | Stuck locks                       |
| In-doubt transactions resolved      | Stuck money                       |
| Customers unaffected                | Angry bank customers              |

---

## 7. Quick Checklist

- [ ] SSH to the machine hosting each **application server** (not just the DMgr).
- [ ] Verify the tranlog directory exists: `ls -lh <profile>/tranlog/`.
- [ ] Confirm `TransactionLog` and `PartnerLog` files exist via `find -type f`.
- [ ] Verify owner is `wasadmin` and size is ~512K.
- [ ] Repeat for **every** node in the cluster.
- [ ] Never open, edit, or delete the log files.
