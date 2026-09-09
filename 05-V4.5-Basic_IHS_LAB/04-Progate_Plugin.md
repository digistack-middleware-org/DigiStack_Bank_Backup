# Propagate plugin-cfg.xml + Configure httpd.conf + Restart IHS

## Concept — Why Propagation Is a Separate Step from Generation

`plugin-cfg.xml` is **generated** on the WAS VM (dsb-dmgr) because WAS knows its own topology. But IHS runs on a **completely different VM** (dsb-ihs).

The file must be **physically copied** from dsb-dmgr to dsb-ihs — this physical copy is called **propagation**.

- **Automatic:** WAS handles this when you click **"Propagate Plug-in"** — it uses the admin port (8008) to push the file
- **Manual:** you can copy it yourself with `scp`

Both methods are shown here.

---

## Concept — httpd.conf Directives

Apache-based web servers (including IHS) use a main configuration file called `httpd.conf` that controls everything about how the server behaves.

Two directives must be added to make IHS load and use the WAS plugin:

| Directive | Purpose |
|---|---|
| `LoadModule was_ap22_module <path-to-plugin-shared-library>` | Tells IHS to load the WAS plugin as an Apache module at startup |
| `WebSpherePluginConfig <path-to-plugin-cfg.xml>` | Tells the loaded plugin where to find its routing table |

> ⚠️ Without **both** lines, IHS runs as a plain web server with **no knowledge of WAS**.

---

## Step 1 — Propagate plugin-cfg.xml via Admin Console (GUI Method)

### Step 1.1 — Open the Admin Console

```
http://192.168.10.10:9060/ibm/console
```

Log in as `wasadmin`.

### Step 1.2 — Navigate to

```
Servers → Server Types → Web Servers
```

### Step 1.3 — Select webserver1

Click the **checkbox** next to `webserver1`.

### Step 1.4 — Click Propagate Plug-in

Click **Propagate Plug-in** from the button row.

WAS connects to dsb-ihs on the admin port (**8008**) and copies the `plugin-cfg.xml` file to the IHS VM.

**Expected result — success message:**

```
The plug-in configuration file was propagated to webserver1.
```

> **Note — If propagation fails with a connection error:** WAS cannot reach dsb-ihs on port 8008 — this is expected in some environments where the IHS admin daemon is not yet configured. Use the **manual SCP method in Step 3** instead. Both methods produce identical results.

---

## Step 3 — Manual SCP Propagation (If Automatic Propagation Failed)

If the wsadmin propagation failed, use this manual method. This is the **guaranteed fallback** that always works regardless of admin daemon configuration.

### Step 3.1 — Create the plugin config directory

On **dsb-ihs** VM:

```bash
mkdir -p /apps/IBM/WebSphere/Plugins/config/webserver1/
```

### Step 3.2 — Copy the file to dsb-ihs via SCP

On the **dsb-dmgr** VM:

```bash
scp \
  /apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/config/cells/devdsbincell01/nodes/devdsbinnode01/servers/webserver1/plugin-cfg.xml \
  root@192.168.10.20:/apps/IBM/WebSphere/Plugins/config/webserver1/plugin-cfg.xml
```

**Expected result:**

```
plugin-cfg.xml     100%  ...
```

### Step 3.3 — Confirm the file arrived

On **dsb-ihs** VM:

```bash
ls -lh /apps/IBM/WebSphere/Plugins/config/web1/plugin-cfg.xml
```

**Expected result:** file listed with non-zero size.

### Step 3.4 — Confirm the key entries in the propagated file

On **dsb-ihs** VM:

```bash
grep "9080" \
  /apps/IBM/WebSphere/Plugins/config/webserver1/plugin-cfg.xml

grep "digistack-b" \
  /apps/IBM/WebSphere/Plugins/config/webserver1/plugin-cfg.xml
```

**Expected result:** both greps return matching lines. The file on dsb-ihs is identical to the one on dsb-dmgr. ✅

---

## Step 4 — Configure httpd.conf on dsb-ihs

> **Concept — httpd.conf:** The configuration file for IBM HTTP Server (and Apache in general). Located at `/apps/IBM/HTTPServer/conf/httpd.conf`. You add two lines at the bottom to tell IHS to load the WAS plugin module.

### Step 4.1 — Confirm the plugin shared library exists

On the **dsb-ihs** VM:

```bash
ls -lh /apps/IBM/WebSphere/Plugins/bin/64bits/mod_was_ap22_http.so
```

**Expected result:** file listed. This is the compiled plugin module that IHS loads.

If the file does not exist at this path, check if the Plugins package was installed to a different path:

```bash
find /apps/IBM/ -name "mod_was_ap22_http.so" 2>/dev/null
```

Use whatever path is shown in all subsequent commands.

### Step 4.2 — Back up the original httpd.conf

```bash
cp /apps/IBM/HTTPServer/conf/httpd.conf \
   /apps/IBM/HTTPServer/conf/httpd.conf.backup-v4.5
```

### Step 4.3 — Confirm the backup exists

```bash
ls -lh /apps/IBM/HTTPServer/conf/httpd.conf.backup-v4.5
```

**Expected result:** backup file listed.

### Step 4.4 — Add the WAS plugin directives to httpd.conf

