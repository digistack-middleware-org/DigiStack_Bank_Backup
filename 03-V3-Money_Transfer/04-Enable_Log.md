# Add WAS Logging Configuration for Transaction Events
Concept — Why configure logging now: The transaction operations (deposit, withdraw) are now running live. You want to confirm that:

    Every transaction writes a log line
    Log rotation is still in place from v1
    The diagnostic trace level is correct


# GUI - Console Method

## 🔑 Open the Console First

Browser → https://:9043/ibm/console (or :9060)
Login: wasadmin /


---

## ✅ Check 1 — JVM Log Rotation Settings (SystemOut / SystemErr)

*This replaces the `RASLoggingService` / `StreamRedirect` part of the script.*

1. In the left navigation:
   **Servers → Server Types → WebSphere application servers**
2. Click **server1**
3. Under **General Properties → Troubleshooting**, click **Logging and Tracing**
4. Click **JVM Logs**
5. Scroll to **File Rotation** section — check the values for both:
   - **System.out stream** (your `SystemOut.log`)
   - **System.err stream** (your `SystemErr.log`)

| Field in GUI | Script equivalent | Expected Value |
|---|---|---|
| Maximum number of historical files | `maxNumberOfBackupFiles` | **3** |
| Maximum log file size | `rolloverSize` | **50 MB** |

- ✔️ If both show **3 files / 50 MB** → **OK** (matches v1 Sprint 4 settings)
- ⚠️ If not → set them, click **OK**, then **Save** at the top

> 💡 This GUI page **IS** the script's "JVM Log Rotation Settings" section — same data, different view.

---

## ✅ Check 2 — Diagnostic Trace Configuration

*This replaces the `TraceService` part of the script.*

1. Go back to **server1 → Logging and Tracing**
2. Click **Diagnostic Trace**
3. Review these fields:

| Field in GUI | Script equivalent | Expected Value |
|---|---|---|
| Trace Output Type | `traceOutputType` | BASIC mode / memory buffer |
| Maximum number of historical files | `maxNumberOfBackupFiles` | 3 |
| Maximum trace file size | `rolloverSize` | 50 MB |
| Startup Trace Specification | `startupTraceSpecification` | `*=info` |

4. Confirm **Startup trace specification** = `*=info`
   - If not → change it to `*=info`, click **OK**, **Save**

> 💡 `*=info` in plain words: *"Log everything at INFO level and above for all components"* — normal operation level, not too noisy, not too quiet.

---

## ✅ Check 3 — Runtime Trace Level (live, without restart)

*This replaces the `AdminControl.getAttribute(traceSpecification)` part of the script.*

1. Go back to **server1 → Logging and Tracing**
2. Click **Change Log Detail Levels** (this is the RUNTIME view — applies immediately, no restart!)
3. In the trace specification text box, verify it shows:

*=info


- ✔️ If yes → **OK**, click **OK** to close
- ⚠️ If it shows something else (e.g. `*=all` from past debugging):
  1. Type `*=info` in the box
  2. Click **OK**
  3. This applies **instantly** to the running server ✅

> 💡 **Config vs Runtime:**
> - The **Diagnostic Trace** page = what loads at next **restart** (config)
> - The **Change Log Detail Levels** page = what's active **right now** (runtime)
>
> This is exactly the difference the script checked (`startupTraceSpecification` vs `traceSpecification`).

---

## ✅ Check 4 — Application Is Running

*This replaces the `deploymentState` part of the script.*

1. Left navigation: **Applications → All Applications**
2. Find **digistack-bank-v3** in the list
3. Check the status icon:

| Icon | Meaning |
|---|---|
| 🟢 Green arrow | Running ✅ |
| ⚪ Grey | Stopped ❌ → check the box → click **Start** |

> Optional deeper check: click the app name → under **Detailed Properties** you can see it's targeting server1 — same info the script's `deploymentState` attribute read.

---

## ✅ Check 5 — See the Actual Transaction Logs (the Real Payoff 📋)

The GUI cannot show log *contents* — logs are files on disk. View them on the **dsb-app VM**:

```bash
cd /opt/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/logs/server1/
tail -f SystemOut.log

Then in your browser: Login → Dashboard → Deposit 100→Withdraw100→Withdraw50 → Withdraw $999,999

You should see lines like:

AccountServlet  Deposit of 100 processed for account 7
AccountServlet  Withdraw of 50 processed for account 7
AccountServlet  Withdraw rejected — InsufficientFundsException, account 7

Also confirm rotation files exist:

ls -lh SystemOut.log SystemOut_*.log

    If you see SystemOut_23.02.15_...log files → rotation is working in practice, not just in config. ✅
```




# Scripting - wasadmin Method

Goto the wasadmin Directory
```
cd /apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/bin/
```
Run the Scripts
```
./wsadmin.sh -lang jython \
    -username wasadmin \
    -password <your-password> \
    -f /tmp/v3_verify_logging.py
```

Verify the Actual Transaction Logs

```bash
cd /opt/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/logs/server1/
tail -f SystemOut.log

Then in your browser: Login → Dashboard → Deposit 100→Withdraw100→Withdraw50 → Withdraw $999,999

You should see lines like:

AccountServlet  Deposit of 100 processed for account 7
AccountServlet  Withdraw of 50 processed for account 7
AccountServlet  Withdraw rejected — InsufficientFundsException, account 7

Also confirm rotation files exist:

ls -lh SystemOut.log SystemOut_*.log

    If you see SystemOut_23.02.15_...log files → rotation is working in practice, not just in config. ✅
```