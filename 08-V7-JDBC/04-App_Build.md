# Build the Application

- You already deployed **WAR files** (v1–v5).
- Now you deploy an **EAR file** (v6).
- Goal: put v6 on **both cluster servers** so they serve the **same thing**.

**Real-life example:**

- WAR = a single book.
- EAR = a gift box with one book inside, plus a cover page that says what's inside.

---

## 2. WAR vs EAR (Keep It Simple)

| | WAR | EAR |
|---|---|---|
| Contains | One web app | Many modules (web + EJB + more) |
| Context root (URL path) | Typed manually at deploy | Written **inside** the EAR already |
| Used in big production? | Rare | Standard |

**Key point:** With an EAR, the URL path (`/digistack-bank`) is **baked in** — the admin doesn't type it anymore.

**What's an EAR physically?** A ZIP file renamed to `.ear`. Inside:

- Your `.war` file
- A settings file: `application.xml`

---

## 3. The Three Files You Create

1. **Parent `pom.xml`** — add one line: `<module>digistack-bank-ear</module>`
   - Tells Maven: "also build the EAR folder."

2. **`istack-bank-ear/pom.xml`** — the EAR's build recipe
   - Says: "I'm an EAR."
   - Says: "Include the WAR inside me."
   - Sets context root: `/digistack-bank`
   - Names the output: `digistack-bank-v7.ear`
   - Uses Java EE 7 — **don't change this** or WAS will reject it.

3. **`application.xml`** — the label on the box
   - Display name: "DigiStack Bank v7" → shows in Admin Console.
   - Which WAR is inside: `digistack-bank-web.war`
   - URL path: `/digistack-bank`

**Memory trick:**

- pom.xml = *how to build the box*.
- application.xml = *the label stuck on the box*.

---

## 4. Step 2 — Check Versions Match

- Parent pom: version `6.0.0` ✅
- Web module pom: version `6.0.0` ✅
- EAR pom: version `6.0.0` ✅

**Why?** Maven builds the WAR first, then the EAR. If versions differ, the build fails.

---
## 5. Step 3 — Test

```bash
mvn clean test
```

## 5. Step 3 — Build

```bash
mvn clean package
```

Then confirm the file exists:

```bash
ls -lh digistack-bank-ear/target/digistack-bank-v6.ear
```

**Expected:** `BUILD SUCCESS` + a file of a few hundred KB.
