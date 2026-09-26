# =============================================================
# Script  : v5_deploy.py
# Version : P01 v5
# Purpose : Uninstall digistack-bank-v4 and deploy
#           digistack-bank-v5.ear to devdsbinappcluster01.
#           Key difference from all prior versions: the
#           deployment target is the CLUSTER, not an individual
#           server. WAS distributes the EAR to all members.
#
# Run from DMgr profile bin on dsb-dmgr:
#   cd /apps/IBM/WebSphere/AppServer/profiles/devdsbindmgr01/bin/
#   ./wsadmin.sh -lang jython -username wasadmin -password <pwd> \
#       -f /tmp/v5_deploy.py
#
# Pre-requisite: digistack-bank-v5.ear at /tmp/ on dsb-dmgr.
# =============================================================

import sys

print("=== DigiStack Bank v5 — Cluster Deployment Script ===")

OLD_APP_NAME  = 'digistack-bank-v4'
NEW_APP_NAME  = 'digistack-bank-v5'
EAR_PATH      = '/tmp/digistack-bank-v5.ear'
CONTEXT_ROOT  = '/digistack-bank'
CLUSTER_NAME  = 'devdsbinappcluster01'
CELL_NAME     = 'devdsbincell01'
VHOST_NAME    = 'default_host'

# ── Step 1: Stop and uninstall old application ──
installedApps = AdminApp.list()

if OLD_APP_NAME in installedApps:
    print("Found '" + OLD_APP_NAME + "' — stopping...")
    try:
        # In a clustered environment, use the cluster MBean
        # to stop the application across all members at once.
        clusterMBean = AdminControl.queryNames(
            'cell=' + CELL_NAME +
            ',type=Cluster,name=' + CLUSTER_NAME + ',*'
        )
        # Stop via ApplicationManager on each node
        appManagers = AdminControl.queryNames(
            'cell=' + CELL_NAME +
            ',type=ApplicationManager,*'
        ).splitlines()

        for am in appManagers:
            try:
                AdminControl.invoke(
                    am, 'stopApplication', OLD_APP_NAME)
                print("  Stopped on: " + am)
            except Exception as e:
                print("  Note (stop): " + str(e))
    except Exception as e:
        print("Note: " + str(e))

    print("Uninstalling '" + OLD_APP_NAME + "'...")
    AdminApp.uninstall(OLD_APP_NAME)
    AdminConfig.save()
    print("'" + OLD_APP_NAME + "' uninstalled.")
else:
    print("'" + OLD_APP_NAME +
          "' not found — skipping uninstall.")

# ── Step 2: Build the cluster deployment target string ──
# This is the critical difference from server-level deployment.
# The target uses 'cluster=' instead of 'node= + server='.
# WAS automatically maps the EAR to ALL cluster members.
clusterTarget = (
    'WebSphere:cell=' + CELL_NAME +
    ',cluster=' + CLUSTER_NAME
)

print("Deployment target: " + clusterTarget)

deployOptions = (
    '-appname '     + NEW_APP_NAME + ' '
    '-contextroot ' + CONTEXT_ROOT + ' '
    '-MapModulesToServers [[ digistack-bank-web '
        'digistack-bank-web-1.0.war,WEB-INF/web.xml '
        + clusterTarget + ' ]] '
    '-MapWebModToVH [[ digistack-bank-web '
        'digistack-bank-web-1.0.war '
        + VHOST_NAME + ' ]]'
)

# ── Step 3: Install to the cluster ──
print("Installing '" + NEW_APP_NAME +
      "' to cluster '" + CLUSTER_NAME + "'...")
try:
    AdminApp.install(EAR_PATH, deployOptions)
    print("Install completed.")
except Exception as e:
    print("ERROR: " + str(e))
    sys.exit(1)

AdminConfig.save()
print("Configuration saved.")

# ── Step 4: Start the application on all cluster members ──
print("Starting '" + NEW_APP_NAME +
      "' on all cluster members...")

appManagers = AdminControl.queryNames(
    'cell=' + CELL_NAME +
    ',type=ApplicationManager,*'
).splitlines()

for am in appManagers:
    try:
        AdminControl.invoke(am, 'startApplication', NEW_APP_NAME)
        print("  Started on: " + am)
    except Exception as e:
        print("  Note (start): " + str(e))

# ── Step 5: Verify application state on each member ──
print("\n--- Application State Per Member ---")
appMBeans = AdminControl.queryNames(
    'type=Application,name=' + NEW_APP_NAME + ',*'
).splitlines()

if appMBeans:
    for appMBean in appMBeans:
        try:
            state = AdminControl.getAttribute(
                appMBean, 'deploymentState')
            # Extract node name from the MBean object name
            nodeName = appMBean.split('node=')[1].split(',')[0] \
                if 'node=' in appMBean else 'unknown'
            print("  " + nodeName + ": " + str(state))
        except Exception as e:
            print("  " + str(e))
else:
    print("  No application MBeans found — may still be starting.")
    print("  Check Admin Console for status.")

# ── Step 6: Confirm old app is gone ──
appsAfter = AdminApp.list()
if OLD_APP_NAME in appsAfter:
    print("\nWARNING: '" + OLD_APP_NAME +
          "' still listed — check Admin Console.")
else:
    print("\nConfirmed: '" + OLD_APP_NAME +
          "' is no longer installed.")

print("\n=== Cluster deployment complete. ===")
print("Key point: deployed to CLUSTER, not individual servers.")
print("Both cluster members automatically received the EAR.")
print("Verify: http://192.168.10.10:<node1-port>/digistack-bank/Home")
print("        http://192.168.10.11:<node2-port>/digistack-bank/Home")