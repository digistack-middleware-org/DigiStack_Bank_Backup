# INC-v4.5-001 — Root Cause Analysis
---

## Why This Breaks EVERYTHING, Not Just the WAS-Routed Paths

> ⭐ This is the **critical, non-obvious lesson** of this drill.

`LoadModule` tells IHS to load the WAS plugin as an **active Apache module at startup** — this succeeded, since the shared library path was correct and unchanged. But once loaded, the plugin module **requires** a valid `WebSpherePluginConfig` directive pointing to a routing table. With that directive commented out, the module **has no configuration to initialize itself with**.

An Apache/IHS module that fails to initialize correctly **does not just silently disable itself** — it can cause Apache's entire request-processing pipeline to fail, because Apache processes the module chain **in order for every single request**, including ones that never should have touched WAS at all (like the bare IHS root page).

The result:

1. The module is present in the request pipeline
2. It errors out early because it has no config
3. That error propagates as an **HTTP 500 for every request** IHS handles — not just the ones meant for WAS

---

## Why the IHS Process Itself Did Not Crash

`ps aux` confirmed `httpd` processes were running. The main IHS server process **starts and binds to port 80 successfully** — the failure only manifests when **actual requests** attempt to flow through the module chain and hit the misconfigured WAS plugin module.

> 📌 This is why `apachectl start` reported no error and the process list looked completely healthy — **yet every single request failed**.

## Why Direct WAS Access Worked Normally Throughout

WAS on dsb-dmgr was **never touched** by this fault. The `WebSpherePluginConfig` directive lives entirely in IHS's configuration on the **dsb-ihs VM** — a completely separate machine. WAS has no awareness of IHS's internal module configuration at all.

> This is the same architectural separation demonstrated throughout v4.5: **IHS and WAS are independent processes on independent VMs** — a fault in one's configuration does not touch the other's runtime.

---

## The Exact Log Evidence That Confirms the Root Cause

```bash
tail -20 /apps/IBM/HTTPServer/logs/error_log
```

Would show a line similar to:

```
[error] mod_was_ap22_http: Unable to locate plugin-cfg.xml -- WebSpherePluginConfig directive not found
```

or an equivalent module initialization failure message, **occurring at every request** rather than only at startup — confirming the module is failing **per-request**, not just failing to load.

## Confirming Diagnostic Commands

```bash
grep -c "WebSpherePluginConfig" /apps/IBM/HTTPServer/conf/httpd.conf
grep "WebSpherePluginConfig" /apps/IBM/HTTPServer/conf/httpd.conf
```

The second command would show the commented-out line, immediately revealing the directive is **present but disabled** — a **one-character difference (`#`)** causing a total outage.

---