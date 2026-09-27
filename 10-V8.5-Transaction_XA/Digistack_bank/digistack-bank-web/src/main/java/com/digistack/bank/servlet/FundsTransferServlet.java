package com.digistack.bank.servlet;

import com.digistack.bank.xa.FundsTransferBean;
import com.digistack.bank.xa.FundsTransferException;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;

/**
 * FundsTransferServlet — v8.5 XA Lab.
 *
 * URL  : /XATransfer
 * GET  : Show XATransfer.jsp form.
 * POST : Parse params → call FundsTransferBean.transfer() →
 *        forward back to XATransfer.jsp with result attributes.
 *
 * Sprint 2 addition: reads simulateCreditFail checkbox.
 * HTML checkbox sends the string "on" when checked and sends nothing
 * (null) when unchecked. "on".equals() handles both cases safely.
 */
@WebServlet("/XATransfer")
public class FundsTransferServlet extends HttpServlet {

    @EJB
    private FundsTransferBean transferBean;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            response.sendRedirect(request.getContextPath() + "/Home");
            return;
        }
        request.getRequestDispatcher("/XATransfer.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            response.sendRedirect(request.getContextPath() + "/Home");
            return;
        }

        // ── Parse parameters ──────────────────────────────────────────────────
        String fromParam          = request.getParameter("fromAccountId");
        String toParam            = request.getParameter("toAccountId");
        String amountParam        = request.getParameter("amount");
        // Checkbox: "on" when ticked, null when unticked.
        boolean simulateCreditFail =
            "on".equals(request.getParameter("simulateCreditFail"));

        if (isBlank(fromParam) || isBlank(toParam) || isBlank(amountParam)) {
            request.setAttribute("errorMessage", "All three fields are required.");
            request.getRequestDispatcher("/XATransfer.jsp").forward(request, response);
            return;
        }

        long fromAccountId;
        long toAccountId;
        BigDecimal amount;

        try {
            fromAccountId = Long.parseLong(fromParam.trim());
            toAccountId   = Long.parseLong(toParam.trim());
        } catch (NumberFormatException e) {
            request.setAttribute("errorMessage",
                "Account IDs must be whole numbers. Received: from=["
                + fromParam + "] to=[" + toParam + "]");
            request.getRequestDispatcher("/XATransfer.jsp").forward(request, response);
            return;
        }

        try {
            amount = new BigDecimal(amountParam.trim());
        } catch (NumberFormatException e) {
            request.setAttribute("errorMessage",
                "Amount must be a valid number. Received: [" + amountParam + "]");
            request.getRequestDispatcher("/XATransfer.jsp").forward(request, response);
            return;
        }

        // ── Invoke EJB ────────────────────────────────────────────────────────
        try {
            transferBean.transfer(fromAccountId, toAccountId, amount, simulateCreditFail);

            // Reached here — WAS committed both XA resources via 2PC.
            request.setAttribute("successMessage",
                "XA Transfer complete. Rs." + amount
                + " moved from account " + fromAccountId
                + " to account " + toAccountId
                + ". Both XA resources committed by WAS via 2PC.");

        } catch (FundsTransferException e) {
            // RuntimeException escaped EJB — WAS rolled back both XA resources.
            String prefix = simulateCreditFail
                ? "XA ROLLBACK PROVEN — "
                : "Transfer FAILED — WAS rolled back both accounts. ";
            request.setAttribute("errorMessage", prefix + e.getMessage());
        }

        request.getRequestDispatcher("/XATransfer.jsp").forward(request, response);
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}