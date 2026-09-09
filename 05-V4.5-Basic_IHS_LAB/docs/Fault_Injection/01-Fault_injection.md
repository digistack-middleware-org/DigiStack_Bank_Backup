# Phase 1 — Fault Injection

> ⚠️ **Perform every step exactly. Do not skip any step.**

---

## Step 1 — Confirm the Environment Is Clean Before Injecting

### Step 1.1 — Test the Home page in the browser

On your Windows browser:

```
http://192.168.10.20/digistack-bank/Home
```

**Expected result before injection:**

- Home page loads normally via IHS
- Footer shows **v4 — Application Lifecycle**
- **Database: Connected** in green

### Step 1.2 — Confirm IHS is running on dsb-ihs

```bash
ps aux | grep httpd | grep -v grep
```

**Expected result:** `httpd` processes listed.

> 🛑 **If the environment is not clean, stop and let me know before continuing.**

---

## Step 2 — Inject the Fault

On the **dsb-ihs** VM, run:

```bash
sed -i 's/WebSpherePluginConfig/#WebSpherePluginConfig/' \
  /apps/IBM/HTTPServer/conf/httpd.conf
```

**Expected result:** no output. That is correct. ✅

> **What this does:** the `sed` command comments out the `WebSpherePluginConfig` directive in `httpd.conf` by prepending a `#`. Once commented, the plugin no longer knows where to find its routing table.

---

## Step 3 — Confirm the Injection Was Applied

```bash
grep "WebSpherePluginConfig" /apps/IBM/HTTPServer/conf/httpd.conf
```

**Expected result — the line now shows a `#` at the start:**

```
#WebSpherePluginConfig /apps/IBM/WebSphere/Plugins/config/webserver1/plugin-cfg.xml
```

---

## Step 4 — Restart IHS to Activate the Change

```bash
/apps/IBM/HTTPServer/bin/apachectl stop
/apps/IBM/HTTPServer/bin/apachectl start
```

**Expected result:** no error output from either command.

### Step 4.1 — Confirm IHS restarted successfully

```bash
ps aux | grep httpd | grep -v grep
```

**Expected result:** `httpd` processes listed — **IHS itself is running fine**.

> 📌 The fault is not that IHS is down — it is that IHS is running **without knowledge of WAS**.

---

## Step 5 — Trigger the Fault

### Step 5.1 — Test the IHS root URL

Open your browser and navigate to:

```
http://192.168.10.20
```

*(IHS root, no path)*

**Observe what happens. Note it down.**

### Step 5.2 — Test the application URL via IHS

Navigate to:

```
http://192.168.10.20/digistack-bank/Home
```

**Observe what happens. Note it down.**

### Step 5.3 — Confirm WAS is completely unaffected

Navigate directly to WAS:

```
http://192.168.10.10:9080/digistack-bank/Home
```

**Observe what happens. Note it down.**

---

> ✅ **Fault injection is complete.**
