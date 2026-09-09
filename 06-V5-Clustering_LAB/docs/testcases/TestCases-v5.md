# TestCases-v5.md
# DigiStack Bank — P01 Version 5 — WAS Clustering
# Test Execution Date: ___________  Executed By: ___________
# EAR Deployed: digistack-bank-v5.ear (to devdsbinappcluster01)

---

## Sign-Off Gate (TCS01 §2.7)
| Gate | Condition | Met? |
|---|---|---|
| 1 | All Critical: Pass | |
| 2 | All High: Pass | |
| 3 | No open Critical/High defects | |
| 4 | Regression Pack (v1-v4.5) re-run and passing | |
| 5 | Reviewer + date recorded | |
| 6 | SetupDoc-v5.md complete | |
| 7 | backupConfig captured | |
| 8 | Smoke test passes | |

Reviewer: _________  Approved Date: _________

---

## Regression Pack — v1 through v4.5 (condensed reference)
Re-run all Critical + High cases from TestCases-v1.md, v2.md, v3.md,
v4.md, v4.5.md (57 Critical+High cases total) against the cluster —
via `http://192.168.10.10:9080/...` (Node1) AND
`http://192.168.10.11:<port>/...` (Node2) at least once each, to
confirm both members serve the app identically. Record pass/fail
per-case in those files' own tables; this file only tracks the
aggregate count below.

| Regression Source | Critical+High Count | Status |
|---|---|---|
| v1 | 13 | |
| v2 | 10 | |
| v3 | 13 | |
| v4 | 8 | |
| v4.5 | 19 | |
| **Total** | **63** | |

---

## v5 Smoke Test
| Check | Expected | Status |
|---|---|---|
| DMgr Admin Console reachable | Loads | |
| Both nodes Synchronized | System Admin → Nodes | |
| Both Node Agents Started | System Admin → Node agents | |
| Cluster Started | Both members green | |
| digistack-bank-v5 Started | Manage Modules shows cluster= target | |
| Node1 direct access | v5 footer loads | |
| Node2 direct access | v5 footer loads, identical | |
| Login/Dashboard/Deposit | Works on both nodes | |

---

## v5 Test Cases

**TC-v5-01** | Critical | Functional
DMgr profile created and started correctly.
Steps: `serverStatus.sh dmgr` from DMgr profile bin, confirm Admin Console loads at DMgr port, cell name `devdsbincell01` visible.
Expected: DMgr STARTED, console loads, cell name correct.

**TC-v5-02** | Critical | Integration
Both nodes federated and synchronized.
Steps: System Administration → Nodes; confirm devdsbinnode01 and devdsbinnode02 both listed, status Synchronized.
Expected: Both nodes present, Synchronized status.

**TC-v5-03** | Critical | Functional
Both Node Agents running.
Steps: System Administration → Node agents.
Expected: Both node agents green arrow Started.

**TC-v5-04** | Critical | Functional
Cluster devdsbinappcluster01 exists with exactly 2 members.
Steps: Servers → Clusters → devdsbinappcluster01 → Cluster members tab.
Expected: 2 members listed — server1 on devdsbinnode01, server1 on devdsbinnode02.

**TC-v5-05** | Critical | Functional
Cluster starts successfully — both members reach STARTED state.
Steps: Start cluster via Admin Console; wait; check Servers → WebSphere application servers.
Expected: Both entries show green arrow.

**TC-v5-06** | Critical | Functional
digistack-bank-v5 deployed to the CLUSTER, not individual servers.
Steps: Applications → digistack-bank-v5 → Manage Modules.
Expected: Server column shows `cell=devdsbincell01,cluster=devdsbinappcluster01`.

**TC-v5-07** | Critical | Functional
Application reachable and functionally identical on both cluster members.
Steps: Load Home page via Node1 port and Node2 port separately; compare footer, DB status, page content.
Expected: Both identical, both show `v5 — WAS Clustering`, both `Database: Connected`.

**TC-v5-08** | Critical | Functional
Memory-to-memory session replication configured.
Steps: Clusters → devdsbinappcluster01 → Session management → Distributed environment settings.
Expected: Memory-to-memory replication selected, mode = Both client and server.

**TC-v5-09** | Critical | Functional
**Failover test — session survives cluster member failure mid-transaction.**
Steps: Log in on Node1; open Account page with deposit amount entered but not submitted; stop server1 on devdsbinnode01; submit the deposit.
Expected: Deposit succeeds (or session survives with balance correctly updated on retry) — user is NOT returned to Login page. DB balance reflects the deposit. SystemOut.log on devdsbinnode02 shows the transaction was processed there.

**TC-v5-10** | Critical | Integration
Failed cluster member restarts and rejoins cleanly.
Steps: After TC-v5-09, run startServer.sh for the killed member; confirm Admin Console shows both members Started again.
Expected: Both members green; cluster fully restored.

**TC-v5-11** | High | Functional
ClassLoader PARENT_FIRST + SINGLE confirmed on the cluster-deployed application.
Steps: Applications → digistack-bank-v5 → Class loading and update detection.
Expected: PARENT_FIRST and Single class loader for application both confirmed.

**TC-v5-12** | High | Integration
v5_deploy.py successfully uninstalls old app and installs new app to the cluster.
Steps: Run v5_deploy.py from DMgr profile bin against a test scenario; observe output.
Expected: Output confirms cluster-target deployment string used, both members show STARTED post-deploy.

**TC-v5-13** | High | Functional
Ripple Start restarts cluster members one at a time without full outage.
Steps: While polling the Home page every few seconds from a script or manual refresh, trigger Ripple Start on the cluster; observe availability during the restart window.
Expected: At least one member remains reachable throughout — no complete outage window during the ripple restart.

**TC-v5-14** | High | Integration
Post-failover deposit succeeds normally on the recovered cluster.
Steps: After TC-v5-10, log in again, perform a ₹100 deposit.
Expected: Success banner, balance updates correctly, no residual session/routing issues.

**TC-v5-15** | Medium | Functional
Both cluster members' HTTP ports documented and distinct.
Steps: Check WC_defaulthost port for each member under Ports.
Expected: Node1 and Node2 have distinct, correctly noted ports (typically 9080 and 9081).

**TC-v5-16** | Medium | Integration
Node synchronization script (v5_sync_nodes.py) reports both nodes successfully synced.
Steps: Run script from DMgr profile bin.
Expected: Both node names printed with "Synchronized successfully."

**TC-v5-17** | Low | Functional
Cluster status visible correctly in Admin Console cluster list view (not just member list).
Steps: Servers → Clusters → WebSphere application server clusters.
Expected: devdsbinappcluster01 listed with correct overall status reflecting member states.

---

## Defect Log
| Defect ID | TC Ref | Priority | Description | Status | Resolution |
|---|---|---|---|---|---|

## Test Summary
| Priority | Total | Pass | Fail | Blocked |
|---|---|---|---|---|
| Critical | 10 | | | |
| High | 4 | | | |
| Medium | 2 | | | |
| Low | 1 | | | |
| **v5 Subtotal** | **17** | | | |
| **Regression (v1-v4.5)** | **63** | | | |
| **Grand Total** | **80** | | | |

## Regression Pack — Forward Reference (new cases added by v5)
| TC ID | Description | Priority |
|---|---|---|
| TC-v5-04 | Cluster has exactly 2 members | Critical |
| TC-v5-06 | App deployed to cluster, not server | Critical |
| TC-v5-08 | Memory-to-memory replication configured | Critical |
| TC-v5-09 | Failover — session survives member failure | Critical |