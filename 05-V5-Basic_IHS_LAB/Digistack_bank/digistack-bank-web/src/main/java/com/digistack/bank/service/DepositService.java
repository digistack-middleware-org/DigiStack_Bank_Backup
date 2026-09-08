package com.digistack.bank.service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.digistack.bank.dao.AccountDao;
import com.digistack.bank.model.Account;

/**
 * DepositService — P01 v6
 *
 * Service layer for the Deposit operation only.
 * Owns all business logic for depositing money into an account.
 * Calls AccountDao for all database access.
 *
 * Responsibilities:
 *   - Parse and validate the amount string from the HTML form
 *   - Retrieve the account and check it exists
 *   - Enforce frozen-account check before attempting deposit
 *   - Call AccountDao.deposit() to execute the SQL UPDATE
 *   - Return the refreshed Account with updated balance
 *
 * Called by: DepositServlet
 *
 * TECHNICAL DEBT (v6): Direct JDBC with hardcoded credentials.
 * Replaced at v7 with WAS-managed JNDI DataSource (jdbc/BankDS).
 */
public class DepositService {

    private static final String JDBC_URL =
        "jdbc:postgresql://192.168.10.30:5432/digistack_bank";
    private static final String JDBC_USER     = "digistack_app";
    private static final String JDBC_PASSWORD = "Wasadmin@951951";

    private final AccountDao accountDao;

    /**
     * Default constructor — used in production.
     * Creates a real AccountDao for live database access.
     */
    public DepositService() {
        this.accountDao = new AccountDao();
    }

    /**
     * Test constructor — allows a mock AccountDao to be injected.
     * Used only by DepositServiceTest via Mockito.
     * Added at v6 to satisfy standing rule TEST01.
     */
    public DepositService(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    /**
     * Deposits an amount into the user's account.
     *
     * Business rules enforced here:
     *   1. Amount must be greater than zero.
     *   2. Account must exist for this user.
     *   3. Account must not be frozen.
     *
     * @param userId The user's ID from the session
     * @param amount The deposit amount as a string from the HTML form
     * @return       The updated Account after the deposit
     * @throws IllegalArgumentException if amount is invalid
     * @throws IllegalStateException    if account is frozen or not found
     * @throws SQLException             on DB error
     */
    public Account deposit(int userId, String amount)
            throws IllegalArgumentException,
                   IllegalStateException,
                   SQLException {

        // ── Step 1: Validate input ──
        // Converts the raw form string to a BigDecimal.
        // Rejects null, empty, non-numeric, zero, and negative values.
        BigDecimal depositAmount = parseAndValidateAmount(amount);

        try (Connection conn = getConnection()) {

            // ── Step 2: Retrieve account ──
            Account account = accountDao.findByUserId(conn, userId);
            if (account == null) {
                throw new IllegalStateException(
                    "No account found for this user.");
            }

            // ── Step 3: Frozen check ──
            // If the account is frozen, reject the deposit immediately.
            // The DAO-level SQL also enforces this (is_frozen = FALSE
            // in the WHERE clause), but we check here first to give
            // the user a clear, specific error message.
            if (account.isFrozen()) {
                throw new IllegalStateException(
                    "Your account is frozen. " +
                    "Please contact support to unfreeze it.");
            }

            // ── Step 4: Execute deposit via DAO ──
            boolean success = accountDao.deposit(
                conn, account.getId(), depositAmount);

            if (!success) {
                // This fires only if the DAO-level frozen check blocked
                // the update — a safety net if the account was frozen
                // between our check above and the UPDATE execution.
                throw new IllegalStateException(
                    "Deposit could not be processed. " +
                    "Please try again.");
            }

            // ── Step 5: Return refreshed account ──
            // Re-fetch from DB so the returned object has the
            // actual new balance, not a locally calculated guess.
            return accountDao.findById(conn, account.getId());
        }
    }

    /**
     * Parses the amount string from the HTML form and validates it.
     *
     * BigDecimal is used — never float or double for money.
     * float/double cannot represent all decimal fractions exactly
     * in binary (e.g. 0.1 + 0.2 = 0.30000000000000004 in floating point).
     * BigDecimal stores exact decimal values — mandatory for financial data.
     *
     * @param amountStr Raw string from request.getParameter("amount")
     * @return          Validated positive BigDecimal amount
     * @throws IllegalArgumentException if null, empty, non-numeric,
     *                                  zero, or negative
     */
    private BigDecimal parseAndValidateAmount(String amountStr) {
        if (amountStr == null || amountStr.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Amount is required.");
        }
        BigDecimal amount;
        try {
            amount = new BigDecimal(amountStr.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                "Invalid amount — please enter a valid number.");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                "Amount must be greater than zero.");
        }
        return amount;
    }

    /**
     * Opens a direct JDBC connection to digistack_bank.
     * Replaced at v7 with a JNDI DataSource lookup.
     */
    /**
     * Opens a direct JDBC connection to digistack_bank.
     * protected (not private) so DepositServiceTest can override
     * this method to return a mocked Connection instead of a
     * real network connection. Replaced at v7 with a JNDI lookup.
     */
    protected Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            JDBC_URL, JDBC_USER, JDBC_PASSWORD);
    }
}