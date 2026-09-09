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

/**
 * HomeServlet — P01 v1
 *
 * Handles GET requests to /Home.
 * Reads the 'bank.name' and 'system.status' values from the
 * app_config table in PostgreSQL and forwards them to Home.jsp
 * for display.
 *
 * TECHNICAL DEBT (v1): Direct JDBC with hardcoded credentials.
 * This is intentional at v1 — the topic here is first EAR deployment
 * and DB connectivity proof. Replace with WAS-managed JNDI DataSource
 * at v7 (jdbc/BankDS, JAAS Auth Alias).
 */
@WebServlet(name = "HomeServlet", urlPatterns = {"/Home", "/home"})
public class HomeServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // ── JDBC connection details (direct — replaced at v7 with JNDI) ──
    // dsb-db VM IP, PostgreSQL port, database name
    private static final String JDBC_URL =
        "jdbc:postgresql://192.168.10.30:5432/digistack_bank";

    private static final String JDBC_USER     = "digistack_app";
    private static final String JDBC_PASSWORD = "Wasadmin@951951";

    // SQL query — reads two config values in one round trip
    private static final String SQL =
        "SELECT config_key, config_value FROM app_config " +
        "WHERE config_key IN ('bank.name', 'system.status')";

    /**
     * Called once when the servlet is first loaded by WAS.
     * Registers the PostgreSQL JDBC driver class.
     */
    @Override
    public void init() throws ServletException {
        try {
            // Load the PostgreSQL JDBC driver from the JAR placed at
            // /opt/IBM/WebSphere/AppServer/lib/ext/jdbc/postgresql-42.7.3.jar
            Class.forName("org.postgresql.Driver");
            log("HomeServlet: PostgreSQL JDBC driver loaded successfully.");
        } catch (ClassNotFoundException e) {
            // If the driver JAR is missing from lib/ext/jdbc/, this fires.
            throw new ServletException(
                "HomeServlet init failed — PostgreSQL JDBC driver not found. " +
                "Ensure postgresql-42.7.3.jar is in WAS lib/ext/jdbc/.", e);
        }
    }

    /**
     * Handles HTTP GET requests.
     * Reads app_config from PostgreSQL and forwards to Home.jsp.
     */
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        // Default values — shown if the DB read fails
        String bankName    = "DigiStack Bank";
        String systemStatus = "Status Unavailable";
        String dbConnStatus = "Error";

        // Open connection, query, close — all in one try-with-resources block.
        // try-with-resources: Java automatically closes the connection,
        // statement, and result set when the block exits, even on error.
        try (Connection conn = DriverManager.getConnection(
                    JDBC_URL, JDBC_USER, JDBC_PASSWORD);
             PreparedStatement ps = conn.prepareStatement(SQL);
             ResultSet rs = ps.executeQuery()) {

            // Iterate through the result rows
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
            log("HomeServlet: DB read successful. bank.name=" + bankName);

        } catch (SQLException e) {
            // Log the full exception to WAS SystemOut.log for diagnosis
            log("HomeServlet: DB read FAILED — " + e.getMessage(), e);
            // dbConnStatus stays "Error", defaults are used for display
        }

        // Put values into the request so Home.jsp can read them
        // using ${bankName}, ${systemStatus}, ${dbConnStatus} EL expressions
        request.setAttribute("bankName",    bankName);
        request.setAttribute("systemStatus", systemStatus);
        request.setAttribute("dbConnStatus", dbConnStatus);

        // Forward to Home.jsp — the servlet hands off rendering to the JSP
        request.getRequestDispatcher("/Home.jsp")
               .forward(request, response);
    }
}