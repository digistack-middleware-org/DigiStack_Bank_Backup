# Full End-to-End Flow Test
Check the URL Working or Not
```
curl -s -o /dev/null -w "%{http_code}" \
  http://192.168.10.10:9080/digistack-bank/Home
```
Expected result ==> 200

```
curl -s -o /dev/null -w "%{http_code}" \
  http://192.168.10.10:9080/digistack-bank/Login
```
Expected result ==> 200

## Flow 1 — Unauthenticated access guard
Step 5.1 — Open a fresh browser tab and navigate directly to:
```
http://192.168.10.10:9080/digistack-bank/Dashboard
```
Expected result — you are redirected to:
```
http://192.168.10.10:9080/digistack-bank/Login
```
✅ Achieved ==> The login page appears. You are NOT shown the Dashboard. The session guard is working.

## Flow 2 — Home page Login button
Step 5.2 — Navigate to:
```
http://192.168.10.10:9080/digistack-bank/Home
```
Expected result — the branded Home page loads with the navy/gold design.

Step 5.3 — Click the Login to NetBanking button 

Expected result — you are taken to:
```
http://192.168.10.10:9080/digistack-bank/Login
```
✅ Achieved ==> The Login page appears with the Welcome Back card.

# NetBanking Portal
## Flow 3 — Wrong password rejected
Step 5.4 — On the Login page enter:
```
Username: customer1
Password: wrongpassword
```
Click Sign In.

Expected result — the Login page reloads with the red error box showing:
```
Invalid username or password. Please try again.
```
The URL stays at /Login — you are not redirected anywhere.

## Flow 4 — Correct login, session created, Dashboard rendered
Step 5.5 — On the Login page enter:
```
Username: customer1
Password: Customer@123
```
Click Sign In.

✅ Achieved ==> Expected result — you are redirected to:
```
http://192.168.10.10:9080/digistack-bank/Dashboard
```
The Dashboard loads showing:
```
Navbar: customer1 username in the top right
Last login bar: Last login: First login (because this is the first login for this user)
Greeting: Good Morning/Afternoon/Evening, peta Venkatesh! 👋
Account card with Ravi Kumar as account name
Role shown as Customer in the right panel
Email shown as peta.venkatesh@digistack.cloud
All Quick Action tiles show as coming soon (disabled)
```
### Confirm user Login at Websphere server Level in SystemOut.log
```
grep -E "LoginServlet" /apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/logs/server1/SystemOut.log | tail -10
```
Expected result — you should see log lines for each login you performed:
```
LoginServlet: Login successful for user: customer1 role: CUSTOMER
```
## Flow 5 — Logout clears the session
Step 5.8 — Click the Logout button in the top right navbar.

✅ Achieved ==> Expected result — you are redirected to:
```
http://192.168.10.10:9080/digistack-bank/Home
```
Press the browser Back button.

Expected result — the Dashboard does NOT reappear. Either:
```
1. The browser shows the cached Dashboard visually, but if you refresh (F5) you are redirected to /Login
2. you are immediately redirected to /Login
```
✅ Achieved ==> Either outcome is correct — the session no longer exists.

Try navigating directly to:
```
http://192.168.10.10:9080/digistack-bank/Dashboard
```
✅ Achieved ==> Expected result — redirected to /Login. Session is fully destroyed.

### Confirm user Login at Websphere server Level in SystemOut.log
```
grep -E "LoginServlet|LogoutServlet" /apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/logs/server1/SystemOut.log | tail -10
```
Expected result — you should see log lines for each login you performed:
```
LoginServlet: Login successful for user: customer1 role: CUSTOMER
LogoutServlet: Session invalidated for user: customer1
```
## Flow 6 — Second login shows real last login timestamp
Step 5.11 — Log in again as customer1 with Customer@123.

✅ Achieved ==> Expected result — the Dashboard now shows in the last login bar:

Last login:  29 Aug 2026, 10:45 AM

This confirms the full last-login mechanism works:
```
First login → DB had null → showed "First login" → set last_login = NOW()
Second login → DB had the first login's timestamp → showed it → updated to NOW() again
```
# Admin portal 
## Flow 7 — Admin login
Log in as:
```
Username: admin1
Password: Admin@123
```
Expected result — Dashboard loads showing:
```
Navbar: admin1 with [Admin] label in gold
Greeting: Good Morning/Afternoon/Evening, Admin User! 👋
Role panel shows: Administrator with the gold shield icon
```
### Confirm user Login at Websphere server Level in SystemOut.log
```
grep -E "LoginServlet" /apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/logs/server1/SystemOut.log | tail -10
```
Expected result — you should see log lines for each login you performed:
```
LoginServlet: Login successful for user: customer1 role: CUSTOMER
LogoutServlet: Session invalidated for user: customer1
LoginServlet: Login successful for user: customer1 role: CUSTOMER
LogoutServlet: Session invalidated for user: customer1
LoginServlet: Login successful for user: admin1 role: ADMINISTRATOR
LogoutServlet: Session invalidated for user: admin1
```
