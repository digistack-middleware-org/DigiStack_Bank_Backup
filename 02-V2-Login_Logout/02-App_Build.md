# Build and Deploy the Application

## Install the Build Tool
1. Install Java

```
sudo dnf install -y java-1.8.0-openjdk-devel
java -version
```
2. Install GIT
```
sudo dnf install git -y
```

4. Install Maven

```
sudo dnf install -y maven
mvn -version
```
5. Clone the Repository
```
git clone <Repo-URL>
```
## Build the Application

From the project directory.
```
mvn clean package
```
Generated artifact ==> digistack-bank-ear/target/digistack-bank-v2.ear


# Create real SHA-256 hashes for Customer1 and Admin1 users Passwords

### Why Hashing
The Problem

Imagine you store a user's password directly in the database:
```
username: venkatesh    |    password: MySecret123
```
If a hacker steals your database, they now know everyone's password instantly. This is a disaster. So we never, ever store the actual password.

The Solution — Hashing

A hash function is like a digital shredder with a special property:
```
"MySecret123"  ──► [SHA-256 function] ──► "3f8a2b1c9d7e4f6a..." (64 characters)
```
```
Key facts about a hash:

    The same input always gives the same output (deterministic)
    You cannot reverse it — from 3f8a2b1c... you can never figure out "MySecret123"
    The output is always a fixed length, no matter how long the password is

```
### How we solve these Problem 
"SeedUsers.java" A small standalone program that:

 1. Connects directly to the database (via JDBC)
 2. Computes the real SHA-256 hashes for Customer@123 and Admin@123 using PasswordUtil
 3. Updates the two placeholder rows with the real hashes

#### Compile SeedUsers
```
javac -cp "digistack-bank-web/target/classes:/apps/IBM/SharedLibs/postgresql/postgresql-42.7.3.jar" \
  digistack-bank-web/src/main/java/com/digistack/bank/util/SeedUsers.java \
  digistack-bank-web/src/main/java/com/digistack/bank/util/PasswordUtil.java \
  -d digistack-bank-web/target/classes
```
#### RUN  SeedUsers
```
java -cp "digistack-bank-web/target/classes:/apps/IBM/SharedLibs/postgresql/postgresql-42.7.3.jar" \
  com.digistack.bank.util.SeedUsers
```
Expected output:
```
Connected to digistack_bank on dsb-db.
Updated customer1 with correct password hash.
Updated admin1 with correct password hash.
Seed complete. Both users ready for login.
```
### Verify Hashes in the Database
After Run these standalone program "SeedUsers.java"
```
psql -U digistack_app -d digistack_bank -h 127.0.0.1 -c "SELECT username, password_salt, LEFT(password_hash,16) || '...' AS hash_preview FROM users;"
```
Expected Output
```
 username  |    password_salt   |    hash_preview
-----------+--------------------+--------------------
 customer1 | a1b2c3d4e5f6a1b2  | 3f8a2b1c9d7e4f6a...
 admin1    | f6e5d4c3b2a1f6e5  | 7d2c1e9b4a8f3d2e...
(2 rows)
```



# Confirm the EAR contains the correct WAR

Compile SeedUsers with both the compiled classes and the JDBC JAR on the classpath.
```
javac -cp "digistack-bank-web/target/classes:/apps/IBM/SharedLibs/postgresql/postgresql-42.7.3.jar" \
  digistack-bank-web/src/main/java/com/digistack/bank/util/SeedUsers.java \
  digistack-bank-web/src/main/java/com/digistack/bank/util/PasswordUtil.java \
  -d digistack-bank-web/target/classes
```
Expected result — no output means success. If you see error messages, share them.

Run this command to list the EAR contents:
```
jar tf digistack-bank-ear\target\digistack-bank-v2.ear
```
jar tf: The jar tool is bundled with your Java installation. tf means "table of contents", it lists every file inside the archive without extracting it. EARs and WARs are ZIP files with different extensions.


Expected result — you should see entries including:
```
META-INF/
META-INF/MANIFEST.MF
META-INF/application.xml
digistack-bank-web-1.0.war
```
The application.xml is the EAR deployment descriptor — Maven's EAR plugin generated it automatically from your pom.xml configuration. The WAR file inside confirms the EAR is correctly assembled.
