# PostgreSQL JDBC Provider on WebSphere


---

## 🎯 The Big Picture

- Your bank app runs on **WebSphere (WAS)**.
- Your data lives in a **PostgreSQL database**.
- These two speak **different languages**.
- **The JDBC driver JAR is the translator between them.**
- Without it, WAS cannot talk to the database. Ever.

```
[WAS App] ←→ [postgresql-42.7.3.jar (translator)] ←→ [PostgreSQL DB]
```

**Today's job:** Put the translator on both servers, then tell WAS where it is.

---

## 📚 Five Words You Must Know

| Word | Simple meaning | Real-life example |
|------|----------------|-------------------|
| **JAR file** | A box containing Java code | A toolbox |
| **JDBC Driver** | The translator between app and database | A language interpreter |
| **JDBC Provider** | WAS's note saying: "Here's my translator and where it lives" | A contact card for the interpreter |
| **Cell scope** | One setting shared by ALL servers | One rule for the whole company |
| **Synchronize** | Copy config from boss server to worker servers | Head office sends memo to all branches |

---

## 🧠 The Golden Rule

> **WAS reads the JAR path on EACH server separately.**

- The **config** is shared. The **file** is NOT.
- So the JAR must exist at the **exact same path** on **both** nodes:
  - `dsb-dmgr` (192.168.10.10)
  - `dsb-node02` (192.168.10.11)

**Real-life example:** You tell both branches "the key is in the drawer at 123 Main Street." If branch has no drawer at that address — locked out. Same address, both buildings. Always.

---

## ✅ Step 1 — Check the JAR Exists on BOTH Nodes

**Do this first. Everything else fails without it.**

**On each server, run:**

```bash
find /apps/IBM/WebSphere/AppServer -name "postgresql*.jar" 2>/dev/null
```

**What you want to see (on BOTH):**

```
/apps/IBM/WebSphere/AppServer/lib/ext/jdbc/postgresql-42.7.3.jar
```

**If missing on node02? Copy it from dmgr:**

```bash
scp /apps/IBM/WebSphere/AppServer/lib/ext/jdbc/postgresql-42.7.3.jar \
    <user>@192.168.10.11:/apps/IBM/WebSphere/AppServer/lib/ext/jdbc/
```

**Then confirm it arrived:**

```bash
ls -lh /apps/IBM/WebSphere/AppServer/lib/ext/jdbc/postgresql-42.7.3.jar
```

**Pass = file exists, size is NOT zero.**

> 🧠 **Remember:** Check first, configure second. Test the water before you dive.

---

## 🖱️ Step 2 — Create the Provider (GUI Method)

### What is a JDBC Provider?

A **contact card** WAS keeps. It says:

- Name of the translator
- Which Java class to call
- Where the JAR file lives on disk

### Do it like this:

1. Open: `https://192.168.10.10:9043/ibm/console`
2. Left menu → **Resources → JDBC → JDBC Providers**
3. ⚠️ **Scope dropdown → set to Cell (`devdsbincell01`)**
4. Click **New**
5. Fill the 3 wizard screens (below)
6. **Finish → Save**

### The 3 wizard screens:

**Screen 1 — Database type:**

- Choose: **User-defined** ❗

**Screen 2 — Class Name:**

| Field | Value |
|-------|-------|
| Implementation class | `org.postgresql.ds.PGConnectionPoolDataSource` |
| Name | `PostgreSQL JDBC Provider` |
| Description | `PostgreSQL 16 JDBC Provider for DigiStack Bank. Driver: postgresql-42.7.3.jar. Added at P01 v7.` |

**Screen 3 — Classpath:**

| Field | Value |
|-------|-------|
| Classpath | `/apps/IBM/WebSphere/AppServer/lib/ext/jdbc/postgresql-42.7.3.jar` |
| Native path | *(leave empty)* |

### Two "Why?" moments:

**Why "User-defined" and not "PostgreSQL"?**

- WAS 9's dropdown list is **old**. PostgreSQL is not in it.
- So we define it ourselves. Like writing your own contact card because the phone book doesn't list the person.

**Why Cell scope?**

- Cell = **all servers inherit it**. One card, everyone reads it.
- Node/server scope = only ONE server gets the card. The other one breaks.
- Classic beginner mistake. Set Cell scope **first**, before clicking New.

**Why is the class name exact?**

- WAS will literally call `org.postgresql.ds.PGConnectionPoolDataSource` inside the JAR.
- One typo = WAS looks for a person who doesn't exist. Error.

**Why no native library path?**

- PostgreSQL driver is ** Java**. No special system files needed. Leave blank.

**Success looks like:** *"The JDBC provider PostgreSQL JDBC Provider was created."* Then click **Save**.

---

## ⌨️ Step 3 — Same Thing via wsadmin (Script Method)

**Why do it twice (GUI + script)?**

- Standing rule: **dual delivery**. GUI for humans to see, script for repeatability/automation.
- Same result, two roads.

**On dsb-dmgr:**

```bash
cd /apps/IBM/WebSphere/AppServer/profiles/devdsbindmgr01/bin
./wsadmin.sh -lang jython
```

**Then, in order — 5 small actions:**

