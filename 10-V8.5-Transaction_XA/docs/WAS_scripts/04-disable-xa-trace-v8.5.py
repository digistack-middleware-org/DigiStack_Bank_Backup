# =============================================================================
# disable-xa-trace-v8.5.py
#
# Purpose  : Restore both cluster members to normal info-only trace.
#            Run after trace capture is complete.
#            Equivalent of Admin Console Step 9.
#
# Run as   :
#   /apps/IBM/WebSphere/AppServer/bin/wsadmin.sh \
#     -lang jython \
#     -conntype SOAP \
#     -host 192.168.10.10 \
#     -port 8879 \
#     -user wasadmin \
#     -password <your-password> \
#     -f wsadmin-scripts/disable-xa-trace-v8.5.py
# =============================================================================

NORMAL_SPEC = '*=info'

CLUSTER_MEMBERS = {
    'devdsbinnode01': 'devdsbinappcluster01_server1',
    'devdsbinnode02': 'devdsbinappcluster01_server2'
}

print('=== Disable XA Transaction Trace — start ===')

for nodeName, serverName in CLUSTER_MEMBERS.items():

    query = ('WebSphere:type=TraceService,'
             'node=' + nodeName + ','
             'process=' + serverName + ',*')

    traceServiceMBean = AdminControl.queryNames(query)

    if not traceServiceMBean or not traceServiceMBean.strip():
        print('WARNING: TraceService MBean not found for '
              + serverName + ' on ' + nodeName
              + '. Is the server running?')
        continue

    AdminControl.setAttribute(
        traceServiceMBean.strip(),
        'traceSpecification',
        NORMAL_SPEC
    )
    print('Trace DISABLED (restored to *=info) on '
          + serverName + ' (' + nodeName + ')')

print('=== Disable XA Transaction Trace — complete ===')