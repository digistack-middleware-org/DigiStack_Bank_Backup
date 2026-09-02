package com.digistack.bank.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * DashboardServlet — P01 v2
 *
 * Handles GET /Dashboard
 *
 * Responsibilities:
 *   1. Session guard — redirect to /Login if no valid session exists.
 *   2. Read session attributes set by LoginServlet.
 *   3. Determine time-of-day greeting (Morning/Afternoon/Evening)
 *      using server-side time — no JS date logic needed.
 *   4. Forward to Dashboard.jsp with all display attributes set.
 *
 * At v2 the Dashboard shows:
 *   - Greeting with name (username fallback until v15 adds full name)
 *   - Last login timestamp (security signal)
 *   - Account summary placeholder (live at v3)
 *   - Quick action tiles (Deposit/Withdraw live at v3; others coming soon)
 *
 * TECHNICAL DEBT (v2): Direct JDBC present for future use.
 * Replaced at v7 with WAS-managed JNDI DataSource (jdbc/BankDS).
 */
@WebServlet(name = "DashboardServlet", urlPatterns = {"/Dashboard", "/dashboard"})
public class DashboardServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // Direct JDBC — replaced at v7 with JNDI DataSource
    private static final String JDBC_URL =
        "jdbc:postgresql://192.168.10.30:5432/digistack_bank";
    private static final String JDBC_USER     = "digistack_app";
    private static final String JDBC_PASSWORD = "Wasadmin@951951";

    @Override
    public void init() throws ServletException {
        try {
            Class.forName("org.postgresql.Driver");
            log("DashboardServlet: PostgreSQL JDBC driver loaded.");
        } catch (ClassNotFoundException e) {
            throw new ServletException(
                "DashboardServlet init failed — JDBC driver not found.", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        // ── Session Guard ──
        // request.getSession(false) returns the existing session
        // without creating a new one. Returns null if no session exists.
        HttpSession session = request.getSession(false);

        if (session == null ||
            session.getAttribute("username") == null) {
            // No valid session — redirect to login page
            log("DashboardServlet: No session — redirecting to Login.");
            response.sendRedirect(
                request.getContextPath() + "/Login");
            return;
        }

        // ── Read Session Attributes ──
        // These were set by LoginServlet on successful login.
        String username  = (String) session.getAttribute("username");
        String fullName  = (String) session.getAttribute("fullName");
        String role      = (String) session.getAttribute("role");
        String lastLogin = (String) session.getAttribute("lastLogin");

        // Use fullName if available, fall back to username.
        // fullName is always set from v2 onward since users.full_name
        // is populated at seeding — this fallback is defensive coding.
        String displayName = (fullName != null && !fullName.isEmpty())
            ? fullName : username;

        // ── Time-of-Day Greeting ──
        // Determined server-side using the WAS server's clock.
        // No JavaScript date logic needed.
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

        // ── Read email from session ──
        // Set by LoginServlet at login time from the users table.
        String email = (String) session.getAttribute("email");
        if (email == null) email = "";

        // ── Set Request Attributes for Dashboard.jsp ──
        request.setAttribute("displayName", displayName);
        request.setAttribute("username",    username);
        request.setAttribute("role",        role);
        request.setAttribute("lastLogin",   lastLogin);
        request.setAttribute("greeting",    greeting);
        request.setAttribute("email",       email);

        // ── Forward to Dashboard.jsp ──
        request.getRequestDispatcher("/Dashboard.jsp")
               .forward(request, response);
    }
}