# When we Do the Smoke Test
We Do the Smoke test after Deploy into the New Environment

Smoke test again when:

    Server restarted / crashed and recovered
    Database patched or migrated
    Configuration changed (JNDI datasource, connection pool, JVM settings)
    Middleware upgraded (e.g., WebLogic patch)


# Smoke Test Checklist – digistack-bank-v3

| # | Check | How | Expected Result |
|---|---|---|---|
| 1 | Admin Console reachable | Browser → port 9060 | Login page loads |
| 2 | server1 Started | Admin Console → Servers | Green arrow ▶ |
| 3 | digistack-bank-v4 Started | Admin Console → Applications | Green arrow ▶ |
| 4 | ClassLoader correct | Applications → digistack-bank-v4 → Class loading | PARENT_FIRST + SINGLE |
| 5 | Home page loads | Browser → `/digistack-bank/Home` | Footer shows v4 label |
| 6 | Login succeeds | `customer1` / `Customer@123` | Dashboard renders |
| 7 | Deposit works | Account page → deposit ₹100 | Success banner, balance updated |
| 8 | Logout works | Click Logout | Redirected to Home |
