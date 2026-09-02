package com.digistack.bank.servlet;

import java.io.IOException;
import java.io.PrintWriter;
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
 * BalanceJsonServlet — P01 v3
 *
 * Handles GET /BalanceJson
 *
 * Returns the authenticated user's current balance as a
 * JSON response for the Dashboard's AJAX balance toggle.
 *
 * Response format:
 *   {"balance":"₹50,000.00"}
 *
 * Returns {"balance":null} if session invalid or DB error.
 * The Dashboard JavaScript handles null gracefully by
 * showing a "View in Account" link instead.
 */
@WebServlet(name = "BalanceJsonServlet",
            urlPatterns = {"/BalanceJson"})
public class BalanceJsonServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final AccountService accountService =
        new AccountService();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        // Response is always JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();

        // Session guard — return null balance if not logged in
        HttpSession session = request.getSession(false);
        if (session == null ||
                session.getAttribute("userId") == null) {
            out.print("{\"balance\":null}");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");

        try {
            Account account =
                accountService.getAccountByUserId(userId);

            if (account == null) {
                out.print("{\"balance\":null}");
            } else {
                // Escape the ₹ symbol safely in JSON
                String formatted = account.getFormattedBalance()
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"");
                out.print("{\"balance\":\"" + formatted + "\"}");
            }

        } catch (SQLException e) {
            log("BalanceJsonServlet: DB error for userId=" +
                userId + " — " + e.getMessage(), e);
            out.print("{\"balance\":null}");
        }
    }
}