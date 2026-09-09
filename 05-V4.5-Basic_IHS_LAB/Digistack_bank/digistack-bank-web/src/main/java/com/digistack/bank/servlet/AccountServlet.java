package com.digistack.bank.servlet;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.digistack.bank.exception.InsufficientFundsException;
import com.digistack.bank.model.Account;
import com.digistack.bank.service.AccountService;

/**
 * AccountServlet — P01 v3
 *
 * Handles GET  /Account → display balance and deposit/withdraw form
 * Handles POST /Account → process deposit or withdrawal
 *
 * GET flow:
 *   1. Session guard — redirect to /Login if no session.
 *   2. Call AccountService.getAccountByUserId() to load balance.
 *   3. Read any result/error message from redirect parameters.
 *   4. Set attributes and forward to Account.jsp.
 *
 * POST flow:
 *   1. Session guard.
 *   2. Read action (deposit/withdraw) and amount from form.
 *   3. Call AccountService.deposit() or AccountService.withdraw().
 *   4. On success  → redirect to /Account?result=success&action=deposit
 *   5. On failure  → redirect to /Account?error=<message>
 *   (PRG pattern — never forward after a POST that modifies data)
 *
 * TECHNICAL DEBT (v3): AccountService uses direct JDBC.
 * Replaced at v7 with JNDI DataSource (jdbc/BankDS).
 */
@WebServlet(name = "AccountServlet",
            urlPatterns = {"/Account", "/account"})
public class AccountServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final AccountService accountService = new AccountService();

    /**
     * GET /Account — load and display the account page.
     */
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        // ── Session Guard ──
        HttpSession session = request.getSession(false);
        if (session == null ||
                session.getAttribute("userId") == null) {
            response.sendRedirect(
                request.getContextPath() + "/Login");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");

        // ── Load Account ──
        Account account = null;
        String loadError = null;

        try {
            account = accountService.getAccountByUserId(userId);
            if (account == null) {
                loadError = "No account found. Please contact support.";
            }
        } catch (SQLException e) {
            log("AccountServlet GET: DB error for userId=" +
                userId + " — " + e.getMessage(), e);
            loadError = "Unable to load account. Please try again.";
        }

        // ── Read PRG result/error parameters ──
        // These are set by the POST redirect below.
        // Example: /Account?result=success&action=deposit&amount=1000
        String result    = request.getParameter("result");
        String action    = request.getParameter("action");
        String amountStr = request.getParameter("amount");
        String error     = request.getParameter("error");

        // ── Set attributes for Account.jsp ──
        request.setAttribute("account",   account);
        request.setAttribute("loadError", loadError);
        request.setAttribute("result",    result);
        request.setAttribute("action",    action);
        request.setAttribute("amount",    amountStr);
        request.setAttribute("error",     error);

        // Read session display attributes
        request.setAttribute("username",
            session.getAttribute("username"));
        request.setAttribute("fullName",
            session.getAttribute("fullName"));
        request.setAttribute("role",
            session.getAttribute("role"));

        request.getRequestDispatcher("/Account.jsp")
               .forward(request, response);
    }

    /**
     * POST /Account — process deposit or withdrawal.
     * Uses PRG pattern — always redirects after processing.
     */
    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        // ── Session Guard ──
        HttpSession session = request.getSession(false);
        if (session == null ||
                session.getAttribute("userId") == null) {
            response.sendRedirect(
                request.getContextPath() + "/Login");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");

        // ── Read Form Fields ──
        String action    = request.getParameter("action");
        String amountStr = request.getParameter("amount");

        // Validate action field
        if (action == null ||
                (!action.equals("deposit") &&
                 !action.equals("withdraw"))) {
            redirectWithError(request, response,
                "Invalid action. Please use the form.");
            return;
        }

        // ── Process Transaction ──
        try {
            Account updatedAccount;

            if ("deposit".equals(action)) {
                updatedAccount = accountService.deposit(
                    userId, amountStr);
                log("AccountServlet: Deposit successful. " +
                    "userId=" + userId +
                    " amount=" + amountStr +
                    " newBalance=" +
                    updatedAccount.getFormattedBalance());
            } else {
                // withdraw
                updatedAccount = accountService.withdraw(
                    userId, amountStr);
                log("AccountServlet: Withdraw successful. " +
                    "userId=" + userId +
                    " amount=" + amountStr +
                    " newBalance=" +
                    updatedAccount.getFormattedBalance());
            }

            // ── PRG — redirect with success parameters ──
            // URL-encode the amount for safe transmission
            String encodedAmount =
                java.net.URLEncoder.encode(amountStr.trim(), "UTF-8");

            response.sendRedirect(
                request.getContextPath() +
                "/Account?result=success" +
                "&action=" + action +
                "&amount=" + encodedAmount);

        } catch (IllegalArgumentException e) {
            // Invalid input (bad amount format, negative, zero)
            log("AccountServlet: Validation error. " +
                "userId=" + userId + " — " + e.getMessage());
            redirectWithError(request, response, e.getMessage());

        } catch (InsufficientFundsException e) {
            // Not enough balance for withdrawal
            log("AccountServlet: Insufficient funds. " +
                "userId=" + userId +
                " requested=" + e.getRequestedAmount() +
                " balance=" + e.getCurrentBalance());
            redirectWithError(request, response,
                "Insufficient funds. Your current balance is " +
                formatAmount(e.getCurrentBalance()) +
                " and you requested " +
                formatAmount(e.getRequestedAmount()) + ".");

        } catch (IllegalStateException e) {
            // Frozen account or account not found
            log("AccountServlet: State error. " +
                "userId=" + userId + " — " + e.getMessage());
            redirectWithError(request, response, e.getMessage());

        } catch (SQLException e) {
            // Database error
            log("AccountServlet: DB error. " +
                "userId=" + userId + " — " + e.getMessage(), e);
            redirectWithError(request, response,
                "A system error occurred. Please try again.");
        }
    }

    /**
     * Redirects to /Account with an error parameter.
     * URL-encodes the error message for safe transmission.
     */
    private void redirectWithError(HttpServletRequest request,
                                    HttpServletResponse response,
                                    String errorMessage)
            throws IOException {
        String encoded;
        try {
            encoded = java.net.URLEncoder.encode(
                errorMessage, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            encoded = "An+error+occurred.";
        }
        response.sendRedirect(
            request.getContextPath() +
            "/Account?error=" + encoded);
    }

    /**
     * Formats a BigDecimal as a Rupee amount string.
     * Example: 50000.00 → "₹50,000.00"
     */
    private String formatAmount(java.math.BigDecimal amount) {
        if (amount == null) return "₹0.00";
        java.text.NumberFormat fmt =
            java.text.NumberFormat.getNumberInstance(
                new java.util.Locale("en", "IN"));
        fmt.setMinimumFractionDigits(2);
        fmt.setMaximumFractionDigits(2);
        return "₹" + fmt.format(amount);
    }
}