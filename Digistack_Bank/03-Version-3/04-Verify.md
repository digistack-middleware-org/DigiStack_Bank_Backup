# Verification -1
Here we verify the Application content will fetch from DB or Not by Crashing the DB

## Open the Browser
```
http://dsb-dmgr.digistack.cloud:9080/digistack-bank/login
```
Login with Username and Password
```
testuser 
Password123!
```
Expected on Home page ==>  A green box saying "Logged in as testuser" and "Last login: [some date/time]", plus a Logout button

#### Logout from the Session

Expected ==> Redirects to the Login page

## verification-2 ==> Account Deposit and WithDraw
##### 1. On Home page,
```
click My Account confirm it shows Current Balance: ₹1000.00
````
confirm it shows Current Balance: ₹1000.00
##### Amount Deposit
```
Enter 200 in Amount, click Deposit
```
confirm balance updates to ₹1200.00 and a success message shows

##### Amount Withdraw {High Amount}
```
Enter 5000 in Amount, click Withdraw
```
confirm it's rejected (more than balance) with a clear failure message, balance stays ₹1200.00

##### Amount Withdraw 
```
Enter 300 in Amount, click Withdraw
```
confirm it succeeds, balance becomes ₹900.00

## verification-3 ==> Direct to Hompage

##### Now try going directly to Homepage 
```
http://dsb-dmgr.digistack.cloud:9080/digistack-bank/home
```
Expected ==> Home page loads, but the green "Logged in as..." box is gone — replaced by a plain "Login" button, since the session was destroyed

## Verification-4 ==> Observing Redeploy / Startup Behavior
Check the Logs
```
tail -f /apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/logs/server1/SystemOut.log
```

02- Stop the Application from Console and Check the Logs

```
tail -f /apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/logs/server1/SystemOut.log
```

03- start the Application from Console and Check the Logs

```
tail -f /apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/logs/server1/SystemOut.log
```
