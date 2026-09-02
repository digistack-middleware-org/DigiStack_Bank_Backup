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
psql -h 192.168.10.30 -U digistack_app -d digistack_bank -f V3__create_accounts.sql
```
#### Verification
Verify the table and seed data:
```
psql -h 192.168.10.30 -U digistack_app -d digistack_bank -c "SELECT id, user_id, account_number, account_type, balance, is_frozen FROM accounts;"
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
psql -U digistack_app -d digistack_bank -h 192.168.10.30 \
  -c "INSERT INTO accounts (user_id, account_number, account_type, balance) VALUES (999, 'DSB9999999999', 'SAVINGS', 100.00);"
```
Expected result — PostgreSQL rejects the insert:
```
ERROR:  insert or update on table "accounts" violates foreign key constraint "fk_accounts_user_id"
DETAIL:  Key (user_id)=(999) is not present in table "users".
```
This confirms the foreign key is working — user ID 999 does not exist in the users table.

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
