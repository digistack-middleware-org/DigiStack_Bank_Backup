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
 * DashboardServlet — P01 v7 (updated from v3)
 *
 * Handles GET /Dashboard → load account, set greeting, forward to
 * Dashboard.jsp.
 *
 * v7 change: init() method removed entirely.
 *   The v3 init() called Class.forName("org.postgresql.Driver") to
 *   register the PostgreSQL JDBC driver before AccountService could
 *   call DriverManager.getConnection(). This is no longer needed:
 *     - AccountService.getConnection() now uses JNDI (Sprint 4)
 *     - WAS loads the JDBC driver via the JDBC Provider classpath
 *       (Sprint 1) — the application never needs to register it
 *   Removing init() eliminates a misleading dependency and closes
 *   the last indirect reference to manual driver management.
 *
 * Everything else (session guard, account load, greeting logic,
 * attribute setting, forward to Dashboard.jsp) unchanged from v3.
 */
@WebServlet(name = "DashboardServlet",
            urlPatterns = {"/Dashboard", "/dashboard"})
public class DashboardServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final AccountService accountService =
        new AccountService();

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
        String username  = (String)  session.getAttribute("username");
        String fullName  = (String)  session.getAttribute("fullName");
        String role      = (String)  session.getAttribute("role");
        String lastLogin = (String)  session.getAttribute("lastLogin");
        String email     = (String)  session.getAttribute("email");
        int    userId    = (Integer) session.getAttribute("userId");

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
        Account account     = null;
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

        request.getRequestDispatcher("/Dashboard.jsp")
               .forward(request, response);
    }
}