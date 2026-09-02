# 🧪 Test Verification Matrix — by Stage

## 🔧 DEV — "Build it right"

| # | Stage | Check | One-line Meaning | Result | Evidence |
|---|---|---|---|---|---|
| 1 | DEV | 🧩 Unit/Component | Test each piece of code separately to confirm it works on its own. | ☐ | |
| 2 | DEV | 👨‍💻 Developer Tests | The developer who wrote the code checks it themselves before handing over. | ☐ | |
| 3 | DEV | 🔍 Code Quality / Security | Automated scan to make sure code is clean, standard-compliant, and has no vulnerabilities. | ☐ | |

## 🧪 SIT — "Make them work together"

| # | Stage | Check | One-line Meaning | Result | Evidence |
|---|---|---|---|---|---|
| 4 | SIT | 🔌 API | Verify every API request/response matches the agreed contract. | ☐ | |
| 5 | SIT | 🔗 Integration | Confirm different modules (app, services, systems) talk to each other correctly. | ☐ | |
| 6 | SIT | 🗄️ Database | Check data is saved, updated, and retrieved correctly from the DB. | ☐ | |
| 7 | SIT | ⚙️ Middleware | Verify the WebSphere server layer (deployment, connections) works fine. | ☐ | |
| 8 | SIT | 🌐 End-to-End | Run the full user flow start-to-finish across all layers. | ☐ | |
| 9 | SIT | ⚠️ Negative | Feed wrong/bad inputs and confirm the app handles them gracefully. | ☐ | |
| 10 | SIT | 🔁 Regression Pack | Re-run old test cases to make sure new changes broke nothing. | ☐ | |

## 👥 UAT — "Business says yes"

| # | Stage | Check | One-line Meaning | Result | Evidence |
|---|---|---|---|---|---|
| 11 | UAT | 🏦 Business Process | Test real business workflows (e.g., open account → deposit → withdraw). | ☐ | |
| 12 | UAT | 🚶 Customer Journey | Follow the actual user path: Login → Dashboard → Logout. | ☐ | |
| 13 | UAT | 💰 Financial/Accounting | Confirm all money calculations, balances, and transactions are accurate. | ☐ | |
| 14 | UAT | ✅ Business Acceptance | Business users review everything and give official sign-off. | ☐ | |

## 🎬 PRE-PROD — "Dress rehearsal"

| # | Stage | Check | One-line Meaning | Result | Evidence |
|---|---|---|---|---|---|
| 15 | PRE-PROD | 💨 Prod-like Smoke | Quick health check on the exact replica of production. | ☐ | |
| 16 | PRE-PROD | 📈 Performance | Load/stress test to confirm speed and stability under traffic. | ☐ | |
| 17 | PRE-PROD | 🔒 Security | Pen-testing and vulnerability scans on the full environment. | ☐ | |
| 18 | PRE-PROD | 🆘 DR/Recovery | Prove backups restore and the system recovers from failure. | ☐ | |
| 19 | PRE-PROD | 📋 Operational Readiness | Confirm runbooks, logs, alerts, and monitoring are ready for the ops team. | ☐ | |
| 20 | PRE-PROD | 🚀 Deployment/Rollback | Prove the deploy works — and that we can safely undo it. | ☐ | |

## 🌍 PROD — "Live but careful"

| # | Stage | Check | One-line Meaning | Result | Evidence |
|---|---|---|---|---|---|
| 21 | PROD | 💨 Smoke | Quick check after go-live: app up? DB connected? | ☐ | |
| 22 | PROD | 🩺 Sanity | Test core features quickly to confirm nothing major is broken. | ☐ | |
| 23 | PROD | 📊 Monitoring | Verify dashboards, logs, and alerts are actually capturing live data. | ☐ | |
| 24 | PROD | 🏦 Business Validation | Do one real business transaction and confirm it works in production. | ☐ | |

---
