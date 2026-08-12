package com.digistack.bank.service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.logging.Logger;

import com.digistack.bank.dao.AccountDao;

public class AccountService {

    private static final Logger logger = Logger.getLogger(AccountService.class.getName());
    private final AccountDao accountDao = new AccountDao();

    /**
     * Returns the current balance for an account.
     */
    public BigDecimal getBalance(int accountId) throws SQLException {
        return accountDao.getBalance(accountId);
    }

    /**
     * Deposits a positive amount into the account.
     * Returns the new balance.
     */
    public BigDecimal deposit(int accountId, BigDecimal amount) throws SQLException, IllegalArgumentException {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be greater than zero.");
        }

        BigDecimal currentBalance = accountDao.getBalance(accountId);
        if (currentBalance == null) {
            throw new IllegalArgumentException("Account not found.");
        }

        BigDecimal newBalance = currentBalance.add(amount);
        accountDao.updateBalance(accountId, newBalance);

        logger.info("AccountService: deposited " + amount + " to account " + accountId + ". New balance: " + newBalance);
        return newBalance;
    }

    /**
     * Withdraws a positive amount from the account, if sufficient balance exists.
     * Returns the new balance.
     */
    public BigDecimal withdraw(int accountId, BigDecimal amount) throws SQLException, IllegalArgumentException {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be greater than zero.");
        }

        BigDecimal currentBalance = accountDao.getBalance(accountId);
        if (currentBalance == null) {
            throw new IllegalArgumentException("Account not found.");
        }

        if (currentBalance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance. Current balance: " + currentBalance);
        }

        BigDecimal newBalance = currentBalance.subtract(amount);
        accountDao.updateBalance(accountId, newBalance);

        logger.info("AccountService: withdrew " + amount + " from account " + accountId + ". New balance: " + newBalance);
        return newBalance;
    }
}