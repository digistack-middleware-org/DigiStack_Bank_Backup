# 🔍 Root Cause Analysis — INC-v5-001

---

## 💉 What Was Injected

An **outbound firewall rule** was added on the `dsb-node02` VM, dropping all outbound TCP traffic to `192.168.10.30` (`dsb-db`) on port `5432`:

```bash
iptables -A OUTPUT -d 192.168.10.30 -p tcp --dport 5432 -j DROP
```

> This is a **network-layer block applied at the OS level on one specific VM** — it has nothing to do with WAS configuration, the cluster definition, or PostgreSQL itself.

---

## 🎯 Why One Member Failed and the Other Did Not

- `server1` on `devdsbinnode02` (`dsb-node02`) is **physically hosted on the VM where the DROP rule was applied**. Every outbound connection attempt from that VM to `192.168.10.30:5432` is **silently discarded by the kernel's netfilter rules** before it ever leaves the VM.
- `server1` on `devdsbinnode01` (`dsb-dmgr`) is a **completely separate VM with no such rule** — its connections to PostgreSQL succeed normally.

Since the cluster **load-balances incoming requests across both members**, roughly half of all customer sessions were routed to the affected member and experienced DB failures, while the other half — routed to the healthy member — saw no issue at all.

> 🧩 This explains the exact **"it works for me but not my colleague"** pattern in the incident ticket — a classic signature of a **per-member infrastructure fault** in a load-balanced cluster, as opposed to an application-wide fault.

---

## 🤔 Why Every WAS-Level Status Indicator Showed Healthy

| Indicator | Why It Looked Fine |
|---|---|
| Node Agent running | Federation and cell membership are **independent of database connectivity** |
| Application "Started" | WAS considers an application started once its **classes load and servlets initialize**. A failed runtime DB call inside `HomeServlet.doGet()` or `AccountServlet.doPost()` is **caught by the existing try/catch blocks** (built in v1 Sprint 3 and v3 Sprint 1) and handled gracefully with an error message — it does not crash the servlet or bring down the application |
| PostgreSQL healthy | The DB was **completely healthy and reachable from every other host** — checking the DB server directly showed no problem, which could mislead an investigator into ruling the database prematurely |

---

## 📌 The Exact Evidence That Confirms the Root Cause

On the affected node (`devdsbinnode02`), grepping `SystemOut.log` shows:

```
HomeServlet: DB read FAILED — Connection to 192.168.10.30:5432 refused. Check that the hostname and port are correct...
```

or, more specifically for a silently-dropped (not refused) connection:

```
HomeServlet: DB read FAILED — java.net.SocketTimeoutException: connect timed out
```

### 🔑 Diagnostic Distinction: REFUSED vs TIMEOUT

| Log Symptom | Meaning | Typical Cause |
|---|---|---|
| `Connection refused` | Remote host **actively rejected** the connection | PostgreSQL not listening, or a firewall on the **DB side** returning a REJECT |
| `connect timed out` | Packets **silently dropped in transit** | An outbound **DROP** iptables rule (as opposed to a REJECT rule) |

> This distinction alone should point an experienced admin toward a **network-path problem** rather than a PostgreSQL configuration problem.

---

## 🛠️ Confirming Network-Level Diagnosis (Fastest Path to RCA)

### 1 — From `dsb-node02` itself:

```bash
telnet 192.168.10.30 5432
# or
nc -zv 192.168.10. 5432
```

> ⏳ This **hangs indefinitely (DROP)** rather than immediately refusing (REJECT) — a telltale sign.

### 2 — From `dsb-dmgr` (the unaffected node):

The same command **connects successfully instantly** — proving the database and network path are fine everywhere except from the one affected host.

### 3 — Inspect the firewall (the smoking gun 🚬):

```bash
iptables -L OUTPUT -n | grep 192.168.10.30
```

> On `dsb-node02` this directly reveals the injected **DROP rule**.

---


