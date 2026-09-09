# Deploy digistack-bank-v5.ear to the Cluster

---

## 📚 First, Understand the Key Idea

### 📦 Deploy to the Cluster = "Deploy Once, Run Everywhere"

**In v1–v4:** You deployed the EAR to **one single server**. If you had 2 servers,
you'd have to install it twice, manually. 😫

**In v5:** You deploy to the **cluster** — and WAS automatically:

- Copies the EAR to **every member** of the team.
- Future members that join the team **automatically get the app too**.

📦 **Analogy:** Instead of hand-delivering a policy document to each branch office,
you send it to **head office** (the cluster), and head office distributes copies to
all branches — including any new branch that opens later.

**The technical difference is one word in the target:**

```
Old (v4):  WebSphere:cell=...,node=...,server=...   ← point to one server
New (v5):  WebSphere:cell=...,cluster=...           ← point to the whole team
```

### 🏷️ No Real Code Changes

The app code is **identical to v4**. Only the footer says "v5" now.
The **real learning** is the clustering topic, not the app.

---

## Step 1 — Update the Footer Labels (v4 → v5)

Open 4 JSP files and change the visible version label:

| File | Find | Replace with |
|---|---|---|
| Home.jsp | `v4 — Application Lifecycle` | `v5 — WAS Clustering` |
| Dashboard.jsp | `... v4` | `... v5` |
| Account.jsp | `... v4` | `... v5` |
| Login.jsp | `... v4` | `... v5` |

> 💡 This is so you can visually confirm which version is running when you open
> the app in a browser.

---

## Step 2 — Rename the Output File

In `digistack-bank-ear\pom.xml`, change:

```xml
<finalName>digistack-bank-v4</finalName>  →  <finalName>digistack-bank-v5</finalName>
```

> 💡 This tells Maven: when you build, name the final file `digistack-bank-v5.ear`.

---

## Step 3 — Build the EAR

```bash
cd C:\Projects\digistack-bank-parent
mvn clean package
```

✅ **Expected:** `[INFO] BUILD SUCCESS`

Check the file exists:

```bash
dir digistack-bank-ear\target\digistack-bank-v5.ear
```

✅ **Expected:** File is listed.

---

## Step 4 — Create the Deployment Script (`v5_deploy.py`)

**How the script works, in plain words:**

1. **Clean up the old version:** Look for `digistack-bank-v4`. If found → stop it on
   every member, uninstall it, save. If not found → skip (the script is **safe to re-run**).
2. **Build the special cluster target string** — the star of this sprint:
   ```
   WebSphere:cell=devdsbincell01,cluster=devdsbinappcluster01
   ```
   Saying `cluster=` (instead of `node=` + `server=`) tells WAS:
   *"Give this app to the whole team."*
