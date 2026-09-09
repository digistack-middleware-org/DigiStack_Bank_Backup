# =============================================================
# Script  : v5_session_replication.py
# Version : P01 v5
# Purpose : Configure memory-to-memory session replication
#           for devdsbinappcluster01.
#           Equivalent to Admin Console:
#           Clusters → devdsbinappcluster01 →
#           Session management → Distributed environment settings
#           → Memory-to-memory replication.
#
# Run from DMgr profile bin on dsb-dmgr:
#   cd /apps/IBM/WebSphere/AppServer/profiles/devdsbindmgr01/bin/
#   ./wsadmin.sh -lang jython -username wasadmin -password <pwd> \
#       -f /tmp/v5_session_replication.py
# =============================================================

import sys

print("=== DigiStack Bank v5 — Session Replication Script ===")

CLUSTER_NAME = 'devdsbinappcluster01'
CELL_NAME    = 'devdsbincell01'

# ── Step 1: Locate the cluster configuration object ──
clusterId = AdminConfig.getid(
    '/ServerCluster:' + CLUSTER_NAME + '/')

if not clusterId:
    print("ERROR: Cluster '" + CLUSTER_NAME + "' not found.")
    sys.exit(1)

print("Found cluster: " + clusterId)

# ── Step 2: Get or create the cluster's ApplicationServer config ──
# The session management settings live under the ApplicationServer
# component of each cluster member, but for cluster-wide settings
# we configure through the cluster's own server components.
members = AdminConfig.list(
    'ClusterMember', clusterId).splitlines()

print("Configuring session replication on " +
      str(len(members)) + " member(s)...")

for member in members:
    memberName = AdminConfig.showAttribute(member, 'memberName')
    nodeName   = AdminConfig.showAttribute(member, 'nodeName')
    print("\nConfiguring: " + memberName + " on " + nodeName)

    # Get the server config for this member
    serverConfig = AdminConfig.getid(
        '/Cell:' + CELL_NAME +
        '/Node:' + nodeName +
        '/Server:' + memberName + '/'
    )

    if not serverConfig:
        print("  WARNING: Server config not found for " +
              memberName + " on " + nodeName)
        continue

    # ── Step 3: Get ApplicationServer component ──
    appServer = AdminConfig.list(
        'ApplicationServer', serverConfig)

    if not appServer:
        print("  WARNING: ApplicationServer not found.")
        continue

    # ── Step 4: Get or create WebContainer ──
    webContainer = AdminConfig.list(
        'WebContainer', appServer)

    if not webContainer:
        print("  WARNING: WebContainer not found.")
        continue

    # ── Step 5: Configure session management ──
    sessionManager = AdminConfig.list(
        'SessionManager', webContainer)

    if sessionManager:
        # Enable distributed sessions
        AdminConfig.modify(sessionManager, [
            ['enable',                    'true'],
            ['enableUrlRewriting',        'false'],
            ['enableCookies',             'true'],
            ['enableSSLTracking',         'false'],
            ['enableProtocolSwitchRewriting', 'false']
        ])
        print("  SessionManager updated.")

        # ── Step 6: Configure distributed environment settings ──
        distEnvSettings = AdminConfig.list(
            'DistributedSessionConfig', sessionManager)

        if distEnvSettings:
            # Set memory-to-memory replication
            AdminConfig.modify(distEnvSettings, [
                ['replicationType', 'BOTH']
            ])
            print("  DistributedSessionConfig: " +
                  "replicationType=BOTH (memory-to-memory).")
        else:
            # Create distributed session config
            AdminConfig.create(
                'DistributedSessionConfig',
                sessionManager,
                [['replicationType', 'BOTH']]
            )
            print("  DistributedSessionConfig created: " +
                  "replicationType=BOTH.")
    else:
        print("  WARNING: SessionManager not found.")

# ── Step 7: Save configuration ──
AdminConfig.save()
print("\nConfiguration saved.")

print("\n=== Session replication configuration complete. ===")
print("Next: Ripple-restart the cluster to activate settings.")
print("      Or run v5_start_cluster.py after a full cluster stop.")
print("Verify: Clusters → devdsbinappcluster01 →")
print("        Session management → Distributed environment settings")
print("        Should show: Memory-to-memory replication selected.")