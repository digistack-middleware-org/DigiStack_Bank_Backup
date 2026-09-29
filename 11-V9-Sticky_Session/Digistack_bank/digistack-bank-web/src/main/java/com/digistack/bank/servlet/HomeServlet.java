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
import javax.sql.DataSource;

/**
 * HomeServlet — P01 v7 (updated from v1)
 *
 * Handles GET /Home → read app_config from DB, forward to Home.jsp.
 *
 * v7 change: Direct JDBC removed entirely.
 *   - Removed: JDBC_URL, JDBC_USER, JDBC_PASSWORD static constants
 *   - Removed: Class.forName("org.postgresql.Driver") in init()
 *   - Removed: init() method entirely (WAS manages driver loading
 *     via the JDBC Provider classpath — the application never needs
 *     to register the driver class manually when using JNDI)
 *   - Added: getConnection() using JNDI lookup of jdbc/BankDS
 *
 * Everything else (SQL query, Home.jsp forwarding, default values
 * on DB failure) is unchanged from v1.
 */
@WebServlet(name = "HomeServlet", urlPatterns = {"/Home", "/home"})
public class HomeServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String SQL =
        "SELECT config_key, config_value FROM app_config " +
        "WHERE config_key IN ('bank.name', 'system.status')";

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        String bankName     = "DigiStack Bank";
        String systemStatus = "Status Unavailable";
        String dbConnStatus = "Error";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String key   = rs.getString("config_key");
                String value = rs.getString("config_value");

                if ("bank.name".equals(key)) {
                    bankName = value;
                } else if ("system.status".equals(key)) {
                    systemStatus = value;
                }
            }

            dbConnStatus = "Connected";
            log("HomeServlet: DB read successful. bank.name=" +
                bankName);

        } catch (SQLException e) {
            log("HomeServlet: DB read FAILED — " +
                e.getMessage(), e);
        }

        request.setAttribute("bankName",     bankName);
        request.setAttribute("systemStatus", systemStatus);
        request.setAttribute("dbConnStatus", dbConnStatus);

        request.getRequestDispatcher("/Home.jsp")
               .forward(request, response);
    }

    /**
     * Obtains a connection from the WAS-managed JNDI DataSource.
     *
     * Replaces DriverManager.getConnection() from v1.
     * WAS resolves "jdbc/BankDS" to the DigiStack Bank DataSource
     * (Sprint 2). Connection pooling, credential management, and
     * pre-test validation are all handled transparently by WAS.
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