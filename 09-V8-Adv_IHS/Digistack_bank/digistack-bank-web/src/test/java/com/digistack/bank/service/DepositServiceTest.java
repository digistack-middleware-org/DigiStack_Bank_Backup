package com.digistack.bank.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.digistack.bank.dao.AccountDao;
import com.digistack.bank.model.Account;

/**
 * DepositServiceTest — P01 v6
 *
 * Unit tests for DepositService, satisfying standing rule TEST01.
 *
 * Strategy:
 *   - AccountDao is mocked with Mockito — no real database is
 *     touched. This makes tests fast (milliseconds) and repeatable
 *     regardless of whether dsb-db is powered on.
 *   - getConnection() is overridden in a test subclass to return
 *     a mocked Connection object — the try-with-resources block
 *     in DepositService.deposit() calls conn.close() automatically,
 *     which Mockito handles safely with no real socket involved.
 *
 * What is NOT tested here:
 *   - The actual SQL in AccountDao (that requires either a real
 *     DB or an in-memory DB like H2 — out of scope for TEST01,
 *     which is a Service-layer requirement only).
 *   - Manual test cases in TestCases-v6.md still cover full
 *     end-to-end behaviour including real SQL.
 */
@ExtendWith(MockitoExtension.class)
class DepositServiceTest {

    @Mock
    private AccountDao mockDao;

    private DepositService depositService;

    /**
     * A test-only subclass of DepositService.
     * Overrides getConnection() to return a mock Connection instead
     * of opening a real socket to 192.168.10.30:5432.
     */
    private static class TestableDepositService extends DepositService {
        private final Connection mockConnection;

        TestableDepositService(AccountDao dao, Connection mockConnection) {
            super(dao);
            this.mockConnection = mockConnection;
        }

        @Override
        protected Connection getConnection() {
            return mockConnection;
        }
    }

    @BeforeEach
    void setUp() {
        Connection mockConnection = mock(Connection.class);
        depositService = new TestableDepositService(mockDao, mockConnection);
    }

    // ── Happy path ──────────────────────────────────────────────

    @Test
    void deposit_validAmount_updatesBalanceSuccessfully()
            throws SQLException {

        // Arrange — an active (not frozen) account with ₹1000 balance
        Account existingAccount = new Account();
        existingAccount.setId(1);
        existingAccount.setUserId(101);
        existingAccount.setBalance(new BigDecimal("1000.00"));
        existingAccount.setFrozen(false);

        Account refreshedAccount = new Account();
        refreshedAccount.setId(1);
        refreshedAccount.setUserId(101);
        refreshedAccount.setBalance(new BigDecimal("1500.00"));
        refreshedAccount.setFrozen(false);

        when(mockDao.findByUserId(any(Connection.class), anyInt()))
            .thenReturn(existingAccount);
        when(mockDao.deposit(any(Connection.class), anyInt(), any(BigDecimal.class)))
            .thenReturn(true);
        when(mockDao.findById(any(Connection.class), anyInt()))
            .thenReturn(refreshedAccount);

        // Act
        Account result = depositService.deposit(101, "500.00");

        // Assert
        assertEquals(new BigDecimal("1500.00"), result.getBalance());
    }

    // ── Validation failures ─────────────────────────────────────

    @Test
    void deposit_nullAmount_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> depositService.deposit(101, null));
    }

    @Test
    void deposit_emptyAmount_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> depositService.deposit(101, "  "));
    }

    @Test
    void deposit_nonNumericAmount_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> depositService.deposit(101, "abc"));
    }

    @Test
    void deposit_zeroAmount_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> depositService.deposit(101, "0"));
    }

    @Test
    void deposit_negativeAmount_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> depositService.deposit(101, "-50"));
    }

    // ── Business rule failures ──────────────────────────────────

    @Test
    void deposit_accountNotFound_throwsIllegalStateException()
            throws SQLException {

        when(mockDao.findByUserId(any(Connection.class), anyInt()))
            .thenReturn(null);

        assertThrows(IllegalStateException.class,
            () -> depositService.deposit(999, "100"));
    }

    @Test
    void deposit_frozenAccount_throwsIllegalStateException()
            throws SQLException {

        Account frozenAccount = new Account();
        frozenAccount.setId(1);
        frozenAccount.setUserId(101);
        frozenAccount.setBalance(new BigDecimal("1000.00"));
        frozenAccount.setFrozen(true);

        when(mockDao.findByUserId(any(Connection.class), anyInt()))
            .thenReturn(frozenAccount);

        assertThrows(IllegalStateException.class,
            () -> depositService.deposit(101, "100"));
    }

    @Test
    void deposit_daoReturnsFalse_throwsIllegalStateException()
            throws SQLException {

        // Simulates a race condition: account was active when we
        // checked, but the UPDATE affected 0 rows (e.g. frozen
        // between our check and the SQL execution).
        Account existingAccount = new Account();
        existingAccount.setId(1);
        existingAccount.setUserId(101);
        existingAccount.setBalance(new BigDecimal("1000.00"));
        existingAccount.setFrozen(false);

        when(mockDao.findByUserId(any(Connection.class), anyInt()))
            .thenReturn(existingAccount);
        when(mockDao.deposit(any(Connection.class), anyInt(), any(BigDecimal.class)))
            .thenReturn(false);

        assertThrows(IllegalStateException.class,
            () -> depositService.deposit(101, "100"));
    }
}