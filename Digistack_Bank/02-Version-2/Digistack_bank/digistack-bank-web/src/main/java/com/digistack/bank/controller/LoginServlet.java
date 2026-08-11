package com.digistack.bank.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.mindrot.jbcrypt.BCrypt;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(LoginServlet.class.getName());

    private static final String DB_URL = "jdbc:postgresql://192.168.10.30:5432/digistack_bank";
    private static final String DB_USER = "digistack_app";
    private static final String DB_PASSWORD = "Wasadmin@951951";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/Login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        String storedHash = null;

        try {
            Class.forName("org.postgresql.Driver");

            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement stmt = conn.prepareStatement(
                         "SELECT password_hash FROM users WHERE username = ?")) {

                stmt.setString(1, username);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        storedHash = rs.getString("password_hash");
                    }
                }
            }

        } catch (ClassNotFoundException e) {
            logger.severe("LoginServlet: PostgreSQL JDBC driver not found: " + e.getMessage());
        } catch (SQLException e) {
            logger.severe("LoginServlet: Database error during login: " + e.getMessage());
        }

        boolean valid = (storedHash != null) && BCrypt.checkpw(password, storedHash);

        if (valid) {
            HttpSession session = request.getSession(true);
            session.setAttribute("username", username);
            session.setAttribute("lastLogin", new java.util.Date());
            logger.info("LoginServlet: successful login for user: " + username);

            response.sendRedirect(request.getContextPath() + "/home");
        } else {
            logger.warning("LoginServlet: failed login attempt for username: " + username);
            request.setAttribute("errorMessage", "Invalid username or password.");
            request.getRequestDispatcher("/Login.jsp").forward(request, response);
        }
    }
}