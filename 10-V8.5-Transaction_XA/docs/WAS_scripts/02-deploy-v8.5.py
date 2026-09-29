# =============================================================================
# deploy-v8.5.py
#
# Purpose  : Undeploy digistack-bank-v8 and deploy digistack-bank-v8.5.ear
#            to the devdsbinappcluster01 cluster.
#
# When     : Run INSTEAD of the Admin Console steps in Sprint 2 Step 11
#            on a fresh environment, or for documentation/repeatability.
#            Do NOT run if the Admin Console install is already done.
#
# Run as   :
#   /apps/IBM/WebSphere/AppServer/bin/wsadmin.sh \
#     -lang jython \
#     -conntype SOAP \
#     -host 192.168.10.10 \
#     -port 8879 \
#     -user wasadmin \
#     -password <your-password> \
#     -f wsadmin-scripts/deploy-v8.5.py
# =============================================================================

import sys

EAR_PATH    = '/tmp/digistack-bank-v8.5.ear'
APP_NAME    = 'digistack-bank-v8.5'
OLD_APP     = 'digistack-bank-v8'
CLUSTER     = 'devdsbinappcluster01'
CELL        = 'devdsbincell01'

print('=== v8.5 Deploy — start ===')

# ── 1. Stop and remove old application if it exists ─────────────────────────
oldApps = AdminApp.list().splitlines()
for app in oldApps:
    if app.strip() == OLD_APP:
        print('Stopping old application: ' + OLD_APP)
        AdminControl.invoke(
            'WebSphere:type=Application,name=' + OLD_APP + ',*', 'stop')
        AdminApp.uninstall(OLD_APP)
        AdminConfig.save()
        print('Uninstalled: ' + OLD_APP)
        break

# ── 2. Install new EAR to cluster ───────────────────────────────────────────
installOptions = (
    '-appname ' + APP_NAME
    + ' -cluster ' + CLUSTER
    + ' -usedefaultbindings'
    + ' -contextroot /digistack-bank'
)
AdminApp.install(EAR_PATH, installOptions)
AdminConfig.save()
print('Installed: ' + APP_NAME)

# ── 3. Start new application ─────────────────────────────────────────────────
AdminControl.invoke(
    'WebSphere:type=Application,name=' + APP_NAME + ',*', 'start')
print('Started: ' + APP_NAME)

# ── 4. Sync nodes ─────────────────────────────────────────────────────────────
for nodeName in ['devdsbinnode01', 'devdsbinnode02']:
    try:
        result = AdminControl.invoke(
            'WebSphere:type=NodeSync,node=' + nodeName + ',*', 'sync')
        print('Node sync ' + nodeName + ': ' + str(result))
    except Exception as e:
        print('Node sync FAILED for ' + nodeName + ': ' + str(e))

print('=== v8.5 Deploy — complete ===')
print('Verify at: http://192.168.10.20/digistack-bank/XATransfer')