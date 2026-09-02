# Root Cause Analysis — INC-v2-001 (customer1 Login Failure)

## Why Login Failed for customer1 Only

When `customer1` tried to log in, `LoginServlet.doPost()` ran this check:

```java
boolean passwordCorrect = PasswordUtil.verify(
    storedSalt, password, storedHash);
```

Here's what happened:

1. `PasswordUtil.verify()` correctly computed `SHA-256(salt + "Customer@123")` → a valid 64-character hash.
2. It compared that against `storedHash` — which was now `CORRUPTED` (only 9 characters).
3. A 9-character string can **never** match a SHA-256 output, so `verify()` returned `false`.
4. LoginServlet logged:

   ```
   LoginServlet: Login failed — wrong password for: customer1
   ```

> **Key point:** The application worked exactly as designed. A non-matching hash is simply treated as a wrong password. There was **no exception, no 500 error, no server problem** — the fault was purely in the **data**.

---

## Why admin1 Was Unaffected

The `UPDATE` statement targeted only `WHERE username = 'customer1'` — just that one row.
`admin1`'s password hash was untouched, so their login worked normally the whole time.

---

## Why No Alert Fired

- The app returned **HTTP 200** on every request — page loads, form processing, and responses were all "healthy."
- At v2 there are no application-level health checks, so infrastructure monitoring saw nothing wrong.
- The failure was inside the **business logic layer** — invisible to infrastructure monitoring.

---

## The Log Clue That Pointed to Root Cause

```
LoginServlet: Login failed — wrong password for: customer1
```

This single line tells us:

- ✅ `customer1` **exists** in the DB (otherwise it would say "username not found")
- ✅ The user is **active** (otherwise a different message would appear)
- ❌ Only the **hash comparison** failed

That leaves three possibilities:

| # | Possible Cause                          | Ruled Out? |
|---|------------------------------------------|------------|
| 1 | User entered the wrong password          | ✅ Yes — customer tried repeatedly, password certain |
| 2 | Password hash in DB was corrupted        | ❌ **Not ruled out** |
| 3 | Hashing logic changed (new deployment)   | ✅ Yes — no redeployment occurred |

All signs point to **Cause 2 — a data integrity issue**.

### The Confirming Query

```sql
SELECT username, LENGTH(password_hash), password_hash
FROM users
WHERE username = 'customer1';
```

A valid SHA-256 hex hash is **always exactly 64 characters**.
Seeing `LENGTH = 9` and value `CORRUPTED` confirms the column was tampered with.

---
