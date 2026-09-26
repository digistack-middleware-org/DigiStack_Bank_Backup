package com.digistack.bank.service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.digistack.bank.dao.AccountDao;
import com.digistack.bank.exception.InsufficientFundsException;
import com.digistack.bank.model.Account;

/**
 * WithdrawService — P01 v7
 *
 * v7 change: Direct JDBC replaced with WAS-managed JNDI DataSource.
 * The JDBC_URL, JDBC_USER, JDBC_PASSWORD constants removed.
 * getConnection() now looks up jdbc/BankDS via JNDI.
 *
 * Business logic (validation, frozen check, funds check, DAO calls)
 * unchanged. Unit tests (TEST01) unchanged.
 */
public class WithdrawService {

    private final AccountDao accountDao;

    public WithdrawService() {
        this.accountDao = new AccountDao();
    }

    /**
     * Test constructor — allows a mock AccountDao to be injected.
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
     * @param amount The withdrawal amount as a string from the form
     * @return       The updated Account after the withdrawal
     * @throws IllegalArgumentException   if amount is invalid
     * @throws IllegalStateException      if frozen or not found
     * @throws InsufficientFundsException if balance is too low
     * @throws SQLException               on DB error
     */
    public Account withdraw(int userId, String amount)
            throws IllegalArgumentException,
                   IllegalStateException,
                   InsufficientFundsException,
                   SQLException {

        BigDecimal withdrawAmount = parseAndValidateAmount(amount);

        try (Connection conn = getConnection()) {

            Account account = accountDao.findByUserId(conn, userId);
            if (account == null) {
                throw new IllegalStateException(
                    "No account found for this user.");
            }

            if (account.isFrozen()) {
                throw new IllegalStateException(
                    "Your account is frozen. " +
                    "Please contact support to unfreeze it.");
            }

            if (account.getBalance().compareTo(withdrawAmount) < 0) {
                throw new InsufficientFundsException(
                    account.getBalance(), withdrawAmount);
            }

            boolean success = accountDao.withdraw(
                conn, account.getId(), withdrawAmount);

            if (!success) {
                throw new InsufficientFundsException(
                    account.getBalance(), withdrawAmount);
            }

            return accountDao.findById(conn, account.getId());
        }
    }

    /**
     * Parses and validates the amount string from the HTML form.
     */
    private BigDecimal parseAndValidateAmount(String amountStr) {
        if (amountStr == null || amountStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Amount is required.");
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
     * Obtains a connection from the WAS-managed JNDI DataSource.
     * protected so WithdrawServiceTest can override with a mock.
     */
    protected Connection getConnection() throws SQLException {
        try {
            InitialContext ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup("jdbc/BankDS");
            return ds.getConnection();
        } catch (NamingException e) {
            throw new SQLException(
                "JNDI lookup failed for jdbc/BankDS: " +
                e.getMessage(), e);
        }
    }
}