**1. Find the cell name:**

```jython
cellName = AdminControl.getCell()
print cellName
```

**2. Get the cell's config ID** (its "home address"):

```jython
cellId = AdminConfig.getid('/Cell:' + cellName + '/')
print cellId
```

Ends in something like `|cell.xml#Cell_1` ✅

**3. Create the provider at Cell scope:**

```jython
jdbcAttrs = [
    ['name', 'PostgreSQL JDBC Provider'],
    ['description', 'PostgreSQL 16 JDBC Provider for DigiStack Bank. Driver: postgresql-42.7.3.jar. Added at P01 v7.'],
    ['implementationClassName', 'org.postgresql.ds.PGConnectionPoolDataSource'],
    ['classpath', '/apps/IBM/WebSphere/AppServer/lib/ext/jdbc/postgresql-42.7.3.jar']
]
jdbcProvider = AdminConfig.create('JDBCProvider', cellId, jdbcAttrs)
print jdbcProvider
```

**Success:** a config ID like `resources.xml#JDBCProvider_1`

**4. Save** (nothing is stored until you do this!):

```jython
AdminConfig.save()
```

**Success:** silent. No news = good news.

**5. Verify, then exit:**

```jython
print AdminConfig.list('JDBCProvider')
quit()
```

**Success:** your provider's name appears in the list.

>🧠 **Pattern to memorize:**
> **Find parent → create child → save → verify.**
> Forgetting `AdminConfig.save()` = you did nothing. Common mistake.

---

## 🔄 Step 4 — Synchronize the Nodes

**Why?**

- The provider was saved on the **boss server (DMgr)**. The worker nodes don't know yet.
- Sync = head office pushing the memo to all branches.

```
[DMgr master config] --sync--> [node01] and [node02]
```

**GUI way:**

- **System administration → Nodes** → select all → **Synchronize**
- **Success:** "Synchronization completed successfully."

**Script way:**

```jython
nodes = AdminConfig.list('Node').splitlines()
for node in nodes:
    nodeName = AdminConfig.showAttribute(node, 'name')
    if 'dmgr' not in nodeName.lower():
        syncResult = AdminControl.invoke(
            AdminControl.completeObjectName(
                'type=NodeSync,node=' + nodeName + ',*'),
            'sync')
        print 'Synced node: ' + nodeName + ' result: ' + str(syncResult)
```

> 🧠 **Note:** The script skips nodes with "dmgr" in the name — the boss doesn't need to send a memo to himself.

**Skip this step =** worker nodes never get the provider = apps fail later, and you'll waste hours hunting the cause.

---

## 🔍 Step 5 — Prove It Works (Check Logs)

**Why?** After sync, each node loads the provider. If the JAR is missing anywhere — **this is where the error shows up.**

**Check DMgr log:**

```bash
grep -i "postgresql\|classpath\|JDBC" \
  /apps/IBM/WebSphere/AppServer/profiles/devdsbindmgr01/logs/dmgr/SystemOut.log \
  | tail -20
```

**Check node02 log:**

```bash
grep -i "postgresql\|classpath\|JDBC" \
  /apps/IBM/WebSphere/AppServer/profiles/<node02-profile>/logs/nodeagent/SystemOut.log \
  | tail -20
```

**✅ GOOD signs:**

- `DSRA8203I: Database product name = PostgreSQL`
- Or nothing at all (silence is fine here)

**❌ BAD signs — fix immediately if you see:**

| Error | Meaning |
|-------|---------|
| `ClassNotFoundException` | WAS can't find the class → wrong class name or wrong JAR |
| `FileNotFoundException` | JAR missing at that path on THIS node |
| `SRVE0199E` | General load failure |

**Also eyeball the console:**

- Resources → JDBC → JDBC Providers → scope = **Cell** → name is listed → click it → all fields correct, no warnings.

---

## 🏁 Final Checklist (Sprint 1 Acceptance)

Tick every box:

- [ ] JAR exists at **identical path** on **both** nodes
- [ ] Provider **`PostgreSQL JDBC Provider`** created at **Cell scope**
- [ ] Class = **`org.postgresql.ds.PGConnectionPoolDataSource`** (exact)
- [ ] Classpath = **`/apps/IBM/WebSphere/AppServer/lib/ext/jdbc/postgresql-42.7.3.jar`**
- [ ] Nodes **synchronized** after creation
- [ ] **Zero classpath errors** in logs on both nodes

---

## 🧩 Cheat Card — The Whole Flow in One Breath

> **JAR on both boxes → Provider at Cell scope (GUI + wsadmin) → Save → Sync → Check logs.**

---

## ⚠️ Top 5 Mistakes to Avoid

1. **JAR on dmgr only, not node02** → breaks only on node02, sneaky bug
2. **Wrong scope (node instead of Cell)** → only one server gets the provider
3. **Typo in the class name** → ClassNotFoundException
4. **Forgetting `AdminConfig.save()`** in wsadmin → nothing was actually saved
5. **Forgetting to sync** → config stuck on DMgr, nodes blind

---

> **Golden rule to end on:** *Config is shared. Files are local. Sync always. Verify always.* ✅
