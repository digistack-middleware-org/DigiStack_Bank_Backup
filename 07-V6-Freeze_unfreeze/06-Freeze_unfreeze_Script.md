# Sprint 4 Made Simple: Freezing Accounts with wsadmin

---

## 1. The Big Idea

- A bank spots fraud → it must freeze the account **fast**.
- Real life: your card gets a hold when the bank sees weird charges. Someone at the bank did that — not you through the app.
- This sprint: freeze/unfreeze using a **script**, not the web UI.
- Why: the app might be down. The admin shouldn't need the app, a customer session, or Java code changes.
- **One key line to remember:** the UI and the script both write to the **same database**. Two doors, same room.

---

## 2. Meet the Players

- **wsadmin** = WebSphere's command-line admin tool. The "backstage door" into WebSphere.
- **Jython** = Python-like language that runs **inside Java**. wsadmin speaks Jython.
- **DMgr** (Deployment Manager) = the "boss server It manages other servers. It does **not** run your app.
- **PostgreSQL** = the database holding account data.
- **JDBC driver** = a small JAR file that lets Java talk to PostgreSQL. A **translator**.

---

## 3. Why Direct SQL (Not Java Classes)?

- Jython runs inside the Java engine (JVM) → it can call Java classes.
- Your app's classes (like `AccountDao`) live in the **app's own classloader** — hard to reach from wsadmin.
- The JDBC driver, though, is a plain JAR — easy to use.
- So: plain JDBC + SQL = the standard, simplest admin approach.
- **Real life:** the admin uses a master key straight to the vault, instead of borrowing the customer's key.

---

## 4. Step 1: The Driver Problem

- The driver JAR normally lives on **app server nodes** — not the DMgr.
- The DMgr never talks to the DB normally, it may lack the driver.
- Fix:
  - `find` command → check if the JAR exists on dsb-dmgr.
  - If missing → `scp` over from dsb-node02.
- **Real life:** the boss's office also needs a phone line to the vault — not just the workers' desks.

---

## 5. The Script Piece by Piece

### a) Imports — Jython grabs Java
- `import java.sql.DriverManager` → Java's connection maker.
- `import java.lang.Class` → Java's class loader.
- This only works because Jython runs **inside** the JVM.

### b) Constants
- URL, user, password — must match `AccountDao.java`.
- Three SQL statements: **check status**, **freeze**, **unfreeze**.
- They use `?` placeholders = **prepared statements** = safe from SQL injection.

### c) The function flow (in order)
1. **Validate** the action (only "freeze" or "unfreeze" allowed).
2. **Load the driver** with `Class.forName("org.postgresql.Driver")`.
3. **Connect** to PostgreSQL.
4. **Check** the account exists → show current status.
5. **Guard**: no double-freeze, no useless unfreeze.
6. **Run the UPDATE**.
7. **finally: always close the connection** — even on errors.

### d) Jython syntax quirks (Python 2 style — Jython 2.7)
- `except Exception, e:` — **not** `as e`.
- `print "text"` — **no parentheses**.
- Wrong style = syntax errors. Remember this.

---

## 6. The Argument Quirk (Most Important Trap!)

- **Normal Python:** `sys.argv[0]` = script name. Real args start at `[1]`.
- **wsadmin args start at **`sys.argv[0]`**. That's it.
- So in this script:
  - `sys.argv[0]` = account number
  - `sys.argv[1]` = action
- If you forget this quirk, you'd try to freeze the word "freeze". 😄
- **Real life:** like a form that starts numbering fields at zero instead of one.

---

## 7. Steps 3–5: Copy, Find, Run

- **Copy** the script to dsb-dmgr with `scp`.
- **Find a real account number** with `psql` (look at `is_frozen` — `f` = active).
- **Go to the DMgr bin folder**, then run:

```bash
./wsadmin.sh -lang jython -f /path/freezeAccount.py DSB0000000001 freeze
```

- `-lang jython` = use Jython.
- `-f` = run this file.
- The rest = your arguments.

---

## 8. Steps 6–8: Prove It Works

| Step | What you do | Why it matters |
|------|-------------|----------------|
| 6 | Log in as customer1 → My Account shows **Frozen** | Script output alone isn't proof — the **UI must show it too** |
| 6b | Try Deposit → **blocked** | Same rule as Sprint 3, now set by the script |
| 7 | Run unfreeze → refresh browser → **Active** | Each page load reads the DB live — no logout needed |
| 8 | Run freeze twice → second says **NO ACTION** | Guard rule mirrors `FreezeService.java` |
| 8b | **Unfreeze at the end** | Leave a clean state for Sprint 5 |

---

## 9. Technical Debt (Honest Weaknesses)

- The password is **hardcoded** in the script = bad practice.
- Noted in comments so future-you knows.
- **v7 plan:** read credentials from a **J2C Authentication Alias** via AdminTask.
- Debt is okay **if it's written down**. Hidden debt is the dangerous kind.

---

## 10. Nice Details Worth Knowing

- Error messages are **helpful**: if the driver is missing, the script tells you exactly where to copy the JAR.
- `rowsAffected == 1` is checked — `account_number` should be, so exactly 1 row is expected.
- "FREEZED" wording comes from `action.upper() + "D"` — cosmetic quirk, not a bug.
- Clear messages (`SUCCESS` / `NO ACTION` / `ERROR`) = friendly for real operators.

---

## 11. Memory Box 🧠

- **Two paths, one database.**
- wsadmin = **Jython inside WebSphere's JVM** → can call Java.
- Driver JAR must be on the **DMgr classpath**.
- wsadmin args start at **`sys.argv[0]`**.
- Jython = **Python 2 syntax** (`except X, e:`, `print "x"`).
- Guards mirror the app's business rules.
- **Always close connections** — use `finally`.
- Passwords in scripts = debt → write it down, fix.

---

✅ Acceptance is only met when: freeze works, unfreeze works, **both seen in the UI**, and the account is left **active** afterward. Once all checks pass, say **"continue sprint"** to move to Sprint 5.
