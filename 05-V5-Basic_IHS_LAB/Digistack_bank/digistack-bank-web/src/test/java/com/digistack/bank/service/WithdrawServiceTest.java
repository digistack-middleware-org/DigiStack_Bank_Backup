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
import com.digistack.bank.exception.InsufficientFundsException;
import com.digistack.bank.model.Account;

/**
 * WithdrawServiceTest — P01 v6
 *
 * Unit tests for WithdrawService, satisfying standing rule TEST01.
 * Same mocking strategy as DepositServiceTest — see that file's
 * class comment for the full explanation.
 */
@ExtendWith(MockitoExtension.class)
class WithdrawServiceTest {

    @Mock
    private AccountDao mockDao;

    private WithdrawService withdrawService;

    private static class TestableWithdrawService extends WithdrawService {
        private final Connection mockConnection;

        TestableWithdrawService(AccountDao dao, Connection mockConnection) {
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
        withdrawService = new TestableWithdrawService(mockDao, mockConnection);
    }

    // ── Happy path ──────────────────────────────────────────────

    @Test
    void withdraw_sufficientFunds_updatesBalanceSuccessfully()
            throws SQLException, InsufficientFundsException {

        Account existingAccount = new Account();
        existingAccount.setId(1);
        existingAccount.setUserId(101);
        existingAccount.setBalance(new BigDecimal("1000.00"));
        existingAccount.setFrozen(false);

        Account refreshedAccount = new Account();
        refreshedAccount.setId(1);
        refreshedAccount.setUserId(101);
        refreshedAccount.setBalance(new BigDecimal("600.00"));
        refreshedAccount.setFrozen(false);

        when(mockDao.findByUserId(any(Connection.class), anyInt()))
            .thenReturn(existingAccount);
        when(mockDao.withdraw(any(Connection.class), anyInt(), any(BigDecimal.class)))
            .thenReturn(true);
        when(mockDao.findById(any(Connection.class), anyInt()))
            .thenReturn(refreshedAccount);

        Account result = withdrawService.withdraw(101, "400.00");

        assertEquals(new BigDecimal("600.00"), result.getBalance());
    }

    // ── Validation failures ─────────────────────────────────────

    @Test
    void withdraw_nullAmount_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> withdrawService.withdraw(101, null));
    }

    @Test
    void withdraw_zeroAmount_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> withdrawService.withdraw(101, "0"));
    }

    @Test
    void withdraw_negativeAmount_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> withdrawService.withdraw(101, "-10"));
    }

    // ── Business rule failures ──────────────────────────────────

    @Test
    void withdraw_accountNotFound_throwsIllegalStateException()
            throws SQLException {

        when(mockDao.findByUserId(any(Connection.class), anyInt()))
            .thenReturn(null);

        assertThrows(IllegalStateException.class,
            () -> withdrawService.withdraw(999, "100"));
    }

    @Test
    void withdraw_frozenAccount_throwsIllegalStateException()
            throws SQLException {

        Account frozenAccount = new Account();
        frozenAccount.setId(1);
        frozenAccount.setUserId(101);
        frozenAccount.setBalance(new BigDecimal("1000.00"));
        frozenAccount.setFrozen(true);

        when(mockDao.findByUserId(any(Connection.class), anyInt()))
            .thenReturn(frozenAccount);

        assertThrows(IllegalStateException.class,
            () -> withdrawService.withdraw(101, "100"));
    }

    @Test
    void withdraw_insufficientFunds_throwsInsufficientFundsException()
            throws SQLException {

        Account existingAccount = new Account();
        existingAccount.setId(1);
        existingAccount.setUserId(101);
        existingAccount.setBalance(new BigDecimal("100.00"));
        existingAccount.setFrozen(false);

        when(mockDao.findByUserId(any(Connection.class), anyInt()))
            .thenReturn(existingAccount);

        // Requesting ₹500 from a ₹100 balance — should fail
        // before the DAO is even called.
        assertThrows(InsufficientFundsException.class,
            () -> withdrawService.withdraw(101, "500.00"));
    }

    @Test
    void withdraw_daoReturnsFalse_throwsInsufficientFundsException()
            throws SQLException {

        // Simulates a race condition: balance was sufficient at
        // Java-level check, but the DB-level WHERE clause
        // (balance >= ?) rejected the UPDATE anyway.
        Account existingAccount = new Account();
        existingAccount.setId(1);
        existingAccount.setUserId(101);
        existingAccount.setBalance(new BigDecimal("1000.00"));
        existingAccount.setFrozen(false);

        when(mockDao.findByUserId(any(Connection.class), anyInt()))
            .thenReturn(existingAccount);
        when(mockDao.withdraw(any(Connection.class), anyInt(), any(BigDecimal.class)))
            .thenReturn(false);

        assertThrows(InsufficientFundsException.class,
            () -> withdrawService.withdraw(101, "100.00"));
    }
}