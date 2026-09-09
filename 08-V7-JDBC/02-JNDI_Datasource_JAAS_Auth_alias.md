# JAAS Alias + DataSource

## 1. Big Picture (Remember This First)

Think of a **bank vault with a key**.

-🏦 **The vault** = PostgreSQL database
- 🔑 **The key** = username + password
- 🗝️ **The key holder** = WebSphere (**not** your app!)

**Your app never holds the key.** just says to WebSphere:

> "Give me a connection to `jdbc/BankDS`."

WebSphere uses its **secret key (JAAS alias)** to open the vault.

```text
Your App  →  "jdbc/BankDS"  →  WebSphere  →  (secret key)  →  PostgreSQL
```

---

## 2. What is a JAAS Auth Alias?

**JAAS** = **J**ava **Authentication and **A**uthorization **S**ervice.
Long name, simple meaning: **a secure place to store a username and password.**

### Real-life example — A hotel

- You (the guest/app) don't carry the master key.
- The front desk (WebSphere) holds it.
- You just ask: "Open room 302 please."
- The desk uses the master key. You never see it.

 Alias contents (our case)

| Field      | Value                         |
| ---------- | ----------------------------- |
| Alias name | `BankDS_Alias`                |
| User       | `digistack_app`               |
| Password   | *(stored **encrypted** by WAS)* |

### Why this matters

- ✅ **No password in code** — auditors see a clean app
- ✅ **One place to change** — password rotated? Edit the alias only. No code change, no re-deploy.
- ✅ **Clean errors** — wrong password fails at the WAS layer, not deep inside your Java code

---

## 3. What is a DataSource?

A DataSource is a **pool of ready-made database connections**.

### Real-life — A taxi stand

- **Without a pool** = customer calls a taxi company, waits for a new taxi to arrive, rides, then the taxi is scrapped. Slow. Expensive.
- **With a pool** = 10 taxis already waiting. Customer takes one, rides, returns it. Next customer uses it immediately.

> **Borrow → Use → Return.** That's all a "connection" really is for your app.

### The three objects and how they connect

```text
BankDS_Alias  ←──  jdbc/BankDS  ←──  Your app's lookup("jdbc/BankDS")
(who to login as)    (the pool)      (the door to knock on)
```

Your app only knows the **name** `jdbc/BankDS`. Everything else is WebSphere's job.

---

## 4. Step 1 — Create the Alias

**GUI path:**
`Security → Global security → JAAS → J2C authentication data → New`

Fill in:

- **Alias:** `BankDS_Alias`
- **User ID:** `digistack_app`
- **Password:** *(DB password)*
- **Save** → Master Configuration

wsadmin (Jython) way:**

```python
AdminTask.createAuthDataEntry('[-alias BankDS_Alias -user digistack_app -password ****]')
AdminConfig.save()
```

**Verify:**

```python
print AdminTask.listAuthDataEntries()
```

✅ See `Bank_Alias` in the list? Done.

---

## 5. Step 2 — Create the DataSource

**GUI path:** `Resources → JDBC → Data sources → New`

> ⚠️ **Scope first:** set it to **Cell level** — so BOTH cluster members get it.
> (Same rule as Sprint 1.)

Fill in across the wizard:

| Wizard step             | What to enter                                         | Why                                                  |
| ----------------------- | ----------------------------------------------------- | ---------------------------------------------------- |
| Name                    | `DigiStack Bank DataSource`                           | Display name                                         |
| JNDI name               | `jdbc/BankDS`                                         | What your code will look up. **Must match exactly.** |
| Provider                | PostgreSQL JDBC Provider (from Sprint 1)              | The driver                                           |
| URL                     | `jdbc:postgresql://192.168.10.30:5432/digistack_bank` | Where the DB lives                                   |
| Helper class            | `GenericDataStoreHelper`                              | PostgreSQL isn't in WAS's built-in list, so we use the generic one |
| Container-managed alias | `BankDS_Alias`                                        | WAS logs in automatically — **this is the one use** |
| Component-managed alias | `BankDS_Alias`                                        | Fallback if app logs in manually. Set it anyway.     |

### Key idea

- **Container-managed** = WAS handles login invisibly. ✅ *Our pattern.*
- **Component-managed** = app passes user/pass itself. Not us — but point it to the same alias as a safety net.

**Save** → Master Configuration.

---

## 6. Step 3 — Synchronize Nodes

The config was written on the **Deployment Manager**. The actual servers (nodes) don't know yet.

**Do:** `System administration → Nodes → select → Full Resynchronize`.

### Real-life example

Head office writes a new rule book. Branch offices keep working with the old one — until the courier delivers. **Resync = the courier.**

Skip this, and Test Connection may fail or behave oddly.

---

## 7. Step 4 — Test Connection (The Acceptance Test)

**Do:** `Resources → JDBC → Data sources → tick DStack Bank DataSource → Test connection`.

**What WAS actually does behind the scenes:**

1. Borrows one connection from the pool
2. Opens a real session to PostgreSQL using the alias credentials
3. Runs a trivial query
4. Returns the connection

**Pass condition: BOTH nodes must say success.**

> ❌ Only node 1 passes? Node 2 likely has a missing JAR or stale config.

### Quick troubleshooting table

| Symptom                                              | Meaning            | Fix                                  |
| ------------------------------------------------ | ------------------ | ------------------------------------ |
| SQL State `08001`                                    | Can't reach the DB | DB down? Port 5432 blocked?          |
| SQL State `28000`                                    | Login rejected     | Wrong user/pass in alias             |
| `ClassNotFoundException: PGConnectionPoolDataSource` | Driver JAR missing | Check JAR path on **that node**      |
| `DSRA8100E`                                          | Helper class wrong | Re-check `GenericDataStoreHelper`    |

---

## 8. Step 5 — Prove No Plaintext Passwords

This is the **audit-check**.

```bash
grep -r "Wasadmin\|digistack_app" \
  .../cells/devdsbincell01/resources.xml
```

**Expected: no output.** 🎉

**Why?** `resources.xml` stores only the **alias name** (`BankDS_Alias`), like a sticky note saying *"ask the front desk."* The actual password lives **encrypted** in `security.xml`.

### Real-life example

An ID card says your **name** — not your PIN. The bank keeps the PIN in a safe.

---

## 9. Memory Recap (30-Second Version)

- 🔑 **JAAS Alias** = encrypted key storage inside WAS. App never sees the key.
- 🏊 **DataSource** = connection pool. Borrow, use, return.
- 🏷️ **JNDI name** (`jdbc/BankDS`) = the lookup. Code knows only this.
- 🌐 **Cell scope** = so both cluster members inherit it.
- 🚚 **Resync** push config from DMgr to nodes.
- ✅ **Test Connection on BOTH nodes** = the real proof.
- 🔒 **Grep resources.xml** = proof of no plaintext password.

### One-line summary

> The app asks for `jdbc/BankDS`; WebSphere holds the key and manages the pool.
