# 🏦 DigiStack Bank — XA vs Non-XA Transaction Simulation

This lab demonstrates the difference between **XA** and **Non-XA** database transactions using a realistic DigiStack Bank fund-transfer scenario.

The simulation uses two bank accounts:

| Account ID | Account Number | Starting Balance |
|---:|---|---:|
| `1` | `DSB0000000001` | ₹94,000 |
| `2` | `DSB0000000002` | ₹10,000 |

> [!NOTE]
> The database uses account IDs `1` and `2`. Do not use `1001` and `1002` for this lab.

---

## 1. What Are We Proving?

Imagine:

- Account `1` = Dad's wallet
- Account `2` = Mom's wallet
- We want to transfer money from Dad to Mom.

### XA

```text
XA says:

"Either BOTH operations happen,
or NOTHING happens."
```

### Non-XA

```text
Non-XA says:

"Each database operation can happen separately."
```

The purpose of this lab is to see this behavior using the actual PostgreSQL database and WebSphere logs.

---

# 2. Lab Architecture

```text
                    🏦 DigiStack Bank
                           |
                    Funds Transfer
                           |
                 ┌─────────┴─────────┐
                 |                   |
             Account 1           Account 2
             ₹94,000             ₹10,000
```

For the XA test:

```text
                    WAS Transaction Manager
                             |
                       Global Transaction
                             |
                 ┌───────────┴───────────┐
                 |                       |
              DebitDS                CreditDS
                 |                       |
             Account 1               Account 2
```

WebSphere coordinates the transaction.

---

# Record the Baseline

A baseline is the **"before" picture**.

We need to know the balances before running any test.

Run:

```bash
sudo -u postgres psql -d digistack_bank -c \
"SELECT id, account_number, balance
 FROM accounts
 WHERE id IN (1,2);"
```

Expected result:

```text
 id | account_number  | balance
----+-----------------+----------
  1 | DSB0000000001   | 94000.00
  2 | DSB0000000002   | 10000.00
(2 rows)
```

Record the values:

```text
BEFORE

Account 1 = ₹94,000
Account 2 = ₹10,000
```

This is your baseline.

> [!TIP]
> Think of the baseline as taking a photograph before starting the experiment. Later, we compare the "after" picture with the "before" picture.

---

# 6. Step 3 — Open DigiStack Bank

Open the XA transfer page in your browser:

```text
http://192.168.10.20/digistack-bank/XATransfer
```

The page should provide fields similar to:

```text
🏦 DigiStack Bank

From Account: [       ]

To Account:   [       ]

Amount:       [       ]

☐ Simulate credit failure

[ Execute XA Transfer ]
```

---

# 7. Step 4 — Test A: Prove XA Rollback

The first test deliberately creates a failure.

The purpose is to prove:

> **XA protects the transaction by rolling back the complete transaction when the credit operation fails.**

Enter:

```text
From Account ID: 1
To Account ID:   2
Amount:          500
```

Enable:

```text
☑ Simulate credit failure
```

Then click:

```text
Execute XA Transfer
```

---

# 8. What Happens During Test A?

The transaction starts:

```text
WAS Transaction Manager
          |
          +-------- Account 1
          |
          +-------- Account 2
```

WebSphere coordinates the transaction.

## Step 1 — Debit

The application attempts to debit ₹500 from Account `1`.

```text
Account 1

₹94,000
   |
   | Debit ₹500
   ↓
₹93,500
```

At this point, the transaction has **not necessarily committed yet**.

The transaction is still under WebSphere's control.

## Step 2 — Credit

The application attempts to credit Account `2`.

Because **Simulate credit failure** is enabled:

```text
Credit operation
       |
       ↓
     ❌ FAIL
```

## Step 3 — WebSphere Detects Failure

WebSphere's transaction manager sees that the transaction cannot complete successfully.

Conceptually:

```text
Credit failed
     |
     ↓
Transaction cannot commit
     |
     ↓
Rollback
```

## Step 4 — Debit Is Rolled Back

The earlier ₹500 debit is undone:

```text
₹93,500
   |
   | ROLLBACK
   ↓
₹94,000
```

Account `2` never receives the ₹500.

---

# 9. Step 5 — Verify XA Rollback in PostgreSQL

This is the most important step.

Do not rely only on the browser message.

Check the actual database.

Run:

```bash
sudo -u postgres psql -d digistack_bank -c \
"SELECT id, account_number, balance
 FROM accounts
 WHERE id IN (1,2);"
```

Expected result:

```text
 id | account_number  | balance
----+-----------------+----------
  1 | DSB0000000001   | 94000.00
  2 | DSB0000000002   | 10000.00
(2 rows)
```

Compare:

```text
BEFORE                 AFTER

Account 1 ₹94,000      Account 1 ₹94,000
Account 2 ₹10,000      Account 2 ₹10,000
```

Nothing changed.

# XA ROLLBACK PROVED ✅

