# Turning Off Transaction Trace in WebSphere — Complete Guide

This document explains how to safely disable full transaction tracing (`*=all`) on a WebSphere Application Server cluster and restore the default trace specification (`*=info`), using both the Admin Console and an automated `wsadmin` script.

---

## 1. Why Disable Transaction Trace?

Full tracing (`*=all` or `Transaction=all`) records everything the server does — useful for short diagnostic captures, but dangerous if left enabled.

Under load, `trace.log` can grow to **gigabytes in minutes**, causing:

- **Disk full** on the server
- **Degraded application performance**
- **Signal-to-noise problems** — real issues get buried in trace noise

> [!IMPORTANT]
> **Golden rule:** Turn trace **ON** → capture the problem → turn trace **OFF**. Never leave full tracing enabled.

---

## 2. Understanding the Trace Specification: `*=info`

A **trace specification** is a rule that tells WebSphere what to record.

`*=info` breaks down as:

| Part | Meaning |
|------|---------|
| `*` | All components |
| `info` | Informational messages and above (warnings, errors) |

This is the **default, safe setting**.

> [!TIP]
> Think of it as: *"Note the headlines only, not every word of the day."*

---

## 3. Method 1 — Admin Console (Manual)

> [!NOTE]
> A cluster has **two members** (servers). Each has its **own** trace setting — you must repeat these steps for **both**.

### 3.1 Member 1 (`server1`)

1. Open the **Admin Console** in a browser.
2. Navigate: **Troubleshooting → Logs and Trace**.
3. Click the server name (e.g., `server1`).
4. Click **Diagnostic Trace Service**.
5. Click the **Runtime** tab.
6. Clear the **Trace Specification** field.
7. Enter: `*=info`
8. Click **Apply**.

**Expected result:** A green confirmation banner appears; trace is restored to info-only.

> [!NOTE]
> **Runtime tab** changes take effect **immediately** (no restart). The **Configuration tab** requires a restart.

### 3.2 Member 2 (`server2`)

Repeat the **exact same steps** for `server2` — same path, same value: `*=info`.

> [!TIP]
> **Analogy:** Changing the thermostat in two rooms — you must visit both. Forgetting `server2` means it keeps recording everything.

---

## 4. Method 2 — `wsadmin` Script (Automation)

Manual steps are fine once, but repeated admin work should be automated (see **NDS01 Rule 7**).

### 4.1 What Is `wsadmin`?

- A **command-line tool** for WebSphere administration.
- Runs scripts (we use **Jython**, a Python-like language) instead of console clicks.

### 4.2 The Script — Line by Line

```python
NORMAL_SPEC = '*=info'
```
The safe setting we want to restore.

```python
CLUSTER_MEMBERS = {
    'devdsbinnode01': 'devdsbinappcluster01_server1',
    'devdsbinnode02': 'devdsbinappcluster01_server2'
}
```
A simple mapping: **node name → server name**.

- **Node** = the physical machine (or managed profile).
- **Server** = the running instance on that node.

```python
query = ('WebSphere:type=TraceService,node=...,process=...,*')
```
Ask WebSphere: *"Find the `TraceService` object for this server."* The `TraceService` component controls trace — like the thermostat control.

```python
traceServiceMBean = AdminControl.queryNames(query)
```
`queryNames` searches for that object (an **MBean** — a "control panel" the admin tool can talk to).

```python
if not traceServiceMBean ...:
    print('WARNING: TraceService MBean not found...')
    continue
```
If the server is not running, the MBean does not exist. We print a warning and **skip instead of crashing** — one broken server should not stop the other.

```python
AdminControl.setAttribute(traceServiceMBean, 'traceSpecification', NORMAL_SPEC)
```
The actual action — sets trace to `*=info`. **Equivalent to clicking Apply in the console.**

```python
print('Trace DISABLED...')
```
Confirms success for that server.

### 4.3 Running the Script

```bash
wsadmin.sh -lang jython -conntype SOAP -host 192.168.10.10 -port 8879 \
  -user wasadmin -password <password> -f disable-xa-trace-v8.5.py
```

| Parameter | Purpose |
|-----------|---------|
| `-lang jython` | Script language |
| `-conntype SOAP` | Connection method to the Deployment Manager |
| `-host 192.168.10.10` | Admin server address |
| `-port 8879` | Standard SOAP admin port |
| `-user` / `-password` | Admin credentials |
| `-f` | Script file to execute |

---

## 5. Console vs Script — Which One?

| Criteria | Admin Console | `wsadmin` Script |
|----------|---------------|------------------|
| **Speed** | Slow (many clicks) | Fast (one command) |
| **Good for** | One-time tasks, learning | Repeated tasks, production work |
| **Error risk** | Human typos | Same script every time |
| **Audit trail** | Manual | Copy of command + output |

> [!TIP]
> **Trainer's tip:** Learn it in the console first. Once you understand it, script it.

---

## 6. Verification Checklist

- **Console:** Confirmation banner appears; **Runtime** tab shows `*=info`.
- **Script:** Two `Trace DISABLED` messages printed.
- **Real check:** Watch `trace.log` — it should **stop growing rapidly**.
- **Extra check:** Compare file size after a few minutes under load.

---

## 7. Key Rules — Mnemonic: **"CAP-OFF"**

- **C**apture first (trace **ON** while reproducing)
- **A**pply `*=info` to **BOTH** members
- **P**refer scripts for repeat work
- **OFF** means off on *every* server — check `server2`!

---

## 8. Common Mistakes to Avoid

- ❌ **Forgetting `server2`** — the most common error in clusters.
- ❌ **Editing the Configuration tab** instead of Runtime → change waits for a restart.
- ❌ **Typo in the spec**, e.g. `* =info` with a space.
- ❌ **Not verifying** that `trace.log` actually stopped growing.
- ❌ **Running the script while servers are stopped** — you will only get warnings (no harm done), but trace is not restored either.

---

## Quick Summary

| Item | Detail |
|------|--------|
| **Step 9** | Console method — set `*=info` on both members, **Runtime** tab |
| **Step 10** | Script method — one `wsadmin` command handles both servers |
| **Why** | `Trace=all` under load = huge files, slow system, full disk |
| **Verify** | `trace.log` stops growing — confirm **both members** |
