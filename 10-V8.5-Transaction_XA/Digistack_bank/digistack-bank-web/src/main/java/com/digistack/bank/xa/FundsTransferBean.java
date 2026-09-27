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
 * FundsTransferBean — Stateless Session Bean, v8.5 XA Lab.
 *
 * @Stateless  : WAS pools instances. No per-user state.
 * @TransactionAttribute(REQUIRED): WAS starts an XA transaction before
 * transfer() runs. Both DataSource connections obtained inside are enrolled
 * automatically. On clean exit → 2PC commit. On RuntimeException → rollback both.
 *
 * Sprint 2 addition: simulateCreditFail parameter.
 * When true, a FundsTransferException is thrown after the debit SQL runs but
 * before the credit SQL runs. Because this is a RuntimeException inside CMT,
 * WAS rolls back the debit as well — proving the XA atomicity guarantee.
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class FundsTransferBean {

    private static final String DEBIT_DS_JNDI  = "java:comp/env/jdbc/DebitDS";
    private static final String CREDIT_DS_JNDI = "java:comp/env/jdbc/CreditDS";

    /**
     * Transfers amount from fromAccountId to toAccountId across two XA DataSources.
     *
     * @param fromAccountId     Account to debit. Must exist, not frozen, sufficient balance.
     * @param toAccountId       Account to credit. Must exist, not frozen.
     * @param amount            Amount to transfer. Must be positive.
     * @param simulateCreditFail If true, throws after debit succeeds — forces XA rollback.
     *                           Used in Sprint 2 to prove WAS rolls back the debit as well.
     * @throws FundsTransferException RuntimeException — WAS rolls back all XA resources.
     */
    public void transfer(long fromAccountId, long toAccountId,
                         BigDecimal amount, boolean simulateCreditFail) {

        // ── Input validation ──────────────────────────────────────────────────
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new FundsTransferException(
                "Amount must be positive. Received: " + amount);
        }
        if (fromAccountId <= 0 || toAccountId <= 0) {
            throw new FundsTransferException(
                "Account IDs must be positive integers.");
        }
        if (fromAccountId == toAccountId) {
            throw new FundsTransferException(
                "Source and destination accounts must be different.");
        }

        DataSource debitDs  = lookupDataSource(DEBIT_DS_JNDI);
        DataSource creditDs = lookupDataSource(CREDIT_DS_JNDI);

        // ── DEBIT via DEBIT_DS ────────────────────────────────────────────────
        // WAS enlists this XA connection in the active transaction automatically.
        // Do NOT call conn.commit() — the container manages the commit.
        try (Connection debitConn = debitDs.getConnection()) {

            validateAccount(debitConn, fromAccountId, amount, true);

            String debitSql = "UPDATE accounts SET balance = balance - ? WHERE id = ?";
            try (PreparedStatement stmt = debitConn.prepareStatement(debitSql)) {
                stmt.setBigDecimal(1, amount);
                stmt.setLong(2, fromAccountId);
                int rows = stmt.executeUpdate();
                if (rows != 1) {
                    throw new FundsTransferException(
                        "Debit UPDATE affected " + rows + " row(s) for account id="
                        + fromAccountId + ". Expected exactly 1.");
                }
            }
            // Debit SQL ran successfully and is held in the open XA transaction.
            // It is NOT yet committed — WAS has not received both Prepare votes yet.

        } catch (SQLException e) {
            throw new FundsTransferException(
                "Debit operation failed on account id=" + fromAccountId
                + ": " + e.getMessage(), e);
        }

        // ── Sprint 2 — Forced failure injection point ─────────────────────────
        // Throwing here proves WAS rolls back the debit even though its SQL ran.
        // The debit connection is still open and enlisted — WAS sends Rollback to it.
        if (simulateCreditFail) {
            throw new FundsTransferException(
                "SIMULATED FAILURE: Credit side forced to fail after debit SQL ran. "
                + "WAS is now rolling back the debit on DEBIT_DS as well. "
                + "Both account balances should be UNCHANGED after this. "
                + "Check the database to confirm.");
        }

        // ── CREDIT via CREDIT_DS ──────────────────────────────────────────────
        // Second XA connection — WAS enlists this one into the same transaction.
        // Now both connections are enrolled. WAS runs 2PC across both on exit.
        try (Connection creditConn = creditDs.getConnection()) {

            validateAccount(creditConn, toAccountId, null, false);

            String creditSql = "UPDATE accounts SET balance = balance + ? WHERE id = ?";
            try (PreparedStatement stmt = creditConn.prepareStatement(creditSql)) {
                stmt.setBigDecimal(1, amount);
                stmt.setLong(2, toAccountId);
                int rows = stmt.executeUpdate();
                if (rows != 1) {
                    throw new FundsTransferException(
                        "Credit UPDATE affected " + rows + " row(s) for account id="
                        + toAccountId + ". Expected exactly 1.");
                }
            }

        } catch (SQLException e) {
            throw new FundsTransferException(
                "Credit operation failed on account id=" + toAccountId
                + ": " + e.getMessage(), e);
        }

        // Both XA connections exit their try-with-resources blocks cleanly.
        // WAS now performs 2PC: Phase 1 Prepare on both, Phase 2 Commit on both.
        // No conn.commit() is ever called in this method.
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private DataSource lookupDataSource(String jndiName) {
        try {
            InitialContext ctx = new InitialContext();
            return (DataSource) ctx.lookup(jndiName);
        } catch (NamingException e) {
            throw new FundsTransferException(
                "Cannot look up DataSource [" + jndiName + "]: " + e.getMessage(), e);
        }
    }

    private void validateAccount(Connection conn, long accountId,
                                 BigDecimal required, boolean checkBalance)
            throws SQLException {
        String sql = "SELECT balance, is_frozen FROM accounts WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, accountId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new FundsTransferException(
                        "Account not found: id=" + accountId);
                }
                if (rs.getBoolean("is_frozen")) {
                    throw new FundsTransferException(
                        "Account is frozen: id=" + accountId);
                }
                if (checkBalance && required != null) {
                    BigDecimal balance = rs.getBigDecimal("balance");
                    if (balance.compareTo(required) < 0) {
                        throw new FundsTransferException(
                            "Insufficient funds in account id=" + accountId
                            + ". Balance=" + balance + ", Required=" + required);
                    }
                }
            }
        }
    }
}