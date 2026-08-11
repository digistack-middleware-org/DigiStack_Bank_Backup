package com.digistack.bank.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

public class AccountDao {

    private static final Logger logger = Logger.getLogger(AccountDao.class.getName());

    private static final String DB_URL = "jdbc:postgresql://192.168.10.30:5432/digistack_bank";
    private static final String DB_USER = "digistack_app";
    private static final String DB_PASSWORD = "Wasadmin@951951";

    /**
     * Fetches the current balance for a given account ID.
     * Returns null if the account doesn't exist.
     */
    public BigDecimal getBalance(int accountId) throws SQLException {
        String sql = "SELECT balance FROM accounts WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, accountId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("balance");
                }
                return null;
            }
        }
    }

    /**
     * Updates the balance for a given account ID to a new absolute value.
     */
    public void updateBalance(int accountId, BigDecimal newBalance) throws SQLException {
        String sql = "UPDATE accounts SET balance = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBigDecimal(1, newBalance);
            stmt.setInt(2, accountId);

            int rowsAffected = stmt.executeUpdate();
            logger.info("AccountDao: updateBalance affected " + rowsAffected + " row(s) for account " + accountId);
        }
    }

    private Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL JDBC driver not found", e);
        }
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
}