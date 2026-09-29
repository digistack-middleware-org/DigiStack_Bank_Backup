package com.digistack.bank.exception;

/**
 * InsufficientFundsException — P01 v3
 *
 * Thrown by AccountService.withdraw() when the requested
 * withdrawal amount exceeds the current account balance.
 *
 * This is a checked exception (extends Exception, not
 * RuntimeException) — callers must explicitly handle it.
 * This is intentional: insufficient funds is a foreseeable
 * business condition, not a programming error.
 *
 * Used by:
 *   AccountService.withdraw() — throws this
 *   AccountServlet.doPost()   — catches this, shows user message
 */
public class InsufficientFundsException extends Exception {

    private static final long serialVersionUID = 1L;

    // The balance at the time of the failed withdrawal attempt.
    // Stored so the error message can show the current balance.
    private final java.math.BigDecimal currentBalance;

    // The amount that was requested but could not be fulfilled.
    private final java.math.BigDecimal requestedAmount;

    public InsufficientFundsException(
            java.math.BigDecimal currentBalance,
            java.math.BigDecimal requestedAmount) {

        super("Insufficient funds: requested "
            + requestedAmount
            + " but available balance is "
            + currentBalance);

        this.currentBalance   = currentBalance;
        this.requestedAmount  = requestedAmount;
    }

    public java.math.BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public java.math.BigDecimal getRequestedAmount() {
        return requestedAmount;
    }
}