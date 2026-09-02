# Fault Injection Lab — Corrupting `customer1` Credentials (digistack-bank)

## Step 1 — Confirm the Environment is Clean Before Injecting

On the **dsb-dmgr VM**, run:

```bash
curl -s -o /dev/null -w "%{http_code}" \
  http://192.168.10.10:9080/digistack-bank/Home
```

**Expected result before injection:**

```
200
```

Then confirm login works:

```bash
curl -s -o /dev/null -w "%{http_code}" \
  http://192.168.10.10:9080/digistack-bank/Login
```

**Expected result:**

```
200
```

> ⚠️ If either returns anything other than `200`, **stop and report before continuing.**

---

## Step 2 — Inject the Fault

Run:

```bash
psql -U digistack_app -d digistack_bank -h 127.0.0.1 \
  -c "UPDATE users SET password_hash = 'CORRUPTED' WHERE username = 'customer1';"
```

- Enter the password `D!g!St@ck2026#` when prompted.

**Expected result:**

```
UPDATE 1
```

---

## Step 3 — Confirm the Injection Was Applied

```bash
psql -U digistack_app -d digistack_bank -h 127.0.0.1 \
  -c "SELECT username, password_hash FROM users WHERE username = 'customer1';"
```

**Expected result — `password_hash` column shows:**

```
 username  | password_hash
-----------+--------------
 customer1 | CORRUPTED
(1 row)
```

---

## Step 4 — Trigger the Fault

Open your browser and attempt to log in:

```
http://192.168.10.10:9080/digistack-bank/Login
```

Enter:

| Field    | Value         |
|----------|---------------|
| Username | `customer1`   |
| Password | `Customer@123` |

Click **Sign In**.

> 👀 **Observe what happens. Do not diagnose yet — just note what you see.**

---

## Step 5 — Confirm the `admin1` Account is Unaffected

On the login page, enter:

| Field    | Value       |
|----------|-------------|
| Username | `admin1`    |
| Password | `Admin@123` |

Click **Sign In**.

> 👀 **Observe what happens. Note it down.**
