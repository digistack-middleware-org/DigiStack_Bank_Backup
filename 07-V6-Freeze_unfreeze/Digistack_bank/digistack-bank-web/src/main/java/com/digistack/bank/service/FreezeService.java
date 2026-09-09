package com.digistack.bank.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.digistack.bank.dao.AccountDao;
import com.digistack.bank.model.Account;

/**
 * FreezeService — P01 v6
 *
 * Service layer for the Freeze and Unfreeze operations.
 * Both operations are owned by this single service because
 * they are two sides of the same admin action — toggling
 * the is_frozen flag on an account.
 *
 * Responsibilities:
 *   - Retrieve the account by ID and verify it exists
 *   - For freeze:   check not already frozen (guard + clear message)
 *   - For unfreeze: check not already unfrozen (guard + clear message)
 *   - Call AccountDao.freeze() or AccountDao.unfreeze()
 *   - Return the refreshed Account reflecting the new state
 *
 * Called by:
 *   FreezeServlet   (UI path — form submission)
 *   UnfreezeServlet (UI path — form submission)
 *   wsadmin Jython scripts (admin ops path — bypasses UI entirely)
 *
 * TECHNICAL DEBT (v6): Direct JDBC with hardcoded credentials.
 * Replaced at v7 with WAS-managed JNDI DataSource (jdbc/BankDS).
 */
public class FreezeService {

    private static final String JDBC_URL =
        "jdbc:postgresql://192.168.10.30:5432/digistack_bank";
    private static final String JDBC_USER     = "digistack_app";
    private static final String JDBC_PASSWORD = "Wasadmin@951951";

    private final AccountDao accountDao;

    public FreezeService() {
        this.accountDao = new AccountDao();
    }

    /**
     * Test constructor — allows a mock AccountDao to be injected.
     * Added at v6 to satisfy standing rule TEST01.
     */
    public FreezeService(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    /**
     * Freezes an account identified by its primary key.
     *
     * Business rules:
     *   1. Account must exist.
     *   2. Account must not already be frozen.
     *      (Attempting to freeze an already-frozen account is a
     *       no-op in SQL but we surface it as an error so the
     *       operator knows the action had no effect.)
     *
     * @param accountId The account's primary key (accounts.id)
     * @return          The refreshed Account with is_frozen = true
     * @throws IllegalStateException if account not found or already frozen
     * @throws SQLException          on DB error
     */
    public Account freeze(int accountId)
            throws IllegalStateException, SQLException {

        try (Connection conn = getConnection()) {

            // ── Retrieve account ──
            Account account = accountDao.findById(conn, accountId);
            if (account == null) {
                throw new IllegalStateException(
                    "Account ID " + accountId + " not found.");
            }

            // ── Already frozen? ──
            if (account.isFrozen()) {
                throw new IllegalStateException(
                    "Account " + accountId +
                    " is already frozen. No action taken.");
            }

            // ── Execute freeze ──
            boolean success = accountDao.freeze(conn, accountId);
            if (!success) {
                throw new IllegalStateException(
                    "Freeze operation failed for account " +
                    accountId + ". Please try again.");
            }

            // ── Return refreshed account ──
            return accountDao.findById(conn, accountId);
        }
    }

    /**
     * Unfreezes an account identified by its primary key.
     *
     * Business rules:
     *   1. Account must exist.
     *   2. Account must currently be frozen.
     *      (Attempting to unfreeze an already-active account is
     *       surfaced as an error for the same reason as above.)
     *
     * @param accountId The account's primary key (accounts.id)
     * @return          The refreshed Account with is_frozen = false
     * @throws IllegalStateException if account not found or not frozen
     * @throws SQLException          on DB error
     */
    public Account unfreeze(int accountId)
            throws IllegalStateException, SQLException {

        try (Connection conn = getConnection()) {

            // ── Retrieve account ──
            Account account = accountDao.findById(conn, accountId);
            if (account == null) {
                throw new IllegalStateException(
                    "Account ID " + accountId + " not found.");
            }

            // ── Already unfrozen? ──
            if (!account.isFrozen()) {
                throw new IllegalStateException(
                    "Account " + accountId +
                    " is not frozen. No action taken.");
            }

            // ── Execute unfreeze ──
            boolean success = accountDao.unfreeze(conn, accountId);
            if (!success) {
                throw new IllegalStateException(
                    "Unfreeze operation failed for account " +
                    accountId + ". Please try again.");
            }

            // ── Return refreshed account ──
            return accountDao.findById(conn, accountId);
        }
    }

    /**
     * Opens a direct JDBC connection to digistack_bank.
     * Replaced at v7 with a JNDI DataSource lookup.
     */
    /**
     * protected so FreezeServiceTest can override with a mock
     * Connection. Replaced at v7 with a JNDI lookup.
     */
    protected Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            JDBC_URL, JDBC_USER, JDBC_PASSWORD);
    }
}