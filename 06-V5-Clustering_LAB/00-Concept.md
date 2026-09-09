## 🎯 What are we doing in this sprint?

We have 2 main jobs:

1. **Create a new computer (VM)** called `dsb-node02` — this will be the **second worker machine** in our cluster.
2. **Create the "DMgr profile"** on the first machine (`dsb-dmgr`) — this is the **boss/brain** of the whole system.

---

## 📚 First, Understand 2 Simple Ideas

### 🧠 What is a DMgr (Deployment Manager)?

Think of it like a **manager in an office**:

- The manager **does not do the actual work** (doesn't run your apps).
- The manager **keeps the master rulebook** (the master configuration) for the whole company (the "cell").
- All other machines (nodes) **take orders and copy the rulebook** from the DMgr.

**So: DMgr = the boss. Nodes = the workers.**

### 🏠 Why does dsb-dmgr hold TWO profiles?

A "profile" is like a **separate apartment inside one building**. Our first VM (`dsb-dmgr`) will have:

| Profile | Role |
|---|---|
| `devdsbinappserver01` | The **worker** (runs apps) |
| `devdsbindmgr01` | The **boss** (manages everything) |

In big companies, the boss gets his own building. But in our **lab**, we save money/machines by putting the boss and one worker in the same building. This is fine for practice.

---

