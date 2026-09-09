# Create DMgr Profile

---

## Step 3 — Create the DMgr Profile (the Boss) on dsb-dmgr

### Step 3.1 — Run the Profile Creation Command

```bash
/apps/IBM/WebSphere/AppServer/bin/manageprofiles.sh -create \
  -profileName devdsbindmgr01 \
  -profilePath /apps/IBM/WebSphere/AppServer/profiles/devdsbindmgr01 \
  -templatePath /apps/IBM/WebSphere/AppServer/profileTemplates/dmgr \
  -cellName devdsbincell01 \
  -nodeName devdsbindmgrnode01 \
  -adminUserName wasadmin \
  -adminPassword <YourPassword> \
  -enableAdminSecurity true
```

**Simple meaning of each part:**

| Part | Meaning |
|---|---|
| `-templatePath .../dmgr` | "Use the **boss template**, not the worker template" |
| `-cellName devdsbincell01` | Name of the whole "company" (cell) |
| `-nodeName ...node01` | Name of this boss node |
| `-adminUserName / -adminPassword` | Boss login ID: `wasadmin` |
| `-enableAdminSecurity true` | Require a login to enter (don't leave the door open) |

✅ **Expected last line:**

```
INSTCONFSUCCESS: Success: Profile devdsbindmgr01 now exists.
```

### Step 3.2 — Start the Boss

```bash
cd /apps/IBM/WebSphere/AppServer/profiles/devdsbindmgr01/bin/
./startManager.sh
```

✅ **Expected:**

```
ADMU3000I: Server dmgr open for e-business
```

> 💡 This line = **"the boss is awake and ready for work."**

### Step 3.3 — Check the Boss's Office (Admin Console) in a Browser

First find which port the DMgr console opened:

```bash
netstat -tlnp | grep java
```

⚠️ **Important:** Normally the console uses port **9060**, but dsb-dmgr **already has an AppServer using 9060**. So the DMgr politely takes the next port: **9061**.

Then open in your browser:

```
http://192.168.10.10:<dmgr-console-port>/ibm/console
```

Log in as `wasadmin`.

✅ **Expected:** Welcome page showing cell name **devdsbincell01**.

---

