# Troubleshooing Steps

1.1 Confirm app is otherwise healthy (rule out app-level issues)
```
curl -s -o /dev/null -w "%{http_code}" \
  http://192.168.10.10:9080/digistack-bank/Home
```
👉 Expected: 200 — confirms this is a logging/monitoring issue, NOT an application outage.

1.2 Confirm read failure as non-root
```
tail -5 \
  /apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/logs/server1/SystemOut.log
```
👉 Expected: Permission denied

1.3 Check file ownership & who's writing
```
stat \
  /apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/logs/server1/SystemOut.log
```