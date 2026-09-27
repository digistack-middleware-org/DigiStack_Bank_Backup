package com.digistack.bank.xa;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * NonXaControlBean — Negative control for the v8.5 XA lab.
 *
 * Purpose: show what happens WITHOUT XA.
 * A non-XA connection with autocommit=true commits the debit the instant
 * executeUpdate() returns. A simulated credit-side failure then throws.
 * The debit cannot be undone — it is permanently committed.
 *
 * This is the "orphaned debit" problem that Two-Phase Commit solves.
 *
 * @Stateless          : WAS manages the bean lifecycle.
 * @TransactionAttribute(NOT_SUPPORTED): No container transaction.
 *                      The connection from jdbc/BankDS is not enlisted.
 *                      autocommit=true is in effect — each SQL statement
 *                      commits immediately on execution.
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class NonXaControlBean {

    // jdbc/BankDS — the existing non-XA DataSource (PGConnectionPoolDataSource).
    // Registered in web.xml resource-ref and ibm-web-bnd.xml since v7.
    private static final String BANK_DS_JNDI = "java:comp/env/jdbc/BankDS";

    /**
     * Simulates an orphaned debit using a non-XA, autocommit connection.
     *
     * Flow:
     *   1. Open connection from jdbc/BankDS (non-XA PGConnectionPoolDataSource).
     *   2. Set autocommit = true  — explicitly, for lab clarity.
     *   3. Validate source account: must exist, not frozen, sufficient balance.
     *   4. Execute debit UPDATE   → committed to disk immediately.
     *   5. Throw NonXaOrphanException — simulates a crash/error on the credit side.
     *
     * The throw at step 5 escapes this method.
     * At that point the debit (step 4) is already on disk.
     * No transaction can roll it back. The credit never ran.
     * This is the orphaned debit.
     *
     * @param fromAccountId  Account to debit. Must exist, not frozen, sufficient balance.
     * @param toAccountId    Account that would have been credited (never actually touched).
     * @param amount         Amount debited.
     * @throws NonXaOrphanException Always thrown — the debit has already committed
     *                              before this exception is constructed.
     */
    public void simulateOrphanedDebit(long fromAccountId,
                                      long toAccountId,
                                      BigDecimal amount) {

        DataSource ds = lookupDataSource(BANK_DS_JNDI);

        try (Connection conn = ds.getConnection()) {

            // Explicit autocommit=true.
            // With @TransactionAttribute(NOT_SUPPORTED) there is no container
            // transaction, so WAS does not manage this connection's commit behaviour.
            // Setting this explicitly makes the lab intent unambiguous.
            conn.setAutoCommit(true);

            // ── Validate source account ───────────────────────────────────────
            // SELECT under autocommit=true — each read is its own auto-transaction.
            validateAccount(conn, fromAccountId, amount);

            // ── DEBIT — commits immediately on executeUpdate() ────────────────
            // This is the critical difference from the XA path.
            // In FundsTransferBean, the debit SQL is HELD in an open XA transaction
            // until WAS decides to commit or roll back.
            // Here, the debit SQL commits to PostgreSQL the instant executeUpdate()
            // returns. There is no "held" state. It is done.
            String debitSql = "UPDATE accounts SET balance = balance - ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(debitSql)) {
                stmt.setBigDecimal(1, amount);
                stmt.setLong(2, fromAccountId);
                int rows = stmt.executeUpdate();
                // ▲ Committed to disk. Cannot be undone.
                if (rows != 1) {
                    throw new NonXaOrphanException(
                        "Debit UPDATE affected " + rows + " rows for account id="
                        + fromAccountId + ". Expected exactly 1.");
                }
            }

            // ── Simulate credit-side failure ──────────────────────────────────
            // The debit above is permanent. This throw simulates any failure:
            // network error, application crash, second-DB timeout, etc.
            // The credit UPDATE never runs. The money is gone from the source
            // account with no corresponding credit to the destination.
            throw new NonXaOrphanException(
                "ORPHANED DEBIT CREATED — NON-XA CONTROL. "
                + "Rs." + amount + " has been permanently debited from account "
                + fromAccountId + ". "
                + "Account " + toAccountId + " was NEVER credited — "
                + "credit-side failure was simulated before the credit SQL ran. "
                + "Run: SELECT id, balance FROM accounts WHERE id IN ("
                + fromAccountId + ", " + toAccountId + "); "
                + "You will see account " + fromAccountId + " balance reduced "
                + "and account " + toAccountId + " balance unchanged. "
                + "Restore: UPDATE accounts SET balance = balance + " + amount
                + " WHERE id = " + fromAccountId + ";");

        } catch (NonXaOrphanException e) {
            // Re-throw the demonstration exception as-is.
            // The calling Servlet catches it and displays the orphan message.
            throw e;
        } catch (SQLException e) {
            throw new NonXaOrphanException(
                "Non-XA control SQL error: " + e.getMessage(), e);
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private DataSource lookupDataSource(String jndiName) {
        try {
            InitialContext ctx = new InitialContext();
            return (DataSource) ctx.lookup(jndiName);
        } catch (NamingException e) {
            throw new NonXaOrphanException(
                "Cannot look up DataSource [" + jndiName + "]: " + e.getMessage(), e);
        }
    }

    /**
     * Validates the source account: exists, not frozen, sufficient balance.
     * Runs under autocommit=true — the SELECT is its own single-statement transaction.
     */
    private void validateAccount(Connection conn, long accountId, BigDecimal required)
            throws SQLException {
        String sql = "SELECT balance, is_frozen FROM accounts WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, accountId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new NonXaOrphanException(
                        "Account not found: id=" + accountId);
                }
                if (rs.getBoolean("is_frozen")) {
                    throw new NonXaOrphanException(
                        "Account is frozen: id=" + accountId);
                }
                BigDecimal balance = rs.getBigDecimal("balance");
                if (balance.compareTo(required) < 0) {
                    throw new NonXaOrphanException(
                        "Insufficient funds in account id=" + accountId
                        + ". Balance=" + balance + ", Required=" + required);
                }
            }
        }
    }
}