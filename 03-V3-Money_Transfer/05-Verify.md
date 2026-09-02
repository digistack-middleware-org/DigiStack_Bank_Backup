# Dashboard Retrofit Verification

---

## ✅ Step 7.1 — Log In

Open the browser and log in:
```
Username: customer1
Password: Customer@123
```

✔️ Expected: Dashboard loads normally.

---

## ✅ Step 7.2 — Verify the Account Card

On the Dashboard, confirm the account card now shows:

| Item | Expected Value |
|---|---|
| Account type | **SAVINGS Account** |
| Masked account number | ****** **** 0001** |
| Status | **Active** (proven by the View Balance button being enabled) |

> 💡 The masked number proves the backend sent real data and the display hides all but the last 4 digits — just like real banks.

---

## ✅ Step 7.3 — Click "View Balance"

**Expected result:** the balance reveals as the current live balance — e.g.

₹45,000.00


(carried over from the Sprint 3 transactions)

> 💡 **Where does this value come from?** The `/BalanceJson` AJAX call fetches it from the database **at click time**. The Dashboard **never hardcodes a balance** — if the DB says ₹45,000, the page says ₹45,000. Proof of live data.

---

## ✅ Step 7.4 — Freeze the Account (Test the Frozen Banner)

On the **dsb-db** VM, temporarily freeze account 1's account via SQL:

```bash
psql -U digistack_app -d digistack_bank -h 127.0.0.1 \
  -c "UPDATE accounts SET is_frozen = TRUE WHERE user_id = 1;"
```
    ⚠️ This simulates what a real bank does during a fraud investigation — the account still exists, but all money movement is blocked.

✅ Step 7.5 — Refresh the Dashboard (F5)

Expected result — the frozen banner appears in the main content area:

⚠️ Your account is frozen — please contact support to restore access.

Also verify:

    ❌ The View Balance button is disabled (greyed out, not clickable)
    ⚠️ The account card shows the frozen warning inside the card

    💡 Remember the layers: the banner is UI (the friendly sign on the door), but the real protection is the Service-layer check — even if someone bypassed the page, frozen accounts cannot transact.

✅ Step 7.6 — Restore (Unfreeze) the Account

On the dsb-db VM:

psql -U digistack_app -d digistack_bank -h 127.0.0.1 \
  -c "UPDATE accounts SET is_frozen = FALSE WHERE user_id = 1;"

✅ Step 7.7 — Refresh the Dashboard Again

Expected result:

    ✔️ Frozen banner is gone
    ✔️ View Balance button is active again
    ✔️ Dashboard behaves exactly as in Step 7.2

✅ Step 7.8 — Confirm Everything in SystemOut.log

On the dsb-app VM:
```
grep "DashboardServlet" \
  /apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/logs/server1/SystemOut.log \
  | tail -5
```
Expected result — log lines showing account loads and frozen state transitions:
```
DashboardServlet: Account loaded for userId=1 accountNumber=DSB0000000001 frozen=false
DashboardServlet: Account loaded for userId=1 accountNumber=DSB0000000001 frozen=true
DashboardServlet: Account loaded for userId=1 accountNumber=DSB0000000001 frozen=false
```
 📋 This is the teller's journal proof:

        frozen=false → account normal
        frozen=true → we froze it (Step 7.4)
        frozen=false → we unfroze it (Step 7.6)

    The logs match every action we took — the journal tells the truth. ⚖️
