# Deploy the Schema to DB from DMGR VM
## Deploy from Deploy server
### Why users Is a Separate Table from app_config

Your database already has an app_config table. It stores things like:

    The bank's name
    System status (up/down)
    Other application settings

The new users table stores:

    Usernames
    Password hashes
    Roles (customer/admin)

#### Install PostgreSQL Clint 

```
sudo dnf -qy module disable postgresql
sudo dnf install -y https://download.postgresql.org/pub/repos/yum/reporpms/EL-8-x86_64/pgdg-redhat-repo-latest.noarch.rpm

sudo dnf install -y postgresql16-server postgresql16-contrib

sudo /usr/pgsql-15/bin/postgresql-15-setup initdb

sudo systemctl enable postgresql-16
sudo systemctl start postgresql-16
```
```
psql -h 192.168.10.30 -U digistack_app -d digistack_bank -f V2__create_users.sql
```
#### Verification
```
psql -h 192.168.10.30 -U digistack_app -d digistack_bank -c "SELECT id, username, role, full_name, is_active FROM users;"
```
Expected output:
```
 id | username  |     role      |  full_name  | is_active
----+-----------+---------------+-------------+-----------
  1 | customer1 | CUSTOMER      | peta venkatesh  | t
  2 | admin1    | ADMINISTRATOR | Admin User  | t
(2 rows)
```

Verify the hashes are now set in the database. On dsb-db:
```
psql -U digistack_app -d digistack_bank -h 192.168.10.30 -c "SELECT username, password_salt, LEFT(password_hash,16) || '...' AS hash_preview FROM users;"
```

# Setup PostgreSQL JDBC Driver

Download the PostgreSQL JDBC driver.
```
wget https://jdbc.postgresql.org/download/postgresql-42.7.3.jar

```
Create Directory for postgresql driver 

```
sudo mkdir -p /apps/IBM/SharedLibs/postgresql
```

Copy to

```
sudo mv postgresql-42.7.3.jar /apps/IBM/SharedLibs/postgresql/
sudo chown wasadmin:wasgrp /apps/IBM/SharedLibs/postgresql/postgresql-42.7.3.jar
```
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

## Build the Package
```
mvn clean package
```
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


# Establish Connection Between DB and Standalone Server 
HERE we Establish Connection between Database to server-1 only, not at Profile level or Cell level or Cluater Level, its at server1 level
Meaning Server-1 only communicate with DB 
## Step:1 ===> Register It as a Shared Library 
### Method-1 ==> using Admin console
Open your browser:
```
https://dsb-dmgr.digistack.cloud:9043/ibm/console
```
Go to Shared Libraries
```
Environment
    ↓
Shared Libraries
```
At the top of the page, click Scope. and Select the application server where your application will run.
```
Node=devdsbinnode01
Server=server1
```
click on Apply

#### Enter the Details
```
Name : PostgreSQLJDBCDriver
Classpath : /apps/IBM/SharedLibs/postgresql/postgresql-42.5.4.jar
```
Leave all other fields at their defaults. and Click on "ok " and Click on "save"
### Method-2 ==> using wasadmin
1. Generate the file "register_Library.py"
```
vim register_Library.py
```
```
node = AdminConfig.getid('/Node:devdsbinnode01/')
server = AdminConfig.getid('/Node:devdsbinnode01/Server:server1/')

libAttrs = [['name', 'PostgreSQLJDBCDriver'], ['classPath', '/apps/IBM/SharedLibs/postgresql/postgresql-42.5.4.jar']]
sharedLib = AdminConfig.create('Library', server, libAttrs)

AdminConfig.save()

print "Shared Library created: " + str(sharedLib)
```
<img width="721" height="399" alt="image" src="https://github.com/user-attachments/assets/ac2c7d13-6525-4b5f-8989-4ead3142857a" />

2. Launch wasadmin to execute the script
```
/apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/bin/wsadmin.sh -lang jython -user wasadmin -password 'Wasadmin@951951'
```
## Step:2 ==> Associate the Shared Library with server1
### Method-1 ==> using Admin console
#### Add These DB Shared Library to the Server
```
Servers
   ↓
Server Types
   ↓
WebSphere application servers
   ↓
server1

```

Open Class Loader Settings
```
Server Infrastructure

↓

Java and Process Management

↓

Class Loader
```
###### Create New Class Loader
Click on 
```
New
```
Leave the default option ==> click on "Ok"

###### open the Newly create Classloader
click on 
```
Shared Library References
```
##### Add New Shared Library
Click on 
```
Add
```
Select 
```
PostgreSQLJDBCDriver
```
Click on "ok"

Click on "Save"

### Method-2 ==> using wasadmin
1. Generate the file "attach_Library_server1.py"
```
vim attach_Library_server1.py
```
```
server = AdminConfig.getid('/Node:devdsbinnode01/Server:server1/')
sharedLib = AdminConfig.getid('/Library:PostgreSQLJDBCDriver/')

classloader = AdminConfig.create('Classloader', server, [])

libRefAttrs = [['libraryName', 'PostgreSQLJDBCDriver']]
AdminConfig.create('LibraryRef', classloader, libRefAttrs)

AdminConfig.save()

print "Class loader created and library attached: " + str(classloader)
```
<img width="726" height="257" alt="image" src="https://github.com/user-attachments/assets/12cba630-4d87-4e49-94e2-486498da9da0" />

2. Launch wasadmin to execute the script
```
/apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/bin/wsadmin.sh -lang jython -user wasadmin -password 'Wasadmin@951951'
```

## Step:3 ==> Restart the Server to reflect the ClassLoader Changes
```
/apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/bin/stopServer.sh server1
/apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver01/bin/startServer.sh server1
```
