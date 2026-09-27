package com.digistack.bank.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Account — P01 v3
 *
 * Model class representing one row from the accounts table.
 * Used to pass account data between DAO, Service, and Servlet
 * without exposing raw SQL ResultSets to upper layers.
 *
 * BigDecimal is used for balance — never float or double for money.
 * Reason: float/double cannot represent all decimal fractions exactly
 * in binary. For example, 0.1 + 0.2 in floating point = 0.30000000000000004.
 * BigDecimal stores exact decimal values — mandatory for financial data.
 */
public class Account {

    private int        id;
    private int        userId;
    private String     accountNumber;
    private String     accountType;
    private BigDecimal balance;
    private boolean    frozen;
    private Timestamp  createdAt;
    private Timestamp  updatedAt;

    // Default constructor — required for instantiation in DAO
    public Account() {}

    // ── Getters ──

    public int getId()                  { return id; }
    public int getUserId()              { return userId; }
    public String getAccountNumber()    { return accountNumber; }
    public String getAccountType()      { return accountType; }
    public BigDecimal getBalance()      { return balance; }
    public boolean isFrozen()           { return frozen; }
    public Timestamp getCreatedAt()     { return createdAt; }
    public Timestamp getUpdatedAt()     { return updatedAt; }

    // ── Setters ──

    public void setId(int id)                          { this.id = id; }
    public void setUserId(int userId)                  { this.userId = userId; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public void setAccountType(String accountType)     { this.accountType = accountType; }
    public void setBalance(BigDecimal balance)         { this.balance = balance; }
    public void setFrozen(boolean frozen)              { this.frozen = frozen; }
    public void setCreatedAt(Timestamp createdAt)      { this.createdAt = createdAt; }
    public void setUpdatedAt(Timestamp updatedAt)      { this.updatedAt = updatedAt; }

    /**
     * Returns a formatted balance string with Indian Rupee symbol.
     * Used in JSPs via account.getFormattedBalance().
     * Example: BigDecimal("50000.00") → "₹50,000.00"
     */
    public String getFormattedBalance() {
        if (balance == null) return "₹0.00";
        // Format with comma separators and 2 decimal places
        java.text.NumberFormat fmt =
            java.text.NumberFormat.getNumberInstance(
                new java.util.Locale("en", "IN"));
        fmt.setMinimumFractionDigits(2);
        fmt.setMaximumFractionDigits(2);
        return "₹" + fmt.format(balance);
    }

    /**
     * Returns masked account number for display.
     * Example: "DSB0000000001" → "**** **** 0001"
     */
    public String getMaskedAccountNumber() {
        if (accountNumber == null || accountNumber.length() < 4) {
            return "****";
        }
        String last4 = accountNumber.substring(
            accountNumber.length() - 4);
        return "**** **** " + last4;
    }

    @Override
    public String toString() {
        return "Account{id=" + id +
               ", userId=" + userId +
               ", accountNumber=" + accountNumber +
               ", type=" + accountType +
               ", balance=" + balance +
               ", frozen=" + frozen + "}";
    }
}