```text
Transfer requested: ₹500

Debit:
    Account 1 → -₹500

Credit:
    Account 2 → FAIL

WAS:
    ROLLBACK

Final result:
    Account 1 → unchanged
    Account 2 → unchanged
```

> [!TIP]
> **Memory hook:** XA test = money should **NOT move** when the transaction fails.

---

# 10. Step 6 — Check WebSphere Logs

Go to the WebSphere server.

Example:

```bash
cd /apps/IBM/WebSphere/AppServer/profiles/devdsbindmgr01/logs/devdsbinappcluster01
```

Search for transaction messages:

```bash
grep -E "WTRN|XAException|rollback|Rollback" SystemOut.log | tail -50
```

Look for WebSphere transaction-related messages.

The important sequence is:

```text
Application failure
       ↓
Transaction marked rollback
       ↓
WAS Transaction Manager
       ↓
Rollback
```

A previous test produced:

```text
WLTC0017E: Resources rolled back due to setRollbackOnly() being called.
```

This confirms WebSphere rolled back the transaction.

---

# 11. Step 7 — Record Test A Result

Record the test in your lab notes:

```text
TEST A — XA ROLLBACK

Before:
Account 1 = ₹94,000
Account 2 = ₹10,000

Transfer:
₹500
1 → 2

Simulate credit failure:
YES

After:
Account 1 = ₹94,000
Account 2 = ₹10,000

RESULT:
PASS ✅

XA protected the transaction.
```

---

# 12. Step 8 — Test B: Non-XA

Now we demonstrate the opposite behavior.

This is the **negative control**.

The purpose is to show what can happen when the debit and credit operations are not coordinated as one global XA transaction.

Think of the difference this way:

```text
XA:

Two children hold hands.

If one falls,
both stop.
```

```text
Non-XA:

Each child walks separately.

One can continue
even if the other stops.
```

> [!WARNING]
> Do not use the `/XATransfer` endpoint and assume that it is a Non-XA test. The Non-XA behavior must come from the separate Non-XA control implementation in the application.

If your application provides:

```text
/XAControl
```

open:

```text
http://192.168.10.20/digistack-bank/XAControl
```

---

# 13. Step 9 — Check the Baseline Again

Before the Non-XA test, verify the balances:

```bash
sudo -u postgres psql -d digistack_bank -c \
"SELECT id, account_number, balance
 FROM accounts
 WHERE id IN (1,2);"
```

The expected baseline is:

```text
Account 1 = ₹94,000
Account 2 = ₹10,000
```

Do not continue if the database is not at the expected baseline.

---

# 14. Step 10 — Run the Non-XA Test

Use:

```text
From Account ID: 1
To Account ID:   2
Amount:          100
```

Do not enable the simulated credit failure unless the Non-XA control page specifically requires it.

The intended demonstration is:

```text
Debit
  |
  ↓
COMMIT
  |
  ↓
Credit
  |
  ↓
FAIL
  |
  ↓
No global XA rollback
```

---

# 15. What the Non-XA Failure Demonstrates

The intended scenario is:

```text
Account 1
₹94,000
   |
   | Debit ₹100
   ↓
₹93,900
   |
   | COMMIT
   ↓
₹93,900
```

Then:

```text
Credit Account 2
       |
       ↓
     ❌ FAIL
```

Because there is no single global XA transaction coordinating both operations, the already-committed debit is not automatically reversed by an XA transaction manager.

The intended result is:

```text
Account 1 = ₹93,900
Account 2 = ₹10,000
```

The ₹100 debit exists, but the corresponding credit does not.

This is the **orphaned debit** scenario.

> [!TIP]
> **Memory hook:** Non-XA test = money can move separately.

---

# 16. Step 11 — Verify the Non-XA Result

Check PostgreSQL:

```bash
sudo -u postgres psql -d digistack_bank -c \
"SELECT id, account_number, balance
 FROM accounts
 WHERE id IN (1,2);"
```

The intended demonstration is:

```text
 id | account_number  | balance
----+-----------------+----------
  1 | DSB0000000001   | 93900.00
  2 | DSB0000000002   | 10000.00
```

Interpretation:

```text
Account 1:
₹94,000 → ₹93,900

Account 2:
₹10,000 → ₹10,000
```

Therefore:

```text
₹100 left Account 1

₹100 did NOT arrive in Account 2
```

That is the orphaned debit.

---

# 17. Step 12 — Clean Up the Database

The Non-XA test deliberately changes the database.

We must restore the original state.

Open PostgreSQL:

```bash
sudo -u postgres psql -d digistack_bank
```

Run:

```sql
BEGIN;

UPDATE accounts
SET balance = balance + 100.00
WHERE id = 1;

COMMIT;
```

Now verify:

```sql
SELECT id, account_number, balance
FROM accounts
WHERE id IN (1,2);
```

Expected:

```text
 id | account_number  | balance
----+-----------------+----------
  1 | DSB0000000001   | 94000.00
  2 | DSB0000000002   | 10000.00
```

Your database is now back to the original baseline.

