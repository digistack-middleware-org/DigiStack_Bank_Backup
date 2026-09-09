# DigiStack Bank v6 — The Whole Version Explained Simply

## What v6 Is All About in One Sentence

**v6 turns DigiStack Bank from "an app that just runs" into an administrable banking system** — where an admin can freeze accounts, operate the system from the command line, and deploy new versions to a multi-machine cluster safely.

Every sprint added one layer of "operations maturity." Let's walk the whole story.

## How the Sprints Connect — The Hidden Thread

Each sprint depended on a choice from an earlier one:

```text
(sync/drift)   ──► guarantees both nodes stay identical,
                    which Sprint 5's cluster deploy relies on

(DB column)    ──► creates the data the whole feature orbits around

(fresh reads,  ──► is what makes Sprint 4's script work
service-layer)    instantly with NO restart

(wsadmin/JDBC) ──► proves the system can be operated
                        outside the app itself

(cluster deploy) ► packages all of it into a named,
                    traceable, two-machine release
```

---

## The Foundation Concept — One Cell, Many Machines

Everything in v6 sits on the **WAS ND (Network Deployment)** architecture:

```text
                    ┌─────────────────────┐
                    │   DMgr (dsb-dmgr)   │  ← the "brain"
                    │  Master repository  │    holds ONE true config
                    └─────────┬───────────┘
                 sync ↓              ↓ sync
        ┌────────────────┐    ┌────────────────┐
        │  node01        │    │  node02        │  ← Node Agents relay
        │  (local copy)  │    │  (local copy)  │    changes down
        │  member1       │    │  member2       │
        └────────────────┘    └────────────────┘
                 \                    /
                  ▼                  ▼
              ┌────────────────────────┐
              │  devdsbinappcluster01  │  ← both members act as ONE
              └────────────────────────┘
                          │
                          ▼
                  PostgreSQL (dsb-db)  ← the single source of truth for DATA
```

> **One config truth** lives on the DMgr. **One data truth** lives in PostgreSQL. Both nodes just carry copies. Almost everything in v6 flows from these two ideas.

---

## Node Sync & Drift ==> Keeping All Machines Identical 

### The problem

Two machines running your app means two copies of the configuration. What if they disagree?

### The concepts

- **Normal sync** = the DMgr pushes only its *changes* down to nodes. One-way: DMgr → node. It's the routine heartbeat.
- **Drift** = the copies diverge. The dangerous kind is **node-side drift**: someone SSHes onto a server and edits a config file by hand, bypassing the DMgr. The DMgr doesn't know — and normal sync can't fix it, because the DMgr has nothing new to push. The rogue edit just sits there.
- **Full resync** = the DMgr sends its **entire** config as a complete overwrite. This erases anything the DMgr doesn't know about — including rogue edits.

### The lesson

> "Synchronized ✓" in the console doesn't mean "identical." It means "I pushed my changes." **Only a full resync guarantees a node is an exact copy of the master.**

### Golden rule it teaches

> **Never edit WAS config files by hand on a node.** Always change config through the DMgr (console or wsadmin) and let sync distribute it.

---

## DB Schema Migration ==> Changing the Database Safely 

### The problem

v6 needs a new feature (freeze accounts), and that feature needs a new column,is_frozen`, in the `accounts` table. You can't just "edit the database" casually — production databases demand discipline.

### The concepts

- **Numbered migrations** (`V4__add_frozen_flag.sql`) — every schema change is a small, numbered, named file. The number tells the story of the database's history: V1 created the table, V4 adds the flag.
  - Note: migration numbers count **schema changes**, not app versions — which is why v6's first schema change is V4.
- **`NOT NULL DEFAULT FALSE`** — the smart part. Adding a column to a table full of existing rows could break things. The default means: every existing account automatically becomes "not frozen" the instant the column appears. **The change is backward-compatible by design** — nothing breaks, no row is left ambiguous.
- **Rollback written next to the migration** — every change ships with its escape hatch (`DROP COLUMN is_frozen`), even if you hope never to use it.
- **The migration file is committed to Git** — the database's shape is version-controlled just like the code. Code and schema evolve together, traceably.

---

## Business Logic + UI ==> Enforcing the Rule in the Right Place 

### The problem

A database flag is just data. It becomes a feature only when the application *respects* it.

### The concepts

- **Service-layer enforcement** — the Deposit and Withdraw operations check `is_frozen` before touching the balance. Crucially, this check lives in the **service layer**, not the UI. Why? Because the UI is just one doorway into the app. If the rule lived only in the buttons, any request that skipped the buttons (a script, a crafted HTTP call) would bypass it. **Rules enforced deep in the service layer apply to everyone.**
- **UI as a mirror, not a gate** — the dashboard *reflects* the state (FROZEN badge, greyed tiles) but isn't what *enforces* it. Good security principle: **show the user the state, enforce the rule underneath.**
- **No caching of the flag** — the app reads `is_frozen` fresh from the database on every relevant request. This design decision (made here) pays off enormously later.

---

## wsadmin + JDBC ==> Operating Without the Browser 

### The problem

An ops engineer at 2 AM gets: "Freeze this account NOW." No deployment, no browser — just a terminal.

### The concepts

- **The Admin Console can't help here** — it manages WAS *infrastructure* (servers, DataSources, deployments), not *application data* (rows in a table). `is_frozen` is a database record. Different world, different tool.
- **wsadmin is secretly a JVM** — and Jython running inside a JVM can use any Java class you put on its classpath. Attach the PostgreSQL JDBC driver JAR with `-wsadmin_classpath`, and suddenly a "WebSphere admin script" can open a direct SQL connection. Infrastructure tool meets database.
- **The script discipline: Check → Guard → Act → Verify → Close.**
  1. **Check** — the account exists and its current state
  2. **Guard** — refuse redundant work (don't freeze an already-frozen account)
  3. **Act** — `UPDATE`, then confirm exactly 1 row changed
  4. **Verify** — read the value back; prove it, don't assume
  5. **Close** — always close the connection
- **Bypassing the service layer has a cost** — the script skips the app's business rules, so it must carry its own minimal safety checks. **Power and responsibility travel together.**

### Why the UI updates instantly with no restart

> Because Sprint 3 designed the app to read the flag fresh from the database every time. **Good application design makes the system operable externally.** If the app had cached the flag, the script would change the DB and the running app would still behave as if nothing happened.

---

## Deploy to Cluster

### The problem

The feature is built and tested on one machine's dev. Now it must reach *both* cluster members as a clean, named, traceable release.

### The concepts

- **The name is decided at build time** — `pom.xml` (`<version>6.0</version>`, `<finalName>digistack-bank-v6</finalName>`) makes Maven produce `digistack-bank-v6.ear`. Distinct names per version = instant traceability; you always know exactly which code is running.
- **Cluster deploy is one action, not two** — point the install at the *cluster*, and WAS automatically: stores it in the master repository → pushes it to every node via sync → starts it on every member. You never deploy to member1 and member2 individually.
- **The replacement dance:** stop v5 → uninstall v5 → Save → install v6 → Save → start. One version live at a time, no context-root collisions. And remember: **WAS config changes are staged until you Save** — unsaved work evaporates.
- **Node sync = no drift between members** — both nodes end up with byte-identical v6, so every user gets identical behaviour regardless of which member serves them.
- **Smoke test as a version fingerprint** — the Freeze button exists *only* in v6. If login works and Freeze/Unfreeze work through IHS on port 80, that's proof v6 code is genuinely live — not v5, not partial.
- **`git tag v6-deployed`** — a permanent bookmark linking the servers' running code to the exact source commit. The running system and the repository now point at each other.

---

