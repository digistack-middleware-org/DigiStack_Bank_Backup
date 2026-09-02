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
If you want to ensble the filewall then oprn port "5432"
```
firewall-cmd --permanent --add-port=5432/tcp
firewall-cmd --reload
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
### Quit Db
```
\q
```
# Deploy USER Schema to database
### Why users Is a Separate Table from app_config

Your database already has an app_config table. It stores things like:

    The bank's name
    System status (up/down)
    Other application settings

The new users table stores:

    Usernames
    Password hashes
    Roles (customer/admin)


### Deploy in Localhost {DB server}
```
psql -h localhost -U digistack_app -d digistack_bank -f V3__create_accounts.sql
```
#### Verification
Verify the table and seed data:
```
psql -U digistack_app -d digistack_bank -h localhost -c "SELECT id, user_id, account_number, account_type, balance, is_frozen FROM accounts;"
```
Expected output:
```
 id | user_id | account_number | account_type |  balance  | is_frozen
----+---------+----------------+--------------+-----------+-----------
  1 |       1 | DSB0000000001  | SAVINGS      | 50000.00  | f
  2 |       2 | DSB0000000002  | SAVINGS      | 10000.00  | f
(2 rows)
```
Confirm the foreign key is enforced:
```
psql -U digistack_app -d digistack_bank -h localhost \
  -c "INSERT INTO accounts (user_id, account_number, account_type, balance) VALUES (999, 'DSB9999999999', 'SAVINGS', 100.00);"
```
Expected result — PostgreSQL rejects the insert:
```
ERROR:  insert or update on table "accounts" violates foreign key constraint "fk_accounts_user_id"
DETAIL:  Key (user_id)=(999) is not present in table "users".
```
This confirms the foreign key is working — user ID 999 does not exist in the users table.

