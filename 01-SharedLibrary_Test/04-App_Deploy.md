
# Deploy the Application {Admin console}
1. Log into Admin Console: https://<vm-ip>:9043/ibm/console
2. Go to: Applications → New Application → New Enterprise Application
3. Choose Remote file system (since the EAR is already on the VM, not your browser's machine) → Browse → navigate to /apps/staging/ears/digistack-bank-v1.ear → Next
4. Choose Fast Path (the simplified wizard, appropriate for a first deploy) → Next
5. On the Select Installation Options screen:
		Leave defaults, confirm Application name shows digistack-bank-v1 (or similar, auto-derived from the EAR)
		Click Next
6. On Map Modules to Servers:
		Select the digistack-bank-web module checkbox
		Confirm the target server shown is server1 on node devdsbinnode01
		Click Next
7. On Map Virtual Host for Web Modules:
		Select the digistack-bank-web module checkbox
		Set Virtual host to default_host (WAS's built-in virtual host — sufficient for this lab; a dedicated virtual host isn't required until multi-app routing needs it)
		Click Next
8. Continue through remaining screens (context root should already show /digistack-bank from our EAR's pom.xml) → click Next through to Summary
9. Click Finish
10. Wait for installation to complete (progress bar/log output appears) — then click the Save link to commit to the master configuration.


# Deploy the Application {wasadmin Script}
Create the wasadmin Script
```
v1_deploy.py
```
```
# =============================================================
# Script  : v1_deploy.py
# Version : P01 v1
# Purpose : Deploy digistack-bank-v1.ear to WAS server1.
#           Idempotent — installs fresh OR updates if already
#           installed. Safe to run multiple times.
#
# Run from dsb-dmgr VM:
#   cd /apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/bin/
#   ./wsadmin.sh -lang jython -username wasadmin -password <pwd> \
#       -f /tmp/v1_deploy.py
#
# Pre-requisite: digistack-bank-v1.ear must be copied to the
#   WAS VM at /tmp/digistack-bank-v1.ear before running this script.
# =============================================================

import sys

print("=== DigiStack Bank v1 — Deployment Script ===")

# ── Configuration ──
# These values match the project naming standard (STD v1.10)
APP_NAME    = 'digistack-bank-v1'          # Name shown in Admin Console
EAR_PATH    = '/tmp/digistack-bank-v1.ear' # Path on the WAS VM
CONTEXT_ROOT = '/digistack-bank'           # URL prefix in the browser
SERVER_NAME  = 'server1'                   # Target server
VHOST_NAME   = 'default_host'             # Virtual host (port 9080)
NODE_NAME    = 'devdsbinnode01'           # Node name per STD naming
CELL_NAME    = 'devdsbincell01'           # Cell name per STD naming

# ── Step 1: Check if the application is already installed ──
# AdminApp.list() returns all installed application names as a string.
# We check whether our app name appears in that list.
installedApps = AdminApp.list()
alreadyInstalled = APP_NAME in installedApps

if alreadyInstalled:
    print("Application '" + APP_NAME + "' is already installed.")
    print("Performing UPDATE deployment...")
else:
    print("Application '" + APP_NAME + "' is not installed.")
    print("Performing FRESH INSTALL...")

# ── Step 2: Build the deployment options string ──
# These options are the scripting equivalent of every wizard page
# in the Admin Console deployment flow.
#
# -appname        : the display name in Admin Console
# -contextroot    : the URL prefix
# -MapModulesToServers : maps the WAR module to the target server
#   format: module_name+war_name+server_path
# -MapWebModToVH  : maps the WAR to a virtual host
#   format: module_name+war_name+virtual_host_name

serverTarget = (
    'WebSphere:cell=' + CELL_NAME +
    ',node=' + NODE_NAME +
    ',server=' + SERVER_NAME
)

deployOptions = (
    '-appname '      + APP_NAME    + ' '
    '-contextroot '  + CONTEXT_ROOT + ' '
    '-MapModulesToServers [[ digistack-bank-web '
        'digistack-bank-web-1.0.war,WEB-INF/web.xml '
        + serverTarget + ' ]] '
    '-MapWebModToVH [[ digistack-bank-web '
        'digistack-bank-web-1.0.war '
        + VHOST_NAME + ' ]]'
)

# ── Step 3: Install or Update ──
try:
    if alreadyInstalled:
        # Update replaces the running application with the new EAR.
        # The application remains mapped to the same server and virtual host.
        AdminApp.update(APP_NAME, 'app', '-operation update -contents ' + EAR_PATH)
        print("AdminApp.update() completed successfully.")
    else:
        # Fresh install using the options string built above.
        AdminApp.install(EAR_PATH, deployOptions)
        print("AdminApp.install() completed successfully.")

except Exception as e:
    print("ERROR during deployment: " + str(e))
    sys.exit(1)

# ── Step 4: Save the configuration ──
# Must be called after every AdminApp.install() or AdminApp.update()
# otherwise the change is lost on the next restart.
AdminConfig.save()
print("Configuration saved.")

# ── Step 5: Start the application ──
# After install/update, the application is not automatically running.
# We use AdminApp to start it explicitly.
try:
    appManager = AdminControl.queryNames(
        'cell=' + CELL_NAME +
        ',node=' + NODE_NAME +
        ',type=ApplicationManager,*'
    )
    AdminControl.invoke(appManager, 'startApplication', APP_NAME)
    print("Application '" + APP_NAME + "' started successfully.")

except Exception as e:
    print("Note: Could not start application via AdminControl: " + str(e))
    print("      Start it manually via Admin Console if needed.")

# ── Step 6: Verify the application is running ──
try:
    appState = AdminControl.getAttribute(
        AdminControl.queryNames(
            'type=Application,name=' + APP_NAME + ',*'
        ),
        'deploymentState'
    )
    print("Application state: " + str(appState))
except Exception as e:
    print("Could not read application state: " + str(e))
    print("Check Admin Console for status.")

print("=== Deployment script complete. ===")
print("Verify in browser: http://192.168.10.10:9080/digistack-bank/Home")
```

## Run the Script
```
cd /apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/bin/

./wsadmin.sh -lang jython \
    -username wasadmin \
    -password <YourPassword> \
    -f /tmp/v1_deploy.py
```

# Verification
1. Open a browser and go to:
```
http://dsb-dmgr.digistack.cloud:9080/digistack-bank/
```
Expected result: Page displays: ==> It Displays the HomePage
2. Open a browser and go to:
```
http://dsb-dmgr.digistack.cloud:9080/digistack-bank/Home
```
Expected result: Page displays: ==> "DB Read Successful: DigiStack Bank - Connected {In green colour}"

2. Confirm the log entry
On dsb-dmgr, run:
Confirm no errors in the log after the update deployment:
```
grep -i "error\|exception\|SEVERE" \
    /apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/logs/server1/SystemOut.log \
    | tail -20
```
Confirm the servlet log line appears after the update:
```
grep "HomeServlet" \
    /apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/logs/server1/SystemOut.log \
    | tail -5
```
Expected result — you should see a line like:
```
[date time] ... HomeServlet: PostgreSQL JDBC driver loaded successfully.
[date time] ... HomeServlet: DB read successful. bank.name=DigiStack Bank
```
