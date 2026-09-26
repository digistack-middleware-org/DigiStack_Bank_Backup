# =============================================================
# Script  : v4_rollback.py
# Version : P01 v4
# Purpose : Roll back digistack-bank-v4 to the v3 code via the
#           Update path — same application name, older EAR
#           content. This is a code-only rollback; it does NOT
#           touch the database. Use this pattern when new code
#           has a problem but the database schema/data is fine.
#
# Run from dsb-dmgr VM:
#   cd /opt/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/bin/
#   ./wsadmin.sh -lang jython -username wasadmin -password <pwd> \
#       -f /tmp/v4_rollback.py
#
# Pre-requisite: digistack-bank-v3-rollback-copy.ear at /tmp/
#   on the WAS VM.
#
# To reverse this rollback (redeploy forward to v4 again):
#   Run v4_deploy.py again, OR use v4_update.py pattern with
#   digistack-bank-v4.ear as the -contents path.
# =============================================================

import sys

print("=== DigiStack Bank v4 — ROLLBACK Script ===")
print("Rolling back to v3 code under the v4 application name.")

APP_NAME = 'digistack-bank-v4'
ROLLBACK_EAR_PATH = '/tmp/digistack-bank-v3-rollback-copy.ear'

# ── Step 1: Confirm the application is installed ──
installedApps = AdminApp.list()
if APP_NAME not in installedApps:
    print("ERROR: '" + APP_NAME + "' is not installed.")
    print("       Cannot roll back an application that " +
          "does not exist.")
    sys.exit(1)

print("Confirmed: '" + APP_NAME + "' is installed.")

# ── Step 2: Read pre-rollback state ──
try:
    preState = AdminControl.getAttribute(
        AdminControl.queryNames(
            'type=Application,name=' + APP_NAME + ',*'
        ),
        'deploymentState'
    )
    print("Pre-rollback state: " + str(preState))
except Exception as e:
    print("Note: Could not read pre-rollback state — " + str(e))

# ── Step 3: Perform the rollback via Update ──
print("Replacing '" + APP_NAME + "' with rollback EAR: " +
      ROLLBACK_EAR_PATH)

try:
    AdminApp.update(
        APP_NAME,
        'app',
        '-operation update -contents ' + ROLLBACK_EAR_PATH
    )
    print("Rollback update completed successfully.")
except Exception as e:
    print("ERROR during rollback: " + str(e))
    sys.exit(1)

# ── Step 4: Save configuration ──
AdminConfig.save()
print("Configuration saved.")

# ── Step 5: Stop and start to flush old classes ──
CELL_NAME = 'devdsbincell01'
NODE_NAME = 'devdsbinnode01'

print("Restarting application to load rolled-back code...")
try:
    appManager = AdminControl.queryNames(
        'cell=' + CELL_NAME +
        ',node=' + NODE_NAME +
        ',type=ApplicationManager,*'
    )
    AdminControl.invoke(appManager, 'stopApplication', APP_NAME)
    print("Stopped.")
    AdminControl.invoke(appManager, 'startApplication', APP_NAME)
    print("Started with rollback code.")
except Exception as e:
    print("Note: restart cycle issue — " + str(e))

# ── Step 6: Verify post-rollback state ──
try:
    postState = AdminControl.getAttribute(
        AdminControl.queryNames(
            'type=Application,name=' + APP_NAME + ',*'
        ),
        'deploymentState'
    )
    print("Post-rollback state: " + str(postState))
except Exception as e:
    print("Could not read post-rollback state: " + str(e))

print("")
print("IMPORTANT: This rollback did NOT touch the database.")
print("           Verify account balances are unchanged by")
print("           comparing against the pre-rollback value.")
print("")
print("=== Rollback complete. ===")
print("Verify in browser: footer should show 'v1 — Foundation'")
print("                    (the pre-v4 label), NOT 'v4 —")
print("                    Application Lifecycle'.")