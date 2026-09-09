-- ============================================================
-- Migration : V3__create_accounts.sql
-- Version   : P01 v3
-- Purpose   : Create accounts table for Deposit/Withdraw.
--             Each row represents one bank account owned
--             by one user. Balance is stored as NUMERIC to
--             avoid floating-point rounding errors that
--             occur with FLOAT/DOUBLE — critical for money.
-- Rollback  : db/rollback/V3__rollback_accounts.sql
-- ============================================================

-- Create the accounts table
CREATE TABLE accounts (
    id              SERIAL          PRIMARY KEY,
    user_id         INTEGER         NOT NULL,
    account_number  VARCHAR(20)     NOT NULL,
    account_type    VARCHAR(20)     NOT NULL DEFAULT 'SAVINGS',
    balance         NUMERIC(15, 2)  NOT NULL DEFAULT 0.00,
    is_frozen       BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

-- Foreign key — account must belong to an existing user.
-- ON DELETE RESTRICT prevents deleting a user who has accounts.
-- Naming follows project standard: fk=<table>_id
ALTER TABLE accounts
    ADD CONSTRAINT fk_accounts_user_id
        FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE RESTRICT;

-- Unique constraint — account number must be unique across all accounts
ALTER TABLE accounts
    ADD CONSTRAINT uq_accounts_account_number
        UNIQUE (account_number);

-- Check constraint — balance can never go below zero at DB level.
-- Business logic also enforces this in AccountService, but the DB
-- constraint is the last line of defence.
ALTER TABLE accounts
    ADD CONSTRAINT chk_accounts_balance
        CHECK (balance >= 0);

-- Check constraint — only known account types allowed
ALTER TABLE accounts
    ADD CONSTRAINT chk_accounts_type
        CHECK (account_type IN ('SAVINGS', 'CURRENT'));

-- Index on user_id — every Dashboard load does WHERE user_id = ?
-- This makes that lookup fast as the accounts table grows.
CREATE INDEX idx_accounts_user_id ON accounts (user_id);

-- ── Seed Accounts ──
-- One savings account per seed user.
-- account_number format: DSB + zero-padded sequential number.
-- Balance seeded with a realistic starting amount.
-- is_frozen = false — accounts start active.
--
-- Seed account 1: belongs to customer1 (users.id = 1)
INSERT INTO accounts (
    user_id,
    account_number,
    account_type,
    balance
) VALUES (
    1,
    'DSB0000000001',
    'SAVINGS',
    50000.00
);

-- Seed account 2: belongs to admin1 (users.id = 2)
INSERT INTO accounts (
    user_id,
    account_number,
    account_type,
    balance
) VALUES (
    2,
    'DSB0000000002',
    'SAVINGS',
    10000.00
);