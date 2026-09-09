# ✅ Fix — Restore the Environment

### Step 1 — Remove the injected iptables rule (on `dsb-node02`)

```bash
iptables -D OUTPUT -d 192.168.10.30 -p tcp --dport 5432 -j DROP
```

### Step 2 — Confirm the rule is gone

```bash
iptables -L OUTPUT -n | grep 192.168.10.30
```

**Expected result:** no output (rule removed).

### Step 3 — Confirm connectivity is restored

```bash
nc -zv 192.168.10.30 5432
```

**Expected result:** immediate success message, e.g.:

```
Connection to 192.168.10.30 5432 port [tcp/*] succeeded!
```

### Step 4 — Confirm via the browser on Node2 directly

```
http://192.168.10.11:<node2-port>/digistack-bank/Home
```

**Expected result:** `Database: Connected` in green, matching Node1.

### Step 5 — Confirm in `SystemOut.log` on `dsb-node02`

```bash
grep "HomeServlet" \
  /apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver02/logs/server1/SystemOut.log \
  | tail -5
```

**Expected result:**

```
HomeServlet: DB read successful. bank.name=DigiStack Bank
```

### Step 6 — Full regression on the previously-affected member

Log in via **Node2** directly → load Dashboard → navigate to Account page → perform a small deposit → confirm success.

### Step 7 — Confirm cluster-wide consistency

Load both nodes side by side:

```
http://192.168.10.10:9080/digistack-bank/Home
http://192.168.10.11:<node2-port>/digistack-bank/Home
```

**Expected:** both identical, both green.

---

# 🛡️ Prevention

### What Would Have Caught This Faster

**1. Per-member health checks, not just cluster-level status.**
WAS's "application Started" status is a **poor proxy** for "application is actually functioning end-to-end." A synthetic transaction monitor that periodically hits **each cluster member's URL directly** (bypassing the load balancer) and checks for `Database: Connected` would have isolated this to one specific member within seconds — rather than relying on ambiguous, inconsistent customer complaints.

**2. Network path monitoring between application-tier and data-tier hosts.**
A simple periodic `nc -zv` or equivalent check from every WAS node to the database host, alerting on failure, would catch this class of fault immediately and **specifically name the affected host** — exactly what a Prometheus node-export + blackbox-exporter combination (introduced in this project at P04) is designed for.

**3. Distinguish DROP from REJECT in firewall policy documentation.**
Any legitimate outbound firewall change to a WAS node should use **REJECT** (fails fast, produces a clear "connection refused" in application logs) rather than **DROP** (produces an ambiguous, slow timeout) — unless silent dropping is specifically the intended security posture. This incident's symptom would have been diagnosed in **seconds** if the log had shown "refused" instead of "timed out."

**4. Change record specificity.**
The maintenance record only said *"outbound connectivity adjustment"* — no specific rule, no specific host, no specific reasoning. Requiring **exact iptables/firewalld rule text in every change record** (matching the project's Golden Rule: *"Document every change"*) would have let the on-call admin immediately correlate the timing and content of the incident with the change log — without needing to run diagnostics at all.

---

## 💡 The Bigger Picture — Why Clustering Earned Its Keep

This fault, while disruptive to roughly half of traffic, did **NOT** cause a full outage — exactly because the cluster has **two independent members on two independent hosts**.

| Deployment | Result of This Same Fault |
|---|---|
| Single-server (pre-v5) | **100% outage** |
| Clustered (v5) | ~50% of traffic degraded; the rest unaffected |

> This incident is a good illustration of clustering's **partial-failure-tolerance** value, even when the specific failure mode (per-node network partition) isn't the "kill a whole server" scenario tested in Sprint 5.