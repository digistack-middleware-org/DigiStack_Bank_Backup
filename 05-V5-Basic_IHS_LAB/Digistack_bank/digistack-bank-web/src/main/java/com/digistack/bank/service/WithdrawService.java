package com.digistack.bank.service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.digistack.bank.dao.AccountDao;
import com.digistack.bank.exception.InsufficientFundsException;
import com.digistack.bank.model.Account;

/**
 * WithdrawService — P01 v6
 *
 * Service layer for the Withdraw operation only.
 * Owns all business logic for withdrawing money from an account.
 * Calls AccountDao for all database access.
 *
 * Responsibilities:
 *   - Parse and validate the amount string from the HTML form
 *   - Retrieve the account and check it exists
 *   - Enforce frozen-account check before attempting withdrawal
 *   - Enforce sufficient-funds check before attempting withdrawal
 *   - Call AccountDao.withdraw() to execute the SQL UPDATE
 *   - Return the refreshed Account with updated balance
 *
 * Called by: WithdrawServlet
 *
 * TECHNICAL DEBT (v6): Direct JDBC with hardcoded credentials.
 * Replaced at v7 with WAS-managed JNDI DataSource (jdbc/BankDS).
 */
public class WithdrawService {

    private static final String JDBC_URL =
        "jdbc:postgresql://192.168.10.30:5432/digistack_bank";
    private static final String JDBC_USER     = "digistack_app";
    private static final String JDBC_PASSWORD = "Wasadmin@951951";

    private final AccountDao accountDao;

    public WithdrawService() {
        this.accountDao = new AccountDao();
    }

    /**
     * Test constructor — allows a mock AccountDao to be injected.
     * Added at v6 to satisfy standing rule TEST01.
     */
    public WithdrawService(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    /**
     * Withdraws an amount from the user's account.
     *
     * Business rules enforced here (in order):
     *   1. Amount must be greater than zero.
     *   2. Account must exist for this user.
     *   3. Account must not be frozen.
     *   4. Balance must be >= withdrawal amount.
     *
     * @param userId The user's ID from the session
     * @param amount The withdrawal amount as a string from the HTML form
     * @return       The updated Account after the withdrawal
     * @throws IllegalArgumentException   if amount is invalid
     * @throws IllegalStateException      if account frozen or not found
     * @throws InsufficientFundsException if balance is too low
     * @throws SQLException               on DB error
     */
    public Account withdraw(int userId, String amount)
            throws IllegalArgumentException,
                   IllegalStateException,
                   InsufficientFundsException,
                   SQLException {

        // ── Step 1: Validate input ──
        BigDecimal withdrawAmount = parseAndValidateAmount(amount);

        try (Connection conn = getConnection()) {

            // ── Step 2: Retrieve account ──
            Account account = accountDao.findByUserId(conn, userId);
            if (account == null) {
                throw new IllegalStateException(
                    "No account found for this user.");
            }

            // ── Step 3: Frozen check ──
            if (account.isFrozen()) {
                throw new IllegalStateException(
                    "Your account is frozen. " +
                    "Please contact support to unfreeze it.");
            }

            // ── Step 4: Sufficient funds check ──
            // compareTo returns negative if balance < withdrawAmount.
            // We check here (in Java) to give a specific error message
            // with the actual balance shown. The DB WHERE clause
            // (balance >= ?) is the final enforcement layer.
            if (account.getBalance().compareTo(withdrawAmount) < 0) {
                throw new InsufficientFundsException(
                    account.getBalance(), withdrawAmount);
            }

            // ── Step 5: Execute withdrawal via DAO ──
            boolean success = accountDao.withdraw(
                conn, account.getId(), withdrawAmount);

            if (!success) {
                // Fires if the account was frozen or funds dropped
                // between our check above and the UPDATE execution.
                // Treat as insufficient funds — safest interpretation.
                throw new InsufficientFundsException(
                    account.getBalance(), withdrawAmount);
            }

            // ── Step 6: Return refreshed account ──
            return accountDao.findById(conn, account.getId());
        }
    }

    /**
     * Parses the amount string from the HTML form and validates it.
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
     * protected so WithdrawServiceTest can override with a mock
     * Connection. Replaced at v7 with a JNDI lookup.
     */
    protected Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            JDBC_URL, JDBC_USER, JDBC_PASSWORD);
    }
}