3. **Install the EAR** with that target → WAS maps it to all members automatically.
4. **Save** the master config.
5. **Start the app on all members** — it asks each node's "Application Manager"
   (the server's app launcher) to start the app.
6. **Verify:** Ask each member's live state (MBean) and print it.
7. **Double-check** the old v4 app is really gone.

---

## Step 5 — Copy Files and Deploy (Two Ways, Like Before)

### Step 5.1 — Copy Files to the Boss Machine

```bash
scp ...\digistack-bank-v5.ear root@192.168.10.10:/tmp/
scp ...\v5_deploy.py root@192.168.10.10:/tmp/
```

### Step 5.2 — GUI Method First (Learning Method)

1. DMgr console → **Applications → WebSphere enterprise applications** → **Install**.
2. Browse and pick the v5 EAR → Next.
3. ⭐ **The key page: "Map modules to servers"** — here you pick the target.
   Choose **devdsbinappcluster01** from the list (not an individual server!),
   tick the WAR module, click **Apply**.
4. Confirm the Server column shows **devdsbinappcluster01** → Next → keep
   `default_host` → Finish → **Save**.
5. Tick `digistack-bank-v5` → **Start** → ✅ green arrow ▶.

### Step 5.3 — Prove It's Cluster-Mapped

Click the app → **Manage Modules**.

✅ **Expected:**

```
digistack-bank-web   WebSphere:cell=...,cluster=devdsbinappcluster01
```

> The word **cluster=** confirms it went to the team, not one server.

### Step 5.4 — Remove the GUI Version

Tick the app → **Stop → Uninstall → Save**.

> 💡 Why install then uninstall? So the script can demonstrate the **full, automated**
> uninstall + install path — same teaching pattern as previous sprints.

### Step 5.5 — Run the Script (Automation Method)

```bash
cd /apps/IBM/WebSphere/AppServer/profiles/devdsbindmgr01/bin/
./wsadmin.sh -lang jython -username wasadmin -password <pwd> -f /tmp/v5_deploy.py
```

✅ **Expected ending:**

```
Installing 'digistack-bank-v5' to cluster 'devdsbinappcluster01'...
--- Application State Per Member ---
  devdsbinnode01: STARTED
  devdsbinnode02: STARTED
=== Cluster deployment complete. ===
```

---

## Step 6 — Test Both Members Directly 🌐

Hit each server's own "door" (the ports you recorded in Sprint 3):

- **Node 1:** `http://192.168.10.10:9080/digistack-bank/Home`
- **Node 2:** `http://192.168.10.11:<node2-port>/digistack-bank/Home`

✅ **Expected:** Both load the **same Home page** with footer **"v5 — WAS Clustering"**.

📌 **Why this matters:** Two different machines, one deployment action —
proving **"deploy once, run everywhere"** works.

---

# Step 7 — Re-apply ClassLoader Settings

## GUI Method - for Re-apply ClassLoader
1. In the left navigation panel, go to:

   > **Applications → Application Types → WebSphere enterprise applications**

2. Click **digistack-bank-v5** in the applications list.

3. Under **Detail Properties**, click **Class loading and update detection.

4. On the configuration page, set the following:

   | Setting | Value |
   |---|---|
   | Class loader order | `Classes loaded with parent class loader first` *(this is PARENT_FIRST)* |
   | Class loader delegation | `Application` *(leave as default)* |
   | WAR class loader policy | `Single class loader for application` *(this is SINGLE)* |

   > **Note:** WAR class loader policy may only be editable if the app contains multiple WAR modules. If it's greyed out, `SINGLE` is already the effective default.

5. Click **OK** or **Apply**.

6. You should see a message:

   ```text
   The Class loading and update detection changes have been applied...
   ```

---

## 7.2 Save the Configuration

1. Click **Save** in the **Messages** box at the top of the (or click **Save** in the console taskbar).
2. On the next screen, confirm by clicking **Save** again.
3. This is the GUI equivalent of `Configuration saved.` from the script.

---

## 7.3 Synchronize the Configuration to All Nodes

Since the app is deployed on a **cluster**, the config must be pushed to all node agents.

### Option A — GUI sync (per node)

1. Go to:

   > **System administration → Nodes**

2. Select the checkboxes for all nodes hosting the cluster members.
3. Click **Full Resynchronize**.
4. Wait until the sync status shows as complete for each node.

### Option B — GUI sync (via node agents)

1. Go to:

   > **System administration → Node agents**

2. Verify all node agents show a green **started** status.

---

## 7.4 Restart the Application / Cluster Members

> ⚠️ The classloader changes **only take effect after restart**.

1. Go to:

   > **Servers → Clusters → WebSphere application server clusters**

2. Check the checkbox next to your cluster (e.g., **digistack-bank-cluster**).
3. Click **Stop**, and wait until cluster status shows **Stopped** (⏹ icon).
4. Then click **Start**, and wait until status shows **Started** (▶ green arrow).

---

## 7.5 Verify

1. Go back to:

   > **Applications → Application Types → WebSphere enterprise applications → digistack-bank-v5 → Class loading and update detection**

2. Confirm:

   - [x] Class loader order = **Classes loaded with parent class loader first** ✅
   - [x] WAR class loader policy = **Single class loader for application** ✅

3. Optionally test the app URL through the web server / cluster to confirm it's serving correctly.

## Wasadmin Method - for Re-apply ClassLoader

⚠️ **Why?** A fresh install **resets** ClassLoader settings back to defaults.
Every time we redeploy, we must re-apply the project's settings
(**PARENT_FIRST / SINGLE**).

**Easy way:** Copy `v4_set_classloader.py` → save as `v5_set_classloader.py` →
change just two lines:

```python
APP_NAME = 'digistack-bank-v5'
print("=== DigiStack Bank v5 — ClassLoader Configuration Script ===")
```

**Run it:**

```bash
scp ... root@192.168.10.10:/tmp/
./wsadmin.sh -lang jython -username wasadmin -password <pwd> -f /tmp/v5_set_classloader.py
```

✅ **Expected:** `PARENT_FIRST`, `SINGLE`, saved, complete.

---
