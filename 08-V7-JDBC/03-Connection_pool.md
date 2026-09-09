# Connection Pool Sizing & Validation

> **Goal:** Size the connection pool and configure validation on `jdbc/BankDS`.
> **Author:** Ox Alpha

---

## 1. What is a Connection Pool?

Think of it like a **taxi stand** outside a hotel.

- The hotel keeps some taxis **waiting at all times** (minimum).
- It can call for more taxis, but only up to a **limit** (maximum).
- If all taxis are busy, guests **wait in a queue**. Wait too long? They give up (timeout error).

In our case:

| Real-life | Our Project |
|---|---|
| Hotel | WebSphere (WAS) |
| Taxis | Database connections |
| City | PostgreSQL |

**Why a pool?**

- Opening a new DB connection is **slow** (like calling a taxi from across town).
- Keeping a few ready = **fast responses**.

---

## 2. Two Settings to Know

| Setting | Meaning | Real-life |
|---|---|---|
| **Min** | Connections always kept open | Taxis parked overnight |
| **Max** | Hard ceiling | Total taxis the hotel can ever hire |

### What goes wrong if you pick badly

| Mistake | Result |
|---|---|
| Max too low | `DSRA0080E` — pool full, requests fail |
| Max too high (across all servers) | PostgreSQL says: `sorry, too many clients` — DB crashes for everyone |
| Min too low | First morning request slow (pool was empty, cold start) |

---

## 3. The Pool Sizing Math (Our Topology)

We have a **2-member cluster** (two app servers).

### Per member

- Peak concurrent requests ≈ `15`
- Buffer for spikes = `5`
- **Max pool per member = 15 + 5 = 20**

### Total

```text
2 members × 20 = 40 connections at peak
```

### Headroom check against PostgreSQL

```text
PostgreSQL max_connections = 100
WAS needs at peak          =  40
────────────────────────────────
Headroom                   =  60  ✅ comfortable
```

Those 60 spare connections are for DBAs, scripts, monitoring, backups.

**Min per member = 5.** Keeps things warm. Baseline = `2 × 5 = 10` connections. Negligible.

---

## 4. The Timeout Values (And Why)

| Parameter | Value | Simple meaning |
|---|---|---|
| Connection timeout | `180s` | How long a request waits for a free connection before giving up |
| Unused timeout | `1800s` | Idle connections *above min* get closed after 30 min |
| Aged timeout | `7200s` | Every connection dies after 2 hours, no exceptions (forced refresh) |
| Reap time | `180s` | How often WAS "sweeps" the pool for stale/idle connections |

> 🚗 **Real-life:** Aged timeout is like replacing your car every 2 years even if it works — prevents old, unreliable parts.

---

## 5. Purge Policy = EntirePool

When WAS finds one dead connection:

- **FailingConnectionOnly:** throw away just that one. Risky — if the DB restarted, **all** connections are probably dead.
- **EntirePool (our choice):** throw away the whole pool, rebuild fresh.

> 🥛 **Real-life:** If one glass in your cupboard has poison, you wash the whole cupboard, not just that glass.

---

## 6. Pool Validation (The Safety Check)

### The problem

- PostgreSQL can kill a connection (restart, DBA kill, idle timeout).
- WAS doesn't know — it still thinks the connection is fine.
- Your app borrows a **dead** connection `Connection reset` error User sees an for no good reason.

### The fix

Before handing any connection to your app, WAS runs a test:

- **Validate new connections** — test when first created
- **Pre-test connections** — test on *every* borrow (the key one!)
- **Validation query:** `SELECT 1`

`SELECT 1` is the lightest possible PostgreSQL query Takes microseconds. No tables touched.

**If the test fails:** WAS silently throws the dead connection away, opens a fresh one, and gives that to your app. **The user never sees an error.> 🚕 **Real-life:** The hotel manager checks each taxi's engine *before* the guest gets in. Broken taxi? Swap it. Guest never notices.

---

## 7. Step-by-Step Configuration

### Step 1 & 3 — GUI Method

1. Admin Console → **Resources → JDBC → Data sources**
2. Click **DigiStack Bank DataSource**
3. **Connection pool properties** → set:
   - Min `5`, Max `20`, Connection timeout `180`, Unused `1800`, Aged `7200`, Reap `180`
   - Purge policy: **EntirePool**
4. Same screen → Connection validation section:
   - ✅ Validate new connections
   - ✅ Pre-test connections
   - Validation query: `SELECT 1`
5. **OK → Save**

### Step 2 & 4 — wsadmin Method (same thing, via script)

1. Start wsadmin on `dsb-dmgr`
2. Find the DataSource → find its ConnectionPool object
3. `AdminConfig.modify(pool, poolAttrs)` — pool sizes
4. `AdminConfig.modify(pool, validationAttrs)` — validation settings
5. Verify with `AdminConfig.show(pool)`
6. `AdminConfig.save()`

### Step 5 — Synchronize Nodes

- System administration → Nodes → **Full Resynchronize**
- This copies the config to **both** members.

### Step 6 — Restart Both Servers ⚠️ Important

- Pool settings are read **at JVM startup only** — they do NOT reload dynamically.
- Restart both servers (or stop/start the cluster).

---

## 8. The Proof — Kill a Connection on Purpose

This proves validation actually works.

### 7a — Find WAS's connections in PostgreSQL

```sql
SELECT pid, usename, client_addr, state
FROM pg_stat_activity
WHERE usename = 'digistack_app';
```

You should see ~5 rows per member (matching min=5). Note one **pid**.

### 7b — Kill one

```sql
SELECT pg_terminate_backend(<pid>);
```

Result: `t` (true). The connection is dead — but WAS doesn't know yet.

### 7c — Confirm it's gone from PostgreSQL

```sql
SELECT pid FROM pg_stat_activity WHERE pid = <pid>;
```

0 rows. ✅

### d — Use the app

Open `http://192.168.10.20/digistack-bank/Dashboard`.

**Expected:** page loads normally. No error.

**What happened in milliseconds:**

1. WAS tried to hand you the dead connection
2. Pre-test `SELECT 1` failed
3. EntirePool purged, fresh connections opened
4. You got a healthy connection. User sees nothing.

### 7e — Confirm fresh connections

```sql
SELECT pid, client_addr, state FROM pg_stat_activity
WHERE usename = 'digistack_app';
```

New PIDs appear. Pool rebuilt itself. ✅

### 7f — Check WAS logs

```bash
grep -i "stale\|purge\|validate" .../SystemOut.log | tail -20
```

You'll see stale-connection / purge messages.

> 🔑 **Key point:** the log shows the problem, but the user saw no error. That's success.

---

## 9. Memory Aids 🧠

- **Pool = taxi stand.** Min = taxis always waiting. Max = hiring limit.
- **40 total** = 2 members × 20. **60 headroom** against `max_connections=100`.
- **EntirePool** = wash the whole cupboard when one glass is bad.
- **Pre-test + SELECT 1** = check the taxi engine before the guest gets in.
- **Restart required** — pool settings load only at startup.
- **Kill one, app works anyway** = validation proven.

---

## ✅ Acceptance Checklist

- [ ] Min 5, Max 20 on `jdbc/BankDS`
- [ ] 40 peak total documented, 60 headroom vs 100
- [ ] All 4 timeouts set (180 / 1800 / 7200 / 180)
- [ ] Purge policy = EntirePool
- [ ] Pre-test on, query = SELECT 1
- [ ] Killed a backend with `pg_terminate_backend()`
- [ ] App request succeeded with no visible error
- [ ] Fresh PIDs confirmed in `pg_stat_activity`
