# When we Do the Smoke Test
We Do the Smoke test after Deploy into the New Environment

Smoke test again when:

    Server restarted / crashed and recovered
    Database patched or migrated
    Configuration changed (JNDI datasource, connection pool, JVM settings)
    Middleware upgraded (e.g., WebLogic patch)


# Smoke Test Checklist – digistack-bank-v4.5

| # | Check | Expected | Result |
|---|-------|----------|--------|
| 1 | DMgr Admin Console reachable | Loads | ⬜ |
| 2 | Both nodes Synchronized | System Admin → Nodes | ⬜ |
| 3 | Both Node Agents Started | Green arrows | ⬜ |
| 4 | Cluster Started | Both members green | ⬜ |
| 5 | digistack-bank-v5 deployed to cluster | Manage Modules shows `cluster=` | ⬜ |
| 6 | Node1 direct access | v5 footer, DB Connected | ⬜ |
| 7 | Node2 direct access | v5 footer, identical | ⬜ |
| 8 | Login → Dashboard → Deposit | Works, balance updates | ⬜ |
| 9 | Session replication confirmed | Distributed env settings = memory-to-memory | ⬜ |

**Legend:** ⬜ = Pending | ✅ = Passed | ❌ = Failed 

