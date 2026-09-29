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
import com.digistack.bank.service.WithdrawService;

/**
 * WithdrawServlet — P01 v6
 *
 * Handles GET  /Withdraw → display withdrawal form with account summary
 * Handles POST /Withdraw → process the withdrawal, PRG redirect
 *
 * GET flow:
 *   1. Session guard — redirect to /Login if no session.
 *   2. Load account via AccountService for the account summary panel.
 *   3. Read any result/error from redirect parameters.
 *   4. Forward to Withdraw.jsp.
 *
 * POST flow:
 *   1. Session guard.
 *   2. Read amount from form.
 *   3. Call WithdrawService.withdraw().
 *   4. On success  → redirect /Withdraw?result=success&amount=<n>
 *   5. On failure  → redirect /Withdraw?error=<message>
 *
 * TECHNICAL DEBT (v6): Services use direct JDBC.
 * Replaced at v7 with WAS-managed JNDI DataSource (jdbc/BankDS).
 */
@WebServlet(name = "WithdrawServlet",
            urlPatterns = {"/Withdraw", "/withdraw"})
public class WithdrawServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final AccountService  accountService  = new AccountService();
    private final WithdrawService withdrawService = new WithdrawService();

    /**
     * GET /Withdraw — load and display the withdrawal form.
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
        Account account  = null;
        String loadError = null;

        try {
            account = accountService.getAccountByUserId(userId);
            if (account == null) {
                loadError = "No account found. Please contact support.";
            }
        } catch (SQLException e) {
            log("WithdrawServlet GET: DB error for userId=" +
                userId + " — " + e.getMessage(), e);
            loadError = "Unable to load account. Please try again.";
        }

        // ── Read PRG parameters ──
        String result    = request.getParameter("result");
        String amountStr = request.getParameter("amount");
        String error     = request.getParameter("error");

        // ── Set Attributes for Withdraw.jsp ──
        request.setAttribute("account",   account);
        request.setAttribute("loadError", loadError);
        request.setAttribute("result",    result);
        request.setAttribute("amount",    amountStr);
        request.setAttribute("error",     error);

        request.setAttribute("username",
            session.getAttribute("username"));
        request.setAttribute("fullName",
            session.getAttribute("fullName"));
        request.setAttribute("role",
            session.getAttribute("role"));

        request.getRequestDispatcher("/Withdraw.jsp")
               .forward(request, response);
    }

    /**
     * POST /Withdraw — process the withdrawal amount.
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

        int userId   = (Integer) session.getAttribute("userId");
        String amount = request.getParameter("amount");

        try {
            Account updated = withdrawService.withdraw(userId, amount);

            log("WithdrawServlet: Withdrawal successful. " +
                "userId=" + userId +
                " amount=" + amount +
                " newBalance=" + updated.getFormattedBalance());

            String encoded =
                java.net.URLEncoder.encode(
                    amount != null ? amount.trim() : "", "UTF-8");

            response.sendRedirect(
                request.getContextPath() +
                "/Withdraw?result=success&amount=" + encoded);

        } catch (IllegalArgumentException e) {
            log("WithdrawServlet: Validation error. userId=" +
                userId + " — " + e.getMessage());
            redirectWithError(request, response, e.getMessage());

        } catch (InsufficientFundsException e) {
            log("WithdrawServlet: Insufficient funds. userId=" +
                userId +
                " requested=" + e.getRequestedAmount() +
                " balance="   + e.getCurrentBalance());
            redirectWithError(request, response,
                "Insufficient funds. Your balance is " +
                e.getCurrentBalance().toPlainString() +
                " and you requested " +
                e.getRequestedAmount().toPlainString() + ".");

        } catch (IllegalStateException e) {
            // Covers: account not found, account frozen
            log("WithdrawServlet: State error. userId=" +
                userId + " — " + e.getMessage());
            redirectWithError(request, response, e.getMessage());

        } catch (SQLException e) {
            log("WithdrawServlet: DB error. userId=" +
                userId + " — " + e.getMessage(), e);
            redirectWithError(request, response,
                "A system error occurred. Please try again.");
        }
    }

    /**
     * Redirects to GET /Withdraw with a URL-encoded error message.
     */
    private void redirectWithError(HttpServletRequest request,
                                   HttpServletResponse response,
                                   String message)
            throws IOException {
        String encoded;
        try {
            encoded = java.net.URLEncoder.encode(message, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            encoded = "An+error+occurred.";
        }
        response.sendRedirect(
            request.getContextPath() + "/Withdraw?error=" + encoded);
    }
}