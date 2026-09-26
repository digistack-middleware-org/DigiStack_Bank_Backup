# =============================================================
# Script  : v4_update.py
# Version : P01 v4
# Purpose : Demonstrate the Update Application path in wsadmin.
#           Updates the EAR registered as digistack-bank-v3
#           with the v4 code, keeping the application name
#           unchanged. This is the standard Update path used
#           for routine code deployments where the app name
#           does not change.
#
# Run from dsb-dmgr VM:
#   cd /opt/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/bin/
#   ./wsadmin.sh -lang jython -username wasadmin -password <pwd> \
#       -f /tmp/v4_update.py
#
# Pre-requisite: digistack-bank-v4.ear at /tmp/ on WAS VM.
# Note: This script is a demonstration of the Update path.
#       Sprint 3 uses v4_deploy.py (Uninstall+Install) to
#       register the application under the digistack-bank-v4
#       name — that is a separate operation from this one.
# =============================================================

import sys

print("=== DigiStack Bank v4 — Update Application Script ===")
print("Demonstrating the Update path (same app name, new code)")

# ── Configuration ──
APP_NAME = 'digistack-bank-v3'    # Registered name — unchanged by Update
EAR_PATH = '/tmp/digistack-bank-v4.ear'  # New EAR to deploy
CELL_NAME = 'devdsbincell01'
NODE_NAME = 'devdsbinnode01'

# ── Step 1: Confirm the application is currently installed ──
installedApps = AdminApp.list()
if APP_NAME not in installedApps:
    print("ERROR: '" + APP_NAME + "' is not installed.")
    print("       Cannot update an application that is not installed.")
    sys.exit(1)

print("Confirmed: '" + APP_NAME + "' is installed.")

# ── Step 2: Read the current application state before update ──
print("Reading pre-update application state...")
try:
    preState = AdminControl.getAttribute(
        AdminControl.queryNames(
            'type=Application,name=' + APP_NAME + ',*'
        ),
        'deploymentState'
    )
    print("Pre-update state: " + str(preState))
except Exception as e:
    print("Note: Could not read pre-update state — " + str(e))

# ── Step 3: Perform the Update ──
# AdminApp.update() takes three arguments:
#   1. The registered application name (stays the same)
#   2. The update type: 'app' = replace entire application
#   3. Options string: -operation update -contents <path to new EAR>
print("Updating '" + APP_NAME + "' with new EAR...")
print("EAR path: " + EAR_PATH)

try:
    AdminApp.update(
        APP_NAME,
        'app',
        '-operation update -contents ' + EAR_PATH
    )
    print("AdminApp.update() completed successfully.")
except Exception as e:
    print("ERROR during update: " + str(e))
    sys.exit(1)

# ── Step 4: Save the configuration ──
AdminConfig.save()
print("Configuration saved.")

# ── Step 5: Stop the application ──
# After an update, the application must be stopped and started
# to ensure the new code is fully loaded. WAS does not always
# hot-reload all class changes without a restart.
print("Stopping '" + APP_NAME + "' to flush old classes...")
try:
    appManager = AdminControl.queryNames(
        'cell=' + CELL_NAME +
        ',node=' + NODE_NAME +
        ',type=ApplicationManager,*'
    )
    AdminControl.invoke(
        appManager, 'stopApplication', APP_NAME)
    print("Application stopped.")
except Exception as e:
    print("Note: Stop skipped — " + str(e))

# ── Step 6: Start the application with new code ──
print("Starting '" + APP_NAME + "' with updated code...")
try:
    AdminControl.invoke(
        appManager, 'startApplication', APP_NAME)
    print("Application started successfully.")
except Exception as e:
    print("Note: Start failed — " + str(e))
    print("      Start manually via Admin Console.")

# ── Step 7: Verify post-update state ──
try:
    postState = AdminControl.getAttribute(
        AdminControl.queryNames(
            'type=Application,name=' + APP_NAME + ',*'
        ),
        'deploymentState'
    )
    print("Post-update state: " + str(postState))
except Exception as e:
    print("Could not read post-update state: " + str(e))

# ── Step 8: Confirm application name is unchanged ──
appsAfter = AdminApp.list()
if APP_NAME in appsAfter:
    print("Confirmed: Application name '" + APP_NAME +
          "' is unchanged — Update path preserves registration.")
else:
    print("WARNING: '" + APP_NAME +
          "' not found after update — check Admin Console.")

print("")
print("Key learning: AdminApp.update() replaces the EAR code")
print("              without changing the application name.")
print("              Compare with AdminApp.uninstall() +")
print("              AdminApp.install() — used in Sprint 3")
print("              when the name changes to digistack-bank-v4.")
print("")
print("=== Update demonstration complete. ===")
print("Verify in browser: footer shows v4 label.")