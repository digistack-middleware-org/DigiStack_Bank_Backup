# Sprint 7 — Sign Off Version 6 (Easy Guide)

**I am Ox Alpha. Let me teach you this step by step.**

---

## 🎯 What Is This Sprint About?

**Goal Officially finish Version 6.

**Three things to do:**
1. Take a backup of the WebSphere configuration (`backupConfig`)
2. Run a quick smoke test (5 checks)
3. Finish the SetupDoc-v6.md

**Think of it like:** Locking a finished house. Before you hand over the keys, you:
- Save a copy of the house blueprint (backup)
- Test the lights,, and doors (smoke test)
- Write the final instruction manual (SetupDoc)

---

## 📦 Step 1 — What Is `backupConfig`?

**Simple meaning:** It's a tool that takes a snapshot of your entire WebSphere setup.

**What it saves:**
- All configuration XML files
- All properties
- All application settings
- All cluster definitions

**Real-life example:** Like saving your game before a boss fight. If things break later, you load the save and you're back to a working state.

**When to run it:** At sign-off time — when everything works and *before* you change anything else.

---

## 🖱️ Method 1 — Backup via Admin Console (GUI)

1. Open Admin Console
2. Go to: **System administration → Backup and configuration**
3. Click **Backup**
4. Choose a folder (or accept the default):
   ```
   /apps/IBM/WebSphere/AppServer/profiles/devdsbindmgr01/config-backups/v6-signoff
   ```
5. Click **OK**

**Expected result:** Success message + a `.zip` file appears in that folder.

---

## ⌨️ Method 2 — Backup via Command Line (Recommended)

**Why recommended?** The backup gets a clear name, so you can find it easily later.

On the `dsb-dmgr` server:

```bash
cd /apps/IBM/WebSphere/AppServer/profiles/devdsbindmgr01/bin

./backupConfig.sh \
  /apps/IBM/WebSphere/AppServer/profiles/devdsbindmgr01/config-backups/digistack-bank-v6-signoff.zip \
 -nostop
```

### What each part means:

| Part | Meaning |
|------|---------|
| First argument | Full path + filename of the backup ZIP. Named `v6-signoff` so it's easy to identify |
| `-nostop` | Backup the server **without stopping it**. Safe in a lab. In production, ask your team first |

### Expected output:
```
ADMU7701I: backupConfig is beginning...
ADMU7702I: Backing up configuration for cell: devdsbincell01
ADMU0505I: Backup file created successfully: .../digistack-bank-v6-signoff.zip
```

### Confirm the file exists:
```bash
ls -lh /apps/IBM/WebSphere/AppServer/profiles/devdsbindmgr01/config-backups/
```

**Expected:** File is there, size is **a few MB** (not 0 bytes).

---

## 🧪 Step 2 — The Smoke Test

### What is a smoke test?

**It's a quick health check — NOT full testing.**

**Name origin:** Comes from electronics. Turn on a machine — if smoke comes out, it's broken. No smoke? Basic things work. Move on.

**Rule:** All 5 checks must pass. If **any one fails** → STOP. Fix it, then restart from Test 1.

---

## ✅ Smoke Test 1 — Home Page Loads

**Go to:** `http://192.168.10.20/digistack-bank/Home`

**Check:**
- Page renders ✅
- System Status shows a working value ✅
- DB Connected shows "Connected" ✅
- Footer shows **v6** ✅

**Real-life example:** Like checking the shop's front door opens before testing the register.

---

## ✅ Smoke Test 2 — Login Works

**Steps:**
1. Go to Login page
2. Enter `customer1` / `Customer@123`

**Expected:**
- Redirected to Dashboard
- Username shows in the navbar
- Last-login timestamp appears

**Real-life example:** Your key actually the front door.

---

## ✅ Smoke Test 3 — Freeze via UI

**Steps:**
1. Go to **My Account → Freeze Account**
2. Confirm freeze

**Expected:**
- Success banner appears
- Account status shows **Frozen**

**Real-life example:** Blocking your ATM card from the banking app.

---

## ✅ Smoke Test 4 — Unfreeze via wsadmin (Command Line)

On `dsb-dmgr`:

```bash
cd /apps/IBM/WebSphere/AppServer/profiles/devdsbindmgr01/bin

./wsadmin.sh -lang jython -f /home/<your-username>/freezeAccount.py <ACCOUNT_NUMBER> unfreeze
```

**Expected output:**
```
Current status: FROZEN
SUCCESS: Account <n> has been UNFREEZED.
```

Then refresh `/Account` in the browser → status shows **Active**.

**Real-life example:** The bank clerk unblocks your card from their system. Tests that the admin-side tool works too, not just the website.

---

## ✅ Smoke Test 5 — Deposit Works

**Steps:**
1. Go to `/Deposit`
2. Enter **₹100**, submit

**Expected:**
- Success banner
- Balance goes up by ₹100

**Real-life example:** Putting money in — the ATM actually credits your account.

---

## ⛔ If Any Test Fails

- **STOP.** Do not sign off.
- Fix the problem.
- Re-run **all 5 tests from Test 1**.
- Only continue when all 5 pass.

---

 📄 Step 3 — Finish the SetupDoc

**Acceptance criteria for sign-off:**
- ✅ SetupDoc is complete
- ✅ SetupDoc was followed start to finish (and it worked!)
- ✅ `backupConfig` backup captured
- ✅ All 5 smoke tests passed

---