package com.digistack.bank.servlet;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.digistack.bank.model.Account;
import com.digistack.bank.service.AccountService;
import com.digistack.bank.service.DepositService;

/**
 * DepositServlet — P01 v6
 *
 * Handles GET  /Deposit → display deposit form with account summary
 * Handles POST /Deposit → process the deposit, PRG redirect
 *
 * GET flow:
 *   1. Session guard — redirect to /Login if no session.
 *   2. Load account via AccountService for the account summary panel.
 *   3. Read any result/error from redirect parameters (PRG pattern).
 *   4. Forward to Deposit.jsp.
 *
 * POST flow:
 *   1. Session guard.
 *   2. Read amount from form.
 *   3. Call DepositService.deposit().
 *   4. On success  → redirect /Deposit?result=success&amount=<n>
 *   5. On failure  → redirect /Deposit?error=<message>
 *
 * PRG = Post/Redirect/Get. After a POST that modifies data, always
 * redirect so that pressing browser Back does not re-submit the form.
 *
 * TECHNICAL DEBT (v6): Services use direct JDBC.
 * Replaced at v7 with WAS-managed JNDI DataSource (jdbc/BankDS).
 */
@WebServlet(name = "DepositServlet",
            urlPatterns = {"/Deposit", "/deposit"})
public class DepositServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final AccountService accountService = new AccountService();
    private final DepositService depositService = new DepositService();

    /**
     * GET /Deposit — load and display the deposit form.
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
        // The account summary panel in Deposit.jsp needs the
        // masked account number, type, balance, and frozen status.
        Account account  = null;
        String loadError = null;

        try {
            account = accountService.getAccountByUserId(userId);
            if (account == null) {
                loadError = "No account found. Please contact support.";
            }
        } catch (SQLException e) {
            log("DepositServlet GET: DB error for userId=" +
                userId + " — " + e.getMessage(), e);
            loadError = "Unable to load account. Please try again.";
        }

        // ── Read PRG parameters from the redirect after POST ──
        // result = "success" means the previous POST completed OK.
        // error  = message means the previous POST failed.
        String result    = request.getParameter("result");
        String amountStr = request.getParameter("amount");
        String error     = request.getParameter("error");

        // ── Set Attributes for Deposit.jsp ──
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

        request.getRequestDispatcher("/Deposit.jsp")
               .forward(request, response);
    }

    /**
     * POST /Deposit — process the deposit amount.
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
            Account updated = depositService.deposit(userId, amount);

            log("DepositServlet: Deposit successful. " +
                "userId=" + userId +
                " amount=" + amount +
                " newBalance=" + updated.getFormattedBalance());

            // PRG — redirect to GET with success parameters
            String encoded =
                java.net.URLEncoder.encode(
                    amount != null ? amount.trim() : "", "UTF-8");

            response.sendRedirect(
                request.getContextPath() +
                "/Deposit?result=success&amount=" + encoded);

        } catch (IllegalArgumentException e) {
            log("DepositServlet: Validation error. userId=" +
                userId + " — " + e.getMessage());
            redirectWithError(request, response, e.getMessage());

        } catch (IllegalStateException e) {
            // Covers: account not found, account frozen
            log("DepositServlet: State error. userId=" +
                userId + " — " + e.getMessage());
            redirectWithError(request, response, e.getMessage());

        } catch (SQLException e) {
            log("DepositServlet: DB error. userId=" +
                userId + " — " + e.getMessage(), e);
            redirectWithError(request, response,
                "A system error occurred. Please try again.");
        }
    }

    /**
     * Redirects to GET /Deposit with a URL-encoded error message.
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
            request.getContextPath() + "/Deposit?error=" + encoded);
    }
}