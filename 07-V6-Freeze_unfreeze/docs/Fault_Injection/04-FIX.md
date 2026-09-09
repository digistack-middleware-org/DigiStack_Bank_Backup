## 6. The Fix (One Command!)

On node02:

```bash
cd /apps/IBM/WebSphere/AppServer/profiles/<node02-profile>/bin
./startNode.sh
```

Success message:
```text
ADMU3000I: Server nodeagent open for e-business
```
---

## 7. Verify the Fix (4 Checks)

1. **Process check:** `ps -ef | grep nodeagent` — PID visible. ✅
2. **Console check:** Node icon grey → **green**. ✅
3. **Sync check:** Click Synchronize → "completed successfully."
   The stuck JVM argument finally reaches node02. ✅
4. **Cleanup:** Remove the test flag `-Dv6.fault.drill=true`
   from JVM arguments → Save → Sync → Restart the app server. ✅

---

## 8. Key Takeaways (Memory Hooks)

- **One stopped process = six symptoms.** Find the root cause, not the symptoms.
- **Node Agent = the bridge.** DMgr needs it for sync, commands, and status.
- **App server ≠ Node Agent.** App can run fine while admin is broken.
- **"Working app" ≠ "healthy node."** Always check node status too.
- **Fix:** `./startNode.sh`. Verify with process, console, and sync.

---

> **One-line summary:** *The phone line was dead; the shop was fine.
> Fix the line, and everything works again.*