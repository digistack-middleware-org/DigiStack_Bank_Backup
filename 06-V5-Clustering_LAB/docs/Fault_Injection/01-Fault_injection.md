## Phase 1 — Fault Injection 💉

> ⚠️ **The fault is live once Step 2 is done. Do not investigate, fix, or attempt to correct anything yet.**

---

### Step 1 — Confirm Clean Baseline

Open both URLs in a browser:

```
http://192.168.10.10:9080/digistack-bank/Home
http://192.168.10.11:<node2-port>/digistack-bank/Home
```

**Expected:**
- ✅ Both load normally
- ✅ Footer shows: `v5 — WAS Clustering`
- ✅ Database: **Connected** (green)
- ✅ Both cluster members **Started** in Admin Console

> 🛑 If not clean — **STOP** and report before continuing.

**Result:** ⬜

---

### Step 2 — Inject the Fault

On the **dsb-node02** VM:

```bash
firewall-cmd --add-port=5432/tcp --permanent --zone=drop 2>/dev/null
iptables -A OUTPUT -d 192.168.10.30 -p tcp --dport 5432 -j DROP
```

> ℹ️ **No output expected from the second command. That is correct.**

**Result:** ⬜

---

### Step 3 — Confirm the Injection

```bash
iptables -L OUTPUT -n | grep 192.168.10.30
```

**Expected result:**

```
DROP  tcp  --  0.0.0.0/0  192.168.10.30  tcp dpt:5432
```

**Result:** ⬜

---

### Step 4 — Trigger the Fault

| Step | Action | Observation | Result |
|------|--------|-------------|--------|
| 4.1 | Open Node2 directly: `http://192.168.10.11:<node2-port>/digistack-bank/Home` | Note status bar | ⬜ |
| 4.2 | Navigate to Node1 directly: `http://192.168.10.10:9080/digistack-bank/Home` | Note status bar | ⬜ |
| 4.3 | Log in via **Node1** as `customer1 / Customer@123` | Does login succeed? | ⬜ |
| 4.4 | If logged in → Account page via **Node1** → deposit **₹500** | Note the result | ⬜ |
| 4.5 | Same browser → Node2 directly: `http://192.168.10.11:<node2-port>/digistack-bank/Dashboard` (session cookie carries over) | the result | ⬜ |

---
