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
import com.digistack.bank.service.FreezeService;

/**
 * UnfreezeServlet — P01 v6
 *
 * Handles GET  /Unfreeze → display unfreeze confirmation page
 * Handles POST /Unfreeze → execute unfreeze, PRG redirect
 *
 * Mirror of FreezeServlet — same structure, opposite operation.
 * Both servlets share FreezeService because freeze and unfreeze
 * are two sides of the same admin operation.
 *
 * TECHNICAL DEBT (v6): No role check — any logged-in user can
 * unfreeze their own account. Role-based gating deferred to v10.
 */
@WebServlet(name = "UnfreezeServlet",
            urlPatterns = {"/Unfreeze", "/unfreeze"})
public class UnfreezeServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final AccountService accountService = new AccountService();
    private final FreezeService  freezeService  = new FreezeService();

    /**
     * GET /Unfreeze — display the unfreeze confirmation page.
     */
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        // ── Session Guard ──
        HttpSession session = request.getSession(false);
        if (session == null ||
                session.getAttribute("userId") == null) {
            response.sendRedirect(
                request.getContextPath() + "/Login");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");

        // ── Load Account ──
        Account account  = null;
        String loadError = null;

        try {
            account = accountService.getAccountByUserId(userId);
            if (account == null) {
                loadError = "No account found. Please contact support.";
            }
        } catch (SQLException e) {
            log("UnfreezeServlet GET: DB error for userId=" +
                userId + " — " + e.getMessage(), e);
            loadError = "Unable to load account. Please try again.";
        }

        // ── Read PRG parameters ──
        String result = request.getParameter("result");
        String error  = request.getParameter("error");

        // ── Set Attributes for Unfreeze.jsp ──
        request.setAttribute("account",   account);
        request.setAttribute("loadError", loadError);
        request.setAttribute("result",    result);
        request.setAttribute("error",     error);

        request.setAttribute("username",
            session.getAttribute("username"));
        request.setAttribute("fullName",
            session.getAttribute("fullName"));
        request.setAttribute("role",
            session.getAttribute("role"));

        request.getRequestDispatcher("/Unfreeze.jsp")
               .forward(request, response);
    }

    /**
     * POST /Unfreeze — execute the unfreeze on the submitted accountId.
     */
    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        // ── Session Guard ──
        HttpSession session = request.getSession(false);
        if (session == null ||
                session.getAttribute("userId") == null) {
            response.sendRedirect(
                request.getContextPath() + "/Login");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");

        // ── Read accountId from hidden form field ──
        String accountIdStr = request.getParameter("accountId");
        if (accountIdStr == null || accountIdStr.trim().isEmpty()) {
            redirectWithError(request, response,
                "Account ID missing. Please use the unfreeze button.");
            return;
        }

        int accountId;
        try {
            accountId = Integer.parseInt(accountIdStr.trim());
        } catch (NumberFormatException e) {
            redirectWithError(request, response,
                "Invalid account ID. Please try again.");
            return;
        }

        try {
            Account unfrozen = freezeService.unfreeze(accountId);

            log("UnfreezeServlet: Account unfrozen successfully. " +
                "userId=" + userId +
                " accountId=" + accountId +
                " accountNumber=" + unfrozen.getAccountNumber());

            response.sendRedirect(
                request.getContextPath() + "/Unfreeze?result=success");

        } catch (IllegalStateException e) {
            // Covers: account not found, already active
            log("UnfreezeServlet: State error. userId=" +
                userId + " — " + e.getMessage());
            redirectWithError(request, response, e.getMessage());

        } catch (SQLException e) {
            log("UnfreezeServlet: DB error. userId=" +
                userId + " — " + e.getMessage(), e);
            redirectWithError(request, response,
                "A system error occurred. Please try again.");
        }
    }

    /**
     * Redirects to GET /Unfreeze with a URL-encoded error message.
     */
    private void redirectWithError(HttpServletRequest request,
                                   HttpServletResponse response,
                                   String message)
            throws IOException {
        String encoded;
        try {
            encoded = java.net.URLEncoder.encode(message, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            encoded = "An+error+occurred.";
        }
        response.sendRedirect(
            request.getContextPath() + "/Unfreeze?error=" + encoded);
    }
}