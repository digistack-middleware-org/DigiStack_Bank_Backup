# =============================================================================
# inspect-tranlog-v8.5.py
#
# Purpose  : Read the Transaction Service configuration for both cluster
#            members — tranlog directory path and timeout values.
#            Documentation/audit script — makes no changes.
#
# Run as   :
#   /apps/IBM/WebSphere/AppServer/bin/wsadmin.sh \
#     -lang jython \
#     -conntype SOAP \
#     -host 192.168.10.10 \
#     -port 8879 \
#     -user wasadmin \
#     -password <your-password> \
#     -f wsadmin-scripts/inspect-tranlog-v8.5.py
# =============================================================================

CLUSTER_MEMBERS = {
    'devdsbinnode01': 'devdsbinappcluster01_server1',
    'devdsbinnode02': 'devdsbinappcluster01_server2'
}

print('=== Transaction Log Configuration — start ===')

for nodeName, serverName in CLUSTER_MEMBERS.items():

    serverId = AdminConfig.getid(
        '/Node:' + nodeName + '/Server:' + serverName + '/')

    if not serverId:
        print('WARNING: Server not found — ' + serverName
              + ' on ' + nodeName)
        continue

    # TransactionService is a child object of the Server configuration.
    tranServiceList = AdminConfig.list('TransactionService', serverId)

    if not tranServiceList or not tranServiceList.strip():
        print('WARNING: No TransactionService found for ' + serverName)
        continue

    tranServiceId = tranServiceList.strip().splitlines()[0]

    # Read the key attributes.
    totalTranLifetimeTimeout = AdminConfig.showAttribute(
        tranServiceId, 'totalTranLifetimeTimeout')
    maxTransactionTimeout = AdminConfig.showAttribute(
        tranServiceId, 'maximumTransactionTimeout')
    tranLogDirectory = AdminConfig.showAttribute(
        tranServiceId, 'transactionLogDirectory')
    enableLoggingForHeuristicReporting = AdminConfig.showAttribute(
        tranServiceId, 'enableLoggingForHeuristicReporting')

    print('')
    print('Server: ' + serverName + ' (node: ' + nodeName + ')')
    print('  tranLogDirectory              : ' + str(tranLogDirectory))
    print('  totalTranLifetimeTimeout (s)  : ' + str(totalTranLifetimeTimeout))
    print('  maximumTransactionTimeout (s) : ' + str(maxTransactionTimeout))
    print('  enableLoggingForHeuristicReporting: '
          + str(enableLoggingForHeuristicReporting))

print('')
print('=== Transaction Log Configuration — complete ===')
print('Physical tranlog file sizes — check on each machine:')
print('  dsb-dmgr  : ls -lh <profile>/tranlog/devdsbincell01/'
      'devdsbinnode01/<server1name>/')
print('  dsb-node02: ls -lh <profile>/tranlog/devdsbincell01/'
      'devdsbinnode02/<server2name>/')