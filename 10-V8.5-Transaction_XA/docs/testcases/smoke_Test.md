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