package com.digistack.bank.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
 * FreezeServiceTest — P01 v6
 *
 * Unit tests for FreezeService, satisfying standing rule TEST01.
 * Covers both freeze() and unfreeze() since they share one class.
 */
@ExtendWith(MockitoExtension.class)
class FreezeServiceTest {

    @Mock
    private AccountDao mockDao;

    private FreezeService freezeService;

    private static class TestableFreezeService extends FreezeService {
        private final Connection mockConnection;

        TestableFreezeService(AccountDao dao, Connection mockConnection) {
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
        freezeService = new TestableFreezeService(mockDao, mockConnection);
    }

    // ── freeze() — happy path ───────────────────────────────────

    @Test
    void freeze_activeAccount_setsFrozenTrue() throws SQLException {

        Account activeAccount = new Account();
        activeAccount.setId(1);
        activeAccount.setFrozen(false);

        Account frozenAccount = new Account();
        frozenAccount.setId(1);
        frozenAccount.setFrozen(true);

        // findById is called twice inside freeze(): once to check
        // current state, once to return the refreshed account.
        // thenReturn(a, b) returns activeAccount on the 1st call
        // and frozenAccount on every call after.
        when(mockDao.findById(org.mockito.ArgumentMatchers.any(Connection.class), anyInt()))
            .thenReturn(activeAccount, frozenAccount);
        when(mockDao.freeze(org.mockito.ArgumentMatchers.any(Connection.class), anyInt()))
            .thenReturn(true);

        Account result = freezeService.freeze(1);

        assertTrue(result.isFrozen());
    }
    // ── freeze() — failure paths ────────────────────────────────

    @Test
    void freeze_accountNotFound_throwsIllegalStateException()
            throws SQLException {

        when(mockDao.findById(org.mockito.ArgumentMatchers.any(Connection.class), anyInt()))
            .thenReturn(null);

        assertThrows(IllegalStateException.class,
            () -> freezeService.freeze(999));
    }

    @Test
    void freeze_alreadyFrozen_throwsIllegalStateException()
            throws SQLException {

        Account alreadyFrozen = new Account();
        alreadyFrozen.setId(1);
        alreadyFrozen.setFrozen(true);

        when(mockDao.findById(org.mockito.ArgumentMatchers.any(Connection.class), anyInt()))
            .thenReturn(alreadyFrozen);

        assertThrows(IllegalStateException.class,
            () -> freezeService.freeze(1));
    }

    @Test
    void freeze_daoReturnsFalse_throwsIllegalStateException()
            throws SQLException {

        Account activeAccount = new Account();
        activeAccount.setId(1);
        activeAccount.setFrozen(false);

        when(mockDao.findById(org.mockito.ArgumentMatchers.any(Connection.class), anyInt()))
            .thenReturn(activeAccount);
        when(mockDao.freeze(org.mockito.ArgumentMatchers.any(Connection.class), anyInt()))
            .thenReturn(false);

        assertThrows(IllegalStateException.class,
            () -> freezeService.freeze(1));
    }

    // ── unfreeze() — happy path ──────────────────────────────────

    @Test
    void unfreeze_frozenAccount_setsFrozenFalse() throws SQLException {

        Account frozenAccount = new Account();
        frozenAccount.setId(1);
        frozenAccount.setFrozen(true);

        Account activeAccount = new Account();
        activeAccount.setId(1);
        activeAccount.setFrozen(false);

        when(mockDao.findById(org.mockito.ArgumentMatchers.any(Connection.class), anyInt()))
            .thenReturn(frozenAccount, activeAccount);
        when(mockDao.unfreeze(org.mockito.ArgumentMatchers.any(Connection.class), anyInt()))
            .thenReturn(true);

        Account result = freezeService.unfreeze(1);

        assertFalse(result.isFrozen());
    }

    // ── unfreeze() — failure paths ───────────────────────────────

    @Test
    void unfreeze_accountNotFound_throwsIllegalStateException()
            throws SQLException {

        when(mockDao.findById(org.mockito.ArgumentMatchers.any(Connection.class), anyInt()))
            .thenReturn(null);

        assertThrows(IllegalStateException.class,
            () -> freezeService.unfreeze(999));
    }

    @Test
    void unfreeze_notFrozen_throwsIllegalStateException()
            throws SQLException {

        Account activeAccount = new Account();
        activeAccount.setId(1);
        activeAccount.setFrozen(false);

        when(mockDao.findById(org.mockito.ArgumentMatchers.any(Connection.class), anyInt()))
            .thenReturn(activeAccount);

        assertThrows(IllegalStateException.class,
            () -> freezeService.unfreeze(1));
    }

    @Test
    void unfreeze_daoReturnsFalse_throwsIllegalStateException()
            throws SQLException {

        Account frozenAccount = new Account();
        frozenAccount.setId(1);
        frozenAccount.setFrozen(true);

        when(mockDao.findById(org.mockito.ArgumentMatchers.any(Connection.class), anyInt()))
            .thenReturn(frozenAccount);
        when(mockDao.unfreeze(org.mockito.ArgumentMatchers.any(Connection.class), anyInt()))
            .thenReturn(false);

        assertThrows(IllegalStateException.class,
            () -> freezeService.unfreeze(1));
    }
}