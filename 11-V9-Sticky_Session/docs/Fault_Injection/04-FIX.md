
## 🛠️ Remediation

> The fix is updating **one field** in the JAAS Auth Alias.

### Method A — Admin Console

1. **Admin Console → Security → Global security**
2. **Java Authentication and Authorization Service → J2C authentication data**
3. Click **BankDS_Alias**
4. Change the Password field back to:

   ```
   Wasadmin@951951
   ```

5. Click **OK → Save** (Master Configuration)

### Method B — wsadmin (Jython)

```python
AdminTask.modifyAuthDataEntry(
    '[-alias BankDS_Alias '
    '-user digistack_app '
    '-password Wasadmin@951951]')
AdminConfig.save()
```

---

## ✅ Post-Fix Verification — Run in Order

### Check 1 — Full Resync both nodes

```
Admin Console → System administration → Nodes
Select all nodes → Full Resynchronize
```

✅ **Expected result:** `Synchronization completed successfully.`

> The corrected alias is now pushed to both node-local repositories.

### Check 2 — Restart both application servers

> The corrected credentials are read from the alias at application
> server startup — they are **not hot-reloaded**.

```
Admin Console → Servers → WebSphere application servers
Select both → Stop → wait → Start
```

✅ **Expected result:** Both servers show green started status.

### Check 3 — Test Connection

```
Resources → JDBC → Data sources
Select DigiStack Bank DataSource → Test connection
```

✅ **Expected result:**

```
The test connection operation for data source DigiStack Bank DataSource
on server server1 at node devdsbinnode01 was successful.
The test connection operation for data source DigiStack Bank DataSource
on server server1 at node devdsbinnode02 was successful.
```

### Check 4 — Application End-to-End

| Action | Expected Result |
|---|---|
| Navigate to `http://192.168.10.20/digistack-bank/Home` | ✅ DB Connected shows **"Connected."** |
| Login as `customer1 / Customer@123` | ✅ Dashboard loads. Session created. |
| Deposit ₹100 | ✅ Success banner. Balance updated. |

---

## 💡 Key Lessons

- **One alias feeds all DB connections** — one wrong password = app-wide DB failure
- **Green app ≠ working app** — the green icon reflects JVM health, not DataSource health
- **SQLSTATE 28000 = authentication failure** — go straight to the alias
- **Credentials are not hot-reloaded** — always restart app servers after alias changes
- **Synchronized ≠ correct** — bad config syncs to all nodes just as fast as good config

---

✅ **Incident INC-v7-001 — Root Cause Confirmed, Remediation Complete.**
