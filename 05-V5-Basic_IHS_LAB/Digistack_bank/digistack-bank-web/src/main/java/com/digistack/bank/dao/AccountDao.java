package com.digistack.bank.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.digistack.bank.model.Account;

/**
 * AccountDao — P01 v3
 *
 * Data Access Object for the accounts table.
 * All SQL for account operations lives here and nowhere else.
 *
 * Design:
 *   - Every method receives a Connection parameter.
 *     The caller (AccountService) owns and manages the connection
 *     lifecycle — open once, pass to DAO, close in a finally block.
 *     This prepares for connection-pool management at v7 (JNDI).
 *   - Every method uses PreparedStatement — never string-concatenated
 *     SQL. Prevents SQL injection attacks.
 *   - Returns null when a record is not found (not an exception).
 *     The Service layer decides what a missing record means.
 *
 * TECHNICAL DEBT (v3): Connection is direct JDBC passed from Service.
 * At v7: replaced with WAS-managed JNDI DataSource (jdbc/BankDS).
 */
public class AccountDao {

    // SQL statements — all defined as constants at the top.
    // Never build SQL strings with + concatenation of user input.

    private static final String SQL_FIND_BY_USER_ID =
        "SELECT id, user_id, account_number, account_type, " +
        "       balance, is_frozen, created_at, updated_at " +
        "FROM accounts " +
        "WHERE user_id = ? " +
        "ORDER BY id ASC " +
        "LIMIT 1";

    private static final String SQL_FIND_BY_ID =
        "SELECT id, user_id, account_number, account_type, " +
        "       balance, is_frozen, created_at, updated_at " +
        "FROM accounts " +
        "WHERE id = ?";

    private static final String SQL_DEPOSIT =
        "UPDATE accounts " +
        "SET balance    = balance + ?, " +
        "    updated_at = NOW() " +
        "WHERE id = ? " +
        "AND   is_frozen = FALSE";

    private static final String SQL_WITHDRAW =
        "UPDATE accounts " +
        "SET balance    = balance - ?, " +
        "    updated_at = NOW() " +
        "WHERE id = ? " +
        "AND   is_frozen = FALSE " +
        "AND   balance >= ?";

    /**
     * Finds the primary account for a given user.
     * Returns null if the user has no account.
     *
     * @param conn   Active database connection (caller manages lifecycle)
     * @param userId The user's ID from the users table
     * @return       Populated Account object, or null if not found
     */
    public Account findByUserId(Connection conn, int userId)
            throws SQLException {

        try (PreparedStatement ps =
                conn.prepareStatement(SQL_FIND_BY_USER_ID)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }
        }
    }

    /**
     * Finds an account by its primary key (accounts.id).
     * Returns null if not found.
     *
     * @param conn      Active database connection
     * @param accountId The account's primary key
     * @return          Populated Account object, or null if not found
     */
    public Account findById(Connection conn, int accountId)
            throws SQLException {

        try (PreparedStatement ps =
                conn.prepareStatement(SQL_FIND_BY_ID)) {

            ps.setInt(1, accountId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }
        }
    }

    /**
     * Deposits an amount into an account.
     * Only succeeds if the account is not frozen.
     *
     * @param conn      Active database connection
     * @param accountId The account's primary key
     * @param amount    The amount to deposit (must be positive)
     * @return          true if the UPDATE affected exactly 1 row
     *                  false if account is frozen or not found
     */
    public boolean deposit(Connection conn,
                           int accountId,
                           BigDecimal amount)
            throws SQLException {

        try (PreparedStatement ps =
                conn.prepareStatement(SQL_DEPOSIT)) {

            ps.setBigDecimal(1, amount);
            ps.setInt(2, accountId);

            int rowsAffected = ps.executeUpdate();
            // Exactly 1 row updated = success.
            // 0 rows = account frozen or not found.
            return rowsAffected == 1;
        }
    }

    /**
     * Withdraws an amount from an account.
     * Only succeeds if:
     *   (a) The account is not frozen, AND
     *   (b) The current balance is >= the withdrawal amount.
     * Both conditions are enforced in the SQL WHERE clause —
     * the DB acts as the final check, not just the Java layer.
     *
     * @param conn      Active database connection
     * @param accountId The account's primary key
     * @param amount    The amount to withdraw (must be positive)
     * @return          true if the UPDATE affected exactly 1 row
     *                  false if insufficient funds, frozen, or not found
     */
    public boolean withdraw(Connection conn,
                            int accountId,
                            BigDecimal amount)
            throws SQLException {

        try (PreparedStatement ps =
                conn.prepareStatement(SQL_WITHDRAW)) {

            ps.setBigDecimal(1, amount);
            ps.setInt(2, accountId);
            ps.setBigDecimal(3, amount);

            int rowsAffected = ps.executeUpdate();
            return rowsAffected == 1;
        }
    }

    /**
     * Maps a ResultSet row to an Account object.
     * Called internally after every SELECT query.
     * The ResultSet cursor must already be positioned on a row
     * (rs.next() must have returned true before calling this).
     */
    private Account mapRow(ResultSet rs) throws SQLException {
        Account account = new Account();
        account.setId(rs.getInt("id"));
        account.setUserId(rs.getInt("user_id"));
        account.setAccountNumber(rs.getString("account_number"));
        account.setAccountType(rs.getString("account_type"));
        account.setBalance(rs.getBigDecimal("balance"));
        account.setFrozen(rs.getBoolean("is_frozen"));
        account.setCreatedAt(rs.getTimestamp("created_at"));
        account.setUpdatedAt(rs.getTimestamp("updated_at"));
        return account;
    }
}