# Fault Injection Lab — SystemOut.log Permission Fault

## Step 1 — Confirm the environment is clean before injecting

On the **dsb-dmgr** VM:

```bash
curl -s -o /dev/null -w "%{http_code}" \
  http://192.168.10.10:9080/digistack-b/Home
```
Expected result before injection:
```
200
```
Then confirm the account balance reads correctly from the DB:
```
psql -U digistack_app -d digistack_bank -h 127.0.0.1 \
  -c "SELECT id, balance, is_frozen FROM accounts user_id = 1;"
```

Expected result — one row showing a positive balance and is_frozen = f:
```
 id |  balance  | is_frozen
----+-----------+-----------
  1 | XXXXX.00  | f
(1 row)
```
    ⚠️ If the environment is not clean, stop and escalate before continuing.

Step 2 — Inject the fault

Run this command on the dsb-dmgr VM:
```
chmod 000 \
  /apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/logs/server1/SystemOut.log
```
No output is expected. That is correct.

Step 3 — Confirm the injection was applied
```
 -lh \
  /apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/logs/server1/SystemOut.log
```
Expected result — permissions show ----------:

---------- 1 root root ... SystemOut.log

Step 4 — Trigger the fault
Step 4.1

Open your browser and log in as customer1 / Customer@123.
Step 4.2

Navigate to the Account page and attempt a deposit of ₹1,000.

👉 Observe what happens in the browser. Note it down.
Step 4.3

On the dsb-dmgr VM, attempt to read the log:
```
tail -20 \
  /apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/logs/server1/SystemOut.log
```
👉 Observe what happens. Note it down.
Step 4.

Attempt to grep the log for AccountServlet entries:
```
grep "AccountServlet" \
  /apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/logs/server1/SystemOut.log
```
👉 Observe what happens. Note it down.


**⚠️ One important note:** The content contains nested code blocks (```bash inside ```markdown), so **don't paste the outer ```markdown fence** into GitHub. Copy everything *between* the outer fence only — GitHub will render it correctly.

**Optional addition:** You can add an observations table at the end for recording results:

```markdown
## Observations Log

| Step | Observation | Result |
|------|-------------|--------|
| 4.2  | Browser behavior on deposit | |
| 4.3  | tail output | |
| 4.4  | grep output | |
