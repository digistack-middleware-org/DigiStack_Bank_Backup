# What we Achieve From these Version-4
## Deploy and RollBack the Application Practice

### 🎯 The Big Picture

**v4's purpose:** Make a tiny app change (just a "v4" label) so all attention
stays on **Sphere administration skills** — the daily tasks every WAS admin
does. No new features, no DB changes, no new Java classes.

---

## 1️⃣ Application States (Lifecycle)

| State | Meaning |
|---|---|
| **Installed** | Registered in WAS but not running |
| **Started** | Running and serving requests ✅ |
| **Stopped** | Not running — users get 503 |
| **Uninstalled** | Completely removed from WAS |

---

## 2️⃣ Two Deployment Paths

| Path | When to use | Analogy |
|---|---|---|
| **Update** | Same app name, new code — everyday deployments | Repainting the house (same address) |
| **Uninstall + Install** | App name changes, or corrupted install needs fresh start | Sell old house, buy new one |

**Update internals:** Stop app → swap EAR files → update config → restart.
(~30–90 sec. Name, URL, settings all preserved.)

**What v4 did:** Update path first (v3 → v4 code, name stayed v3) → then
Uninstall+Install to get the correct name `digistack-bank-v4`.

---

## 3️⃣ Stop / Start / Restart — App vs Server

| Action | User impact |
|---|---|
| **Stop app** | 503 error — server fine, other apps safe (one shop closed) |
| **Stop server** | Everything dies — no connection at all (whole mall closed) |
| **Restart** | Fresh start for a misbehaving app |

> ⚠️ Stopping the wrong thing on a shared server = big outage. Check **what** you're stopping.

**503 vs no connection:**
- `503` = server alive, app off
- No response = server down

This is how you diagnose issues.

---

## 4️⃣ Three Rollback Strategies

| Strategy | Use when |
|---|---|
| **VM Snapshot** | Disaster — revert everything (app + config + DB) |
| **Redeploy prior EAR** ✅ | Code broken, data fine — most common in real jobs |
| **restoreConfig + redeploy** | A WAS config change broke things |

**v4 practiced "Redeploy prior EAR"** because only code changed (label),
zero DB changes.

**Why practice calmly?** Fire-drill principle — real rollbacks happen at 2 AM
under pressure. Practice now = muscle memory later. Executing once beats
reading a doc.

---

## 5️⃣ Formal Clean Deployment (after rollback)

> 🏆 **Golden rule:** After any emergency rollback, always do a full clean
> forward deployment from a fresh build — never leave production running on a
> "shuffled" file from `/tmp/`.

- [ ] Build EAR fresh from source
- [ ] Verify artifact
- [ ] Prove both deploy paths (Console + wsadmin)
- [ ] Document everything (deployment record = receipt for auditors/teammates)

---

## 6️⃣ Request Flow

```text
Browser → Server port (9080) → Web container → App (context root) → Servlet/JSP → HTML back
```

   App Stopped → 503
   Server Stopped → no response at all

7️⃣ Rollback Request Flow

v4 running ✅ → 503 (app stopped) → 503 (v installing) → v3 started ✅

    Server stays up, only the app bounces
    URL unchanged, brief downtime window
    Proof: v3 label visible in browser
    Never leave production on a rollback-shuffled file → clean redeploy after

🏁 v4 Achievements Checklist

    Deploy via Admin Console
    Deploy via wsadmin
    Update path (same name)
    Uninstall + Install (new name)
    Stop / Start / Restart app + server
    503 vs connection-failure diagnosis
    Real rollback v4 → v3, verified, redeploy
    Formal clean deployment + documentation

