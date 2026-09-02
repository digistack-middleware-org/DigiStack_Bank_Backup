# When we Do the Smoke Test
We Do the Smoke test after Deploy into the New Environment

Smoke test again when:

    Server restarted / crashed and recovered
    Database patched or migrated
    Configuration changed (JNDI datasource, connection pool, JVM settings)
    Middleware upgraded (e.g., WebLogic patch)


# Smoke Test Checklist – digistack-bank-v4.5

| # | Check | How | Expected Result |
|---|---|---|---|
| 1 | Admin Console reachable | Browser → port 9060 | Login page loads |
| 2 | server1 Started | Admin Console → Servers | Green arrow |
| 3 | digistack-bank-v4 Started | Admin Console → Applications | Green arrow |
| 4 | webserver1 defined | Admin Console → Servers → Web Servers | webserver1 listed |
| 5 | IHS process running | dsb-ihs → `ps aux \| grep httpd` | Process present |
| 6 | Home page via IHS | Browser → `http://192.168.10.20/digistack-bank/Home` | Footer shows v4 label, DB Connected green |
| 7 | Login via IHS | customer1 / Customer@123 via IHS URL | Dashboard renders |
| 8 | Deposit via IHS | Account page → deposit ₹100 | Success banner, balance updated |
| 9 | IHS stop proves front door | Stop IHS → port 80 fails → WAS port 9080 still works → restart IHS | Confirmed per TC-v4.5-14 |

