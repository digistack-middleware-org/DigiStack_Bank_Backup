# Configure WAS Logging via Admin Console (GUI Method)

Open the Admin Console in your browser:
```
http://192.168.10.10:9060/ibm/console
```
Navigate to the logging configuration:
```
Troubleshooting → Logs and Trace
```

```
Step 1.1 — Click on server1 in the table that appears.

Step 1.2 — Click Diagnostic Trace in the Additional Properties section.

Step 1.3 — On the Diagnostic Trace page, confirm or set these values:

Trace Output: select File
File Name: leave as default (${SERVER_LOG_ROOT}/trace.log) — do not change
Maximum File Size: enter 50
Maximum Number of Historical Files: enter 3

Click OK.
```

Navigate to the logging configuration:
```
Troubleshooting → Logs and Trace → server1
```

```
Step 1.4 — Click JVM Logs in the Additional Properties section.

Step 1.5 — On the JVM Logs page, configure SystemOut.log rotation:

Under the System.out section:

        File Name: leave as default (${SERVER_LOG_ROOT}/SystemOut.log)
        Maximum File Size: enter 50
        Maximum Number of Historical Files: enter 3

Under the System.err section:

        File Name: leave as default (${SERVER_LOG_ROOT}/SystemErr.log)
        Maximum File Size: enter 50
        Maximum Number of Historical Files: enter 3

Click OK.
```
Save the configuration

```
Save
```
### To Set the log detail level
Navigate back to:
```
Troubleshooting → Logs and Trace → server1 → Diagnostic Trace
```
```
Click the Runtime tab ==> In the Trace Specification field, confirm it reads: "*=info"
```
If it shows something different, clear the field and type *=info exactly.
```
Click Apply.
```

#### Concept HERE ==> Runtime tab vs Configuration tab:

Configuration tab ==> The Configuration tab sets values that apply after the next restart.
Runtime tab ==> The Runtime tab changes the value immediately on the running server without a restart {useful for temporarily increasing log verbosity to diagnose an issue.}


# Configure WAS Logging via Wasadmin (Automation Method)

```
v1_set_logging.py
```
```
# =============================================================
# Script  : v1_set_logging.py
# Version : P01 v1
# Purpose : Configure WAS SystemOut/SystemErr log rotation
#           and set diagnostic trace level to *=info.
#           Equivalent to the Admin Console steps in Sprint 4.
#
# Run from dsb-dmgr VM:
#   cd /apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/bin/
#   ./wsadmin.sh -lang jython -username wasadmin -password <pwd> \
#       -f /tmp/v1_set_logging.py
# =============================================================

import sys

print("=== DigiStack Bank v1 — Logging Configuration Script ===")

# ── Step 1: Locate the server1 object in the WAS configuration ──
# AdminConfig is a wsadmin built-in object that lets you read and
# write WebSphere configuration objects (servers, datasources, etc.)

# Get the ID of server1 in the cell
server = AdminConfig.getid('/Server:server1/')

if not server:
    print("ERROR: Could not find server1 in the configuration.")
    print("       Ensure you are connected to the correct profile.")
    sys.exit(1)

print("Found server: " + server)

# ── Step 2: Configure SystemOut.log rotation ──
# RASLoggingService controls the JVM log files (SystemOut, SystemErr)
rasService = AdminConfig.list('RASLoggingService', server)
print("RASLoggingService ID: " + rasService)

# streamRedirect = the configuration object for one log stream
# There are two: one for stdout (SystemOut) and one for stderr (SystemErr)
streamRedirects = AdminConfig.list('StreamRedirect', rasService).splitlines()

for redirect in streamRedirects:
    # Read the current fileName attribute to identify which log this is
    fileName = AdminConfig.showAttribute(redirect, 'fileName')
    print("Configuring stream: " + str(fileName))

    # Set rotation: 50 MB max file size, keep 3 historical files
    AdminConfig.modify(redirect, [
        ['maxNumberOfBackupFiles', '3'],
        ['rolloverSize',           '50']
    ])
    print("  -> maxNumberOfBackupFiles=3, rolloverSize=50 MB applied.")

# ── Step 3: Configure Diagnostic Trace level ──
# TraceService controls what gets written to trace.log
traceService = AdminConfig.list('TraceService', server)
print("TraceService ID: " + traceService)

AdminConfig.modify(traceService, [
    ['startupTraceSpecification', '*=info'],
    ['traceOutputType',           'SPECIFIED_FILE'],
    ['maxNumberOfBackupFiles',    '3'],
    ['rolloverSize',              '50']
])
print("TraceService -> startupTraceSpecification=*=info, rotation configured.")

# ── Step 4: Save the configuration to disk ──
# Without this save, changes exist only in memory and are lost on restart.
AdminConfig.save()
print("Configuration saved to master repository.")

# ── Step 5: Apply trace level change immediately on running server ──
# AdminControl interacts with the live running server (unlike AdminConfig
# which edits stored configuration). This sets *=info right now without
# requiring a server restart.
try:
    traceAdmin = AdminControl.queryNames('type=TraceService,*')
    AdminControl.invoke(traceAdmin, 'setTraceSpecification', '*=info')
    print("Runtime trace level set to *=info immediately.")
except Exception as e:
    print("Note: Runtime trace update skipped (server may not be running): "
          + str(e))

print("=== Logging configuration complete. ===")
```
### Execute the wasadmin Script
Goto the Folder 
```
/apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/bin/
```
Execute the Script
```
./wsadmin.sh -lang jython \
    -username wasadmin \
    -password <YourPassword> \
    -f /tmp/v1_set_logging.py
```