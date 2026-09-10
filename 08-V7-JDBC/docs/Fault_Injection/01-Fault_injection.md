# 🎓 Phase 1 — Fault Injection: JAAS Auth Alias Password Mismatch

> Simulates a real-world failure: a database password rotation applied in
> PostgreSQL but NOT updated in the WAS JAAS Auth Alias.

---

## 1. What is a JAAS Auth Alias?

- It is a **saved username + password** inside WebSphere (WAS).
- WAS uses it to **log in to the database** for you.
- Think of it like a **saved contact card** WAS shows to PostgreSQL.
- Your app never sees the password. WAS handles it.

**Real-life example:**
Like a receptionist (WAS) holding a key card (alias) to open the server
room (database) for guests (your app).

---

## 2. What is a JNDI DataSource?

- It is a **named connection factory** in WAS.
- Your app says: *"Give me `DigiStack Bank DataSource`"* — no DB details needed.
- Behind the scenes, WAS uses the **JAAS alias** to authenticate.

**Chain to remember:**

    App → JNDI DataSource → JAAS Alias → PostgreSQL

If the alias is wrong, the whole chain breaks.

---

## 3. Why Do We Inject This Fault?

- DBAs **rotate passwords regularly** (security rule).
- Very common real mistake: DBA changes the password in PostgreSQL but
  **forgets to tell the WAS admin**.
- Result: the app **suddenly fails**, everyone panics, nobody knows why.

**This exercise teaches you to recognize and fix that.**

---

## 4. Step-by-Step: Injecting the Fault

### Step A — Change the alias password

- Open console: `https://192.168.10.10:9043/ibm/console`
- Left menu → **Security → Global security**
- Scroll to **Authentication** → click **Java Authentication and Authorization Service**
- Click **J2C authentication data**
- Click **BankDS_Alias**
- Change Password: `Wasadmin@951951` → `WrongPassword@999`
- User ID stays: `digistack_app` ✅ (unchanged)
- Click **OK → Save**

### Step B — Force nodes to pick up the change

- **System administration → Nodes**
- Select **all nodes** → **Full Resynchronize**
- ✅ Expect: *"Synchronization completed successfully."*

> ⚠️ **Note:** Resync succeeded — that only proves config **files** copied
> fine. It does **NOT** prove the password works.

### Step C — Restart app servers

- **Servers → Server Types → WebSphere application servers**
- Select both → **Stop** → wait for stopped
- Select both → **Start** → wait for green ✅

**Why restart?**
The connection pool **caches old credentials**. A restart forces WAS to
load the new (wrong) alias.

### Step D — Confirm the fault

- **Resources → JDBC → Data sources**
- Check **DigiStack Bank DataSource** → **Test connection**
- ❌ You will see a failure, something like:

    The test connection operation failed.
    DSRA4000E: Failed to connect to the datasource.
    User ID or password is invalid. (DSRA4004E / SQLSTATE 28P01)

> 🚫 **Do NOT fix it yet.** Just note the exact error message.

---

## 5. What Just Happened? (The Logic)

| Piece           | Status                              |
|-----------------|-------------------------------------|
| PostgreSQL      | ✅ Healthy, new password active     |
| WAS alias       | ❌ Old/wrong password               |
| App servers     | ✅ Running fine                     |
| Test connection | ❌ Fails                            |

**Key insight:**
The app server can be **up** while the database connection is **broken**.
Healthy processes ≠ healthy connectivity.

---

## 6. Real-World Impact

If this were production:

- 💳 Customers can't log in to banking app
- 📉 Transactions fail with `500` errors
- 🔥 App logs fill with `Connection refused / authentication failed`
- 😰 On-call team gets paged at 2 AM

---

## 7. Quick Memory Cheat Sheet

- **Alias** = saved DB credentials in WAS
- **JNDI** = friendly name app uses to reach the DB
- **Password rotation without WAS update = classic outage**
- **Resync success ≠ connection success**
- **Restart loads new credentials into the pool**
- **Test Connection = your health check tool**

---
