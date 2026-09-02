# Deploying digistack-bank-v3.ear to WebSphere Application Server (Admin Console)
# Configure ClassLoader via Admin Console
Step 1.1 — Open the Admin Console:
```
http://192.168.10.10:9060/ibm/console
```

Log in as wasadmin.

Step 1.2 — Navigate to the application's ClassLoader settings:
```
Applications → Application Types → WebSphere enterprise applications
```
Click on digistack-bank-v2 {(still v2 at this point — v3 EAR is Not yet deployed}

Step 1.3 — On righ Hand Side, Click on 
```
Detail Properties section → Class loading and update detection
```

Step 1.4 — Configure these settings:
```
Class loader order ==>  select Classes loaded with parent class loader first (PARENT_FIRST)
WAR class loader policy ==>  select Single class loader for application
```
Click on OK and Save the configurations

#### Stop and restart server1 to apply the ClassLoader configuration:
```
/apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/bin/stopServer.sh server1
/apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/bin/startServer.sh server1
```
## Prerequisites
- Admin Console URL: `https://<host>:9043/ibm/console`
- EAR file already staged on the VM at `/opt/staging/ears/digistack-bank-v3.ear`

---

## Step-by-Step Deployment

### 1. Log into the Admin Console
- Open: `https://<host>:9043/ibm/console`
- Authenticate with your admin credentials.

### 2. Start the Install Wizard
- Navigate to: **Applications → New Application → New Enterprise Application**

### 3. Provide the EAR Path
- Choose **Remote file system** (since the EAR is already on the VM, not your browser's machine).
- Click **Browse** → navigate to:
  ```
  /opt/staging/ears/digistack-bank-v3.ear
  ```
- Click **Next**.

### 4. Choose Installation Path
- Select **Fast Path** (the simplified wizard — appropriate for a first deploy).
- Click **Next**.

### 5. Select Installation Options
- Leave defaults.
- Confirm **Application name** shows `digistack-bank-v3` (or similar, auto-derived from the EAR).
- Click **Next**.

### 6. Map Modules to Servers
- Select the checkbox for the `digistack-bank-web` module.
- Confirm the target server shown is:
  ```
  server1 on node devdsbinnode01
  ```
- Click **Next**.

### 7. Map Virtual Host for Web Modules
- Select the checkbox for the `digistack-bank-web` module.
- Set **Virtual host** to `default_host`
  > WAS's built-in virtual host — sufficient for this lab; a dedicated virtual host isn't required until multi-app routing needs it.
- Click **Next**.

### 8. Remaining Screens → Summary
- Continue through the remaining screens (context root should already show `/digistack-bank` from the EAR's `pom.xml`).
- Click **Next** through to the **Summary** page.

### 9. Finish Installation
- Click **Finish**.

### 10. Save the Configuration
- Wait for installation to complete (progress bar / log output appears).
- Click the **Save** link to commit to the master configuration.

---

## ✅ Deployment Complete
The application `digistack-bank-v3` is now installed and committed to the master configuration.
Verify it under **Applications → Application Types → WebSphere enterprise applications**.

# Step 4.3 — Confirm the server log shows the new servlets initialised

```
grep -E "DashboardServlet|AccountServlet" \
  /apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/logs/server1/SystemOut.log \
  | tail -5
```
Expected result — you should see init messages for all four servlets:
```
DashboardServlet: PostgreSQL JDBC driver loaded.
DashboardServlet: Account loaded for userId=1 ...
```
