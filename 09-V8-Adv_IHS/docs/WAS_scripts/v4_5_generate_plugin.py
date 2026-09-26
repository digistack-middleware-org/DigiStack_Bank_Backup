# =============================================================
# Script  : v4_5_generate_plugin.py
# Version : P01 v4.5
# Purpose : Generate plugin-cfg.xml for webserver1 against
#           the standalone AppServer (server1).
#           Equivalent to Admin Console:
#           Servers → Web Servers → tick webserver1 →
#           Generate Plug-in.
#
# Run from dsb-dmgr VM:
#   cd /opt/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/bin/
#   ./wsadmin.sh -lang jython -username wasadmin -password <pwd> \
#       -f /tmp/v4_5_generate_plugin.py
#
# What this script does:
#   1. Locates the webserver1 definition in the WAS configuration
#   2. Calls generatePluginCfg() which produces plugin-cfg.xml
#      on disk at the profile's config directory
#   3. Confirms the file exists and shows its location
#   4. Displays key routing entries from the generated file
# =============================================================

import sys
import os

print("=== DigiStack Bank v4.5 — Generate Plugin Script ===")

WEB_SERVER_NAME = 'webserver1'
NODE_NAME       = 'devdsbinnode01'
CELL_NAME       = 'devdsbincell01'
PROFILE_ROOT    = '/opt/IBM/WebSphere/AppServer/profiles/devdsbinappserver01'

# ── Step 1: Confirm webserver1 exists ──
wsId = AdminConfig.getid('/Server:' + WEB_SERVER_NAME + '/')
if not wsId:
    print("ERROR: '" + WEB_SERVER_NAME +
          "' not found in configuration.")
    print("       Run v4_5_create_webserver.py first.")
    sys.exit(1)

print("Found: '" + WEB_SERVER_NAME + "' — proceeding.")

# ── Step 2: Generate the plugin configuration ──
# AdminTask.generatePluginCfg() generates plugin-cfg.xml for the
# specified web server. The file is written to the WAS profile's
# config directory on dsb-dmgr (this VM — NOT dsb-ihs yet).
# Propagation to dsb-ihs happens in Sprint 4 (propagatePluginCfg).
print("Generating plugin-cfg.xml for '" +
      WEB_SERVER_NAME + "'...")

try:
    result = AdminTask.generatePluginCfg(
        ['-serverName', WEB_SERVER_NAME,
         '-nodeName',   NODE_NAME,
         '-options',    ''
        ]
    )
    print("generatePluginCfg() completed.")
    if result:
        print("Result: " + str(result))
except Exception as e:
    print("ERROR during generation: " + str(e))
    sys.exit(1)

# ── Step 3: Confirm the file was created on disk ──
# The expected location follows WAS's standard config path pattern.
pluginCfgPath = (
    PROFILE_ROOT + '/config/cells/' + CELL_NAME +
    '/nodes/' + NODE_NAME +
    '/servers/' + WEB_SERVER_NAME +
    '/plugin-cfg.xml'
)

print("\nExpected plugin-cfg.xml location:")
print(pluginCfgPath)

# Check if file exists using wsadmin's Java interop
try:
    f = open(pluginCfgPath, 'r')
    content = f.read()
    f.close()
    fileSizeKB = len(content) / 1024
    print("File exists. Size: ~" +
          str(round(fileSizeKB, 1)) + " KB")
    print("File successfully generated on disk.")
except Exception as e:
    print("Note: Could not read file via Python open() — " +
          str(e))
    print("Verify manually: ls -lh " + pluginCfgPath)

# ── Step 4: Display key routing entries ──
print("\n--- Key Entries in plugin-cfg.xml ---")
print("(Verify these manually with grep on the dsb-dmgr VM)")
print("")
print("Commands to verify after this script completes:")
print("")
print("1. Confirm file exists:")
print("   ls -lh " + pluginCfgPath)
print("")
print("2. Confirm WAS server hostname (should be dsb-dmgr IP):")
print("   grep 'ServerIOTimeout\|Hostname' " + pluginCfgPath)
print("")
print("3. Confirm port 9080 is referenced:")
print("   grep '9080' " + pluginCfgPath)
print("")
print("4. Confirm /digistack-bank context root is present:")
print("   grep 'digistack-bank' " + pluginCfgPath)

print("\n=== Plugin generation complete. ===")
print("Next step: Propagate to dsb-ihs (Sprint 4)")
print("           using v4_5_propagate_plugin.py")