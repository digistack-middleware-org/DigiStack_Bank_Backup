-- ═══════════════════════════════════════════════════════════════
-- V4__add_frozen_flag.sql — DigiStack Bank P01 v6
--
-- Purpose: Adds the is_frozen column to the accounts table.
-- This column is the foundation of the Freeze/Unfreeze feature
-- built in Sprint 3. When TRUE, AccountDao.deposit() and
-- AccountDao.withdraw() both refuse to execute (enforced in
-- their SQL WHERE clauses).
--
-- Migration numbering: V4 continues from V3__create_accounts.sql,
-- the last migration in this project's actual sequence.
--
-- ROLLBACK: If this migration needs to be reversed, run the
-- DOWN section at the bottom of this file manually (rollback
-- is not automatic — no migration tool like Flyway is wired
-- into this project yet, so DOWN is applied by hand).
-- ═══════════════════════════════════════════════════════════════

-- ── UP ─────────────────────────────────────────────────────────

-- Add the column. NOT NULL with a DEFAULT means every existing
-- row in the accounts table gets is_frozen = FALSE automatically —
-- no existing account becomes frozen by accident when this
-- migration runs.
ALTER TABLE accounts
    ADD COLUMN is_frozen BOOLEAN NOT NULL DEFAULT FALSE;

-- Add a comment on the column — visible when inspecting the
-- table structure later (e.g. via \d accounts in psql).
-- This documents intent directly in the database, not just in
-- application code comments.
COMMENT ON COLUMN accounts.is_frozen IS
    'TRUE = all deposits and withdrawals blocked. ' ||
    'Set/unset via FreezeService (v6) or wsadmin script (v6 Sprint 4).';

-- ── VERIFICATION QUERY (run manually after UP, not part of migration) ──
-- SELECT id, account_number, is_frozen FROM accounts;
-- Expected: every row shows is_frozen = f (false)


-- ═══════════════════════════════════════════════════════════════
-- DOWN — ROLLBACK (run manually only if this migration must be
-- reversed; NOT executed automatically)
-- ═══════════════════════════════════════════════════════════════
--
-- ALTER TABLE accounts DROP COLUMN is_frozen;
--
-- WARNING: Dropping this column will break AccountDao.freeze(),
-- AccountDao.unfreeze(), and the SQL WHERE clauses in deposit()
-- and withdraw() that reference is_frozen. Do not roll back this
-- migration while v6 or later application code is deployed.