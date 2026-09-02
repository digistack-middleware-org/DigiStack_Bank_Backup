# Fix — Restore the Environment

## Step 1 — Restore the Correct Hash (on Windows laptop)

Run the `SeedUsers` utility — it recomputes and restores the correct hash automatically:

```bash
java -cp "digistack-bank-web\target\classes;C:\Tools\postgresql-42.7.3.jar" ^
  com.digistack.bank.util.SeedUsers
```

**Expected result:**

```
Connected to digistack_bank on dsb-db.
Updated customer1 with correct password hash.
Updated admin1 with correct password hash.
Seed complete. Both users ready for login.
```

## Step 2 — Verify the Hash Is Restored (on dsb-db VM)

```bash
psql -U digistack_app -d digistack_bank -h 127.0.0.1 \
  -c "SELECT username, LENGTH(password_hash) AS hash_len FROM users;"
```

**Expected result:**

```
 username  | hash_len
-----------+----------
 customer1 |       64
 admin1    |       64
(2 rows)
```

Both rows show `hash_len = 64` — valid SHA-256 hashes.

## Step 3 — Confirm Login in the Browser

Go to:

```
http://192.168.10.10:9080/digistack-bank/Login
```

Log in with `customer1` / `Customer@123` → click **Sign In**.

**Expected result:** Dashboard loads with greeting and last login timestamp. ✅ Login fully restored.

## Step 4 — Confirm in the Logs (on dsb-dmgr VM)

```bash
grep "LoginServlet" \
  /apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/logs/server1/SystemOut.log \
  | tail -5
```

**Expected result:**

```
LoginServlet: Login successful for user: customer1 role: CUSTOMER
```

---

# Prevention

### 1. Check the log first — always
If a user insists their password is correct but the log says *"wrong password"*, go inspect the **database row** — not the application code.

### 2. Hash length validation query
A quick sanity check — valid SHA-256 hashes are always 64 characters:

```sql
SELECT username FROM users
WHERE LENGTH(password_hash) != 64;
```

Any row returned = corrupted hash. Run it as a daily DB health check from v2 onward.

### 3. Database audit logging
Enable PostgreSQL's `pgaudit` extension to log every `UPDATE` on the `users` table (who, when, from where). The injected `UPDATE` would show up in the logs immediately.

### 4. Principle of least privilege
`digistack_app` currently has ALL privileges — fine for dev, risky for production. In production:
- App user gets only `SELECT, INSERT, UPDATE` on specific columns.
- `UPDATE` on `password_hash` restricted to a dedicated credential-management role.

### 5. From v10 onward (WAS Security)
Credential management moves into the WAS security domain. A corrupted hash in the DB would still fail WAS's own credential checks — adding another layer of protection.
