# P01 v4.5 — Sprint 5: End-to-End Flow Verification — Browser→IHS→WAS→DB

**What this sprint delivers:** The complete **Browser→IHS→WAS→DB** request path proven end-to-end — the Home page loading via IHS port 80 with the live `app_config` DB read confirmed, IHS `access_log` showing the request, WAS `SystemOut.log` showing the forwarded request, and a **deliberate IHS stop** proving IHS the real front door (not a pass-through illusion).

This is the sprint deliverable for v4.5 per the roadmap.

---

## Concept — Why "Proving IHS Is the Front Door" Matters

It is possible to **misread** a successful page load — the browser could be hitting WAS directly on port 9080 while IHS is running but not actually routing anything.

> **The definitive proof is stopping IHS entirely** and confirming the browser gets a **connection error** (not a WAS response).

- If stopping IHS produces a connection error → IHS is **genuinely in the path** ✅
- If the page still loads after IHS is stopped → something is wrong — the browser is **bypassing IHS** ❌

---

## Step 1 — Confirm Both Servers Are Running Before

### Step 1.1 — Confirm WAS is running

On **dsb-dmgr** VM:

```bash
cd /apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/bin/
./serverStatus.sh server1 -username wasadmin -password <YourPassword>
```

**Expected result:**

```
ADMU0508I: The Application Server "server1" is STARTED.
```

If server1 is not started, start it:

```bash
./startServer.sh server1
```

### Step1.2 — Confirm PostgreSQL is running

On **dsb-db** VM:

```bash
systemctl status postgresql-16
```

**Expected result:** `Active: active (running)`

### Step 1.3 — Confirm IHS is running

On **dsb-ihs** VM:

```bash
ps aux | grep httpd | grep -v grep
```

**Expected result:** one or more `httpd` processes listed.

### Step 1.4 — Confirm the application is started

From the Admin Console:

```
Applications → Application Types → WebSphere enterprise applications
```

**Expected result:** `digistack-bank-v4` shows green arrow ▶

---

## Step 2 — Test the Home Page via IHS Port 80

### Step 2.1 — Open the Home page via IHS

Open your Windows browser and navigate to:

```
http://192.168.10.20/digistack-bank/Home
```

> ⚠️ **Note carefully:** this is the **IHS IP (192.168.10.20)** on **port 80** — *not* the WAS IP (192.168.10.10) on port 9080.

**Expected result:** the DigiStack Bank Home page loads exactly as it does when accessed directly via WAS:

- Page renders fully with the navy/gold design
- Status bar shows **Database: Connected** in green
- Footer shows **v4 — Application Lifecycle**

### Step 2.2 — Test the context root redirect via IHS

```
http://192.168.10.20/digistack-bank/
```

**Expected result:** same Home page loads. The welcome-file redirect works through IHS exactly as through WAS directly.

### Step 2.3 — Note what you did NOT type

You did **not** type port 9080. You did **not** type the dsb-dmgr IP.

The browser went to **IHS on port 80**, and IHS **silently forwarded** the request to WAS on port 9080. The user sees only the application — the reverse proxy is **transparent**.

---

## Step 3 — Confirm IHS Received the Request in Its access_log

### Step 3.1 — Check the access log

On the **dsb-ihs** VM:

```bash
tail -10 /apps/IBM/HTTPServer/logs/access_log
```

**Expected result — entries showing your Windows browser's IP making a GET request:**

```
192168.X.X - - [date time] "GET /digistack-bank/Home HTTP/1.1" 200 XXXX
192.168.X.X - - [date time] "GET /digistack-bank/ HTTP/1.1" 200 XXXX
```

> ✅ The HTTP **200** status code confirms IHS received the request and got a successful response back from WAS to return to the browser.

### Step 3.2 — Confirm the plugin log shows the routing decision

```bash
tail -20 /apps/IBM/WebSphere/Plugins/logs/webserver1/http_plugin.log
```

**Expected result — entries showing the plugin matched the URI pattern and routed to WAS:**

```
... uri: /digistack-bank/Home
... Routing to: ... server1 ...
```

> The exact log format varies by plugin version — look for entries referencing `digistack-bank` and a server name or IP/port.

---

## Step 4 — Confirm WAS Received the Forwarded Request in SystemOut.log

