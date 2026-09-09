## 📦 Step 1 — Capture backupConfig Baseline

### What is backupConfig?

- It's a WebSphere tool that saves your server configuration
- Think of it as a **"Save Game" button** 🎮
- If something breaks later, you can restore to this exact state

### Why "baseline"?

> A baseline = the known-good state at a moment in time.
> Like a photo taken before renovating a house — so you can always go back.

### The Commands (run on `dsb-dmgr`)

**1. Go to the right folder:**

```bash
cd /apps/IBM/WebSphere/AppServer/profiles/devdsbindmgr01/bin
```

**2. Create the backup (labelled v7-signoff):**

```bash
./backupConfig.sh \
  /apps/IBM/WebSphere/AppServer/profiles/devdsbindmgr01/config-backups/digistack-bank-v7-signoff.zip \
  -nostop
```

**Command breakdown:**

| Part | Meaning |
|---|---|
| `./backupConfig.sh` | Run the backup tool |
| `...v7-signoff.zip` | Where to save + name the file |
| `-nostop` | Don't shut down the server while backing up (keeps it running) |

**3. What success looks like:**

```
ADMU701I: backupConfig is beginning...
ADMU7702I: Backing up configuration for: devdsbincell01
ADMU0505I: Backup file created successfully: ...v7-signoff.zip
```

**4. Verify the file exists and is NOT empty:**

```bash
ls -lh /apps/IBM/WebSphere/AppServer/profiles/devdsbindmgr01/config-backups/
```

- ✅ **Expected:** Both `v6-signoff.zip` AND `v7-signoff.zip` are there.
- ❌ **Fail:** File missing, or size is 0.

> **Real-life rule:** Like a bank keeping passbooks while issuing new ones. **Old backups are never deleted** — v6 must survive alongside v7.

---

## 🧪 Step 2 — Final Smoke Test

### What is a smoke test?

- A **quick, basic check** that the app works
- Named after electricians who powered on a device and checked for **smoke** — if no smoke, at least it's not broken! 🔌
- It doesn't test deep features — just: *"Does the main thing work?"*

**Rule: All 5 checks must pass. Any 1 failure = Sprint not done.**

---

### 🔥 Smoke Test 1 — Home Page + Database

- **Go to:** `http://192.168.10.20/digistack-bank/Home`
- **Pass if:**
  - Page renders
  - Footer shows **v6** ← *this is correct, NOT a bug* (footer is a UI string last touched at v6)
  - DB Connected says **"Connected."**
  - System Status looks operational

> **Memory tip:** Page loads + DB talks = app is alive.

---

### 🔥 Smoke Test 2 — Login

- **Go to:** `/Login`
- **Enter:** `customer1` / `Customer@123`
- **Pass if:** Redirected to `/Dashboard` with last login time shown

> **Memory tip:** Right key opens the door, and it shows when you were last here.

---

### 🔥 Smoke Test 3 — Deposit ₹500

- **Go to:** `/Deposit` → enter **₹500** → submit
- **Pass if:** Success banner + balance goes **UP by exactly ₹500**

> **Memory tip:** Money in = balance rises. Simple math.

---

### 🔥 Smoke Test 4 — Withdraw ₹100

- **Go to:** `/Withdraw` → enter **₹100** → submit
- **Pass if:** Success banner + balance goes **DOWN by exactly ₹100**

> **Memory tip:** Money out = balance falls. Still simple math.

---

### 🔥 Smoke Test 5 — Failed Withdraw Rolls Back

This is the **most important test** — it checks safety.

- **Step A:** Note your current balance → call it **B**
- **Step B:** Try to withdraw **more than B**
- **Pass if:  - Error banner: **"Insufficient funds"**
  - Balance **still exactly B** — unchanged

> **Real-life example:** Ask an ATM for ₹10,000 when you have ₹500. The ATM says "No" — and does NOT take your ₹500 anyway. That's a **clean rollback**.
>
> If the balance changed during a failure → data corruption → test FAILED.

---

## 📝 Summary Cheat Sheet

| Item | Action | Pass Condition |
|---|---|---|
| backupConfig | Run with `-nostop`, labelled `v7-signoff` | ADMU0505I message + non-zero + v6 zip still there |
| Smoke 1 | Open Home page | Page + "Connected." + footer v6 (not a bug) |
| Smoke 2 | Login customer1 | Land on Dashboard + last login time |
| Smoke 3 | Deposit ₹500 | Banner + balance +₹500 |
| 4 | Withdraw ₹100 | Banner + balance −₹100 |
| Smoke 5 | Withdraw > B | "Insufficient funds" + balance unchanged |
| SetupDoc | Write it all down | A stranger can follow it start to finish |

---

## 🧠 One-Line Takeaway

> **Back it up. Test all 5. Write it down. Sign off v7.** ✅
