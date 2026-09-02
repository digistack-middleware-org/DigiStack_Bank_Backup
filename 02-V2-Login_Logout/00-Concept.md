# What we Achieve From these Version
```
1. Open /Login                → Login page shows
2. Try Dashboard without login → guard redirects to Login ✅ 
3. Enter WRONG password        → "Invalid credentials" error ✅ 
4. Enter correct credentials   → session created, redirect to Dashboard
5. Dashboard shows greeting    → "Welcome, Venkatesh!" ✅
6. Dashboard shows last login  → "Previous login"  or timestamp
7. Log out                     → session destroyed, redirected to Home ✅
8. Press Back to Dashboard     → guard catches it → back to Login ✅
```

# Backend Side Concepts 
## 🧠 Concept 1 — HTTP Session (How the Server "Remembers" You)

The Problem 
```
Here's a strange but important fact about the web: HTTP has no memory.

Every time your browser asks a server for a page, the server treats it as a brand-new stranger. So how does the server know "this is Venkatesh  — he already logged in" when he clicks from the home page to his account balance?

Without a fix, the user would have to log in again on every single page. Terrible.
```

The Solution - "Sessions"
A session is like a hand stamp at a concert:
```
At the entrance (login), security checks your ticket (credentials)
If valid, they stamp your hand (create a session + give you the JSESSIONID cookie)
Every time you re-enter the hall (make a new request), they just glance at your stamp (the cookie) instead of re-checking your ticket
When you leave for good (logout / session expires), the stamp is invalid
```
When you log in successfully, this happens internally 
```
1. You submit correct username + password
2. WebSphere (the server) creates a "session" — a small block of memory
   that says: "This is Ravi, he's logged in, he's a CUSTOMER"
3. That session gets a unique ID, e.g.  A7X9K2...
4. The server sends that ID to your browser as a cookie called JSESSIONID
5. Your browser stores the cookie
```
Then, on every following request:

```
Browser: "Show me my balance"  +  cookie JSESSIONID=A7X9K2...
Server:  *looks up A7X9K2* → "Ah, that's Ravi's session — he's logged in. Here's his balance."
```
Key Points to Remember
```
Session object    ==> 	Server-side memory holding your login info
Session ID 	      ==>   The unique name-tag for that memory block
JSESSIONID cookie ==>  	The ID sent to your browser so it can prove who it is
Why it matters 	==>   This is how login works across multiple pages
```
# 🔒 Concept 2 — Why Login Forms Use POST (Not GET)
The Two Ways a Browser Sends Data to a Server
GET — data is attached to the URL:
```
https://bank.com/login?username=ravi&password=MySecret123
```

POST — data is sent inside the request body, hidden from the URL:
```
httpsbank.com/login
(hidden in the request: username=ravi&password=MySecret123)
```

### Why Passwords Must NEVER Use GET
If the login form used GET, the password would end up:
```
🚫 In the browser address bar — visible to anyone looking at your screen, and saved in browser history
🚫 In server access logs — every URL is typically logged, so MySecret123 would sit in a log file forever, readable by anyone with server access
🚫 In browser cache and bookmarks — the full password-URL could be saved or shared accidentally
🚫 In proxy/firewall logs — any device sitting between you and the server can record full URLs
```
What Backend we Build in these Version
```
1. User opens Login.jsp
        ↓
2. Types username + password, clicks Login
   (data sent via POST — never in the URL)
        ↓
3. LoginServlet.doPost() receives the data
        ↓
4. Looks up the user in the users table (USER table!)
        ↓
5. Uses PasswordUtil.verify():
   hash(storedSalt + typedPassword) == storedHash ?
        ↓
   ┌────┴─────┐
   ✅ Match     ❌ No match
   ↓              ↓
6. Create      Show error message
   session        "Invalid credentials"
   (JSESSIONID    back on Login.jsp
   cookie sent)
   ↓
7. Redirect to home/account page — server now "remembers" the user on every request via the cookie
```

# UI Side Concepts
## 🛡️ Concept 1 — Session Guard 
The Problem
```
Here's the thing: a user doesn't have to use your login page to reach a URL. 
Anyone can simply type the address directly into the browser: "http://yourserver:9080/Dashboard"
No login. No password. 
If the dashboard page would just render itself, this stranger would see the dashboard as if they were logged in. Security hole!
```
The Solution — The Guard
```
when a user logs in, Backend WebSphere creates a session — server-side memory holding who they are.
```

