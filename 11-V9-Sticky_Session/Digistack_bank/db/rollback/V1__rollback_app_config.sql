-- ============================================================
-- Rollback   : V1__rollback_app_config.sql
-- Rolls back : V1__create_app_config.sql
-- Effect     : Drops the app_config table entirely.
--              Run this only if V1 migration must be reversed.
-- ============================================================

DROP TABLE IF EXISTS app_config;