package com.digistack.bank.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.digistack.bank.dao.AccountDao;
import com.digistack.bank.model.Account;

/**
 * AccountService — P01 v6 (trimmed from v3)
 *
 * Responsibility after v6 split:
 *   ONE method only — retrieve the account for a given user.
 *
 * deposit()  → moved to DepositService
 * withdraw() → moved to WithdrawService
 * freeze()   → moved to FreezeService
 * unfreeze() → moved to FreezeService
 *
 * This class is still used by:
 *   DashboardServlet — loads account for display (masked number, type)
 *   BalanceJsonServlet — loads account for the AJAX balance toggle
 *
 * TECHNICAL DEBT (v3–v6): Direct JDBC with hardcoded credentials.
 * Replaced at v7 with WAS-managed JNDI DataSource (jdbc/BankDS).
 */
public class AccountService {

    private static final String JDBC_URL =
        "jdbc:postgresql://192.168.10.30:5432/digistack_bank";
    private static final String JDBC_USER     = "digistack_app";
    private static final String JDBC_PASSWORD = "Wasadmin@951951";

    private final AccountDao accountDao = new AccountDao();

    /**
     * Retrieves the account for a given user.
     * Returns null if no account is linked to this user.
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
     * Opens a direct JDBC connection to digistack_bank.
     * Replaced at v7 with a JNDI DataSource lookup.
     */
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            JDBC_URL, JDBC_USER, JDBC_PASSWORD);
    }
}