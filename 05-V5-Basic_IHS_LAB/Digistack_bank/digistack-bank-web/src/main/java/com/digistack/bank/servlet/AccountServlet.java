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

/**
 * AccountServlet — P01 v6 (trimmed from v3)
 *
 * Handles GET /Account → display account details (view only).
 *
 * v6 change: POST handling removed entirely.
 *   Deposit  → now handled by DepositServlet  (/Deposit)
 *   Withdraw → now handled by WithdrawServlet (/Withdraw)
 *
 * This servlet's only job now is to load the account for the
 * logged-in user and forward to Account.jsp for display.
 * Account.jsp in v6 shows account details plus navigation
 * links to /Deposit, /Withdraw, /Freeze, /Unfreeze.
 *
 * TECHNICAL DEBT (v6): AccountService uses direct JDBC.
 * Replaced at v7 with WAS-managed JNDI DataSource (jdbc/BankDS).
 */
@WebServlet(name = "AccountServlet",
            urlPatterns = {"/Account", "/account"})
public class AccountServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final AccountService accountService = new AccountService();

    /**
     * GET /Account — load and display the account details page.
     */
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        // ── Session Guard ──
        // If no valid session exists, send user back to login.
        HttpSession session = request.getSession(false);
        if (session == null ||
                session.getAttribute("userId") == null) {
            response.sendRedirect(
                request.getContextPath() + "/Login");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");

        // ── Load Account ──
        Account account   = null;
        String loadError  = null;

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

        // ── Set Request Attributes for Account.jsp ──
        request.setAttribute("account",   account);
        request.setAttribute("loadError", loadError);

        // Pass session display values to JSP
        request.setAttribute("username",
            session.getAttribute("username"));
        request.setAttribute("fullName",
            session.getAttribute("fullName"));
        request.setAttribute("role",
            session.getAttribute("role"));

        request.getRequestDispatcher("/Account.jsp")
               .forward(request, response);
    }
}