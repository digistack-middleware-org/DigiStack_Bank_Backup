# ✅ Fix — Restore the Environment (INC-v3-001)

## Step 1 — Restore the correct permissions

On the **dsb-dmgr** VM:

```bash
chmod 640 \
  /apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/logs/server1/SystemOut.log
```

> 💡 **Concept — `chmod 640`:** Owner (WAS process / root) gets read+write (`6`), group gets read-only (`4`), others get nothing (`0`). This is the standard permission for a log file owned by the WAS process user — readable by the admin group for monitoring, writable only by the owner.

No output is expected. That is correct.

---

## Step 2 — Confirm permissions are restored

```bash
ls -lh \
  /apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/logs/server1/SystemOut.log
```

**Expected result:**

```
-rw-r----- 1 root root 2.1M ... SystemOut.log
```

`-rw-r-----` = owner read+write, group read, others none. ✅ Correct.

---

## Step 3 — Confirm the log is now readable

```bash
tail -20 \
  /apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/logs/server1/SystemOut.log
``**Expected result:** The last 20 lines of the log appear **without a permission error**. You should see WAS startup messages and servlet init lines.

---

## Step 4 — Force WAS to write a fresh log entry

Open the browser and navigate to:

```
http://192.168.10.10:9080/digistack-bank/Home
```

---

## Step 5 — Confirm new entries are appearing

```bash
tail -5 \
  /apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/logs/server1/SystemOut.log
```

**Expected result:** You see **new timestamped entries** after the page load. The log is live again. ✅

---

## Step 6 — Confirm AccountServlet transactions are logging correctly

Perform a small deposit (**100**) in the browser, then:

```bash
grep "AccountServlet" \
  /apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/logs/server1/SystemOut.log \
  | tail -3
```

**Expected result:**

```
AccountServlet: Deposit successful. userId=1 amount=100 newBalance=₹XXXXX.XX
```

The audit trail is restored. ✅

---

## 🛡️ Prevention

### 1. Check file permissions as the FIRST diagnostic step
On any **"log gone silent"** alert, the sequence is:

```
alert fires → ls -lh SystemOut.log → ---------- permissions = immediate diagnosis
```

The entire investigation should take **under 60 seconds** once you know this pattern.

### 2. `lsof` to confirm the JVM is still holding the file open
If permissions are wrong but the process still has the file descriptor open from before the `chmod`, some writes may still succeed (on Linux, **open file descriptors survive permission changes**). If the descriptor was also closed — for example after a log rotation — no writes would succeed at all. `lsof` immediately distinguishes these two cases.

### 3. Immutable file attributes as a guard
In production, critical log files are often protected with `chattr +a` (**append-only attribute**) — this prevents `chmod`, `rm`, and overwrite operations while still allowing the logging process to append. A junior admin running `chmod 000` on an append-only file would get:

```
Operation not permitted
```

### 4. Separate log monitoring user with read-only access
The monitoring pipeline's tailer process should run as a **dedicated low-privilege user** with read-only access to the logs directory. When that user receives `Permission denied`, the alert fires. This is exactly what happened here — the monitoring pipeline was the only thing that caught the fault### 5. Observability (P04 onward)
- **Prometheus + Grafana** — monitors the WAS JVM's own metrics including log write errors and file descriptor counts.
- **OpenSearch** — ingests log lines; a sudden drop in ingestion rate from a healthy JVM triggers a **"log pipeline broken"** alert automatically, regardless of whether the external tailer is running.

### 6. Audit trail for chmod operations on log directories
The Linux audit daemon (`auditd`) can log every `chmod` or `chown` call on the WAS logs directory:

```bash
auditctl -w /apps/IBM/WebSphere/AppServer/profiles/ \
  -p wa -k was_log_changes
```

This would have immediately identified **which user ran the `chmod 000` and when** — closing the *"a junior admin did a routine log rotation check"* ambiguity in the incident ticket.