### Step 4.1 — Check the WAS log

On the **dsb-dmgr** VM:

```bash
grep "HomeServlet" \
  /apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/logs/server1/SystemOut.log \
  | tail -5
``**Expected result:**

```
HomeServlet: DB read successful. bank.name=DigiStack Bank


This confirms **all three**:

1. The request reached WAS (`HomeServlet` executed)
2. WAS connected to PostgreSQL on dsb-db (`DB read successful`)
3. The full **Browser→IHS→WAS→DB** chain is live ✅

---

## Step 5 — Test the Login and Transaction Flow via IHS

### Step 5.1 — Navigate to the Login page via IHS

```
http://192.168.10.20/digistack-bank/Login
```

**Expected result:** Login page renders correctly.

### Step 5.2 — Log in

Log in as `customer1` / `Customer@123`.

**Expected result:** Dashboard loads. The session cookie (`JSESSIONID`) is correctly handled through the IHS reverse proxy — session management works **transparently**.

### Step 5.3 — Perform a deposit

Navigate to the **Account** page and perform a small deposit of **₹100**.

**Expected result:** green success banner. Balance updated.

### Step 5.4 — Test the AJAX call

Click **View Balance** on the Dashboard — confirm the AJAX call to `/BalanceJson` also routes correctly through IHS.

**Expected result:** balance reveals **without page reload**. This confirms AJAX requests also route correctly through the plugin.

### Step 5.5 — Log out

Click **Logout**. Confirm redirect to Home page via IHS.

---

## Step 6 — Compare Direct WAS Access vs IHS Access

> **Concept:** At v4.5, direct WAS access is **still possible**. In production, you would block port 9080 at the firewall so ALL traffic must go through IHS. At v4.5 this restriction is **not yet enforced — that is deliberate**, allowing you to compare both paths side by side.

### Step 6.1 — Via IHS (port 80)

In one browser tab:

```
http://192.168.10.20/digistack-bank/Home
```

### Step 6.2 — Direct to WAS (port 9080)

In another browser tab:

```
http://192.168.10.10:9080/digistack-bank/Home
```

**Expected result:** both tabs show **identical pages**. This confirms IHS is acting as a **transparent reverse proxy** — same application, two paths.

### Step 6.3 — Record the comparison

Note this side-by-side comparison in your test notes.

> 📌 The **v8 deliverable** (IHS cluster era) includes making port 9080 firewalled — from v8 onward, **only the IHS path works**.

---

## Step 7 — Prove IHS Is the Real Front Door (Stop IHS Test)

> ⭐ This is the **definitive proof** that IHS is genuinely in the routing path.

### Step 7.1 — Stop IHS on dsb-ihs

```bash
/apps/IBM/HTTPServer/bin/apachectl stop
```

Wait **5 seconds** for the process to fully terminate.

### Step 7.2 — Confirm IHS is stopped

```bash
ps aux | grep httpd | grep -v grep
```

**Expected result:** no output. IHS is down.

### Step 7.3 — Test the IHS URL from the browser

```
http://192.168.10.20/digistack-bank/Home
```

**Expected result — the browser shows a connection error:**

```
This site can't be reached
192.168.10.20 refused to connect.
```

or similar — the exact message depends on your browser.

> The key point: **nothing responds on port 80** — IHS is the only process that was listening there.

### Step 7.4 — Confirm WAS is still running

Navigate directly to WAS:

```
http://192.168.10.10:9080/digistack-bank/Home
```

**Expected result:** the Home page loads normally.

> **WAS is completely unaffected by IHS stopping.** The WAS process continues running — only the front-door web server is down.

### This proves conclusively:

- ✅ IHS was the **genuine front door** for port 80 traffic
- ✅ WAS is **still alive** behind it
- ✅ Stopping IHS is **not the same** as stopping WAS
- ✅ Traffic **cannot reach WAS via port 80** when IHS is down

### Step 7.5 — Start IHS again

```bash
/apps/IBM/HTTPServer/bin/apachectl start
```

### Step 7.6 — Confirm IHS is running

```bash
ps aux | grep httpd | grep -v grep
```

### Step 7.7 — Confirm the IHS path is restored

```
http://192.168.10.20/digistack-bank/Home
```

**Expected result:** Home page loads again via IHS. ✅
