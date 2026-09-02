# Update Application via Admin Console (GUI Method)

## 💡 Concept — Update replaces the EAR under the existing app name

The application currently registered as `digistack-bank-v3` will have its EAR
replaced with `digistack-bank-v4.ear`.

---

## Step 2.1 — Open the Admin Console

```
http://192.168.10.10:9060/ibm/console
```

Log in as `wasadmin`.

## Step 2.2 — Navigate to

**Applications → Application Types → WebSphere enterprise applications**

## Step 2.3 — Open the app detail page

Click on `digistack-bank-v3`.

## Step 2.4 — Click **Update**

(In the button row at the top of the detail page.)

## Step 2.5 — On the Update Application page, select:

```
(•) Replace the entire application
```

## 💡 Concept — Three Update options

| Option | What it does | When to use |
|---|---|---|
| **Replace the entire application** | Replaces the complete EAR | Code changes affecting multiple modules ✅ |
| **Replace or add a single module** | Replaces one WAR/JAR within the EAR | Targeted module updates |
| **Replace or add a single file** | Replaces one file inside a module | Rarely used in practice |

> ✅ You **always** use "Replace the entire application" in this project.

## Step 2.6 — Click **Browse** (next to "Local file system")

Navigate to and select:

```
C:\Projects\digistack-bank-parent\digistack-bank-ear\target\digistack-bank-v4.ear
```

Click **Open**.

## Step 2.7 — Click **Next**

WAS uploads and analyses the EAR. This takes **10 to 30 seconds**.

## Step 2.8 — On the "Preparing for the application installation" page

✅ Accept all defaults. **Do not change any mappings** — the existing server
and virtual host mappings are preserved automatically during an Update.

Click **Next**.

## Step 2.9 — On the Summary page, confirm:

- [x] Application name: `digistack-bank-v3` (unchanged — this is correct)
- [x] The WAR is still mapped to `server1`

Click **Finish**.

WAS processes the update. You will see a progress page with messages.

**✅ Expected result** — last message on the installation output page:

```
Application digistack-bank-v3 updated successfully.
```

## Step 2.10 — Click **Save** in the banner

## Step 2.11 — Return to the application list

**Applications → Application Types → WebSphere enterprise applications**

## Step 2.12 — Stop and Start `digistack-bank-v3`

Ensures the new code is fully active:

1. Tick the checkbox next to `digistack-bank-v3`
2. Click **Stop** → wait for red ❌
3. Tick again → click **Start**

**✅ Expected result — green arrow ▶. The update is complete.**

---

# Step 3 — Verify the Update Took Effect in the Browser

## Step 3.1 — Open the browser and navigate to:

```
http://192.168.10.10:9080/digistack-bank/Home
```

## Step 3.2 — Scroll to the bottom of the page and read the footer

**✅ Expected result — footer now shows:**

```
DigiStack Bank — © 2026. For educational purposes only. | WebSphere ND 9.0.5.28 | v4 — Application Lifecycle
```

> The `v4 — Application Lifecycle` label confirms the new EAR code is running.
> The v3 label is gone.

## Step 3.3 — Log in and check the Dashboard footer

Login: `customer1` / `Customer@123`

**✅ Expected result — Dashboard footer shows `v4`.**

## Step 3.4 — Navigate to the Account page and check the footer

**✅ Expected result — Account page footer shows `v4`.**

## Step 3.5 — Confirm the app is still fully functional

Perform a small **deposit of ₹500** and confirm the success banner appears.

> ✅ The application still works despite the code swap — Update preserved
> everything except the code.

---
# Verify the Update 
### Step 1 — Open the browser and navigate to:
```
http://192.168.10.10:9080/digistack-bank/Home
```
Expected result — footer shows "v4 — Application Lifecycle". The wsadmin Update path deployed the v4 code successfully.

### Step 2 — Check SystemOut.log confirms the stop and start cycle:
```
grep -E "WSVR0220I|WSVR0221I|WSVR0024I" \
  /apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/logs/server1/SystemOut.log \
  | tail -10
```
Expected result — you should see stop and start messages timestamped during the update:
```
WSVR0221I: Application digistack-bank-v3 stopped successfully.
WSVR0220I: Application digistack-bank-v3 started successfully.
```
#### Concept — WAS Application Lifecycle Log Messages:
```
Message ID	       Meaning
WSVR0220I	    Application started
WSVR0221I	    Application stopped
WSVR0024I	    Application installed
```
### Step 3 — Confirm the application is still fully functional:
```
Log in as customer1 / Customer@123 → Dashboard loads → navigate to Account page → perform ₹500 deposit → success banner appears → balance updated.
```

