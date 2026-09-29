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
 * FreezeServlet — P01 v6
 *
 * Handles GET  /Freeze → display freeze confirmation page
 * Handles POST /Freeze → execute freeze, PRG redirect
 *
 * GET flow:
 *   1. Session guard.
 *   2. Load the logged-in user's account via AccountService.
 *   3. Read any result/error from redirect parameters.
 *   4. Forward to Freeze.jsp.
 *
 * POST flow:
 *   1. Session guard.
 *   2. Read accountId from the hidden form field.
 *   3. Call FreezeService.freeze(accountId).
 *   4. On success → redirect /Freeze?result=success
 *   5. On failure → redirect /Freeze?error=<message>
 *
 * Why accountId from the form, not userId from session?
 *   FreezeService.freeze() takes an accountId (accounts.id),
 *   not a userId. We load the account in GET to know the ID,
 *   then pass it back as a hidden field in the form so POST
 *   knows which account to freeze without a second DB lookup.
 *
 * TECHNICAL DEBT (v6): No role check — any logged-in user can
 * freeze their own account. Role-based gating deferred to v10.
 */
@WebServlet(name = "FreezeServlet",
            urlPatterns = {"/Freeze", "/freeze"})
public class FreezeServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final AccountService accountService = new AccountService();
    private final FreezeService  freezeService  = new FreezeService();

    /**
     * GET /Freeze — display the freeze confirmation page.
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
            log("FreezeServlet GET: DB error for userId=" +
                userId + " — " + e.getMessage(), e);
            loadError = "Unable to load account. Please try again.";
        }

        // ── Read PRG parameters ──
        String result = request.getParameter("result");
        String error  = request.getParameter("error");

        // ── Set Attributes for Freeze.jsp ──
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

        request.getRequestDispatcher("/Freeze.jsp")
               .forward(request, response);
    }

    /**
     * POST /Freeze — execute the freeze on the submitted accountId.
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
                "Account ID missing. Please use the freeze button.");
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
            Account frozen = freezeService.freeze(accountId);

            log("FreezeServlet: Account frozen successfully. " +
                "userId=" + userId +
                " accountId=" + accountId +
                " accountNumber=" + frozen.getAccountNumber());

            response.sendRedirect(
                request.getContextPath() + "/Freeze?result=success");

        } catch (IllegalStateException e) {
            // Covers: account not found, already frozen
            log("FreezeServlet: State error. userId=" +
                userId + " — " + e.getMessage());
            redirectWithError(request, response, e.getMessage());

        } catch (SQLException e) {
            log("FreezeServlet: DB error. userId=" +
                userId + " — " + e.getMessage(), e);
            redirectWithError(request, response,
                "A system error occurred. Please try again.");
        }
    }

    /**
     * Redirects to GET /Freeze with a URL-encoded error message.
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
            request.getContextPath() + "/Freeze?error=" + encoded);
    }
}