package com.digistack.bank.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.digistack.bank.service.AccountService;

@WebServlet("/account")
public class AccountController extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(AccountController.class.getName());
    private final AccountService accountService = new AccountService();

    // Hardcoded for v3 — real customer/account linking comes in later versions.
    private static final int ACCOUNT_ID = 1;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!isLoggedIn(request)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        showAccountPage(request, response, null);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!isLoggedIn(request)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String action = request.getParameter("action");
        String amountStr = request.getParameter("amount");
        String message = null;

        try {
            BigDecimal amount = new BigDecimal(amountStr);

            if ("deposit".equals(action)) {
                accountService.deposit(ACCOUNT_ID, amount);
                message = "Deposit successful.";
            } else if ("withdraw".equals(action)) {
                accountService.withdraw(ACCOUNT_ID, amount);
                message = "Withdrawal successful.";
            } else {
                message = "Unknown action requested.";
            }

        } catch (NumberFormatException e) {
            message = "Please enter a valid numeric amount.";
        } catch (IllegalArgumentException e) {
            message = e.getMessage();
        } catch (SQLException e) {
            logger.severe("AccountController: database error during " + action + ": " + e.getMessage());
            message = "A system error occurred. Please try again.";
        }

        showAccountPage(request, response, message);
    }

    private void showAccountPage(HttpServletRequest request, HttpServletResponse response, String message)
            throws ServletException, IOException {
        try {
            BigDecimal balance = accountService.getBalance(ACCOUNT_ID);
            request.setAttribute("balance", balance);
        } catch (SQLException e) {
            logger.severe("AccountController: failed to fetch balance: " + e.getMessage());
            request.setAttribute("balance", "Unavailable");
        }

        if (message != null) {
            request.setAttribute("message", message);
        }

        request.getRequestDispatcher("/Account.jsp").forward(request, response);
    }

    private boolean isLoggedIn(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute("username") != null;
    }
}