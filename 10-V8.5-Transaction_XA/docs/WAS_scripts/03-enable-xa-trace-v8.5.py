# =============================================================================
# enable-xa-trace-v8.5.py
#
# Purpose  : Enable Transaction trace (com.ibm.ws.Transaction*=all) at
#            runtime on both cluster members.
#            Equivalent of Admin Console Steps 2 and 3.
#
# Effect   : Runtime only — trace activates immediately, no server restart.
#            Does NOT write to the Configuration layer (no AdminConfig.save()).
#            Trace stops when the server is restarted unless you also set the
#            Configuration layer (see comment at the bottom of this script).
#
# Run as   :
#   /apps/IBM/WebSphere/AppServer/bin/wsadmin.sh \
#     -lang jython \
#     -conntype SOAP \
#     -host 192.168.10.10 \
#     -port 8879 \
#     -user wasadmin \
#     -password <your-password> \
#     -f wsadmin-scripts/enable-xa-trace-v8.5.py
# =============================================================================

import sys

# ── Substitution variables ────────────────────────────────────────────────────
# Replace these with your actual values from Step 1.
TRACE_SPEC = '*=info:com.ibm.ws.Transaction*=all'

# Map of node name → server name for all cluster members.
CLUSTER_MEMBERS = {
    'devdsbinnode01': 'devdsbinappcluster01_server1',
    'devdsbinnode02': 'devdsbinappcluster01_server2'
}

print('=== Enable XA Transaction Trace — start ===')
print('Trace spec: ' + TRACE_SPEC)

for nodeName, serverName in CLUSTER_MEMBERS.items():

    # ── Query the live TraceService MBean for this server ─────────────────────
    # MBeans are only available for running servers.
    # If a server is stopped, queryNames returns an empty string — caught below.
    query = ('WebSphere:type=TraceService,'
             'node=' + nodeName + ','
             'process=' + serverName + ',*')

    traceServiceMBean = AdminControl.queryNames(query)

    if not traceServiceMBean or not traceServiceMBean.strip():
        print('WARNING: TraceService MBean not found for '
              + serverName + ' on ' + nodeName
              + '. Is the server running?')
        continue

    # ── Apply the trace specification at runtime ──────────────────────────────
    # traceSpecification is a writeable attribute on the TraceService MBean.
    # Setting it takes effect immediately — no server restart needed.
    AdminControl.setAttribute(
        traceServiceMBean.strip(),
        'traceSpecification',
        TRACE_SPEC
    )
    print('Trace ENABLED on ' + serverName + ' (' + nodeName + ')')

print('')
print('=== Enable XA Transaction Trace — complete ===')
print('Both cluster members now write Transaction trace to their trace.log.')
print('')
print('Trace.log locations:')
print('  Member 1 (dsb-dmgr):  /apps/IBM/WebSphere/AppServer/profiles/'
      'devdsbinnode01/logs/devdsbinappcluster01_server1/trace.log')
print('  Member 2 (dsb-node02): /apps/IBM/WebSphere/AppServer/profiles/'
      'devdsbinnode02/logs/devdsbinappcluster01_server2/trace.log')
print('')
print('NOTE: This is a runtime-only change.')
print('The trace will stop the next time either server is restarted.')

# =============================================================================
# OPTIONAL — persist to Configuration layer so trace survives restarts:
# (Do NOT use for the lab — generates very large log files if left on.)
#
# for nodeName, serverName in CLUSTER_MEMBERS.items():
#     serverId = AdminConfig.getid(
#         '/Node:' + nodeName + '/Server:' + serverName + '/')
#     traceServiceId = AdminConfig.list('TraceService', serverId)
#     AdminConfig.modify(traceServiceId,
#                        [['startupTraceSpecification', TRACE_SPEC]])
# AdminConfig.save()
# =============================================================================