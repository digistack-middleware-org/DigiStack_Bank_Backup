# =============================================================
# Script  : v3_verify_logging.py
# Version : P01 v3
# Purpose : Verify WAS logging configuration is correct for v3.
#           Confirms rotation settings and trace level are still
#           as configured in v1 Sprint 4.
#           Does NOT change configuration — read-only verification.
#
# Run from dsb-dmgr VM:
#   cd /apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/bin/
#   ./wsadmin.sh -lang jython -username wasadmin -password <pwd> \
#       -f /tmp/v3_verify_logging.py
# =============================================================

print("=== DigiStack Bank v3 — Logging Verification Script ===")

# ── Locate server1 ──
server = AdminConfig.getid('/Server:server1/')
if not server:
    print("ERROR: server1 not found.")
    import sys
    sys.exit(1)

print("Server: " + server)

# ── Check RASLoggingService (SystemOut / SystemErr) ──
rasService = AdminConfig.list('RASLoggingService', server)
streamRedirects = AdminConfig.list(
    'StreamRedirect', rasService).splitlines()

print("\n--- JVM Log Rotation Settings ---")
for redirect in streamRedirects:
    fileName    = AdminConfig.showAttribute(redirect, 'fileName')
    maxFiles    = AdminConfig.showAttribute(
        redirect, 'maxNumberOfBackupFiles')
    rolloverSize = AdminConfig.showAttribute(
        redirect, 'rolloverSize')
    print("Stream : " + str(fileName))
    print("  maxNumberOfBackupFiles : " + str(maxFiles))
    print("  rolloverSize (MB)      : " + str(rolloverSize))

    # Validate expected values
    if str(maxFiles) != '3':
        print("  WARNING: Expected 3 backup files — found " +
              str(maxFiles))
    else:
        print("  OK: rotation = 3 files")

    if str(rolloverSize) != '50':
        print("  WARNING: Expected 50 MB — found " +
              str(rolloverSize))
    else:
        print("  OK: rollover = 50 MB")

# ── Check TraceService ──
traceService = AdminConfig.list('TraceService', server)
traceSpec    = AdminConfig.showAttribute(
    traceService, 'startupTraceSpecification')
traceOutput  = AdminConfig.showAttribute(
    traceService, 'traceOutputType')
traceFiles   = AdminConfig.showAttribute(
    traceService, 'maxNumberOfBackupFiles')
traceSize    = AdminConfig.showAttribute(
    traceService, 'rolloverSize')

print("\n--- Diagnostic Trace Settings ---")
print("startupTraceSpecification : " + str(traceSpec))
print("traceOutputType           : " + str(traceOutput))
print("maxNumberOfBackupFiles    : " + str(traceFiles))
print("rolloverSize (MB)         : " + str(traceSize))

if str(traceSpec) != '*=info':
    print("WARNING: Trace spec is not *=info — " +
          "current value: " + str(traceSpec))
else:
    print("OK: trace level = *=info")

# ── Check Runtime Trace Level ──
print("\n--- Runtime Trace Level ---")
try:
    traceAdmin = AdminControl.queryNames('type=TraceService,*')
    currentSpec = AdminControl.getAttribute(
        traceAdmin, 'traceSpecification')
    print("Current runtime trace spec: " + str(currentSpec))
    if str(currentSpec) != '*=info':
        print("Note: Runtime spec differs from config — " +
              "apply *=info if needed.")
    else:
        print("OK: runtime trace = *=info")
except Exception as e:
    print("Could not read runtime trace: " + str(e))

# ── Confirm application is running ──
print("\n--- Application Status ---")
try:
    appState = AdminControl.getAttribute(
        AdminControl.queryNames(
            'type=Application,name=digistack-bank-v3,*'
        ),
        'deploymentState'
    )
    print("digistack-bank-v3 state: " + str(appState))
except Exception as e:
    print("Could not read app state: " + str(e))

print("\n=== Logging verification complete. ===")