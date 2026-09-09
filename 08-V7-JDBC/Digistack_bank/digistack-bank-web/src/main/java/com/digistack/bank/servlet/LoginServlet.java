package com.digistack.bank.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import com.digistack.bank.util.PasswordUtil;

/**
 * LoginServlet — P01 v7 (updated from v2)
 *
 * Handles GET  /Login → display Login.jsp
 * Handles POST /Login → validate credentials, create session
 *
 * v7 change: Direct JDBC removed entirely.
 *   - Removed: JDBC_URL, JDBC_USER, JDBC_PASSWORD static constants
 *   - Removed: DriverManager.getConnection() calls
 *   - Added:   private getConnection() using JNDI lookup of jdbc/BankDS
 *
 * The Connection object is obtained once per login attempt and used
 * for both the user-lookup query AND the last_login update — both
 * operations share one pooled connection, which is returned to the
 * pool at the end of the try-with-resources block. This is correct
 * and efficient — no need to open two separate pool connections for
 * two queries in the same request.
 *
 * All authentication logic (password hashing, session creation,
 * last-login update) unchanged from v2.
 */
@WebServlet(name = "LoginServlet",
            urlPatterns = {"/Login", "/login"})
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String SQL_FIND_USER =
        "SELECT id, username, password_hash, password_salt, " +
        "       role, full_name, email, is_active, last_login " +
        "FROM users " +
        "WHERE username = ?";

    private static final String SQL_UPDATE_LAST_LOGIN =
        "UPDATE users SET last_login = NOW() WHERE id = ?";

    /**
     * GET /Login — display the login page.
     * If already logged in, redirect to Dashboard.
     */
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession existingSession = request.getSession(false);
        if (existingSession != null &&
                existingSession.getAttribute("username") != null) {
            response.sendRedirect(
                request.getContextPath() + "/Dashboard");
            return;
        }

        request.getRequestDispatcher("/Login.jsp")
               .forward(request, response);
    }

    /**
     * POST /Login — validate credentials and create session.
     */
    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (username != null) username = username.trim();

        if (username == null || username.isEmpty() ||
                password == null || password.isEmpty()) {
            forwardWithError(request, response,
                "Username and password are required.");
            return;
        }

        try (Connection conn = getConnection();
             PreparedStatement ps =
                 conn.prepareStatement(SQL_FIND_USER)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    log("LoginServlet: Login failed — " +
                        "username not found: " + username);
                    forwardWithError(request, response,
                        "Invalid username or password. " +
                        "Please try again.");
                    return;
                }

                int     userId     = rs.getInt("id");
                String  storedHash = rs.getString("password_hash");
                String  storedSalt = rs.getString("password_salt");
                String  role       = rs.getString("role");
                String  fullName   = rs.getString("full_name");
                String  email      = rs.getString("email");
                boolean isActive   = rs.getBoolean("is_active");
                java.sql.Timestamp lastLogin =
                    rs.getTimestamp("last_login");

                if (!isActive) {
                    log("LoginServlet: Login failed — " +
                        "account inactive: " + username);
                    forwardWithError(request, response,
                        "Your account is inactive. " +
                        "Please contact support.");
                    return;
                }

                boolean passwordCorrect = PasswordUtil.verify(
                    storedSalt, password, storedHash);

                if (!passwordCorrect) {
                    log("LoginServlet: Login failed — " +
                        "wrong password for: " + username);
                    forwardWithError(request, response,
                        "Invalid username or password. " +
                        "Please try again.");
                    return;
                }

                // ── Password correct — create session ──

                HttpSession oldSession = request.getSession(false);
                if (oldSession != null) {
                    oldSession.invalidate();
                }

                HttpSession session = request.getSession(true);
                session.setAttribute("userId",   userId);
                session.setAttribute("username", username);
                session.setAttribute("role",     role);
                session.setAttribute("fullName", fullName);
                session.setAttribute("email",    email);

                String lastLoginDisplay;
                if (lastLogin != null) {
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

                // Update last_login — reuses the same pooled
                // connection as the user-lookup query above.
                try (PreparedStatement psUpdate =
                        conn.prepareStatement(
                            SQL_UPDATE_LAST_LOGIN)) {
                    psUpdate.setInt(1, userId);
                    psUpdate.executeUpdate();
                }

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
     * Forwards back to Login.jsp with an error message.
     */
    private void forwardWithError(HttpServletRequest request,
                                   HttpServletResponse response,
                                   String message)
            throws ServletException, IOException {
        request.setAttribute("errorMessage", message);
        request.getRequestDispatcher("/Login.jsp")
               .forward(request, response);
    }

    /**
     * Obtains a connection from the WAS-managed JNDI DataSource.
     *
     * Replaces DriverManager.getConnection() from v2.
     * The same connection is reused for both the user-lookup query
     * and the last_login UPDATE in doPost() — efficient, and
     * correct since both are in the same request lifecycle.
     */
    private Connection getConnection() throws SQLException {
        try {
            InitialContext ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup("jdbc/BankDS");
            return ds.getConnection();
        } catch (NamingException e) {
            throw new SQLException(
                "JNDI lookup failed for jdbc/BankDS: " +
                e.getMessage(), e);
        }
    }
}