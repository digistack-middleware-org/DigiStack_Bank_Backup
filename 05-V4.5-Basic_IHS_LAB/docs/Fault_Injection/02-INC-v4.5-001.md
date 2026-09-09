# INCIDENT REPORT

| Field | Detail |
|---|---|
| **Incident ID** | INC-v4.5-001 |
| **Severity** | 🔴 High |
| **Time** | Reported following a routine IHS configuration change window |
| **Application / Service** | DigiStack Bank — Web Tier (IHS Reverse Proxy) |

---

## Business Impact

All customer traffic to the DigiStack Bank public URL is failing. Customers attempting to reach the bank via the standard web address receive an error page. The bank's core application appears to be **completely down from the customer's perspective**, even though internal monitoring shows the backend systems as healthy.

## Customer / Business Symptom

Every page — including the basic IHS landing page with no application path — returns an error to the browser. Customers report the site **"will not load at all."**

## Initial Alert / Ticket

Reported by an internal admin performing a **post-change verification check** immediately after a routine IHS configuration change window.

---

## Observed Error

Browser shows an **HTTP 500 Internal Server Error** when accessing **ANY path** on the IHS host — including the bare IHS root URL with no application path at all.

> ⚠️ This is unusual — even requests that should be served **directly by IHS** with no involvement of WAS are failing.

## Scope

All requests to the IHS host (`192.168.10.20`) fail with HTTP 500 — both the bare IHS root and the application path. WAS itself, accessed directly on its own port, continues to respond normally.

## NOT Affected ✅

| Component | Status |
|---|---|
| Direct access to WAS AppServer on its native port | Works normally — Home page, Login, Dashboard, and Account page all load correctly when the browser **bypasses the web tier** entirely |
| PostgreSQL on dsb-db | `active (running)` |
| The IHS process itself | Confirmed running via `ps aux` — has **not crashed** |

## Recent Change

A routine configuration change was made to the IHS host's **main configuration file** shortly before the incident, followed by a **restart of the web server** to apply the change.

> ⚠️ The specific line(s) changed were **not recorded in the change ticket**.

---

## Starting Evidence

1. **Browser:** HTTP 500 on `http://192.168.10.20` (bare root, no application path)
2. **Browser:** HTTP 500 on `http://192.168.10.20/digistack-bank/Home`
3. **Browser:** `http://192.168.10.10:9080/digistack-bank/Home` — loads normally, full application functionality confirmed
4. **Web tier host:** `ps aux | grep httpd` — process(es) confirmed running, not crashed
5. **Change record:** confirms a config file edit + restart occurred on the web tier host shortly before the incident

### Retrieve First Log Evidence from the Web Tier Host

```bash
tail -20 /apps/IBM/HTTPServer/logs/error_log
```

---

> 🛑 **STOP HERE.**
