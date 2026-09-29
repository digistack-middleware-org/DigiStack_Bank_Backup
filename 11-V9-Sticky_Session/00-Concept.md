# Sticky Session Routing at the Plugin Layer

> [!NOTE]
> This guide explains how session affinity (sticky sessions) works in a WebSphere Application Server (WAS) cluster behind IBM HTTP Server (IHS), and how to configure, verify, and troubleshoot it.

---

## 1. Background — Why This Problem Exists

In a clustered environment, requests are distributed across multiple application server members. By default, IHS uses **round-robin** routing, which sends consecutive requests to different members.

### The Problem in Practice

| Real Bank | Your WAS Environment |
|---|---|
| Teller Alice | Cluster Member 1 |
| Teller Bob | Cluster Member 2 |
| Receptionist | IBM HTTP Server (IHS) |
| Customer folder | Session (in WAS memory) |
| Random routing | Round-robin (default) |

If a user's session is created on Member 1, but the next request is routed to Member 2, the user is effectively logged out:

```text
Login   → Member 1  ✅ Logged in (session created here)
Click 1 → Member 2  ❌ Session not found — user appears logged out
```

---

## 2. What Is a Session?

A **session** is a block of server-side memory created when a user logs in. It stores identity, account, and preference data for the duration of the user's interaction.

### The Locker Analogy

- The **session** = a locker at the gym.
- The **locker key** = a long random string called `JSESSIONID`.
- WAS provides the key to the browser as a **cookie**.
- On every request, the browser returns the key.
- WAS uses it to open the "locker" → the user stays logged in.

> [!IMPORTANT]
> The session exists **only on the server that created it**. Other members have no knowledge of it. This is why round-robin routing breaks logged-in state.

---

## 3. What Is a CloneID?

The **CloneID** is a suffix that WAS appends to the `JSESSIONID` value. It uniquely identifies which cluster member created the session.

### Structure

```text
JSESSIONID = ABC123XYZ.member1node01
             └───key───┘└────tag────┘

ABC123XYZ      = session key
.member1node01 = CloneID (Member 1's fingerprint)
```

### How Sticky Routing Works

1. First request → IHS picks a member (e.g., Member 1).
2. Member 1 creates the session and returns the cookie containing its CloneID.
3. Browser stores the cookie.
4. Next request → browser sends the cookie → IHS reads it.
5. IHS sees the CloneID `.member1node01` → routes to Member 1.
6. All subsequent requests go to the same member.

This behavior is called **session affinity** (sticky sessions).

---

## 4. Role of `plugin-cfg.xml`

IHS is a web server with no built-in knowledge of WAS members. WAS generates **`plugin-cfg.xml`**, which acts as IHS's routing instruction manual. It defines:

- Which cluster members exist (hostnames, IPs).
- Which ports each member listens on.
- **Which CloneID belongs to which member** ← critical for stickiness.

> [!WARNING]
> **No CloneID in `plugin-cfg.xml` = no stickiness.** If routing is not sticky, verify that the CloneID attribute appears on each `<Server>` entry in this file first.

---

## 5. Plugin Request Logging

By default, IHS does not log individual routing decisions. Enabling **plugin request logging** makes IHS write every routing decision to `http_plugin.log` — which server was chosen and why.

> [!TIP]
> You cannot fix what you cannot see. This log is your primary debugging and verification tool.

---

## 6. Configuration Procedure

**Goal:** Ensure all requests from one logged-in session are routed to the same member, and prove it with logs.

### Step 1 — Verify CloneIDs Exist

- Open `plugin-cfg.xml`.
- Confirm each `<Server>` entry has a `CloneID` attribute.
- Each member must have a **unique** CloneID.

### Step 2 — Ensure the Plugin Can Read the File

- `plugin-cfg.xml` must reside where the IHS plugin can read it.
- WAS normally regenerates it automatically on cluster topology changes.

### Step 3 — Enable Plugin Request Logging

- Configure the plugin to write to `http_plugin.log`.

### Step 4 — Restart and Test

- Restart IHS so the plugin reloads its configuration.
- Log in to the application (e.g., DigiStack Bank) from a browser.
- Submit five requests within the **same session**.

### Step 5 — Verify

Open `http_plugin.log` and inspect all five entries.

**Success criteria:**

```text
Request 1 → Member 1
Request 2 → Member 1
Request 3 → Member 1
Request 4 → Member 1
Request 5 → Member 1
```

All five requests routed to the same member = sticky sessions working.

---

## 7. Troubleshooting — Common Mistakes

| Mistake | Consequence | Fix |
|---|---|---|
| Testing with a fresh browser session each time | No cookie → no CloneID → no stickiness | Test within one logged-in session |
| Stale `plugin-cfg.xml` | Missing or incorrect CloneIDs | Regenerate the plugin config after cluster changes |
| Browser caching an old cookie | Routing to a retired member | Clear cookies or use a private window |
| Assuming stickiness is broken | Wasted debugging effort | Check `http_plugin.log` first |
| Forgetting to restart IHS | Config changes not applied | Restart/reload IHS after any plugin config change |

---

## 8. Quick Reference — The 3-2-1 of Sticky Sessions

- **3 players:** Browser, IHS, WAS members.
- **2 artifacts:** `JSESSIONID` cookie (with CloneID) and `plugin-cfg.xml`.
- **1 rule:** Same CloneID → same member, every time.

---

## 9. Summary

- A **session** is server-side memory that remembers the user.
- **`JSESSIONID`** is the key to that memory, held by the browser as a cookie.
- **CloneID** is a suffix in the `JSESSIONID` identifying the member that owns the session.
- **`plugin-cfg.xml`** maps CloneIDs to members for IHS.
- IHS reads the CloneID and routes to the same member → **session affinity**.
- **`http_plugin.log`** proves routing behavior, request by request.

---

## 10. Next Steps

Sticky sessions are the foundation for advanced session management strategies:

| Sprint Topic | Description |
|---|---|
| Memory-to-memory replication | Members share session data; either can serve the user |
| Database-backed sessions | Sessions persisted in PostgreSQL; survive a full member restart |

> [!TIP]
> Every session management strategy relies on the same `JSESSIONID`/CloneID plumbing. Master sticky routing first — replication builds directly on it.
