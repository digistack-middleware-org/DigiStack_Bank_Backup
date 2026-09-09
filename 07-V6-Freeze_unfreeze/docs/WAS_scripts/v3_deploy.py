# =============================================================
# Script  : v3_deploy.py
# Version : P01 v3
# Purpose : Uninstall digistack-bank-v2 and deploy
#           digistack-bank-v3.ear to WAS server1.
#
# Run from dsb-dmgr VM:
#   cd /apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/bin/
#   ./wsadmin.sh -lang jython -username wasadmin -password <pwd> \
#       -f /tmp/v3_deploy.py
#
# Pre-requisite: digistack-bank-v3.ear must be at /tmp/ on the WAS VM.
# =============================================================

import sys

print("=== DigiStack Bank v3 — Deployment Script ===")

OLD_APP_NAME  = 'digistack-bank-v2'
NEW_APP_NAME  = 'digistack-bank-v3'
EAR_PATH      = '/tmp/digistack-bank-v3.ear'
CONTEXT_ROOT  = '/digistack-bank'
SERVER_NAME   = 'server1'
VHOST_NAME    = 'default_host'
NODE_NAME     = 'devdsbinnode01'
CELL_NAME     = 'devdsbincell01'

# ── Step 1: Stop and uninstall old application ──
installedApps = AdminApp.list()

if OLD_APP_NAME in installedApps:
    print("Found '" + OLD_APP_NAME + "' — stopping...")
    try:
        appManager = AdminControl.queryNames(
            'cell=' + CELL_NAME +
            ',node=' + NODE_NAME +
            ',type=ApplicationManager,*'
        )
        AdminControl.invoke(
            appManager, 'stopApplication', OLD_APP_NAME)
        print("Stopped '" + OLD_APP_NAME + "'.")
    except Exception as e:
        print("Note: stop skipped — " + str(e))

    print("Uninstalling '" + OLD_APP_NAME + "'...")
    AdminApp.uninstall(OLD_APP_NAME)
    AdminConfig.save()
    print("'" + OLD_APP_NAME + "' uninstalled.")
else:
    print("'" + OLD_APP_NAME +
          "' not found — skipping uninstall.")

# ── Step 2: Install new application ──
print("Installing '" + NEW_APP_NAME + "'...")

serverTarget = (
    'WebSphere:cell=' + CELL_NAME +
    ',node=' + NODE_NAME +
    ',server=' + SERVER_NAME
)

deployOptions = (
    '-appname '     + NEW_APP_NAME + ' '
    '-contextroot ' + CONTEXT_ROOT + ' '
    '-MapModulesToServers [[ digistack-bank-web '
        'digistack-bank-web-1.0.war,WEB-INF/web.xml '
        + serverTarget + ' ]] '
    '-MapWebModToVH [[ digistack-bank-web '
        'digistack-bank-web-1.0.war '
        + VHOST_NAME + ' ]]'
)

try:
    AdminApp.install(EAR_PATH, deployOptions)
    print("Install completed.")
except Exception as e:
    print("ERROR: " + str(e))
    sys.exit(1)

AdminConfig.save()
print("Configuration saved.")

# ── Step 3: Start new application ──
print("Starting '" + NEW_APP_NAME + "'...")
try:
    appManager = AdminControl.queryNames(
        'cell=' + CELL_NAME +
        ',node=' + NODE_NAME +
        ',type=ApplicationManager,*'
    )
    AdminControl.invoke(
        appManager, 'startApplication', NEW_APP_NAME)
    print("'" + NEW_APP_NAME + "' started successfully.")
except Exception as e:
    print("Note: start via AdminControl failed — " + str(e))
    print("      Start manually via Admin Console if needed.")

# ── Step 4: Verify ──
try:
    appState = AdminControl.getAttribute(
        AdminControl.queryNames(
            'type=Application,name=' + NEW_APP_NAME + ',*'
        ),
        'deploymentState'
    )
    print("Application state: " + str(appState))
except Exception as e:
    print("Could not read state: " + str(e))

# ── Step 5: Confirm old app is gone ──
appsAfter = AdminApp.list()
if OLD_APP_NAME in appsAfter:
    print("WARNING: '" + OLD_APP_NAME +
          "' still appears — check Admin Console.")
else:
    print("Confirmed: '" + OLD_APP_NAME +
          "' is no longer installed.")

print("=== Deployment complete. ===")
print("Verify: http://192.168.10.10:9080/digistack-bank/Home")