```
Browser: "Show me /Dashboard"
    ↓
DashboardServlet: "Hmm... do you have a valid session?"
    ↓
┌─────────────────────┬──────────────────────────────┐
│ ✅ Session exists    │ ❌ No session (never logged │
│    (user logged in)  │    in, or session expired)   │
│                      │                              │
│ → Show the           │ → Redirect to /Login         │
│   Dashboard          │   "Sorry, please log in      │
│                      │    first"                    │
└─────────────────────┴──────────────────────────────┘

```
# 🔁 Concept 2 — POST/Redirect/GET (The Back-Button Trick)
The Problem
```
After a successful login, LoginServlet needs to send the user to the Dashboard.
```
The Soulution
 There are two ways to do that:
```
Option A — Forward (the naive way):
      The server hands the request internally to the Dashboard.
                     The browser never knows — it still thinks it's on the login POST.

Option B — Redirect (the correct way):
       The server tells the browser: "Forget this — go make a fresh request to /Dashboard."
                        The browser then does a clean GET.
```
HERE Option A cause trouble --> why Because the browser still thinks the last action was a form submission (POST)
What Option-A will DO
```
1. User logs in → lands on Dashboard (via forward)
2. User presses the Back button
3. Browser says:
         ⚠️ "Confirm Form Resubmission — The page you're looking for
               used information you entered. Return to that form?"
4. If the user clicks OK, the browser submits the password form AGAIN. Confusing for the user, and re-submitting login forms is bad practice.
```
The Real Solution is - option B {Redirection} With POST/Redirect/GET:
```
1. Browser: POST /Login (submits username + password)
2. Server:  "Login OK! Redirecting you to /Dashboard"
3. Browser: GET /Dashboard   ← a fresh, clean request
```
Now the browser's last action is a simple GET, not a form submission. So:
```
✅ Back button → goes cleanly back to the login page (no scary warning)
✅ Refresh (F5) → just reloads the Dashboard, never re-submits the form
✅ No password ever gets re-sent accidentally
```
How Backend and UI All Fits Together
```
Sprint 2 (already built):
  User logs in → LoginServlet checks credentials → session created
        ↓
  LoginServlet REDIRECTS (not forwards) to /Dashboard   ← POST/Redirect/GET
        ↓
Sprint 3 (new):
  Browser: GET /Dashboard
        ↓
  DashboardServlet — THE GUARD:
    "Is there a valid session?"
        ↓
   ✅ Yes                          ❌ No
    ↓                               ↓
  Reads session attributes      Redirect to /Login
  (name, role, last login)      (user must log in first)
    ↓
  Dashboard.jsp renders:
   - "Welcome, Ravi!"  ← greeting from session
   - Last login time   ← from session/database
   - Account summary   ← placeholder for now
   - Quick action tiles ← buttons for future features
```
# 🚨 Very Big Problem HERE
```
User clicks "Log Out" → Browser shows some goodbye page. Done?
```
logging in created a session on the server AND a JSESSIONID cookie in the browser

When User Logot means doesn't destroy either of them:
```
🚨 The session object still lives in WebSphere's memory — still valid, still says "Ravi is logged in"
🚨 The cookie still exists in the browser — still carrying the session ID
```

Why This Is Dangerous
```
The session ID is like a key.
If someone copies that cookie {JessionID} — for example on a shared computer, or over an insecure network —
      they can send it to the server and reuse your session without ever knowing your password.
            Because your Session still alive in Backend Server

Attacker: "Here's Venkateh's JSESSIONID cookie"
Server:   "Ah, Venkatesh's session! You're logged in. Here's his account."
```
## The Solution — session.invalidate()

```
When the user clicks Log Out ==>  the LogoutServlet calls: ==> session.invalidate() 
```
 This destroys the session object on the server {WebSphere} immediately

The cookie {JessionID} still exists in the browser, but it's now worthless because the Server side Session will destroyed 

If any Hacker use this and Try to Login -> Backend Session Terminate -> Now it will redirect to Login Page