# Inspect the installedApps Directory

## 💡 Concept — installedApps directory

When WAS deploys an EAR, it **expands (unzips)** it into a directory under the
profile's `installedApps` folder.

- This is the **live running code**.
- When you do an Update, WAS **replaces the contents** of this directory with
  the new EAR's contents.
- Looking at this directory confirms **what code is actually running**.

---

## Step 7.1 — On the `dsb-dmgr` VM, inspect the directory

```bash
ls -lh \
  /apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/installedApps/devdsbincell01/
```

**✅ Expected result** — you should see a directory named after the application:

```
digistack-bank-v3.ear/
```

## Step 7.2 — Look inside the expanded EAR

```bash
ls -lh \
  /apps/IBM/Web/AppServer/profiles/devdsbinappserver01/installedApps/devdsbincell01/digistack-bank-v3.ear/
```

**✅ Expected result:**

```
digistack-bank-web-1.0.war/
META-INF/
```

## Step 7.3 — Confirm the v4 JSP is inside

```bash
grep "v4" \
  /apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/installedApps/devdsbincell01/digistack-bank-v3.ear/digistack-bank-web-1.0.war/Home.jsp
```

**✅ Expected result** — the grep finds the v4 label in the footer:

```
            &nbsp;|&nbsp; v4 — Application Lifecycle
```

> ✅ This confirms the **v4 code is physically present** in the installedApps
> directory — the Update replaced the files correctly.

---
#  Application Lifecycle (Stop/Start/Restart) + Register digistack-bank-v4

## 🎯 What this sprint delivers

Deliberate practice of **Stop, Start, and Restart** operations on both the
application and the server itself — observing exactly what each does to
end-user requests — followed by the formal **Uninstall+Install** cycle to
register the application under its correct v4 name
(`digistack-bank-v4`), matching the project's naming convention.

## 💡 Concept — Why Stop/Start/Restart matters as its own topic

So far you have stopped and started applications as a **side-effect** of
deployment. This sprint isolates those operations to build a precise mental
model:

- What happens to a browser request when the application is **Stopped**?
- What is the difference between stopping the **application** versus stopping
  the **server**?

> ⚠️ These distinctions matter enormously in production — stopping the wrong
> thing can take down **every application on a shared server**.

---

# Step 1 — Observe Application Stop Behavior

## Step 1.1 — Confirm the application is currently running

Open the browser:

```
http://.168.10.10:9080/digistack-bank/Home
```

**✅ Expected result** — Home page loads normally with v4 footer.

## Step 1.2 — Stop the application via Admin Console

**Applications → Application Types → WebSphere enterprise applications**

Tick `digistack-bank-v3` (still registered under this name at this point) →
click **Stop**.

**✅ Expected result** — status changes to **red ❌ (Stopped)**. No error shown
in Admin Console — this is a clean, expected operation.

## Step 1.3 — Attempt to access the Home page while the application is Stopped

```
http://192.168.10.10:9080/digistack-bank/Home
```

**✅ Expected result** — the browser receives an error. WAS returns:

```
HTTP 404 Not Found
```

or a WAS-branded error page stating the requested resource is unavailable,
depending on WAS version behavior.

> The **server itself is still running** — only the application stopped
> responding.

## Step 1.4 — Confirm the WAS server and Admin Console are unaffected

```
http://192.168.10.10:9060/ibm/console
```

**✅ Expected result** — Admin Console loads normally.

> This proves that stopping an application does **not** stop the server —
> other applications (if any existed) would continue running unaffected.

## Step 1.5 — Confirm in SystemOut.log

```bash
grep "WSVR0221I" \
  /apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/logs/server1/SystemOut.log \
  | tail -3
```

**✅ Expected result:**

```
WSVR0221I: Application digistack-bank-v stopped successfully.
```

---

# Observe Application Start Behavior

## Step 2.1 — Start the application via Admin Console

Tick `digistack-bank-v3` → click **Start**.

