# =============================================================================
# regenerate-plugin-v9.py
#
# Purpose  : Regenerate plugin-cfg.xml for webserver1 and propagate it
#            to dsb-ihs. Equivalent of Admin Console Steps 3-7 above.
#            Run at the start of v9 to ensure IHS has a fully current
#            plugin-cfg.xml before session management testing begins.
#
# Run as   :
#   /apps/IBM/WebSphere/AppServer/bin/wsadmin.sh \
#     -lang jython \
#     -conntype SOAP \
#     -host 192.168.10.10 \
#     -port 8879 \
#     -user wasadmin \
#     -password <your-password> \
#     -f wsadmin-scripts/regenerate-plugin-v9.py
# =============================================================================

import sys

WEB_SERVER_NAME = 'webserver1'
NODE_NAME       = 'devdsbinnode01'

print('=== Plugin Regenerate and Propagate — v9 ===')
print('')

# ── Find the Web Server configuration object ──────────────────────────────────
# A Web Server in WAS configuration is stored as a Server object
# with a specific type. We find it by name under its node.
webServerId = AdminConfig.getid(
    '/Node:' + NODE_NAME + '/Server:' + WEB_SERVER_NAME + '/')

if not webServerId:
    print('ERROR: Web server ' + WEB_SERVER_NAME
          + ' not found under node ' + NODE_NAME)
    print('Check the web server definition in Admin Console:')
    print('  Servers -> Web servers')
    sys.exit(1)

print('Found web server: ' + WEB_SERVER_NAME)

# ── Generate the plugin-cfg.xml ───────────────────────────────────────────────
# AdminControl.invoke on the WebServer MBean triggers the same
# action as clicking "Generate Plug-in" in the Admin Console.
try:
    webServerMBean = AdminControl.queryNames(
        'WebSphere:type=WebServer,name=' + WEB_SERVER_NAME
        + ',node=' + NODE_NAME + ',*')

    if not webServerMBean or not webServerMBean.strip():
        print('ERROR: WebServer MBean not found.')
        print('  The IHS Administration Server (port 8008) must be running')
        print('  on dsb-ihs for the MBean to be available.')
        sys.exit(1)

    webServerMBean = webServerMBean.strip()
    print('Found WebServer MBean: ' + webServerMBean)

    # Generate plugin-cfg.xml
    print('Generating plugin-cfg.xml...')
    AdminControl.invoke(webServerMBean, 'generatePlugin')
    print('Generation complete.')

    # Propagate to IHS
    print('Propagating plugin-cfg.xml to dsb-ihs...')
    AdminControl.invoke(webServerMBean, 'propagatePlugin')
    print('Propagation complete.')

except Exception as e:
    print('Generate/Propagate failed: ' + str(e))
    print('')
    print('Common causes:')
    print('  1. IHS Administration Server not running on dsb-ihs port 8008.')
    print('     Fix: ssh wasadmin@192.168.10.20')
    print('          /apps/IBM/HTTPServer/bin/adminctl start')
    print('  2. webserver1 definition missing from WAS config.')
    print('     Fix: recreate the web server definition (v4.5 procedure).')
    sys.exit(1)

print('')
print('=== Plugin Regenerate and Propagate — complete ===')
print('')
print('Verify on dsb-ihs:')
print('  ls -la /apps/IBM/HTTPServer/conf/plugin-cfg.xml')
print('  grep -i CloneID /apps/IBM/HTTPServer/conf/plugin-cfg.xml')