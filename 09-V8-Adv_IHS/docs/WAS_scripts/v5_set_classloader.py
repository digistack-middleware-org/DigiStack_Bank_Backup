# =============================================================
# Script  : v3_set_classloader.py
# Version : P01 v3
# Purpose : Configure ClassLoader policy for the DigiStack Bank
#           application in WAS.
#           Sets PARENT_FIRST class loader order and SINGLE
#           WAR class loader policy.
#           Equivalent to the Admin Console steps in Sprint 3.
#
# Run from dsb-dmgr VM:
#   cd /opt/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/bin/
#   ./wsadmin.sh -lang jython -username wasadmin -password <pwd> \
#       -f /tmp/v3_set_classloader.py
#
# Note: Run this AFTER deploying the v3 EAR so the application
#       name digistack-bank-v3 exists in the configuration.
# =============================================================

import sys

print("=== DigiStack Bank v3 — ClassLoader Configuration Script ===")

# ── Application name to configure ──
# Update this value at each version when the EAR name changes.
APP_NAME = 'digistack-bank-v5'

# ── Step 1: Locate the application's deployment object ──
# AdminConfig.getid() returns the WAS internal ID string for a
# named configuration object. The format is a XPath-like path.
appDeployment = AdminConfig.getid(
    '/Deployment:' + APP_NAME + '/')

if not appDeployment:
    print("ERROR: Application '" + APP_NAME +
          "' not found in configuration.")
    print("       Ensure the v3 EAR is deployed before running" +
          " this script.")
    sys.exit(1)

print("Found deployment: " + appDeployment)

# ── Step 2: Get the ApplicationConfig object ──
# ApplicationConfig holds the ClassLoader settings for the EAR.
appConfig = AdminConfig.showAttribute(
    appDeployment, 'config')

if not appConfig:
    print("ERROR: Could not retrieve ApplicationConfig " +
          "for " + APP_NAME)
    sys.exit(1)

print("ApplicationConfig: " + appConfig)

# ── Step 3: Get the ClassLoader object ──
# The ClassLoader object is nested inside ApplicationConfig.
classLoader = AdminConfig.showAttribute(appConfig, 'classloader')

if not classLoader:
    print("ERROR: No ClassLoader found in ApplicationConfig.")
    sys.exit(1)

print("ClassLoader object: " + classLoader)

# ── Step 4: Configure ClassLoader order ──
# PARENT_FIRST = WAS classes loaded before application classes.
# This is the correct setting when relying on WAS-provided
# resources (lib/ext/, JDBC drivers, JNDI DataSources).
AdminConfig.modify(classLoader, [
    ['mode', 'PARENT_FIRST']
])
print("ClassLoader mode set to: PARENT_FIRST")

# ── Step 5: Configure WAR ClassLoader policy ──
# SINGLE = all WAR modules in this EAR share one ClassLoader.
# At v3 with one WAR this makes no practical difference, but
# establishing the explicit setting now is correct practice.
AdminConfig.modify(appConfig, [
    ['warClassLoaderPolicy', 'SINGLE']
])
print("WAR ClassLoader policy set to: SINGLE")

# ── Step 6: Save the configuration ──
AdminConfig.save()
print("Configuration saved.")

print("=== ClassLoader configuration complete. ===")
print("Restart the application or server to apply changes.")