**✅ Expected result** — status changes to **green arrow ▶** within a few
seconds.

## Step 2.2 — Confirm the Home page is reachable again

```
http://192.168.10.10:9080/digistack-bank/Home
```

**✅ Expected result** — Home page loads normally.

## Step 2.3 — Confirm in SystemOut.log

```bash
grep "WSVR0220I" \
  /apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/logs/server1/SystemOut.log \
  | tail -3
```

**✅ Expected result:**

```
WSVR0220I: Application digistack-bank-v3 started successfully.
```

---

# Step 3 — Observe Server Stop Behavior (Contrast with Application Stop)

## 💡 Concept — Stopping the server vs stopping the application

Stopping the server (`server1`) terminates the **entire JVM process**. Every
application running inside it goes down — not just one.

> ⚠️ This a much more disruptive operation and is **never done casually in
> production** without a maintenance window.

## Step 3.1 — On the `dsb-dmgr` VM, stop the server

```bash
cd /apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/bin/
./stopServer.sh server1 -username wasadmin -password <YourPassword>
``**✅ Expected result:**

```
ADMU4000I: Server server1 stop completed.
```
```
## Step 3.2 — Attempt to access the Admin Console

```
http://192.168.10.10:9060/ibm/console
```

**✅ Expected result** — the browser **cannot connect at all**. Connection
refused or timeout — because the JVM process (which hosts both the Admin
Console and the application) is completely down.

## Step 3.3 — Attempt to access the Home page

```
http://192.168.10.10:9080/digistack-bank/Home
```

**✅ Expected result** — same outcome, connection refused. **Nothing is
listening on port 9080** because the `server1` process does not exist.

## Step 3.4 — Confirm the process is actually gone on the VM

```bash
ps -ef | grep java | grep server1
```

**✅ Expected result** — no matching process found (only the `grep` command
itself may appear in the list, which does not count).

> 🔑 **The critical distinction:**
> - **Application Stop** = one app down, server alive, Admin Console alive.
> - **Server Stop** = everything down, nothing reachable.

---

# Step 4 — Start the Server Again

```bash
./startServer.sh server1
```

**✅ Expected result:**

```
ADMU3000I: Server server1 open for e-business; process id is XXXXX
```

> ⏱️ This takes **60–120 seconds**. Confirm the Admin Console and Home page
> are reachable again once complete.

---

# Step 5 — Practice Server Restart (Stop + Start Combined)

## 💡 Concept — Restart

A single operation that performs **Stop followed by Start**. Used when a
configuration change requires the JVM to reload (like the ClassLoader change
in v3 Sprint 3) but you do not want to run two separate commands.

## Step 5.1 — On the `dsb-dmgr` VM

There is **no single `restartServer.sh` script** in standalone WAS — restart
is performed as **two sequential commands**:

```bash
./stopServer.sh server1 -username wasadmin -password <YourPassword> && ./startServer.sh server1
```

## 💡 Concept — The `&&` operator

In bash, `&&` runs the second command **only if the first command succeeds**
(exits with status 0). This chains stop and start into a single line,
behaving like a "restart" — but it is really **two discrete operations
chained together**, not a single WAS lifecycle action.

> 📌 In a clustered environment (v5 onward), a proper Node Agent-driven
> restart works differently — this standalone-profile restart pattern is
> specific to v1–v4.

**✅ Expected result:**

```
ADMU4000I: Server server1 stop completed.
ADMU3000I: Server server1 open for e-business; process id is XXXXX
```

## Step 5.2 — Confirm everything is reachable after the restart

```
http://192.168.10.10:9060/ibm/console
http://192.168.10.10:9080/digistack-bank/Home
```

**✅ Both should load normally.**

---

## 🏁 Sprint 3 (Part 1) Verification Checklist

- [x] App **Stopped** → browser gets error (404 / WAS error page)
- [x] Admin Console **still works** while app is stopped
- [x] `WSVR0221I` confirmed in SystemOut.log (app stop)
- [x] App **Started** → Home page loads again
- [x] `WSVR0220I` confirmed in SystemOut.log (app start)
- [x] Server **Stopped** → Admin Console AND app unreachable (connection refused)
- [x] `ps -ef` confirms no `server1` java process
- [x] Server **Started** → everything reachable again
- [x] Restart via `stopServer.sh && startServer.sh` works
