# Rollback Exercise — Deliberate Rollback to v3, Verify, Redeploy v4
## 💡 Concept — Why practice rollback deliberately

In a real incident, rollback is performed **under pressure, often at 2 AM,
often by someone who has never done it before**. Practicing the exact steps
now — calmly, with nothing actually broken — builds the **muscle memory** so
that when a real emergency happens, the procedure is already familiar.

> Executing it once, deliberately, is what separates **documentation** from
> **competence**.

## 💡 Concept — Rollback scope for v4

This rollback is a **surgical redeploy of the prior EAR** — not a VM snapshot
restore. No database schema changed between v3 and v4 (v4 made **zero DB
changes**), so a code-only rollback is sufficient and correct.

> 🏆 This is the most common real-world rollback scenario:
> *"the new code has a problem, the data is fine, just put the old code back."*

---

# Step 1 — Confirm Current State Before Rollback

## Step 1.1 — Confirm v4 is currently running

```
http://192.168.10.10:9080/digistack-bank/Home
```

**✅ Expected result** — footer shows `v4 — Application Lifecycle`.

## Step 1.2 — Confirm the account balance before rollback

(So you can verify data is untouched by the rollback:)

```bash
psql -U digistack_app -d digistack_bank -h 127.0.0.1 \
  -c "SELECT id, balance FROM accounts WHERE user_id = 1;"
```

> 📝 **Note down the exact balance value shown.**

**✅ Expected result** — a row with the current balance from your Sprint 2/3
transactions.

## Step 1.3 — Confirm the Admin Console shows `digistack-bank-v4` as Started

**Applications → Application Types → WebSphere enterprise applications**

**✅ Expected result** — `digistack-bank-v4` with **green arrow ▶**.

---

# Step 2 — Perform the Rollback via Admin Console (GUI Method)

## 💡 Concept — Rollback via Update

Since the application is currently registered as `digistack-bank-v4`, rolling
back to v3 code means using the **Update path** (same registered name) with
the v3 rollback EAR as the replacement content.

- You are **not** changing the application name back to `digistack-bank-v3`
  — that would be a full uninstall/reinstall.
- You are simply putting the **older code under the current registration**,
  which is a legitimate rollback pattern when only **code** (not naming)
  needs reverting.

## Step 2.1 — Open the Admin Console

```
http://192.168.10.10:9060/ibm/console
```

## Step 2.2 — Navigate to

**Applications → Application Types → WebSphere enterprise applications**

## Step 2.3 — Click on `digistack-bank-v4`

## Step 2.4 — Click **Update**

## Step 2.5 — Select

```
(•) Replace the entire application
```

## Step 2.6 — Click **Browse** and select the rollback EAR

```
C:\Projects\digistack-bank-parent\digistack-bank-ear\target\digistack-bank-v3-rollback-copy.ear
```

Click **Open**.

## Step 2.7 — Click **Next** through the wizard

✅ Accepting all defaults.

## Step 2.8 — On the Summary page, confirm

- [x] Application name: `digistack-bank-v4` (unchanged — only the code is rolling back)

Click **Finish**.

**✅ Expected result:**

```
Application digistack-bank-v4 updated successfully.
```

## Step 2.9 — Click **Save**

## Step 2.10 — Stop and Start to ensure the rollback code is fully active

Tick `digistack-bank-v4` → **Stop** → wait for red ❌ → tick again → **Start**.

**✅ Expected result** — **green arrow ▶**.

---

# Step 3 — Verify the Rollback Took Effect

## Step 3.1 — Open the browser and navigate to

```
http://192.168.10.10:9080/digistack-bank/Home
```

## Step 3.2 — Read the footer

**✅ Expected result** — the footer now shows the **OLD** label:

```
DigiStack Bank — © 2026. For educational purposes only. | WebSphere ND 9.0.5.28 | v1 — Foundation
```

> 🔑 The `v4 — Application Lifecycle` label is **GONE** — replaced by the
> original `v1 — Foundation` text that was in `Home.jsp` before the Sprint 1
> edits of this version. This is the **visible proof that the rollback
> succeeded** — you deployed genuinely older code.

## Step 3.3 — Confirm the application is still fully functional

Log in as `customer1` / `Customer@123`. Confirm Dashboard loads.

## Step 3.4 — Confirm the database was NOT affected by the rollback

```bash
psql -U digistack_app -d digistack_bank -h 127.0.0.1 \
  -c "SELECT id, balance FROM accounts WHERE user_id = 1;"
```

**✅ Expected result** — the balance is **exactly the same** as what you
recorded in Step 1.2.

> 🔑 The rollback only reverted **application code** — it never touched the
> database. This is the key property of a code-only rollback:
> **data integrity is preserved**.

## Step 3.5 — Confirm a transaction still works on the rolled-back code

Navigate to the **Account** page, perform a small **deposit of ₹100**.

**✅ Expected result** — deposit succeeds normally. The rolled-back code is
functionally identical to v4 except for the footer label — this confirms the
rollback did not break any functionality.

## Step 3.6 — Record the new balance after this test deposit

> 📝 You will need to verify it survives the forward-redeploy in Step 6.

---

# Step 4 — Confirm the Rollback in SystemOut.log

```bash
grep -E "WSVR0221I|WSVR0220I" \
  /apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/logs/server1/SystemOut.log \
  | tail -6
```

**✅ Expected result** — you should see the stop/start cycle for the rollback
deployment:

```
WSVR0221I: Application digistack-bank-v4 stopped successfully.
WSVR0220I: Application digistack-bank-v4 started successfully.
```

---

# Step 6.4 — Confirm the Balance Survived the Rollback → Forward Cycle

Confirm the balance survived the entire **rollback → forward** cycle intact:

```bash
psql -U digistack_app -d digistack_bank -h 127.0.0.1 \
  -c "SELECT id, balance FROM accounts WHERE user_id = 1;"
```

**✅ Expected result** — balance matches the value you recorded in
**Step 3.6** (after the ₹100 test deposit performed on the rolled-back code).

> 🔑 This proves conclusively that **neither the rollback nor the forward
> redeploy touched the database** — only application code moved.

---

# Step 6.5 — Confirm ClassLoader Settings Are Still Correct

> 💡 A fresh **Update** via Replace does **NOT** reset ClassLoader settings —
> only a fresh **Install** does.

Navigate to:

**Applications → digistack-bank-v4 → Class loading and update detection**

**✅ Expected result** — `PARENT_FIRST` and `SINGLE` still shown, since
**Update** (not Install) was used for both the rollback and the forward
redeploy in this step.

---

# Step 6.6 — Final Functional Confirmation

1. Log in as `customer1` / `Customer@123`
2. **Dashboard** loads with correct balance ✅
3. **Account** page → deposit **₹100** → success banner ✅
4. **Logout** ✅

---

