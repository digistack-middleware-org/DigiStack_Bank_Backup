# FaultDrill-v3.md
# DigiStack Bank — P01 Version 3
# Sprint 8 — Fault Injection + Incident Simulation
# Non-gating — Version 3 sign-off not dependent on this sprint.

---

## Drill Summary

| Field | Value |
|---|---|
| Incident ID | INC-v3-001 |
| Version | P01 v3 |
| Date | ___________ |
| Severity | High |
| Category | Log File Permissions / Audit Trail Loss |
| Time to RCA | ___________ |
| Environment Restored | Yes |

---

## Fault Injected

SystemOut.log file permissions were set to 000 on dsb-dmgr,
removing all read and write access for all users and processes:

```bash
chmod 000 \
  /apps/IBM/WebSphere/AppServer/profiles/\
devdsbinappserver01/logs/server1/SystemOut.log
```

No application restart was required — the permission change
takes effect immediately at the OS level.

---

## Symptoms Observed

- Monitoring pipeline alert: log tailer process received
  "Permission denied" on SystemOut.log
- All log monitoring dashboards went silent despite confirmed
  HTTP traffic
- Application appeared fully functional to end users:
  Home page, Login, Dashboard, Account page all responded
  normally — no HTTP 500, no error pages
- Admin Console: server1 Started, digistack-bank-v3 Started
- PostgreSQL on dsb-db: active (running)
- Financial transactions (Deposit/Withdraw) executed with
  no application-layer audit trail
- `tail -20 SystemOut.log` returned: Permission denied
- `grep "AccountServlet" SystemOut.log` returned: Permission denied

---

## Root Cause

`chmod 000` removed all read/write/execute permissions from
SystemOut.log. WAS's logging mechanism is non-blocking —
when the JVM cannot write to the log file, it silently
swallows the write failure and continues processing requests
normally. No exception is thrown to the application layer.
No HTTP error is returned to the browser.

Key diagnostic evidence:
```bash
ls -lh SystemOut.log
# ---------- 1 root root 2.1M ... SystemOut.log
# ^^^^^^^^^^ zero permissions for all — immediate signal
```

The `----------` permission string is the definitive indicator
of chmod 000. File present, content intact, permissions absent.

---

## Fix Applied

1. Restored correct permissions:
```bash
   chmod 640 SystemOut.log
```
2. Confirmed: `ls -lh` shows `-rw-r-----`
3. Confirmed: `tail -20` readable without error
4. Triggered page load to force new log entry
5. Performed ₹100 deposit — confirmed in log:
   `AccountServlet: Deposit successful. userId=1 ...`
6. Audit trail fully restored

---

## Prevention

1. First action on "log gone silent" alert: run
   `ls -lh SystemOut.log` — `----------` = chmod 000,
   diagnosis complete in under 60 seconds.
2. Use `lsof | grep SystemOut.log` to confirm whether
   the JVM still holds the file descriptor open.
3. Protect log files with `chattr +a` (append-only) in
   production — prevents chmod, rm, and overwrite while
   allowing log appends.
4. Run monitoring tailer as a dedicated read-only user —
   permission denial immediately triggers an alert.
5. Implement `auditd` rules on the WAS logs directory to
   capture every chmod/chown with user identity and timestamp.
6. From P04: Prometheus/Grafana monitors JVM metrics;
   OpenSearch ingestion rate drop auto-alerts on log
   pipeline failure.

---

## Lessons Learned

- WAS silently swallows log write failures — a broken log
  file NEVER produces an HTTP 500 or any user-visible error.
  This makes log permission failures uniquely deceptive: the
  application looks healthy while the audit trail is dark.
- In a regulated financial environment, unlogged transactions
  are a compliance violation regardless of DB correctness.
  Log integrity is as important as data integrity.
- `----------` in `ls -lh` output is one of the most
  immediately actionable signals in Linux operations — any
  admin seeing it on a log file should treat it as P1.
- The monitoring pipeline (external log tailer) was the only
  detection mechanism that fired. At v3 there is no
  application-level health check. P04's observability
  stack closes this gap with multiple independent signals.

---

## Environment Restoration Confirmed

- [x] `chmod 640` applied to SystemOut.log
- [x] `ls -lh` shows `-rw-r-----`
- [x] `tail -20` readable without error
- [x] New log entries confirmed after page load
- [x] AccountServlet deposit log line confirmed
- [x] No residual fault artifacts remaining