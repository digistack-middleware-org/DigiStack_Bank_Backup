-- ═══════════════════════════════════════════════════════════════
-- V4__add_frozen_flag_ROLLBACK.sql — DigiStack Bank P01 v6
--
-- Purpose: Reverses V4__add_frozen_flag.sql — removes the
-- is_frozen column from the accounts table entirely.
--
-- WHEN TO USE THIS:
--   Only if you need to completely undo the Freeze/Unfreeze
--   feature and return the accounts table to its v5 shape.
--
-- WARNING — READ BEFORE RUNNING:
--   If you run this while v6 (or later) application code is
--   deployed, the application will break immediately:
--     - AccountDao.freeze() / unfreeze()   → SQL error (column
--       does not exist)
--     - AccountDao.deposit() / withdraw()  → SQL error, because
--       their WHERE clauses reference is_frozen
--     - Account.java's isFrozen()/setFrozen() will still compile
--       fine in Java, but any live query touching is_frozen
--       will fail at runtime with:
--         ERROR: column "is_frozen" does not exist
--
--   Before running this rollback, you must also revert or
--   redeploy an application version that does not reference
--   is_frozen (i.e. roll back to v5 application code, not just
--   the database).
--
-- DATA LOSS WARNING:
--   If any account currently has is_frozen = TRUE, that frozen
--   status is PERMANENTLY LOST when this column is dropped.
--   There is no way to recover which accounts were frozen after
--   running this script. Check first — see Step 1 below.
-- ═══════════════════════════════════════════════════════════════


-- ── STEP 1 (recommended) — Check before dropping ─────────────────
-- Run this SELECT first and read the output before proceeding.
-- If any row shows is_frozen = t (true), pause and reconsider —
-- you are about to lose that information permanently.

SELECT id, account_number, is_frozen
FROM accounts
WHERE is_frozen = TRUE;

-- Expected result if safe to proceed: 0 rows returned.
-- If rows ARE returned: stop here. Do not proceed with the
-- DROP COLUMN below until you have manually recorded which
-- account IDs were frozen, in case you need that information later.


-- ── STEP 2 — Execute the rollback ─────────────────────────────────
-- Only run this after confirming Step 1 above.

ALTER TABLE accounts
    DROP COLUMN is_frozen;


-- ── STEP 3 — Verify the rollback ──────────────────────────────────
-- Run this after the DROP COLUMN above completes.

-- \d accounts
-- Expected result: is_frozen no longer appears in the column list.