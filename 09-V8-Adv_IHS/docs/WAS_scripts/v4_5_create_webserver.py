# =============================================================
# Script  : v4_5_create_webserver.py
# Version : P01 v4.5
# Purpose : Create the webserver1 Web Server Definition in WAS.
#           Equivalent to Admin Console:
#           Servers → Server Types → Web Servers → New.
#
# Run from dsb-dmgr VM:
#   cd /opt/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/bin/
#   ./wsadmin.sh -lang jython -username wasadmin -password <pwd> \
#       -f /tmp/v4_5_create_webserver.py
#
# Pre-requisite: IBM HTTP Server installed on dsb-ihs (192.168.10.20)
#   at /opt/IBM/HTTPServer and Web Server Plug-ins at
#   /opt/IBM/WebSphere/Plugins.
# =============================================================

import sys

print("=== DigiStack Bank v4.5 — Web Server Definition Script ===")

# ── Configuration ──
WEB_SERVER_NAME   = 'webserver1'
IHS_HOSTNAME      = '192.168.10.20'
IHS_PORT          = '80'
IHS_INSTALL_ROOT  = '/opt/IBM/HTTPServer'
IHS_CONFIG_FILE   = '/opt/IBM/HTTPServer/conf/httpd.conf'
PLUGIN_INSTALL_ROOT = '/opt/IBM/WebSphere/Plugins'
WAS_INSTALL_ROOT  = '/opt/IBM/WebSphere/AppServer'
ADMIN_PORT        = '8008'

# ── Step 1: Check if webserver1 already exists ──
# AdminConfig.getid() returns the WAS internal ID of an object
# by type and name. Returns empty string if not found.
existingWebServer = AdminConfig.getid(
    '/Server:' + WEB_SERVER_NAME + '/')

if existingWebServer:
    print("Web Server '" + WEB_SERVER_NAME +
          "' already exists: " + existingWebServer)
    print("Skipping creation — definition already present.")
    print("To re-create: delete via Admin Console first.")
else:
    print("'" + WEB_SERVER_NAME +
          "' not found — creating...")

    # ── Step 2: Get the node to create the web server under ──
    # In a standalone profile there is only one node.
    nodeList = AdminConfig.list('Node').splitlines()
    if not nodeList:
        print("ERROR: No node found in configuration.")
        sys.exit(1)

    # Filter out any DMgr node if present
    targetNode = None
    for node in nodeList:
        nodeName = AdminConfig.showAttribute(node, 'name')
        if 'dmgr' not in nodeName.lower():
            targetNode = node
            targetNodeName = nodeName
            break

    if not targetNode:
        targetNode = nodeList[0]
        targetNodeName = AdminConfig.showAttribute(
            targetNode, 'name')

    print("Using node: " + targetNodeName)

    # ── Step 3: Create the Web Server Definition ──
    # AdminTask.createWebServer() creates the complete web server
    # definition in one call. Parameters mirror the GUI wizard fields.
    try:
        AdminTask.createWebServer(
            targetNodeName,
            ['-name',         WEB_SERVER_NAME,
             '-templateName', 'IHS',
             '-serverConfig',
             ['-webPort',           IHS_PORT,
              '-webInstallRoot',    IHS_INSTALL_ROOT,
              '-webProtocol',       'HTTP',
              '-configurationFile', IHS_CONFIG_FILE,
              '-adminPort',         ADMIN_PORT,
              '-adminUserID',       '',
              '-adminPasswd',       ''
             ],
             '-primaryServerConfig',
             ['-webHostName', IHS_HOSTNAME]
            ]
        )
        print("Web Server definition created successfully.")
    except Exception as e:
        print("ERROR creating Web Server definition: " + str(e))
        sys.exit(1)

    # ── Step 4: Verify the definition was created ──
    newWebServer = AdminConfig.getid(
        '/Server:' + WEB_SERVER_NAME + '/')
    if newWebServer:
        print("Verified: '" + WEB_SERVER_NAME +
              "' exists in configuration.")
        print("Configuration ID: " + newWebServer)
    else:
        print("WARNING: Definition created but " +
              "getid() returned empty.")

    # ── Step 5: Save the configuration ──
    AdminConfig.save()
    print("Configuration saved.")

# ── Step 6: Read and display the current Web Server details ──
print("\n--- Current Web Server Definition Details ---")
try:
    wsId = AdminConfig.getid('/Server:' + WEB_SERVER_NAME + '/')
    if wsId:
        # Show key attributes of the web server definition
        attrs = [
            'name',
        ]
        for attr in attrs:
            try:
                val = AdminConfig.showAttribute(wsId, attr)
                print(attr + ": " + str(val))
            except:
                pass

        # Show web server specific configuration
        print("Web server registered under node: " +
              targetNodeName if 'targetNodeName' in dir() else
              "See Admin Console for node details")
except Exception as e:
    print("Note: Could not read details — " + str(e))

print("\n=== Web Server Definition complete. ===")
print("Next: Generate plugin-cfg.xml (Sprint 3)")
print("Verify in Admin Console: Servers → Web Servers")
print("Expected status: Stopped (correct — plugin not yet propagated)")