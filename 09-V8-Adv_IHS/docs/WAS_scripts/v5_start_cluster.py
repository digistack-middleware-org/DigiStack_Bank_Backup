# =============================================================
# Script  : v5_start_cluster.py
# Version : P01 v5
# Purpose : Start all members of devdsbinappcluster01.
#           Equivalent to Admin Console:
#           Servers → Clusters → tick cluster → Start.
#
# Run from DMgr profile bin on dsb-dmgr:
#   cd /apps/IBM/WebSphere/AppServer/profiles/devdsbindmgr01/bin/
#   ./wsadmin.sh -lang jython -username wasadmin -password <pwd> \
#       -f /tmp/v5_start_cluster.py
# =============================================================

import sys
import time

print("=== DigiStack Bank v5 — Start Cluster Script ===")

CLUSTER_NAME = 'devdsbinappcluster01'
CELL_NAME    = 'devdsbincell01'

# ── Step 1: Locate the cluster MBean ──
# AdminControl works with the live running cell (MBeans).
# AdminConfig works with stored configuration (XML files).
# To start a cluster we need the live MBean, not the config object.
clusterMBean = AdminControl.queryNames(
    'cell=' + CELL_NAME +
    ',type=Cluster,name=' + CLUSTER_NAME + ',*'
)

if not clusterMBean:
    print("ERROR: Cluster MBean not found.")
    print("       Ensure both Node Agents are running.")
    print("       Start Node Agents first, then retry.")
    sys.exit(1)

print("Found cluster MBean: " + clusterMBean)

# ── Step 2: Check current state ──
try:
    currentState = AdminControl.getAttribute(
        clusterMBean, 'state')
    print("Current cluster state: " + str(currentState))
except Exception as e:
    print("Note: Could not read state — " + str(e))
    currentState = 'unknown'

# ── Step 3: Start the cluster ──
if str(currentState) == 'websphere.cluster.running':
    print("Cluster is already running — no action needed.")
else:
    print("Starting cluster '" + CLUSTER_NAME + "'...")
    try:
        AdminControl.invoke(clusterMBean, 'start')
        print("Start command issued.")
        print("Waiting 60 seconds for members to start...")
        time.sleep(60)
    except Exception as e:
        print("ERROR starting cluster: " + str(e))
        sys.exit(1)

# ── Step 4: Verify each member's state ──
print("\n--- Cluster Member States ---")
clusterId = AdminConfig.getid(
    '/ServerCluster:' + CLUSTER_NAME + '/')
members = AdminConfig.list(
    'ClusterMember', clusterId).splitlines()

for m in members:
    mName = AdminConfig.showAttribute(m, 'memberName')
    mNode = AdminConfig.showAttribute(m, 'nodeName')
    try:
        memberMBean = AdminControl.queryNames(
            'cell=' + CELL_NAME +
            ',node=' + mNode +
            ',name=' + mName +
            ',type=Server,*'
        )
        if memberMBean:
            state = AdminControl.getAttribute(
                memberMBean, 'state')
            print(mName + " on " + mNode +
                  ": " + str(state))
        else:
            print(mName + " on " + mNode +
                  ": MBean not found (still starting?)")
    except Exception as e:
        print(mName + " on " + mNode +
              ": " + str(e))

print("\n=== Cluster start complete. ===")
print("Both members should show 'STARTED'.")
print("Verify: Servers → WebSphere application servers")