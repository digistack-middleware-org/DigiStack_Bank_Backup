package com.digistack.bank.xa;

/**
 * NonXaOrphanException — v8.5 Sprint 2 negative control.
 *
 * Thrown by NonXaControlBean.simulateOrphanedDebit() after the debit SQL
 * has already been auto-committed via a non-XA connection.
 *
 * The debit is permanent at the moment this exception is constructed.
 * There is no rollback. This is what XA exists to prevent.
 */
public class NonXaOrphanException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public NonXaOrphanException(String message) {
        super(message);
    }

    public NonXaOrphanException(String message, Throwable cause) {
        super(message, cause);
    }
}