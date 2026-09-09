# Simple Lesson: Database Migrations + the Frozen Flag

---

## 1. What is a database migration?

- A **migration** = a small script that changes your database.
- It can **add** a table, a column, or change something.
- It is saved as a **file**, so you always know what changed and when.

**Real-life example:** Think of your database as a house. A migration is like a
written instruction: *"Add a new door to the kitchen."* You keep the instruction
in a file, so anyone can see what was changed.

---

## 2. Why are migrations numbered? (V1, V2, V3...)

- Each migration file has a number: **V1, V2, V3, V4...**
- The number shows the **order** the database changed.
- The number is **not** the app version (v5, v6). It is the database's own history.

**Example:**

- V1 → create the base schema
- V2 → create users table
- V3 → create accounts table
- V4 → add `is_frozen` column ← **our new one**

**Rule:** Always check your `database/migrations/` folder first. Use the
**next free number**. Never reuse a number.

---

## 3. The naming style

```
V4__add_frozen_flag.sql
```

- `V4` → order number
- `__` → two underscores
- `add_frozen_flag` → short description of what it does
- `.sql` → it's a SQL file

**Why this matters:** Anyone opening the folder instantly knows the order and
the purpose.

---

## 4. What did our V4 migration do?

It added one new column to the `accounts` table:

```sql
is_frozen BOOLEAN NOT NULL DEFAULT FALSE
```

Word by word:

- **BOOLEAN** → holds only TRUE or FALSE
- **NOT NULL** → every row must have a value (no blanks allowed)
- **DEFAULT FALSE** → new and existing accounts start as **unfrozen**

**Real-life example:** Like giving every bank account a small switch: "frozen"
or "not frozen." Every existing account gets the switch set to **OFF**
automatically.

---

## 5. Why DEFAULT FALSE is important

- Existing accounts must not break.
- With `DEFAULT FALSE`, every old row gets `is_frozen = false` instantly.
- No account gets frozen **by accident**.

**Real-life example:** Imagine a bank upgrade that accidentally froze everyone's
money. Disaster! The default prevents that.

---

## 6. UP and DOWN (every migration has two directions)

- **UP** = apply the change (add the column)
- **DOWN** = undo the change (drop the column)

**Rule in this project:** DOWN is written but **not automatic**. No tool like
Flyway is connected. You run rollback **by hand** if ever needed.

**Real-life example:** Like keeping the receipt and the return policy when you
buy a TV. You hope you never return it — but you keep the paperwork anyway.

---

## 7. The comment on the column

```sql
COMMENT ON COLUMN accounts.is_frozen IS
    'TRUE = all deposits and withdrawals blocked...';
```

- A comment lives **inside the database itself**.
- When someone inspects the table later, they see what the column means.
- No need to hunt through Java code for an explanation.

**Real-life example:** Like a label on a switch panel: *"This switch stops all
water to the house."*

---

## 8. How the migration was applied

Two ways, same result:

**Method A — from your laptop:**

1. `psql -h 192.168.10.30 -U digistack_app -d digistack_bank`
2. `\i database/migrations/V4__add_frozen_flag.sql`
3. See output: `ALTER TABLE` and `COMMENT`

**Method B — SSH into the DB server**, copy the file there, run with `-f`.

---

## 9. How to verify it worked

Two checks:

1. `\d accounts` → you should see:
   `is_frozen | boolean | not null default false`
2. `SELECT id, account_number, is_frozen FROM accounts;`
   → every row shows `f` (false). Nothing frozen yet.

**Real-life example:** After installing a new light switch, you flip it and
check the light works. Verification = trust but confirm.

---

## 10. The rollback script — and why it's separate

File: `V4__add_frozen_flag_ROLLBACK.sql`

- The DOWN in the main file is **commented out** (just a note).
- The rollback file is **directly runnable**, no editing needed.

---

## 11. Dangers of the rollback — read before running

**Danger 1 — App breaks:**

- Java code (AccountDao) uses `is_frozen`.
- Drop the column while v6 code runs → error:
  `ERROR: column "is_frozen" does not exist`
- Deposits, withdrawals, freeze, unfreeze all fail.

**Danger 2 — Data loss is permanent:**

- If some accounts are frozen (`is_frozen = t`), dropping the column
  **erases that info forever**.
- You can't recover which accounts were frozen.

---

## 12. The safety check (Step 1 of the rollback)

Before dropping, always run:

```sql
SELECT id, account_number, is_frozen
FROM accounts
WHERE is_frozen = TRUE;
```

- **0 rows** → safe to proceed.
- **Rows returned** → STOP. Write down those account IDs first.

**Real-life example Before demolishing a shed, check no one's bike is stored
inside.

---

## 13. Key rules to remember

- ✅ Migrations are numbered by **database change order**, not app version.
- ✅ Always check the folder for the **next free number**.
- ✅ Every migration has **UP** (apply) and **DOWN** (undo).
- ✅ Use `NOT NULL DEFAULT` so old data stays safe.
- ✅ Add a **COMMENT** so the database explains itself.
- ✅ Keep a **runnable rollback script**.
- ✅ **Check before you drop** — data loss is forever.
- ✅ Never roll back the DB without also rolling back the **app code** that
  uses it.

---
    