-- ═══════════════════════════════════════════════════════════════
-- V4__add_frozen_flag.sql — DigiStack Bank P01 v6
--
-- Purpose: Documents the is_frozen column (created in V3) via
-- COMMENT ON COLUMN. No schema change — column already exists.
-- This column is the foundation of the Freeze/Unfreeze feature
-- built in Sprint 3. When TRUE, AccountDao.deposit() and
-- AccountDao.withdraw() both refuse to execute (enforced in
-- their SQL WHERE clauses).
--
-- Migration numbering: V4 follows V3__create_accounts.sql, which
-- already includes is_frozen in its CREATE TABLE definition.
-- ═══════════════════════════════════════════════════════════════

-- ── UP ─────────────────────────────────────────────────────────

-- Column already exists — created in V3__create_accounts.sql.
-- Nothing to alter. This migration only adds documentation
-- (the column comment below) for the Freeze/Unfreeze feature.

COMMENT ON COLUMN accounts.is_frozen IS
    'TRUE = all deposits and withdrawals blocked. ' ||
    'Set/unset via FreezeService (v6) or wsadmin script (v6 Sprint 4).';

-- ── VERIFICATION QUERY (run manually after UP, not part of migration) ──
-- SELECT id, account_number, is_frozen FROM accounts;
-- Expected: every row shows is_frozen = f (false)

-- ── DOWN — ROLLBACK (run manually only) ────────────────────────
-- DROP COMMENT only — the column itself is owned by V3.
-- COMMENT ON COLUMN accounts.is_frozen IS NULL;
--
-- Do NOT drop the column here: V3 creates it, and dropping it
-- would break AccountDao.freeze(), AccountDao.unfreeze(), and
-- the SQL WHERE clauses in deposit() and withdraw().
