# Deploy the Application

## 6. Step 4 — Deploy (GUI Method)

1. Open console: `https://192.168.10.10:9043/ibm/console`
2. Go to: **Applications → WebSphere enterprise applications**
3. Old version there? Click it → **Update**. New? Click **Install**.
4. Pick your EAR file.
5. **Most important screen: Map modules to servers**
   - Select the **cluster** (not individual servers).
   - This is how both members get the app in one action.
6. Accept default virtual host → Finish → **Save**.

**Real-life example:** Selecting the cluster = sending one to a group instead of texting each person separately.

---

## 7. Step 5 — Deploy (wsadmin Method, Same Thing)

```jython
AdminApp.install('/home/user/digistack-bank-v6.ear',
    '[-appname "DigiStack Bank v6" -cluster YOUR_CLUSTER_NAME '
    '-contextroot /digistack-bank -usedefaultbindings]')
AdminConfig.save()
```

- Don't know the cluster name? Run:

```jython
print AdminConfig.list('ServerCluster')
```

- GUI or wsadmin — **you only need one**, but learn both.

---

## 8. Step 6 — Full Resync (Push Files to Both Nodes)

- Console → **System administration → Nodes**
- Select all → **Full Resynchronize**

**Why "full" and not normal sync?**

- Normal sync = only small config changes.
- Full sync = pushes the actual new EAR file cleanly to both nodes.

**Real-life example:** Normal sync = forwarding a text. Full resync = handing each person a fresh printed copy.

---

## 9. Step 7 — Start the App

- Check the box next to **DigiStack Bank v6** → **Start**.
- Green arrow = running.

---

## 10. Step 8 — Prove "No Drift" (The Big Test)

**"Drift"** = one server has v6, the other still has v5. Bad.

Test each server directly:

- `http://192.168.10.10:80/digistack-bank/Home` (member 1)
- `http://192.168.10.11:9080/digistack-bank/Home` (member 2)

Check both show:

- ✅ Footer says **v6**
- ✅ **Freeze/Unfreeze** tiles visible

Then test through IHS (load balancer): `http://192.168.10.20/digistack-bank/Home`

**Real-life example:** Like checking both branches of a bank serve the same menu — one branch still selling last week's flyer = drift.

---

## 11. Step 9 — Confirm via wsadmin

```jython
appManager = AdminControl.queryNames('type=ApplicationManager,*')
print AdminControl.getAttribute(appManager, 'applicationsRunning')
```

- Output should list **DigiStack Bank v6**.

---

## 13. Acceptance Checklist

- ✅ EAR built (`BUILD SUCCESS`)
- ✅ Deployed to **cluster** (both members)
- ✅ Both show v6 footer + Freeze/Unfreeze
- ✅ Full Resync done → no drift
