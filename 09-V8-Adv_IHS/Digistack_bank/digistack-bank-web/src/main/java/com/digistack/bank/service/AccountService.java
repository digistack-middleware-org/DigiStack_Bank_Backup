package com.digistack.bank.service;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.digistack.bank.dao.AccountDao;
import com.digistack.bank.model.Account;

/**
 * AccountService — P01 v7
 *
 * v7 change: Direct JDBC replaced with WAS-managed JNDI DataSource.
 *
 * The three static constants (JDBC_URL, JDBC_USER, JDBC_PASSWORD)
 * and the DriverManager.getConnection() call are removed entirely.
 * getConnection() now performs a JNDI lookup of jdbc/BankDS.
 *
 * WAS resolves "jdbc/BankDS" to the DigiStack Bank DataSource
 * configured in Sprint 2. Credentials are held in the JAAS Auth
 * Alias (BankDS_Alias) — never visible in application code.
 *
 * The injectable constructor and protected getConnection() introduced
 * at v6 for TEST01 are preserved — unit tests are unaffected by this
 * migration because test subclasses override getConnection() entirely,
 * meaning they never reach the JNDI lookup code path.
 */
public class AccountService {

    private final AccountDao accountDao;

    /**
     * Default constructor — used in production.
     */
    public AccountService() {
        this.accountDao = new AccountDao();
    }

    /**
     * Test constructor — allows a mock AccountDao to be injected.
     * Used only by unit tests (TEST01).
     */
    public AccountService(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

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
     * Obtains a connection from the WAS-managed JNDI DataSource.
     *
     * Replaces DriverManager.getConnection() from v1–v6.
     * protected (not private) so unit test subclasses can override
     * this method to return a mocked Connection — same testability
     * pattern established at v6 for TEST01.
     *
     * The JNDI name "jdbc/BankDS" maps to the DataSource created
     * in Sprint 2 using the PostgreSQL JDBC Provider from Sprint 1.
     * Connection pooling, credential management, and validation are
     * all handled by WAS — the application code sees only a
     * standard java.sql.Connection.
     *
     * @throws SQLException wrapping any NamingException so callers
     *                      only need to catch one exception type
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