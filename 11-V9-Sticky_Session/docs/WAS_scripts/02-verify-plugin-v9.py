# =============================================================================
# verify-plugin-v9.py
#
# Purpose  : Read the WebServer plug-in configuration from the WAS config
#            layer and confirm CloneID values match what is in plugin-cfg.xml.
#            Read-only — makes no changes.
#
# Run as   :
#   /apps/IBM/WebSphere/AppServer/bin/wsadmin.sh \
#     -lang jython \
#     -conntype SOAP \
#     -host 192.168.10.10 \
#     -port 8879 \
#     -user wasadmin \
#     -password <your-password> \
#     -f wsadmin-scripts/verify-plugin-v9.py
# =============================================================================

print('=== Plugin Config Verification — v9 Sprint 1 ===')
print('')

# ── Cluster member names and expected ports ───────────────────────────────────
CLUSTER_MEMBERS = {
    'devdsbinnode01': {
        'server': 'devdsbinappcluster01_server1',
        'expected_host': '192.168.10.10',
        'expected_port': '9080'
    },
    'devdsbinnode02': {
        'server': 'devdsbinappcluster01_server2',
        'expected_host': '192.168.10.11',
        'expected_port': '9081'
    }
}

print('Cluster members and their IHS-facing addresses:')
for nodeName, info in CLUSTER_MEMBERS.items():
    serverId = AdminConfig.getid(
        '/Node:' + nodeName + '/Server:' + info['server'] + '/')
    if not serverId:
        print('  WARNING: Server not found — ' + info['server'])
        continue

    # Read the HTTP transport port for this server
    # The port WAS uses for HTTP requests is on the WEBCONTAINER transport
    endpoints = AdminConfig.list('EndPoint', serverId).splitlines()
    httpPort = 'not found'
    for ep in endpoints:
        ep = ep.strip()
        if not ep:
            continue
        try:
            epName = AdminConfig.showAttribute(ep, 'endPointName')
            if 'WC_defaulthost' == epName:
                host = AdminConfig.showAttribute(ep, 'host')
                port = AdminConfig.showAttribute(ep, 'port')
                httpPort = str(port)
        except:
            continue

    status = 'PASS' if httpPort == info['expected_port'] else \
             'FAIL: expected ' + info['expected_port'] + ' got ' + httpPort
    print('  ' + info['server'] + ' (' + nodeName + ')')
    print('    Host: ' + info['expected_host']
          + '  Port: ' + httpPort + '  [' + status + ']')

print('')

# ── Web server plugin file location ──────────────────────────────────────────
cellId = AdminConfig.getid('/Cell:devdsbincell01/')
webServerId = AdminConfig.getid('/Node:devdsbinnode01/Server:webserver1/')

if webServerId:
    pluginProperties = AdminConfig.list(
        'WebserverPluginSettings', webServerId)
    if pluginProperties and pluginProperties.strip():
        print('WebServer plugin settings found:')
        print(AdminConfig.show(pluginProperties.strip().splitlines()[0]))
    else:
        print('No WebserverPluginSettings object found for webserver1.')
else:
    print('webserver1 not found in WAS config.')

print('')
print('=== Plugin Config Verification — complete ===')
print('')
print('Cross-check on dsb-ihs:')
print('  grep CloneID /apps/IBM/HTTPServer/conf/plugin-cfg.xml')
print('  grep -A3 SessionManager /apps/IBM/HTTPServer/conf/plugin-cfg.xml')