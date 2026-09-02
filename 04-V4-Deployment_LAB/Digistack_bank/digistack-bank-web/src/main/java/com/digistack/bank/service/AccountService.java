package com.digistack.bank.service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.digistack.bank.dao.AccountDao;
import com.digistack.bank.exception.InsufficientFundsException;
import com.digistack.bank.model.Account;

/**
 * AccountService — P01 v3
 *
 * Service layer for account operations.
 * Contains all business logic for Deposit and Withdraw.
 * Calls AccountDao for all database access.
 *
 * Responsibilities:
 *   - Input validation (amount positive, not zero)
 *   - Account retrieval and null-check
 *   - Frozen account enforcement
 *   - Insufficient funds enforcement (Withdraw only)
 *   - Connection management (open, use, close)
 *   - Calling AccountDao methods
 *
 * TECHNICAL DEBT (v3): Direct JDBC with hardcoded credentials.
 * Replaced at v7 with WAS-managed JNDI DataSource (jdbc/BankDS).
 */
public class AccountService {

    // Direct JDBC — replaced at v7 with JNDI DataSource
    private static final String JDBC_URL =
        "jdbc:postgresql://192.168.10.30:5432/digistack_bank";
    private static final String JDBC_USER     = "digistack_app";
    private static final String JDBC_PASSWORD = "Wasadmin@951951";

    private final AccountDao accountDao = new AccountDao();

    /**
     * Retrieves the account for a given user.
     *
     * @param userId The user's ID from the session
     * @return       The user's Account, or null if not found
     * @throws SQLException on DB error
     */
    public Account getAccountByUserId(int userId)
            throws SQLException {

        try (Connection conn = getConnection()) {
            return accountDao.findByUserId(conn, userId);
        }
    }

    /**
     * Deposits an amount into the user's account.
     *
     * Business rules enforced here:
     *   1. Amount must be greater than zero.
     *   2. Account must exist.
     *   3. Account must not be frozen.
     *
     * @param userId The user's ID from the session
     * @param amount The deposit amount as a string from the form
     * @return       The updated Account after deposit
     * @throws IllegalArgumentException if amount is invalid
     * @throws IllegalStateException    if account frozen or not found
     * @throws SQLException             on DB error
     */
    public Account deposit(int userId, String amount)
            throws IllegalArgumentException,
                   IllegalStateException,
                   SQLException {

        // ── Validate input ──
        BigDecimal depositAmount = parseAndValidateAmount(amount);

        try (Connection conn = getConnection()) {

            // ── Retrieve account ──
            Account account = accountDao.findByUserId(conn, userId);
            if (account == null) {
                throw new IllegalStateException(
                    "No account found for this user.");
            }

            // ── Check frozen ──
            if (account.isFrozen()) {
                throw new IllegalStateException(
                    "Your account is frozen. " +
                    "Please contact support.");
            }

            // ── Perform deposit via DAO ──
            boolean success = accountDao.deposit(
                conn, account.getId(), depositAmount);

            if (!success) {
                throw new IllegalStateException(
                    "Deposit could not be processed. " +
                    "Please try again.");
            }

            // ── Return refreshed account with updated balance ──
            return accountDao.findById(conn, account.getId());
        }
    }

    /**
     * Withdraws an amount from the user's account.
     *
     * Business rules enforced here:
     *   1. Amount must be greater than zero.
     *   2. Account must exist.
     *   3. Account must not be frozen.
     *   4. Balance must be >= withdrawal amount.
     *
     * @param userId The user's ID from the session
     * @param amount The withdrawal amount as a string from the form
     * @return       The updated Account after withdrawal
     * @throws IllegalArgumentException   if amount is invalid
     * @throws IllegalStateException      if account frozen or not found
     * @throws InsufficientFundsException if balance too low
     * @throws SQLException               on DB error
     */
    public Account withdraw(int userId, String amount)
            throws IllegalArgumentException,
                   IllegalStateException,
                   InsufficientFundsException,
                   SQLException {

        // ── Validate input ──
        BigDecimal withdrawAmount = parseAndValidateAmount(amount);

        try (Connection conn = getConnection()) {

            // ── Retrieve account ──
            Account account = accountDao.findByUserId(conn, userId);
            if (account == null) {
                throw new IllegalStateException(
                    "No account found for this user.");
            }

            // ── Check frozen ──
            if (account.isFrozen()) {
                throw new IllegalStateException(
                    "Your account is frozen. " +
                    "Please contact support.");
            }

            // ── Check sufficient funds ──
            if (account.getBalance().compareTo(withdrawAmount) < 0) {
                throw new InsufficientFundsException(
                    account.getBalance(), withdrawAmount);
            }

            // ── Perform withdrawal via DAO ──
            boolean success = accountDao.withdraw(
                conn, account.getId(), withdrawAmount);

            if (!success) {
                // This should not happen if our balance check above
                // passed — but the DB-level check is the final guard.
                throw new InsufficientFundsException(
                    account.getBalance(), withdrawAmount);
            }

            // ── Return refreshed account with updated balance ──
            return accountDao.findById(conn, account.getId());
        }
    }

    /**
     * Parses the amount string from the HTML form and validates it.
     * The form submits amounts as strings — this method converts
     * them to BigDecimal and enforces positivity.
     *
     * @param amountStr The raw string from request.getParameter("amount")
     * @return          Validated positive BigDecimal amount
     * @throws IllegalArgumentException if null, empty, not a number,
     *                                  or not greater than zero
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
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            JDBC_URL, JDBC_USER, JDBC_PASSWORD);
    }
}