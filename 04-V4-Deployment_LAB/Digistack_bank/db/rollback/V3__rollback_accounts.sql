-- ============================================================
-- Rollback   : V3__rollback_accounts.sql
-- Rolls back : V3__create_accounts.sql
-- Effect     : Drops the accounts table entirely including
--              all constraints, indexes, and seed data.
--              Run this only if V3 migration must be reversed.
-- ============================================================

DROP TABLE IF EXISTS accounts;