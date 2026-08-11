# DB Installation & Configuration

## PostgreSQL Installation
### Step:1 ==> Add the official PostgreSQL YUM repository
```
sudo dnf install -y https://download.postgresql.org/pub/repos/yum/reporpms/EL-8-x86_64/pgdg-redhat-repo-latest.noarch.rpm
```
### Step:2 ==> Disable built-in (older) PostgreSQL module

```
sudo dnf -qy module disable postgresql
```

### Step:3 ==> Install PostgreSQL 16 server and client
```
sudo dnf install -y postgresql16-server postgresql16-contrib
```

### Step:4 ==> Initialize the database
```
sudo /usr/pgsql-16/bin/postgresql-16-setup initdb
```

### Step:5 ==> Enable and start the PostgreSQL service
```
sudo systemctl enable postgresql-16
sudo systemctl start postgresql-16
```

### Step:6 ==> Verify PostgreSQL service
```
sudo systemctl status postgresql-16
```
```
sudo -u postgres psql -c "SELECT version();"
```
# Configure Postgresql DB
#### Allow Remote Host connect to DB
1. Edit the "postgresql.conf" file in path "/var/lib/pgsql/16/data/postgresql.conf"
```
sudo vim /var/lib/pgsql/16/data/postgresql.conf
```
ADD these Under connection settings
```
listen_addresses = '*'
```

2. Edit the "pg_hba.conf" file in path "/var/lib/pgsql/16/data/pg_hba.conf"

```
sudo vim /var/lib/pgsql/16/data/pg_hba.conf
```
Edit IPV4 Local Connection Method from ident to md5 these lines 
```
# IPv4 local connections:
host    all             all             127.0.0.1/32            md5
```
Also add these lines for the Password for the User "appuser" so we need to mention these line, take these password for the user "appuser" for DB "user-account" form any IP
```
# Allow remote user connections from a single IP
host    all             all             0.0.0.0/0          md5
```

Also add these lines We encrypt the Password for the User "appuser" so we need to mention these line, take these encrypetd password for the user "appuser" for DB "user-account" form any IP
```
host    all             all             0.0.0.0/0          scram-sha-256 
```

Restart postgressql DB
```
sudo systemctl restart postgresql-16
```
3. Disable the Firewall
```
sudo systemctl stop firewalld
sudo systemctl disable firewalld
```

## Tables and App user Create
# Create Database and Tables

### Run on dsb-db, as the postgres OS user
```
sudo -u postgres psql
```
### Create Database and "digistack_app" user and give permissions
```
CREATE DATABASE digistack_bank;
CREATE USER digistack_app WITH PASSWORD 'Wasadmin@951951';
GRANT ALL PRIVILEGES ON DATABASE digistack_bank TO digistack_app;
```
### Quit Db
```
\q
```
### Grant Schema-Level Privileges to user "digistack_app"
```
sudo -i -u postgres
psql -d digistack_bank
```
```
GRANT ALL ON SCHEMA public TO digistack_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO digistack_app;
```
# Deploy Schema
```
psql -h localhost -U digistack_app -d digistack_bank -f V2__create_users.sql
```
#### Verification
```
psql -h localhost -U digistack_app -d digistack_bank -c "SELECT * FROM users;"
```
Expected output:
```
id=1, username=testuser, and password_hash=PLACEHOLDER_REPLACE_WITH_REAL_BCRYPT_HASH.
```
### Quit Db
```
\q
```

## Update the Database with the Real Hash
```
psql -h localhost -U digistack_app -d digistack_bank
```

```
UPDATE users
SET password_hash = '$2a$10$ccmg6mcdzL9M0fCz0M62y./g6yRD4qCRYlnpjBeXCFMND9SIRG/k.'
WHERE username = 'testuser';
```
#### Verification
```
SELECT username, password_hash FROM users;
```
Expected ==> Shows testuser with the new hash (starting $2a$10$ccmg6...), not the placeholder text anymore.

### Quit Db
```
\q
```

## Keep the Migration File With real Hash
chanf your Db Migration file "V2__create_users.sql"

Change the line:

```
VALUES ('testuser', 'PLACEHOLDER_REPLACE_WITH_REAL_BCRYPT_HASH');
```
Replace with:

```
VALUES ('testuser', '$2a$10$ccmg6mcdzL9M0fCz0M62y./g6yRD4qCRYlnpjBeXCFMND9SIRG/k.');
```