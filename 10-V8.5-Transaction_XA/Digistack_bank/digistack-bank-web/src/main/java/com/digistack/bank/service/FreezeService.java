package com.digistack.bank.service;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.digistack.bank.dao.AccountDao;
import com.digistack.bank.model.Account;

/**
 * FreezeService — P01 v7
 *
 * v7 change: Direct JDBC replaced with WAS-managed JNDI DataSource.
 * The JDBC_URL, JDBC_USER, JDBC_PASSWORD constants removed.
 * getConnection() now looks up jdbc/BankDS via JNDI.
 *
 * Business logic (freeze/unfreeze, redundant-operation guards)
 * unchanged. Unit tests (TEST01) unchanged.
 *
 * Note: freezeAccount.py (wsadmin script) still uses direct JDBC
 * because it runs inside the DMgr JVM, not an application server
 * JVM, and cannot use a JNDI DataSource the same way. That script
 * is updated separately via AdminTask credential lookup — a v7
 * improvement documented in SetupDoc-v7.md.
 */
public class FreezeService {

    private final AccountDao accountDao;

    public FreezeService() {
        this.accountDao = new AccountDao();
    }

    /**
     * Test constructor — allows a mock AccountDao to be injected.
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
     *
     * @param accountId The account's primary key (accounts.id)
     * @return          The refreshed Account with is_frozen = true
     * @throws IllegalStateException if not found or already frozen
     * @throws SQLException          on DB error
     */
    public Account freeze(int accountId)
            throws IllegalStateException, SQLException {

        try (Connection conn = getConnection()) {

            Account account = accountDao.findById(conn, accountId);
            if (account == null) {
                throw new IllegalStateException(
                    "Account ID " + accountId + " not found.");
            }

            if (account.isFrozen()) {
                throw new IllegalStateException(
                    "Account " + accountId +
                    " is already frozen. No action taken.");
            }

            boolean success = accountDao.freeze(conn, accountId);
            if (!success) {
                throw new IllegalStateException(
                    "Freeze operation failed for account " +
                    accountId + ". Please try again.");
            }

            return accountDao.findById(conn, accountId);
        }
    }

    /**
     * Unfreezes an account identified by its primary key.
     *
     * Business rules:
     *   1. Account must exist.
     *   2. Account must currently be frozen.
     *
     * @param accountId The account's primary key (accounts.id)
     * @return          The refreshed Account with is_frozen = false
     * @throws IllegalStateException if not found or not frozen
     * @throws SQLException          on DB error
     */
    public Account unfreeze(int accountId)
            throws IllegalStateException, SQLException {

        try (Connection conn = getConnection()) {

            Account account = accountDao.findById(conn, accountId);
            if (account == null) {
                throw new IllegalStateException(
                    "Account ID " + accountId + " not found.");
            }

            if (!account.isFrozen()) {
                throw new IllegalStateException(
                    "Account " + accountId +
                    " is not frozen. No action taken.");
            }

            boolean success = accountDao.unfreeze(conn, accountId);
            if (!success) {
                throw new IllegalStateException(
                    "Unfreeze operation failed for account " +
                    accountId + ". Please try again.");
            }

            return accountDao.findById(conn, accountId);
        }
    }

    /**
     * Obtains a connection from the WAS-managed JNDI DataSource.
     * protected so FreezeServiceTest can override with a mock.
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