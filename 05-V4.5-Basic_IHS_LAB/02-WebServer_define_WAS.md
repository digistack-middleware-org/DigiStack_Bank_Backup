# Web Server Definition in WAS

**What this sprint delivers:** A `webserver1` Web Server Definition created inside the WAS Admin Console and confirmed via wsadmin — the WAS-side configuration object that represents the IHS server and tells WAS how to generate the plugin routing table for it.

Without this object, WAS has no knowledge that IHS exists and cannot generate any plugin configuration.

---

## Concept — Web Server Definition

When you create a Web Server Definition in WAS, you are telling WAS three things:

1. *"There is a web server called `webserver1` out there"*
2. *"It lives at this hostname/IP and listens on this port"*
3. *"Its IHS installation is at this path, and here is where to put the plugin config files"*

WAS uses this information to generate `plugin-cfg.xml` (Sprint 3) — the routing table that IHS reads to know where to forward requests. The Web Server Definition is the **prerequisite** for that generation step.

---

## Concept — Why Status Shows "Stopped" After Creation

After creating the Web Server Definition, Admin Console shows `webserver1` with a red X (**Stopped**).

> ✅ This is **expected and correct** — WAS cannot communicate with IHS yet because the plugin has not been propagated to IHS and the IHS admin port configuration is not yet active.

The status will turn **green** once the plugin is deployed and IHS can respond to WAS's status queries on port 8008.

---

## Step 1 — Create the Web Server Definition via Admin Console (GUI Method)

### Step 1.1 — Open the Admin Console

Open the Admin Console in your Windows browser:

```
http://192.168.10.10:9060/ibm/console
```

Log in as `wasadmin`.

### Step 1.2 — Navigate to the Web Servers section

```
Servers → Server Types → Web Servers
```

### Step 1.3 — Click New

Click **New** to create a new Web Server Definition.

### Step 1.4 — Step 1 of the wizard — Select the node

You will see a table listing available nodes. Select:

```
devdsbinnode01
```

Click **Next**.

### Step 1.5 — Step 2 — Web server properties

Fill in these fields exactly:

| Field | Value |
|---|---|
| Web server name | `webserver1` |
| Type | IBM HTTP Server |
| Host name | `192.168.10.20` |
| Web server installation location | `/apps/IBM/HTTPServer` |
| Service name | *(leave blank — Linux does not use Windows service names)* |
| Web server config file | `/apps/IBM/HTTPServer/conf/httpd.conf` |
| Web server port | `80` |

### Step 1.6 — Plug-in properties (same page)

Fill in the **Plug-in properties** section on the same page:

| Field | Value |
|---|---|
| WAS installation location | `/apps/IBM/WebSphere/AppServer` |
| Plug-ins installation location | `/apps/IBM/WebSphere/Plugins` |

> **Concept — Plug-ins installation location:** The WAS Web Server Plug-ins (the Plugins package installed alongside IHS) provides the shared library (`mod_was_ap22_http.so`) that IHS loads. The Plugins installation location tells WAS where this package was installed on the IHS VM — WAS needs this path to know where to write the propagated `plugin-cfg.xml`.

Click **Next**.

### Step 1.7 — Step 3 — Confirmation page

Review the summary. Confirm:

| Item | Value |
|---|---|
| Web server name | `webserver1` |
| Type | IBM HTTP Server |
| Host name | `192.168.10.20` |
| Port | `80` |
| Node | `devdsbinnode01` |

Click **Finish**.

### Step 1.8 — Save the configuration

Click **Save** in the banner.

### Step 1.9 — Confirm webserver1 appears in the Web Servers list

Navigate back to:

```
Servers → Server Types → Web Servers
```

**Expected result:** `webserver1` is listed with status showing a **red X (Stopped)**. This is correct at this stage. ✅

---

## Step 3 — Inspect the Web Server Definition in Admin Console

After the definition is created (by either method), verify every field is correct.

### Step 3.1 — Navigate to

```
Servers → Server Types → Web Servers
```

### Step 3.2 — Open webserver1

Click on `webserver1` to open the detail page.

### Step 3.3 — Confirm General Properties

Confirm these values on the **General Properties** tab:

| Property | Expected Value |
|---|---|
| Web server name | `webserver1` |
| Type | IBM HTTP Server |
| Host name | `192.168.10.20` |
| Web server port | `80` |
| Web server config file | `/apps/IBM/HTTPServer/conf/httpd.conf` |

### Step 3.4 — Confirm Plug-in Properties

Click **Additional Properties → Plug-in Properties** and confirm:

| Property | Expected Value |
|---|---|
| Web server plug-in installation location | `/apps/IBM/WebSphere/Plugins` |

### Step 3.5 — Check the status

Confirm the status on the Web Servers list page shows a **red X (Stopped)**.

> ⚠️ This is correct — **do not try to start it now**. The status will change once the plugin is propagated in Sprint 4.

---

## Step 4 — Confirm the Web Server Definition in the WAS Configuration Files

> **Concept — WAS configuration on disk:** Every object you create via Admin Console or wsadmin is persisted as an XML file on disk inside the WAS profile's `config/` directory. You can read these files to confirm what WAS has stored.

### Step 4.1 — Find the webserver1 configuration

On the **dsb-dmgr** VM:

```bash
find \
  /apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/config/ \
  -name "serverindex.xml" \
  -path "*/devdsbinnode01/*"
```

**Expected result:** path to the `serverindex.xml` file for the node.

### Step 4.2 — Confirm webserver1 appears in the server index

```bash
grep -i "webserver1" \
  /apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/config/cells/devdsbincell01/nodes/devdsbinnode01/serverindex.xml
```

**Expected result — one or more lines referencing `webserver1`:**

```
... serverName="webserver1" ...
```

> ✅ This confirms WAS has persisted the Web Server Definition to disk — it will survive server restarts.
