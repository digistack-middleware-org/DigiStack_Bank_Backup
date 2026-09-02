# Befor do Experiments Take Backup
## Take the Existing Application Backup
```
mkdir -p /opt/backups
/apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/bin/backupConfig.sh /opt/backups/v1-baseline-config.zip
ls -la /opt/backups/v1-baseline-config.zip
```

# Verification -1
Here we verify the Application content will fetch from DB or Not by Crashing the DB

## Stop the Database
01- stop the DB
```
sudo systemctl stop postgresql-16
```
02-Refresh the Browser
```
http://dsb-dmgr.digistack.cloud:9080/digistack-bank/
```
Expected result: Page still renders (doesn't crash/blank-screen), showing a red "DB Read Failed: ERROR" message with RED Colour.
## start the Database
01- start the DB
```
sudo systemctl start postgresql-16
```
02-Refresh the Browser
```
http://dsb-dmgr.digistack.cloud:9080/digistack-bank/
```
Expected result: Page now shows "DigiStack Bank - Connected" with Green Colour


