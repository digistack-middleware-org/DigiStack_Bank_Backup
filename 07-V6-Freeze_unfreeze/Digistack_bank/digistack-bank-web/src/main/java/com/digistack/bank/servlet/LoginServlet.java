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

import com.digistack.bank.util.PasswordUtil;

/**
 * LoginServlet — P01 v2
 *
 * Handles GET  /Login → display Login.jsp
 * Handles POST /Login → validate credentials, create session
 *
 * Flow on POST:
 *   1. Read username and password from the form.
 *   2. Query users table for a row matching the username.
 *   3. If found and active: hash(storedSalt + enteredPassword)
 *      and compare to storedHash using PasswordUtil.verify().
 *   4. If match: create HttpSession, store user attributes, redirect
 *      to Dashboard.
 *   5. If no match or user not found: forward back to Login.jsp
 *      with an error message.
 *
 * TECHNICAL DEBT (v2): Direct JDBC with hardcoded credentials.
 * Replaced at v7 with WAS-managed JNDI DataSource (jdbc/BankDS).
 */
@WebServlet(name = "LoginServlet", urlPatterns = {"/Login", "/login"})
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // Direct JDBC — replaced at v7 with JNDI DataSource
    private static final String JDBC_URL =
        "jdbc:postgresql://192.168.10.30:5432/digistack_bank";
    private static final String JDBC_USER     = "digistack_app";
    private static final String JDBC_PASSWORD = "Wasadmin@951951";

    // SQL — fetch the user row for the entered username
    // Parameterised query (?) prevents SQL injection.
    // SQL injection = an attacker entering SQL code as their username
    // to manipulate the query. PreparedStatement prevents this by
    // treating the parameter as data, never as SQL.
    private static final String SQL_FIND_USER =
        "SELECT id, username, password_hash, password_salt, " +
        "       role, full_name, email, is_active, last_login " +
        "FROM users " +
        "WHERE username = ?";

    // SQL — update last_login timestamp after successful login
    private static final String SQL_UPDATE_LAST_LOGIN =
        "UPDATE users SET last_login = NOW() WHERE id = ?";

    /**
     * GET /Login — just show the login page.
     * Called when the user clicks the Login button on the Home page.
     */
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        // If already logged in, redirect straight to Dashboard
        HttpSession existingSession = request.getSession(false);
        if (existingSession != null &&
            existingSession.getAttribute("username") != null) {
            response.sendRedirect(
                request.getContextPath() + "/Dashboard");
            return;
        }

        // Not logged in — show the login page
        request.getRequestDispatcher("/Login.jsp")
               .forward(request, response);
    }

    /**
     * POST /Login — validate credentials and create session.
     * Called when the user submits the login form.
     */
    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        // Read form fields — trim() removes accidental leading/
        // trailing spaces the user may have typed.
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (username != null) username = username.trim();

        // Basic input validation — empty fields rejected immediately.
        if (username == null || username.isEmpty() ||
            password == null || password.isEmpty()) {
            forwardWithError(request, response,
                "Username and password are required.");
            return;
        }

        // Query the database for the user
        try (Connection conn = DriverManager.getConnection(
                    JDBC_URL, JDBC_USER, JDBC_PASSWORD);
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_USER)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    // Username not found — use a generic message.
                    // Never say "username not found" — that tells an
                    // attacker which usernames are valid.
                    log("LoginServlet: Login failed — username not " +
                        "found: " + username);
                    forwardWithError(request, response,
                        "Invalid username or password. Please try again.");
                    return;
                }

                // User row found — read the fields
                int     userId       = rs.getInt("id");
                String  storedHash   = rs.getString("password_hash");
                String  storedSalt   = rs.getString("password_salt");
                String  role         = rs.getString("role");
                String  fullName     = rs.getString("full_name");
                String  email        = rs.getString("email");
                boolean isActive     = rs.getBoolean("is_active");
                java.sql.Timestamp lastLogin = rs.getTimestamp("last_login");

                // Check account is active
                if (!isActive) {
                    log("LoginServlet: Login failed — account inactive: "
                        + username);
                    forwardWithError(request, response,
                        "Your account is inactive. " +
                        "Please contact support.");
                    return;
                }

                // Verify the password using PasswordUtil
                boolean passwordCorrect = PasswordUtil.verify(
                    storedSalt, password, storedHash);

                if (!passwordCorrect) {
                    log("LoginServlet: Login failed — wrong password " +
                        "for: " + username);
                    forwardWithError(request, response,
                        "Invalid username or password. Please try again.");
                    return;
                }

                // ── Password correct — create the session ──

                // Invalidate any existing session first (security best
                // practice — prevents session fixation attacks where an
                // attacker pre-creates a session ID and tricks the user
                // into using it).
                HttpSession oldSession = request.getSession(false);
                if (oldSession != null) {
                    oldSession.invalidate();
                }

                // Create a new session
                // true = create if one doesn't exist
                HttpSession session = request.getSession(true);

                // Store user information as session attributes.
                // These are available on every subsequent request
                // via session.getAttribute("username") etc.
                session.setAttribute("userId",    userId);
                session.setAttribute("username",  username);
                session.setAttribute("role",      role);
                session.setAttribute("fullName",  fullName);
                session.setAttribute("email",     email);

                // Format the last login timestamp for display.
                // "Last login: <timestamp>" is shown on the Dashboard
                // as a security signal — "was this you?"
                String lastLoginDisplay;
                if (lastLogin != null) {
                    // Format: "29 Aug 2026, 10:45 AM"
                    java.text.SimpleDateFormat sdf =
                        new java.text.SimpleDateFormat(
                            "dd MMM yyyy, hh:mm a");
                    lastLoginDisplay = sdf.format(lastLogin);
                } else {
                    lastLoginDisplay = "First login";
                }
                session.setAttribute("lastLogin", lastLoginDisplay);

                log("LoginServlet: Login successful for user: "
                    + username + " role: " + role);

                // Update last_login in the database to NOW()
                // so the NEXT login shows this login's timestamp.
                try (PreparedStatement psUpdate =
                        conn.prepareStatement(SQL_UPDATE_LAST_LOGIN)) {
                    psUpdate.setInt(1, userId);
                    psUpdate.executeUpdate();
                }

                // Redirect to Dashboard — POST/Redirect/GET pattern.
                // Redirect (not forward) so pressing browser Back does
                // not re-submit the login form.
                response.sendRedirect(
                    request.getContextPath() + "/Dashboard");
            }

        } catch (SQLException e) {
            log("LoginServlet: DB error during login — " +
                e.getMessage(), e);
            forwardWithError(request, response,
                "A system error occurred. Please try again later.");
        }
    }

    /**
     * Helper — forward back to Login.jsp with an error message.
     * Sets the error as a request attribute so Login.jsp can display it.
     */
    private void forwardWithError(HttpServletRequest request,
                                   HttpServletResponse response,
                                   String message)
            throws ServletException, IOException {
        request.setAttribute("errorMessage", message);
        request.getRequestDispatcher("/Login.jsp")
               .forward(request, response);
    }
}