> [!NOTE]
> Even the cleanup uses a proper database transaction. Always restore the database after a destructive test.

---

# 18. Complete XA vs Non-XA Comparison

| Area | XA | Non-XA |
|---|---|---|
| Transaction coordinator | WAS Transaction Manager | No global XA coordinator |
| Debit | Part of global transaction | Independent operation |
| Credit | Part of global transaction | Independent operation |
| Failure handling | Global rollback | Operations may remain partially committed |
| Two-phase commit | Yes | No |
| Prepare phase | Yes | No |
| All-or-nothing behavior | Yes | Not guaranteed |
| Orphaned debit | Prevented when XA transaction is correctly configured | Possible |
| Lab result | Balances return to baseline after failure | Debit can remain after credit failure |

---

# 19. Complete Simulation Flow

```text
                 🏦 DigiStack Bank
                        |
                  Funds Transfer
                        |
              ┌─────────┴─────────┐
              |                   |
          Account 1           Account 2
          ₹94,000             ₹10,000
```

## Test A — XA

```text
Move ₹500
    |
    ↓
Debit succeeds
    |
    ↓
Credit fails
    |
    ↓
WAS Transaction Manager
    |
    ↓
ROLLBACK
    |
    ├── Account 1 → ₹94,000
    |
    └── Account 2 → ₹10,000
```

Result:

```text
✅ Nothing changed
```

## Test B — Non-XA

```text
Move ₹100
    |
    ↓
Debit commits
    |
    ↓
Credit fails
    |
    ↓
No global XA rollback
    |
    ├── Account 1 → ₹93,900
    |
    └── Account 2 → ₹10,000
```

Result:

```text
❌ Orphaned debit
```

---

# 20. Five Things to Remember

### 1. Baseline comes first

```text
BEFORE → TEST → AFTER
```

Without the baseline, you cannot prove what changed.

### 2. XA means all-or-nothing

```text
Success → both commit

Failure → both rollback
```

### 3. Non-XA operations can become separated

```text
Debit commits
    +
Credit fails
    =
Possible orphaned debit
```

### 4. PostgreSQL is the final proof

The browser tells you what the application says happened.

PostgreSQL tells you what actually happened to the balances.

### 5. WTRN messages help prove WebSphere transaction behavior

Search:

```bash
grep -E "WTRN|XAException|rollback|Rollback" SystemOut.log | tail -50
```

---

# 21. Troubleshooting Checklist

## XA test fails before the transaction starts

Check:

```bash
sudo -u postgres psql -d digistack_bank -c \
"SHOW max_prepared_transactions;"
```

The value must be greater than `0`.

---

## `prepared transactions are disabled`

PostgreSQL is not configured for prepared transactions.

The error looks like:

```text
ERROR: prepared transactions are disabled
Hint: Set max_prepared_transactions to a nonzero value.
```

Configure PostgreSQL and restart the PostgreSQL service.

---

## `Account not found`

Verify the actual account IDs:

```bash
sudo -u postgres psql -d digistack_bank -c \
"SELECT id, account_number, balance FROM accounts;"
```

For this lab:

```text
Account 1 = DSB0000000001
Account 2 = DSB0000000002
```

---

## Duplicate servlet mapping

If WebSphere reports:

```text
SRVE9016E: Unable to insert mapping
```

check whether the same URL is declared both through:

```java
@WebServlet(...)
```

and:

```xml
<servlet-mapping>
```

Use one mapping mechanism consistently.

---

## Transaction rollback

Search:

```bash
grep -E "WTRN|XAException|RollbackException|rollback|Rollback" SystemOut.log | tail -100
```

---

# 22. Acceptance Checklist

| # | Check | Expected Proof |
|---:|---|---|
| 1 | PostgreSQL XA enabled | `max_prepared_transactions > 0` |
| 2 | Baseline recorded | Account 1 = ₹94,000; Account 2 = ₹10,000 |
| 3 | XA failure test executed | Simulated credit failure |
| 4 | XA rollback verified | Both balances remain at baseline |
| 5 | WebSphere logs checked | WTRN/XA/rollback messages |
| 6 | Non-XA control executed | Separate Non-XA path |
| 7 | Orphaned debit demonstrated | Debit remains after credit failure |
| 8 | Database restored | Both balances return to baseline |
| 9 | Existing application pages checked | No regression |

---

# 23. Final Mental Model

```text
                 XA TRANSACTION
                 ==============

                  WAS
           Transaction Manager
                    |
             "All together!"
                    |
          ┌─────────┴─────────┐
          ↓                   ↓
       Debit                Credit
          |                   |
          └─────────┬─────────┘
                    ↓
                 COMMIT
                    OR
                 ROLLBACK
```

```text
                 NON-XA
                 ======

                Debit
                  |
               COMMIT
                  |
                  X
                  |
               Credit
                  |
                FAIL

             No global
             rollback
```

## Core Memory Hook

```text
XA:

"ALL or NOTHING."

Non-XA:

"Each operation can stand alone."
```
