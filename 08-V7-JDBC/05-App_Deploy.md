# Deploying a New App Version + Proving JNDI Works

---

## What Is Happening Here (Big Picture)

Think of a bank app running on a server (WebSphere). You built a new version (v7). Now you need to:

1. Stop the old app (v6)
2. Install the new app (v7)
3. Make sure it connects to the database the **new way** (JNDI)
4. Test everything still works
. Test that failed money don't corrupt data

> **Real-life example:** Like replacing the engine of a running bus. You stop the bus, swap the engine, restart, then test-drive it before letting passengers on.

---

## Step 3 — Deploy Using the GUI (Web Browser)

**Where:** Admin Console at `https://192.168.10.10:9043/ibm/console`

### Steps (in order):

1. **Login** to the Admin Console.
2. Go to **Applications → Application Types → WebSphere enterprise applications**.
3. Find **DigiStack Bank v6**.
4. ✅ Tick its checkbox.
5. Click **Stop**. Wait until it shows "stopped".
   - *Why? You can't replace a running app — like you can't change a tire while driving.*
6. Click app **name** → click **Update**.
7. Select **"Replace the entire application."**
8. **Browse** and pick the new file:
   - `digistack-bank-ear/target/digistack-bank-v7.ear`
9. Click **Next** through the wizard, keep defaults.
10. **Important screen:** *Map modules to servers* — make sure the **cluster** is selected (not a single server).
    - *A cluster = multiple servers working as one. Like a team, not one person.*
11. Click **Finish**.
12. ✅ Check for: *"Application digistack-bank-v7 installed successfully."*
13. Click **Save** (Master Configuration).
    - *Save = make it permanent. Without this, changes are lost.*
14. Tick the app → click **Start**.
15. ✅ Look for the **green started icon**.

---

## Step 4 — Deploy Using wsadmin (Command Line)

Same job as Step 3, but done with commands instead of clicking. Good for automation.

### Steps:

1. **Copy the EAR file** to the server:

   ```bash
   scp digistack-bank-ear/target/digistack-bank-v7.ear \
       yourname@192.168.10.10:/home/yourname/
   ```

   - *`scp` = secure copy over the network.*

2. **Login to dsb-dmgr** and go to the wsadmin tool:

   ```bash
   cd /apps/IBM/WebSphere/AppServer/profiles/devdsbindmgr01/bin
   ./wsadmin.sh -lang jython
   ```

3. **Update the app** (Python-like commands):

   ```jython
   AdminApp.update('DigiStack Bank v6', 'app',
       '[-operation update '
       '-contents /home/yourname/digistack-bank-v7.ear '
       '-usedefaultbindings]')
   AdminConfig.save()
   ```

   - `AdminApp.update` = replace the app.
   - `AdminConfig.save()` = save changes. **Never forget this!**

4. **Start the app:**

   ```jython
   appMgr = AdminControl.queryNames('type=ApplicationManager,*').splitlines()[0]
   AdminControl.invoke(appMgr, 'startApplication', '"DigiStack Bank v7"')
   ```

5. ✅ **Expected:** no red error text. Then type `quit()` to exit.

> **Note:** You only do Step 3 **OR** Step 4 — both do the same thing.
> **GUI = easier.** **wsadmin = scriptable and repeatable.**

---

## Step 5 — Synchronize Nodes

1. In Admin Console: **System administration → Nodes**.
2. Select **all nodes** → **Full Resynchronize**.

**Why?** In a cluster, the manager holds the config. Each node (server machine) must get a **copy** of the new config/app.

> **Real-life example:** The head office updates the rulebook. Every branch office must receive the new copy.

✅ Expected: **Both nodes synchronized.**

---

## Step 6 — Prove JNDI Actually Works

This is the **first real proof** the database connection migration succeeded.

### What is JNDI? (simple)

- **Old way:** App stores DB username/password in its own code. Bad — hard to change.
- **New way (JNDI):** App asks the server: *"Give me a connection named `jdbc/BankDS`."* The server manages credentials and connection pooling.
- *Real-life example: Instead of building your own well, you ask the city water supply for water.*

### How to test:

1. Open browser: `http://192.168.10.20/digistack-bank/Home`
2. ✅ Home page loads and **`dbConnStatus` = "Connected"**

**What this proves:** The app looked up `jdbc/BankDS` via JNDI, got a pooled connection, ran a real SQL query (`SELECT ... FROM app_config`), and got results. If JNDI failed, it would show "Error".

3. **Check the server log:**

   ```bash
   grep -i "jdbc/BankDS\|JNDI\|BankDS" \
     /apps/IBM/WebSphere/AppServer/profiles/devdsbindmgr01/logs/<server>/SystemOut.log \
     | tail -20
   ```

✅ Expected: **No NamingException.**
❌ If you see *"JNDI lookup failed"* → **STOP. Fix before continuing.**

---

## Step 7 — Test All Features Through JNDI

A quick smoke test. Every action now uses the JNDI connection:

| # | Action | Expected Result |
|---|--------|-----------------|
| 1 | Login (`customer1` / `Customer@123`) | Dashboard loads ✓ |
| 2 | Deposit ₹500 | Success banner, balance up ✓ |
| 3 | Withdraw ₹200 | Success banner, balance down ✓ |
| 4 | Freeze account | Account frozen ✓ |
| 5 | Unfreeze account | Account active ✓ |
| 6 | Logout | Redirected to Home ✓ |

✅ All six working = **JNDI migration is live and proven.**

---

## Step 8 — Test That Failures Roll Back Cleanly

### The rule being tested:

A failed withdrawal must leave the balance **actly unchanged**. No half-done money movement.

### Test A — Simple failed withdrawal:

1. Check current balance in DB:

   ```sql
   SELECT balance FROM accounts
   WHERE account_number = '<customer1 account number>';
   ```

   Call this value **B**.

2. In the browser: go to **/Withdraw**, enter an amount **bigger than B**
   (e.g., B = ₹1,000, withdraw ₹9,999).

3. ✅ Expected: Error banner *"Insufficient funds"*. **No** success banner.

4. **Confirm balance unchanged:**

   ```sql
   SELECT balance FROM accounts
   WHERE account_number = '<customer1 account number>';
   ```

   ✅ Still **B** — the app caught the problem **before** touching the database.

**Why this works:** `WithdrawService` checks the balance in Java **first**.
If not enough money, it throws `InsufficientFundsException` — the SQL never runs.

### Test B — Failure *during* SQL (deeper test):

Make the Java check pass but the SQL fail (e.g., the SQL has its own `WHERE balance >= ?` guard). This tests the **DAO layer's rollback** — even mid-operation failures leave no partial changes.

### Also check the log:

```bash
grep -i "Insufficient\|rollback\|withdraw" \
  /apps/IBM/WebSphere/AppServer/profiles/devdsbindmgr01/logs/<server>/SystemOut.log \
  | tail -10
```

✅ Expected: a clean log line about insufficient funds. **No SQL stack trace.**

---

## Key Takeaways (memorize these)

- 🛑 **Stop before update.** Never replace a running app.
- 💾 **Always click Save** (GUI) or run `AdminConfig.save()` (wsadmin). Unsaved = lost.
- 🖥️ **GUI vs wsadmin** — same result. Pick one. wsadmin is better for repeating.
- 🔄 **Synchronize nodes** so every server in the cluster gets the new version.
- 🧪 **Test JNDI first** — "Connected" on the home page = migration works.
- 💰 **Failed money ops must roll back** — balance unchanged, no partial updates.
- 📋 **Check SystemOut.log** — the log always tells the truth about what happened.
