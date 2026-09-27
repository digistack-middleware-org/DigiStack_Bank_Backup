# =============================================================================
# create-xa-datasources-v8.5.py
#
# Purpose  : Create PostgreSQL XA JDBC Provider, jdbc/DebitDS, jdbc/CreditDS
#            at Cell scope — NDS01 Rule 7 wsadmin equivalent of Admin Console
#            Steps 2-7 in Sprint 1.
#
# When     : Run on a FRESH environment only.
#            On the existing lab, these DataSources are already present —
#            running this script again will create duplicates.
#
# Run as   :
#   /apps/IBM/WebSphere/AppServer/bin/wsadmin.sh \
#     -lang jython \
#     -conntype SOAP \
#     -host 192.168.10.10 \
#     -port 8879 \
#     -user wasadmin \
#     -password <your-password> \
#     -f wsadmin-scripts/create-xa-datasources-v8.5.py
# =============================================================================

import sys

print('=== v8.5 XA DataSource Setup — start ===')

# ── 1. Locate the Cell ───────────────────────────────────────────────────────
cellId = AdminConfig.getid('/Cell:devdsbincell01/')
if not cellId:
    print('ERROR: Cell devdsbincell01 not found. Check cell name.')
    sys.exit(1)
print('Cell: ' + cellId)

# ── 2. PostgreSQL JDBC JAR path ──────────────────────────────────────────────
# Adjust the jar filename to match what is in your lib/ext directory.
pgJar = '/apps/IBM/WebSphere/AppServer/lib/ext/postgresql-42.7.3.jar'

# ── 3. Create PostgreSQL XA JDBC Provider at Cell scope ─────────────────────
# Guard: skip creation if it already exists.
xaProviderId = ''
allProviders = AdminConfig.list('JDBCProvider', cellId).splitlines()
for p in allProviders:
    if not p.strip():
        continue
    try:
        name = AdminConfig.showAttribute(p.strip(), 'name')
        if name == 'PostgreSQL XA JDBC Provider':
            xaProviderId = p.strip()
            print('XA Provider already exists — skipping creation: ' + xaProviderId)
            break
    except:
        continue

if not xaProviderId:
    xaProviderAttrs = [
        ['name',                    'PostgreSQL XA JDBC Provider'],
        ['implementationClassName', 'org.postgresql.xa.PGXADataSource'],
        ['classpath',               pgJar],
        ['description',             'PostgreSQL XA provider for v8.5 XA DataSources']
    ]
    xaProviderId = AdminConfig.create('JDBCProvider', cellId, xaProviderAttrs)
    print('Created XA Provider: ' + xaProviderId)

# ── 4. JAAS Auth Alias (created at v7 — reused here) ────────────────────────
authAlias = 'devdsbincell01/BankDS_Alias'

# ── 5. Helper: create one XA DataSource with its pool and connection props ───
def createXaDataSource(providerId, dsName, jndiName, description, authAlias):
    """
    Creates a DataSource under providerId with the given name and JNDI name.
    Adds connection pool config and serverName/portNumber/databaseName
    custom properties pointing to digistack_bank on dsb-db.
    """
    dsAttrs = [
        ['name',                                'DigiStack ' + dsName + ' XA DataSource'],
        ['jndiName',                            jndiName],
        ['description',                         description],
        ['authDataAlias',                       authAlias],
        ['xaRecoveryAuthAlias',                 authAlias],
        ['componentManagedAuthenticationAlias', authAlias]
    ]
    dsId = AdminConfig.create('DataSource', providerId, dsAttrs)
    print('Created DataSource ' + jndiName + ': ' + dsId)

    # Connection pool
    AdminConfig.create('ConnectionPool', dsId, [
        ['minConnections',    '2'],
        ['maxConnections',    '10'],
        ['connectionTimeout', '180'],
        ['agedTimeout',       '1800'],
        ['reapTime',          '180'],
        ['unusedTimeout',     '1800']
    ])
    print('Connection pool set for ' + jndiName)

    # Connection properties (PGXADataSource JavaBean properties)
    propSet = AdminConfig.create('J2EEResourcePropertySet', dsId, [])
    AdminConfig.create('J2EEResourceProperty', propSet, [
        ['name',  'serverName'],
        ['value', '192.168.10.30'],
        ['type',  'java.lang.String']
    ])
    AdminConfig.create('J2EEResourceProperty', propSet, [
        ['name',  'portNumber'],
        ['value', '5432'],
        ['type',  'java.lang.Integer']
    ])
    AdminConfig.create('J2EEResourceProperty', propSet, [
        ['name',  'databaseName'],
        ['value', 'digistack_bank'],
        ['type',  'java.lang.String']
    ])
    print('Connection properties set for ' + jndiName)
    return dsId

# ── 6. Create DEBIT_DS ───────────────────────────────────────────────────────
debitDsId = createXaDataSource(
    xaProviderId,
    'Debit',
    'jdbc/DebitDS',
    'XA DataSource — debit side for v8.5 XA lab',
    authAlias
)

# ── 7. Create CREDIT_DS ──────────────────────────────────────────────────────
creditDsId = createXaDataSource(
    xaProviderId,
    'Credit',
    'jdbc/CreditDS',
    'XA DataSource — credit side for v8.5 XA lab',
    authAlias
)

# ── 8. Save to master configuration ─────────────────────────────────────────
AdminConfig.save()
print('Configuration saved.')

# ── 9. Sync both nodes ───────────────────────────────────────────────────────
for nodeName in ['devdsbinnode01', 'devdsbinnode02']:
    try:
        result = AdminControl.invoke(
            'WebSphere:type=NodeSync,node=' + nodeName + ',*',
            'sync'
        )
        print('Node sync ' + nodeName + ': ' + str(result))
    except Exception as e:
        print('Node sync FAILED for ' + nodeName + ': ' + str(e))

print('=== v8.5 XA DataSource Setup — complete ===')
print('Verify via Admin Console: Resources > JDBC > Data sources')
print('Test Connection on both jdbc/DebitDS and jdbc/CreditDS before proceeding.')