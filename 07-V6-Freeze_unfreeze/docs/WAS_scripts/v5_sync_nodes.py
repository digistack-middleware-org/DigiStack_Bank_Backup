# =============================================================
# Script  : v5_sync_nodes.py
# Version : P01 v5
# Purpose : Synchronize all federated nodes with the DMgr
#           master configuration.
#           Equivalent to Admin Console:
#           System Administration → Nodes → Full Resynchronize.
#
# Run from dsb-dmgr VM (from DMgr profile bin, not AppServer):
#   cd /apps/IBM/WebSphere/AppServer/profiles/devdsbindmgr01/bin/
#   ./wsadmin.sh -lang jython -username wasadmin -password <pwd> \
#       -f /tmp/v5_sync_nodes.py
# =============================================================

print("=== DigiStack Bank v5 — Node Synchronization Script ===")

# ── Get all nodes except the DMgr node ──
# AdminControl.queryNames returns MBean object names matching
# a pattern. The NodeSync MBean handles synchronization per node.
nodes = AdminControl.queryNames('type=NodeSync,*')

if not nodes:
    print("No NodeSync MBeans found — nodes may not be running.")
else:
    nodeList = nodes.splitlines()
    print("Found " + str(len(nodeList)) + " node(s) to sync.")

    for node in nodeList:
        try:
            # Extract the node name for display
            nodeName = AdminControl.getAttribute(node, 'nodeName')
            print("Synchronizing node: " + nodeName + "...")

            # invoke sync() triggers a full resynchronization
            result = AdminControl.invoke(node, 'sync')
            if str(result) == 'true':
                print("  -> " + nodeName +
                      ": Synchronized successfully.")
            else:
                print("  -> " + nodeName +
                      ": Sync returned: " + str(result))
        except Exception as e:
            print("  -> Error syncing node: " + str(e))

print("\n=== Node synchronization complete. ===")
print("Verify: System Administration → Nodes in Admin Console")
print("Both nodes should show 'Synchronized' status.")