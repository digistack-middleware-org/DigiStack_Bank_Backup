-- ============================================================
-- Migration : V1__create_app_config.sql
-- Version   : P01 v1
-- Purpose   : Create app_config table and seed initial values.
--             This table holds application-level configuration
--             as key-value pairs. v1 uses it purely as a
--             PostgreSQL connectivity test — HomeServlet reads
--             one row on page load to prove DB connectivity.
-- Rollback  : db/rollback/V1__rollback_app_config.sql
-- ============================================================

-- Create the app_config table
-- Each row stores one configuration key and its value.
CREATE TABLE app_config (
    id          SERIAL          PRIMARY KEY,
    config_key  VARCHAR(100)    NOT NULL UNIQUE,
    config_value VARCHAR(500)   NOT NULL,
    description VARCHAR(500),
    created_at  TIMESTAMP       NOT NULL DEFAULT NOW()
);

-- Constraints follow project naming standard:
--   pk = id (already defined as PRIMARY KEY above)
--   uq = uq_<table>_<col>
ALTER TABLE app_config
    ADD CONSTRAINT uq_app_config_config_key UNIQUE (config_key);

-- Seed: the bank name — the value HomeServlet reads at v1
INSERT INTO app_config (config_key, config_value, description)
VALUES (
    'bank.name',
    'DigiStack Bank',
    'Display name of the bank — read by HomeServlet on page load'
);

-- Seed: system status — displayed in the Home page status bar
INSERT INTO app_config (config_key, config_value, description)
VALUES (
    'system.status',
    'All Systems Operational',
    'Current system status displayed on the public Home page'
);