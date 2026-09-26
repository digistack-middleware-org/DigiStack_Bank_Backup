# =============================================================
# Script  : v2_deploy.py
# Version : P01 v2
# Purpose : Uninstall digistack-bank-v1 and deploy
#           digistack-bank-v2.ear to WAS server1.
#           Follows the version-change deployment pattern —
#           uninstall old name, install new name fresh.
#           Used when the application name changes between
#           versions (v1 → v2, v2 → v3, etc.).
#
# Run from dsb-dmgr VM:
#   cd /opt/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/bin/
#   ./wsadmin.sh -lang jython -username wasadmin -password <pwd> \
#       -f /tmp/v2_deploy.py
#
# Pre-requisite: digistack-bank-v2.ear must be at /tmp/ on the WAS VM.
# =============================================================

import sys

print("=== DigiStack Bank v2 — Deployment Script ===")

# ── Configuration ──
OLD_APP_NAME  = 'digistack-bank-v1'
NEW_APP_NAME  = 'digistack-bank-v2'
EAR_PATH      = '/tmp/digistack-bank-v2.ear'
CONTEXT_ROOT  = '/digistack-bank'
SERVER_NAME   = 'server1'
VHOST_NAME    = 'default_host'
NODE_NAME     = 'devdsbinnode01'
CELL_NAME     = 'devdsbincell01'

# ── Step 1: Stop and uninstall the old application ──
installedApps = AdminApp.list()

if OLD_APP_NAME in installedApps:
    print("Found '" + OLD_APP_NAME + "' — stopping it...")

    # Stop the old application before uninstalling.
    # Uninstalling a running application can leave WAS in an
    # inconsistent state on some versions — always stop first.
    try:
        appManager = AdminControl.queryNames(
            'cell=' + CELL_NAME +
            ',node=' + NODE_NAME +
            ',type=ApplicationManager,*'
        )
        AdminControl.invoke(appManager, 'stopApplication', OLD_APP_NAME)
        print("Stopped '" + OLD_APP_NAME + "' successfully.")
    except Exception as e:
        print("Note: Could not stop '" + OLD_APP_NAME +
              "' (may already be stopped): " + str(e))

    # Uninstall the old application
    print("Uninstalling '" + OLD_APP_NAME + "'...")
    AdminApp.uninstall(OLD_APP_NAME)
    AdminConfig.save()
    print("'" + OLD_APP_NAME + "' uninstalled and config saved.")

else:
    print("'" + OLD_APP_NAME + "' not found — skipping uninstall.")
    print("(This is normal if v1 was already manually removed.)")

# ── Step 2: Install the new application ──
print("Installing '" + NEW_APP_NAME + "' from " + EAR_PATH + "...")

serverTarget = (
    'WebSphere:cell=' + CELL_NAME +
    ',node=' + NODE_NAME +
    ',server=' + SERVER_NAME
)

deployOptions = (
    '-appname '      + NEW_APP_NAME  + ' '
    '-contextroot '  + CONTEXT_ROOT  + ' '
    '-MapModulesToServers [[ digistack-bank-web '
        'digistack-bank-web-1.0.war,WEB-INF/web.xml '
        + serverTarget + ' ]] '
    '-MapWebModToVH [[ digistack-bank-web '
        'digistack-bank-web-1.0.war '
        + VHOST_NAME + ' ]]'
)

try:
    AdminApp.install(EAR_PATH, deployOptions)
    print("AdminApp.install() completed successfully.")
except Exception as e:
    print("ERROR during install: " + str(e))
    sys.exit(1)

# ── Step 3: Save configuration ──
AdminConfig.save()
print("Configuration saved.")

# ── Step 4: Start the new application ──
print("Starting '" + NEW_APP_NAME + "'...")
try:
    appManager = AdminControl.queryNames(
        'cell=' + CELL_NAME +
        ',node=' + NODE_NAME +
        ',type=ApplicationManager,*'
    )
    AdminControl.invoke(appManager, 'startApplication', NEW_APP_NAME)
    print("Application '" + NEW_APP_NAME + "' started successfully.")
except Exception as e:
    print("Note: Could not start via AdminControl: " + str(e))
    print("      Start manually via Admin Console if needed.")

# ── Step 5: Verify the new application is running ──
try:
    appState = AdminControl.getAttribute(
        AdminControl.queryNames(
            'type=Application,name=' + NEW_APP_NAME + ',*'
        ),
        'deploymentState'
    )
    print("Application state: " + str(appState))
except Exception as e:
    print("Could not read application state: " + str(e))
    print("Check Admin Console for status.")

# ── Step 6: Confirm old application is gone ──
installedAppsAfter = AdminApp.list()
if OLD_APP_NAME in installedAppsAfter:
    print("WARNING: '" + OLD_APP_NAME +
          "' still appears in the application list.")
    print("         Check Admin Console manually.")
else:
    print("Confirmed: '" + OLD_APP_NAME +
          "' is no longer installed.")

print("=== Deployment complete. ===")
print("Context root unchanged: http://192.168.10.10:9080/digistack-bank/")
print("Verify: http://192.168.10.10:9080/digistack-bank/Home")
