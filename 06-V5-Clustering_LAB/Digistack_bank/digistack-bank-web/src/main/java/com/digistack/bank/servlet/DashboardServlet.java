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
 * DashboardServlet — P01 v3 (updated)
 *
 * Handles GET /Dashboard
 *
 * Responsibilities:
 *   1. Session guard — redirect to /Login if no valid session.
 *   2. Read session attributes set by LoginServlet.
 *   3. Load the user's account via AccountService — provides
 *      masked account number, type, and frozen status for display.
 *   4. Determine time-of-day greeting (server-side).
 *   5. Forward to Dashboard.jsp with all display attributes set.
 *
 * v3 change: AccountService call added to load account data.
 *   Dashboard now shows live account number and frozen status.
 *   Balance is fetched separately via /BalanceJson (AJAX).
 *
 * TECHNICAL DEBT (v3): AccountService uses direct JDBC.
 * Replaced at v7 with JNDI DataSource (jdbc/BankDS).
 */
@WebServlet(name = "DashboardServlet",
            urlPatterns = {"/Dashboard", "/dashboard"})
public class DashboardServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final AccountService accountService =
        new AccountService();

    @Override
    public void init() throws ServletException {
        try {
            Class.forName("org.postgresql.Driver");
            log("DashboardServlet: PostgreSQL JDBC driver loaded.");
        } catch (ClassNotFoundException e) {
            throw new ServletException(
                "DashboardServlet init failed — " +
                "JDBC driver not found.", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        // ── Session Guard ──
        HttpSession session = request.getSession(false);
        if (session == null ||
                session.getAttribute("username") == null) {
            log("DashboardServlet: No session — " +
                "redirecting to Login.");
            response.sendRedirect(
                request.getContextPath() + "/Login");
            return;
        }

        // ── Read Session Attributes ──
        String username  = (String) session.getAttribute("username");
        String fullName  = (String) session.getAttribute("fullName");
        String role      = (String) session.getAttribute("role");
        String lastLogin = (String) session.getAttribute("lastLogin");
        String email     = (String) session.getAttribute("email");
        int    userId    = (Integer) session.getAttribute("userId");

        // Display name — fullName preferred, username as fallback
        String displayName =
            (fullName != null && !fullName.isEmpty())
            ? fullName : username;

        // ── Time-of-Day Greeting ──
        java.util.Calendar cal = java.util.Calendar.getInstance();
        int hour = cal.get(java.util.Calendar.HOUR_OF_DAY);
        String greeting;
        if (hour >= 5 && hour < 12) {
            greeting = "Good Morning";
        } else if (hour >= 12 && hour < 17) {
            greeting = "Good Afternoon";
        } else {
            greeting = "Good Evening";
        }

        // ── Load Account Data ──
        // Used to display masked account number, account type,
        // and frozen status on the Dashboard account card.
        // Balance is fetched separately via /BalanceJson (AJAX)
        // when the user clicks View Balance — not loaded here
        // on every Dashboard visit to keep the page fast.
        Account account    = null;
        String accountError = null;

        try {
            account = accountService.getAccountByUserId(userId);
            if (account == null) {
                accountError = "No account linked to this user.";
                log("DashboardServlet: No account found " +
                    "for userId=" + userId);
            } else {
                log("DashboardServlet: Account loaded for " +
                    "userId=" + userId +
                    " accountNumber=" +
                    account.getAccountNumber() +
                    " frozen=" + account.isFrozen());
            }
        } catch (SQLException e) {
            accountError = "Could not load account details.";
            log("DashboardServlet: DB error loading account " +
                "for userId=" + userId +
                " — " + e.getMessage(), e);
        }

        // ── Set Request Attributes for Dashboard.jsp ──
        request.setAttribute("displayName",  displayName);
        request.setAttribute("username",     username);
        request.setAttribute("role",         role);
        request.setAttribute("lastLogin",    lastLogin);
        request.setAttribute("greeting",     greeting);
        request.setAttribute("email",        email);
        request.setAttribute("account",      account);
        request.setAttribute("accountError", accountError);

        // Forward to Dashboard.jsp
        request.getRequestDispatcher("/Dashboard.jsp")
               .forward(request, response);
    }
}