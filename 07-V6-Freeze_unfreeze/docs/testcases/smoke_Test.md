# When we Do the Smoke Test
We Do the Smoke test after Deploy into the New Environment

Smoke test again when:

    Server restarted / crashed and recovered
    Database patched or migrated
    Configuration changed (JNDI datasource, connection pool, JVM settings)
    Middleware upgraded (e.g., WebLogic patch)


# Smoke Test Checklist – digistack-bank-v6

# Step 2 — Final Smoke Test

> **Purpose:** A smoke test is **not** exhaustive testing — Sprint 6 already
> handled that. A smoke test is a quick end-to-end pass confirming the
> application is alive and the most important flows still work.
>
> The name comes from hardware testing: *"does smoke come out when you turn
> it on?"* — if the basics work, move forward.
>
> **Rule:** Run all five checks in order. **All five must pass before
> sign-off.**

---

## Smoke Test 1 — Home page loads from IHS (load balancer path)

| | |
|---|---|
| **Action** | Navigate to: <http://192.168.10.20/digistack-bank/Home> |
| **Expected** | Home page renders. |
| | ✅ **System Status** shows operational value |
| | ✅ **DB Connected** shows `Connected` |
| | ✅ Footer shows **v6** |

---

## Smoke Test 2 — Login succeeds

| | |
|---|---|
| **Action** | Click **Login** (or navigate to `/Login`) |
| | Enter `customer1` / `Customer@123` |
| **Expected** | Redirected to `/Dashboard` |
| | ✅ Username shown in navbar |
| | ✅ Last login timestamp shown in the last-login bar |

---

## Smoke Test 3 — Freeze via UI works

| | |
|---|---|
| **Action** | Navigate to **My Account → Freeze Account** |
| | Confirm Freeze |
| **Expected** | ✅ Success banner |
| | ✅ Account status shows **Frozen** |

---

## Smoke Test 4 wsadmin unfreeze works

**Action — on `dsb-dmgr`, run:**

```bash
cd /apps/IBM/WebSphere/AppServer/profiles/devdsbindmgr01/bin
./wsadmin.sh -lang jython -f /home/<your-username>/freezeAccount.py <ACCOUNT_NUMBER> unfreeze


