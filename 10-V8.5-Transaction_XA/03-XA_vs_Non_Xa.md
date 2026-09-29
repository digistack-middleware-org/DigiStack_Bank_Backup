# Prove XA DataSource vs Non-XA Datasource
## 5. Step 12 — Record the Baseline

A baseline is a "before" photo. Before touching anything, record current balances:

```sql
SELECT id, account_number, balance
FROM   accounts
WHERE  id IN (1001, 1002);
```

Example output:

```text
 id  | account_number | balance
-----+----------------+---------
1001 | ACC001         | 5000.00
1002 | ACC002         | 3000.00
```

> [!NOTE]
> Later you compare "after" vs "before." Without a baseline, you cannot prove nothing changed — like weighing yourself before a diet.

## 6. Step 13 — Test A: Prove XA Rollback Works

### Procedure

1. Browser: `http://192.168.10.20/digistack-bank/XATransfer`
2. Fill: From `1001` → To `1002`, Amount `500.00`
3. ✅ Tick **"Simulate credit failure"** — deliberately makes the credit step fail.
4. Click **Execute XA Transfer**.

### What Happens Under the Hood

1. Debit Rs.500 from `1001` → SQL runs ✅
2. Credit Rs.500 to `1002` → forced failure ❌
3. WAS (the transaction manager) sees the failure → tells the debit DB: "Undo that debit!"
4. **Rollback** → both balances untouched.

### Proof

Run the psql query again:

```text
1001 | ACC001 | 5000.00   ← unchanged ✅
1002 | ACC002 | 3000.00   ← unchanged ✅
```

If `1001` is lower → rollback failed. Check the log:

```text
/apps/IBM/WebSphere/AppServer/profiles/devdsbindmgr01/logs/devdsbinappcluster01/SystemOut.log
```

Look for **WTRN** errors (WTRN = WebSphere Transaction messages).

> [!TIP]
> **Memory hook:** XA test = money should **NOT** move.

## 7. Step 14 — Test B: The Negative Control (Non-XA)

A **negative control** deliberately does it the bad way, to show what happens without XA — showing the broken version to appreciate the fixed version.

### Procedure

Same form (scroll down), but:

- **No** "Simulate credit failure" checkbox
- Amount only `100.00` (small, because we lose it on purpose)

### What Happens

1. Debit 100 from `1001` → runs and commits immediately (no coordinator watching).
2. Credit to `1002` → fails.
3. Nobody rolls anything back.

### Result

```text
1001 | ACC001 | 4900.00   ← Rs.100 gone! ❌
1002 | ACC002 | 3000.00   ← never received it
```

Rs.100 vanished into thin air. This is called an **orphaned debit** — the debit exists, but its partner credit never did.

> [!TIP]
> **Memory hook:** Non-XA test = money **DOES** move (wrongly).

### Why This Matters

This is exactly what causes real bank "phantom losses" when systems aren't transaction-safe. The demo recreates a famous production failure — safely.

## 8. Step 15 — Clean Up

Test B caused real damage. Fix it:

```sql
BEGIN;
UPDATE accounts SET balance = balance + 100.00 WHERE id = 1001;
COMMIT;

SELECT id, account_number, balance
FROM   accounts
WHERE  id IN (1001, 1002);
-- Verify back to 5000.00
```

> [!NOTE]
> Note the `BEGIN ... COMMIT` — even the fix itself is a proper transaction. Practice what you preach. Always restore state before the next sprint — a dirty database ruins the next test's baseline.

## 9. Step 16 — Acceptance Checklist

| # | Check | Proof |
|---|-------|-------|
| 1 | XA rollback works (Test A) | Balances = baseline in psql |
| 2 | Non-XA orphan happens (Test B) | `1001` reduced, `1002` unchanged |
| 3 | State restored (Step 15) | Balances = baseline again |
| 4 | Right UI messages | Test A = red error, "XA ROLLBACK PROVEN"; Test B = amber warning, "ORPHANED DEBIT" |
| 5 | No regression | All old pages (Home, Login, Dashboard, Deposit, Withdraw, Freeze, Unfreeze) still work |

## 10. Cheat Sheet — 5 Things to Remember

1. **XA = all-or-nothing.** Coordinated by WAS. Failure → rollback → balances unchanged.
2. **Non-XA = each DB alone.** Failure → orphaned debit → money lost.
3. **Deploy flow:** Stop → Uninstall → Install (map to cluster) → Start → Sync nodes.
4. **Always record a baseline** before tests and **restore state** after destructive tests.
5. **Proof comes from psql, not the browser.** Browser shows the story; the database shows the truth.

## 11. Self-Test Questions

1. Why must you Stop an app before uninstalling?
2. What does Full Resync on nodes actually do?
3. In Test A, what happens to the debit SQL when the credit fails?
4. Why is Test B called a "negative control"?
5. Where do you look for WTRN errors?

