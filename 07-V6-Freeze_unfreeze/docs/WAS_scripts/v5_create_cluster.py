# =============================================================
# Script  : v5_create_cluster.py
# Version : P01 v5
# Purpose : Create devdsbinappcluster01 with two cluster members:
#           server1 on devdsbinnode01 (dsb-dmgr)
#           server1 on devdsbinnode02 (dsb-node02)
#           Equivalent to Admin Console:
#           Servers → Clusters → New.
#
# Run from DMgr profile bin on dsb-dmgr:
#   cd /apps/IBM/WebSphere/AppServer/profiles/devdsbindmgr01/bin/
#   ./wsadmin.sh -lang jython -username wasadmin -password <pwd> \
#       -f /tmp/v5_create_cluster.py
# =============================================================

import sys

print("=== DigiStack Bank v5 — Create Cluster Script ===")

CLUSTER_NAME = 'devdsbinappcluster01'
CELL_NAME    = 'devdsbincell01'
NODE1_NAME   = 'devdsbinnode01'
NODE2_NAME   = 'devdsbinnode02'
SERVER_NAME  = 'server1'

# ── Step 1: Check if cluster already exists ──
existingCluster = AdminConfig.getid(
    '/ServerCluster:' + CLUSTER_NAME + '/')

if existingCluster:
    print("Cluster '" + CLUSTER_NAME +
          "' already exists: " + existingCluster)
    print("Skipping creation.")
    print("Reading current cluster member list...")

    # Show existing members
    members = AdminConfig.list(
        'ClusterMember', existingCluster).splitlines()
    print("Current members: " + str(len(members)))
    for m in members:
        mName = AdminConfig.showAttribute(m, 'memberName')
        mNode = AdminConfig.showAttribute(m, 'nodeName')
        print("  " + mName + " on " + mNode)

else:
    print("Cluster '" + CLUSTER_NAME +
          "' not found — creating...")

    # ── Step 2: Create the cluster ──
    # AdminTask.createCluster() creates the cluster object
    # and the first member in a single call.
    try:
        AdminTask.createCluster(
            ['-clusterConfig',
             ['-clusterName', CLUSTER_NAME],
             '-clusterMembers',
             ['-memberName', SERVER_NAME,
              '-nodeName',   NODE1_NAME
             ]
            ]
        )
        print("Cluster '" + CLUSTER_NAME +
              "' created with first member (" +
              SERVER_NAME + " on " + NODE1_NAME + ").")
    except Exception as e:
        print("ERROR creating cluster: " + str(e))
        sys.exit(1)

    # ── Step 3: Add the second cluster member ──
    try:
        AdminTask.createClusterMember(
            ['-clusterName',  CLUSTER_NAME,
             '-memberConfig',
             ['-memberName', SERVER_NAME,
              '-nodeName',   NODE2_NAME
             ]
            ]
        )
        print("Second member added: " +
              SERVER_NAME + " on " + NODE2_NAME + ".")
    except Exception as e:
        print("ERROR adding second member: " + str(e))
        sys.exit(1)

    # ── Step 4: Save configuration ──
    AdminConfig.save()
    print("Configuration saved.")

    # ── Step 5: Verify both members exist ──
    clusterId = AdminConfig.getid(
        '/ServerCluster:' + CLUSTER_NAME + '/')
    members = AdminConfig.list(
        'ClusterMember', clusterId).splitlines()
    print("\nCluster members created: " + str(len(members)))
    for m in members:
        mName = AdminConfig.showAttribute(m, 'memberName')
        mNode = AdminConfig.showAttribute(m, 'nodeName')
        print("  " + mName + " on " + mNode)

    print("\nExpected: 2 members")
    if len(members) == 2:
        print("OK: 2 members confirmed.")
    else:
        print("WARNING: Expected 2 members, found " +
              str(len(members)))

print("\n=== Cluster creation complete. ===")
print("Next: Sync nodes and start the cluster (Sprint 3 Step 3)")
print("Verify: Servers → Clusters in Admin Console")