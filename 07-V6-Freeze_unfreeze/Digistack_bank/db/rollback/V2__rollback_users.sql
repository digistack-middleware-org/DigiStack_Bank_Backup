-- ============================================================
-- Rollback   : V2__rollback_users.sql
-- Rolls back : V2__create_users.sql
-- Effect     : Drops the users table and all its constraints
--              and indexes entirely.
--              Run this only if V2 migration must be reversed.
-- ============================================================

DROP TABLE IF EXISTS users;