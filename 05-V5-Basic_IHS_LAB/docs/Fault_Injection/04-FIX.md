# Fix — Restore the Environment

## Step 1 — Uncomment the directive

On the **dsb-ihs** VM:

```bash
sed -i 's/#WebSpherePluginConfig/WebSpherePluginConfig/' \
  /apps/IBM/HTTPServer/conf/httpd.conf
```

## Step 2 — Confirm the fix was applied

```bash
grep "WebSpherePluginConfig" /apps/IBM/HTTPServer/conf/httpd.conf
```

**Expected result — the line no longer has a `#`:**

```
WebSpherePluginConfig /apps/IBM/WebSphere/Plugins/config/webserver1/plugin-cfg.xml
```

## Step 3 — Test the configuration syntax before restarting

Good practice reinforced from Sprint 4:

```bash
/apps/IBM/HTTPServer/bin/apachectl configtest
```

**Expected result:**

```
Syntax OK
```

## Step 4 — Restart IHS

```bash
/apps/IBM/HTTPServer/bin/apachectl stop
/apps/IBM/HTTPServer/bin/apachectl start
```

## Step 5 — Confirm the fix in the browser (bare IHS root)

First test the bare IHS root:

```
http://192.168.10.20
```

**Expected result:** IHS default page loads normally (**HTTP 200**, not 500). ✅

## Step 6 — Confirm the application path

```
http://192.168.10.20/digistack-bank/Home
```

**Expected result:**

- Home page renders correctly
- **Database: Connected** in green
- Footer shows **v4 — Application Lifecycle**

## Step 7 — Confirm the IHS error_log shows no further module errors

```bash
tail -10 /apps/IBM/HTTPServer/logs/error_log
```

**Expected result:** no `mod_was_ap22_http` error lines after the restart timestamp.

## Step 8 — Full regression check

Log in as `customer1` / `Customer@123` via IHS, confirm Dashboard, perform a small deposit, confirm Logout — **full path proven working again**. ✅

---

# Prevention

## What Would Have Caught This Faster

### 1. `apachectl configtest` before every restart — unconditionally

This is the **single most important habit** from this drill.

> ⚠️ **Critical nuance:** a commented-out `WebSpherePluginConfig` line does **NOT** fail `configtest` — Apache considers a loaded-but-unconfigured module a **runtime issue, not a syntax error**. This means `configtest` alone is not sufficient to catch this specific fault.

> **Lesson to internalize: syntax validity ≠ functional correctness.**

### 2. Post-restart smoke test on the bare root path, not just the application path

If the on-call admin's first check after any IHS config change is:

```bash
curl http://192.168.10.20
```

*(no path)* — and it returns anything other than **HTTP 200**, that is an **instant, unambiguous signal** that IHS itself — independent of any WAS routing — is broken.

This is a **faster and more diagnostic** first check than testing the application URL directly.

### 3. Never comment out one half of a directive pair

`LoadModule` and `WebSpherePluginConfig` are a **matched pair** — loading the module without configuring it is a broken half-state that is:

- **Easy to introduce accidentally** (e.g., commenting out a line while editing nearby content)
- **Hard to spot visually** in a long config file

### 4. Configuration change diffing

Before restarting IHS after any manual edit, run:

```bash
diff /apps/IBM/HTTPServer/conf/httpd.conf \
     /apps/IBM/HTTPServer/conf/httpd.conf.backup-v4.5
```

This immediately surfaces exactly what changed — a **single `#` character addition** — **before the restart is even performed**, catching the mistake pre-emptively.

### 5. Version control for httpd.conf

Since `httpd.conf` lives outside the project's Git repository (it is VM-local, as noted in **SetupDoc-v4.5.md §4.4**), consider committing a copy of `httpd.conf` to Git after every verified-working state — specifically so that `git diff` can be used the same way `diff` was used above, **with full history**, not just a single backup file.