Check first whether the directives are **already present** (the Plugins installer may have added them automatically):

```bash
grep -i "LoadModule was_ap22_module\|WebSpherePluginConfig" \
  /apps/IBM/HTTPServer/conf/httpd.conf
```

- **If the grep returns the two lines** → they were auto-added by the Plugins installer. Confirm they reference the correct paths and **skip to Step 4.6**.
- **If the grep returns nothing** → add the directives manually:

```bash
cat >> /apps/IBM/HTTPServer/conf/httpd.conf << 'EOF'

# ── WAS Plugin Configuration (added P01 v4.5) ──
# LoadModule tells IHS to load the WAS plugin as an Apache module.
# This must be present for IHS to know about WAS at all.
LoadModule was_ap22_module /apps/IBM/WebSphere/Plugins/bin/64bits/mod_was_ap22_http.so

# WebSpherePluginConfig tells the loaded plugin where to find
# the routing table (plugin-cfg.xml) generated by WAS.
WebSpherePluginConfig /apps/IBM/WebSphere/Plugins/config/webserver1/plugin-cfg.xml
EOF
```

### Step 4.5 — Confirm the directives are now in httpd.conf

```bash
grep -i "LoadModule was_ap22_module\|WebSpherePluginConfig" \
  /apps/IBM/HTTPServer/conf/httpd.conf
```

**Expected result — both lines present:**

```
LoadModule was_ap22_module /apps/IBM/WebSphere/Plugins/bin/64bits/mod_was_ap22_http.so
WebSpherePluginConfig /apps/IBM/WebSphere/Plugins/config/webserver1/plugin-cfg.xml
```

### Step 4.6 — Test the httpd.conf syntax before restarting IHS

A syntax error in `httpd.conf` will prevent IHS from starting:

```bash
/apps/IBM/HTTPServer/bin/apachectl configtest
```

**Expected result:**

```
Syntax OK
```

> ⚠️ If you see any error, open `httpd.conf` with `vi`, fix the offending line, and re-run `configtest` until you see `Syntax OK`.

---

## Step 5 — Restart IHS to Load the Plugin

### Step 5.1 — Stop IHS

```bash
/apps/IBM/HTTPServer/bin/apachectl stop
```

Wait **3–5 seconds** for the process to fully terminate.

### Step 5.2 — Confirm IHS is stopped

```bash
ps aux | grep httpd | grep -v grep
```

**Expected result:** no output (no httpd processes running).

### Step 5.3 — Start IHS

```bash
/apps/IBM/HTTPServer/bin/apachectl start
```

### Step 5.4 — Confirm IHS started successfully

```bash
ps aux | grep httpd | grep -v grep
```

**Expected result:** one or more `httpd` processes listed.

### Step 5.5 — Check the IHS error log for the plugin load confirmation

```bash
tail -20 /apps/IBM/HTTPServer/logs/error_log
```

**Expected result — look for a line similar to:**

```
[notice] mod_was_ap22_http: plugin loaded successfully
```

:

```
[notice] IBM_HTTP_Server/9.0.5.28 configured -- resuming normal operations
```

The absence of `ERROR` or `FAILED` lines is also a good signal. ✅

### Step 5.6 — Confirm the plugin log file was created

The plugin creates its own log file, separate from IHS's `error_log`:

```bash
ls -lh /apps/IBM/WebSphere/Plugins/logs/webserver1/
```

**Expected result — one or more log files including `http_plugin.log`:**

```
-rw-r--r--. 1 root root ... http_plugin.log
```

### Step 5.7 — Check the plugin's own log

```bash
tail -20 /apps/IBM/WebSphere/Plugins/logs/webserver1/http_plugin.log
```

**Expected result — entries showing the plugin initialised and read the config file:**

```
... plugin config file: .../plugin-cfg.xml
... Successfully loaded plugin configuration
```

---

## Step 6 — Verify the Configuration from the WAS Side

### Step 6.1 — Check via Admin Console

On the **dsb-dmgr** VM, confirm WAS can now see the IHS server status. Navigate to:

```
Servers → Server Types → Web Servers
```

The status of `webserver1` **may** now show a green arrow if WAS can communicate with the IHS admin port.

> ⚠️ If it still shows a red X, that is **acceptable at this stage** — the Admin Console status depends on the IHS admin daemon being configured, which is separate from the plugin routing working. Sprint 5 verifies the routing end-to-end.

### Step 6.2 — Query webserver1 via wsadmin

```bash
cd /apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/bin/

./wsadmin.sh -lang jython \
    -username wasadmin \
    -password <YourPassword> \
    -c "
try:
    state = AdminControl.getAttribute(
        AdminControl.queryNames('type=WebServer,name=webserver1,*'),
        'state'
    )
    print('webserver1 state: ' + str(state))
except Exception as e:
    print('Note: state query returned: ' + str(e))
    print('This is acceptable if IHS admin daemon not configured.')
    print('Sprint 5 verifies routing end-to-end via browser test.')
"
```

**Expected result — either:**

```
webserver1 state: STARTED
```

Or:

```
Note: state query returned: ...
This is acceptable if IHS admin daemon not configured.
Sprint 5 verifies routing end-to-end via browser test.
```

> ✅ Either outcome is acceptable. The definitive routing test is in **Sprint 5**.
