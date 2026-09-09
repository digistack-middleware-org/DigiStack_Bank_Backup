# =============================================================
# Script  : v4_5_propagate_plugin.py
# Version : P01 v4.5
# Purpose : Propagate plugin-cfg.xml from dsb-dmgr to dsb-ihs.
#           Equivalent to Admin Console:
#           Servers → Web Servers → tick webserver1 →
#           Propagate Plug-in.
#
# Run from dsb-dmgr VM:
#   cd /opt/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/bin/
#   ./wsadmin.sh -lang jython -username wasadmin -password <pwd> \
#       -f /tmp/v4_5_propagate_plugin.py
#
# Pre-requisite: plugin-cfg.xml already generated (Sprint 3).
#   IHS admin daemon must be reachable on dsb-ihs:8008
#   for automatic propagation to work.
#   If propagation fails, use manual SCP method documented
#   in Sprint 4 Step 3 of SetupDoc-v4.5.md.
# =============================================================

import sys

print("=== DigiStack Bank v4.5 — Propagate Plugin Script ===")

WEB_SERVER_NAME = 'webserver1'
NODE_NAME       = 'devdsbinnode01'

# ── Step 1: Confirm webserver1 exists ──
wsId = AdminConfig.getid('/Server:' + WEB_SERVER_NAME + '/')
if not wsId:
    print("ERROR: '" + WEB_SERVER_NAME +
          "' not found.")
    print("       Run Sprint 2 and Sprint 3 scripts first.")
    sys.exit(1)

print("Found: '" + WEB_SERVER_NAME + "'")

# ── Step 2: Confirm plugin-cfg.xml was generated ──
CELL_NAME    = 'devdsbincell01'
PROFILE_ROOT = '/opt/IBM/WebSphere/AppServer/profiles/devdsbinappserver01'
pluginCfgPath = (
    PROFILE_ROOT + '/config/cells/' + CELL_NAME +
    '/nodes/' + NODE_NAME +
    '/servers/' + WEB_SERVER_NAME +
    '/plugin-cfg.xml'
)

try:
    f = open(pluginCfgPath, 'r')
    f.close()
    print("Confirmed: plugin-cfg.xml exists on this VM.")
except Exception as e:
    print("ERROR: plugin-cfg.xml not found at:")
    print("  " + pluginCfgPath)
    print("  Run Sprint 3 (Generate Plug-in) first.")
    sys.exit(1)

# ── Step 3: Propagate the plugin to dsb-ihs ──
# propagatePluginCfg() pushes the file to the IHS VM via
# the IHS admin port (8008). If the admin daemon on dsb-ihs
# is not running, this call will fail — use manual SCP instead.
print("Propagating plugin-cfg.xml to dsb-ihs...")

try:
    result = AdminTask.propagatePluginCfg(
        ['-serverName', WEB_SERVER_NAME,
         '-nodeName',   NODE_NAME
        ]
    )
    print("Propagation completed.")
    if result:
        print("Result: " + str(result))
    print("plugin-cfg.xml is now on dsb-ihs at:")
    print("  /opt/IBM/WebSphere/Plugins/config/" +
          WEB_SERVER_NAME + "/plugin-cfg.xml")

except Exception as e:
    print("NOTE: Automatic propagation failed: " + str(e))
    print("")
    print("This is expected if the IHS admin daemon is not")
    print("running on dsb-ihs:8008.")
    print("")
    print("Use manual SCP propagation instead:")
    print("")
    print("On dsb-dmgr VM, run:")
    print("  scp " + pluginCfgPath + " \\")
    print("  root@192.168.10.20:/opt/IBM/WebSphere/Plugins/")
    print("  config/" + WEB_SERVER_NAME + "/plugin-cfg.xml")
    print("")
    print("Create the directory on dsb-ihs first if needed:")
    print("  ssh root@192.168.10.20 'mkdir -p \\")
    print("  /opt/IBM/WebSphere/Plugins/config/" +
          WEB_SERVER_NAME + "'")

print("\n=== Propagation script complete. ===")
print("Next: Configure httpd.conf on dsb-ihs (Sprint 4 Step 4)")