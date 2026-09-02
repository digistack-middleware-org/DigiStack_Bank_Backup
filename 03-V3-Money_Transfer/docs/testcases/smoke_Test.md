# When we Do the Smoke Test
We Do the Smoke test after Deploy into the New Environment

Smoke test again when:

    Server restarted / crashed and recovered
    Database patched or migrated
    Configuration changed (JNDI datasource, connection pool, JVM settings)
    Middleware upgraded (e.g., WebLogic patch)


# Smoke Test Checklist – digistack-bank-v3
| # | Check | How | Expected Result |
|---|-------|-----|-----------------|
| 1 | Admin Console reachable | Browser → port 9060 | Login page loads |
| 2 | server1 Started | Admin Console → Servers | Green arrow |
| 3 | digistack-bank-v3 Started | Admin Console → Applications | Green arrow |
| 4 | Home page loads | Browser → /digistack-bank/Home | Database: Connected green |
| 5 | Login succeeds | customer1 / Customer@123 | Dashboard renders |
| 6 | Dashboard account card | Observe after login | SAVINGS + masked number shown |
| 7 | Deposit works | Account page → deposit ₹500 | Success banner, balance updated |
| 8 | Withdraw works | Account page → withdraw ₹500 | Success banner, balance unchanged net |
| 9 | Logout works | Click Logout | Redirected to Home |
