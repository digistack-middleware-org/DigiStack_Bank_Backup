-- V3__create_accounts.sql
-- Creates the accounts table for P01 v3's Deposit/Withdraw feature.

CREATE TABLE accounts (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id),
    balance NUMERIC(15,2) NOT NULL DEFAULT 0.00
);

-- Seed account linked to our existing testuser, starting balance 1000.00
INSERT INTO accounts (user_id, balance)
VALUES (1, 1000.00);