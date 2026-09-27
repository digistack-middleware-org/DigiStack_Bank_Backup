package com.digistack.bank.xa;

/**
 * FundsTransferException — v8.5 XA Lab.
 *
 * Extends RuntimeException deliberately.
 *
 * In EJB CMT with @TransactionAttribute(REQUIRED), throwing a RuntimeException
 * from inside the EJB method causes WAS to:
 *   1. Mark the current transaction rollback-only.
 *   2. Roll back ALL enlisted XA resources (jdbc/DebitDS and jdbc/CreditDS)
 *      after the method exits.
 *
 * Your code never calls Connection.rollback() or UserTransaction.rollback().
 * Throwing this exception is the entire rollback mechanism.
 *
 * This is the standard enterprise pattern for CMT error signalling.
 */
public class FundsTransferException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public FundsTransferException(String message) {
        super(message);
    }

    public FundsTransferException(String message, Throwable cause) {
        super(message, cause);
    }
}