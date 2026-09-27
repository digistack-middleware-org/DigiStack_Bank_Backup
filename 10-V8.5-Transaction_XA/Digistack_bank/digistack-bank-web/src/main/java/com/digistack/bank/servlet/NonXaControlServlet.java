package com.digistack.bank.servlet;

import com.digistack.bank.xa.NonXaControlBean;
import com.digistack.bank.xa.NonXaOrphanException;

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
 * NonXaControlServlet — v8.5 Sprint 2 negative control entry point.
 *
 * URL  : /XAControl   (POST only — form submission from XATransfer.jsp)
 * POST : Parse form inputs → call NonXaControlBean.simulateOrphanedDebit() →
 *        the call ALWAYS throws NonXaOrphanException (by design) →
 *        catch it, set ctrlOrphanMessage request attribute →
 *        forward to XATransfer.jsp to display the orphan details.
 *
 * The throw from NonXaControlBean is the expected, correct behaviour for the
 * negative control test. The debit is already committed at the point of throw.
 *
 * No GET handler — this endpoint is POST-only. Navigating to /XAControl
 * directly in a browser will result in a 405 from the default HttpServlet.
 */
@WebServlet("/XAControl")
public class NonXaControlServlet extends HttpServlet {

    @EJB
    private NonXaControlBean controlBean;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            response.sendRedirect(request.getContextPath() + "/Home");
            return;
        }

        // Form field names are prefixed with "ctrl" to avoid colliding with
        // the XA form fields on the same JSP.
        String fromParam   = request.getParameter("ctrlFromAccountId");
        String toParam     = request.getParameter("ctrlToAccountId");
        String amountParam = request.getParameter("ctrlAmount");

        if (isBlank(fromParam) || isBlank(toParam) || isBlank(amountParam)) {
            request.setAttribute("ctrlErrorMessage",
                "All three fields are required for the non-XA control test.");
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
            request.setAttribute("ctrlErrorMessage",
                "Account IDs must be whole numbers.");
            request.getRequestDispatcher("/XATransfer.jsp").forward(request, response);
            return;
        }

        try {
            amount = new BigDecimal(amountParam.trim());
        } catch (NumberFormatException e) {
            request.setAttribute("ctrlErrorMessage",
                "Amount must be a valid number.");
            request.getRequestDispatcher("/XATransfer.jsp").forward(request, response);
            return;
        }

        // simulateOrphanedDebit() always throws NonXaOrphanException.
        // That throw is the demonstration — the debit committed before it.
        // Catch it here and surface the orphan details to the JSP.
        try {
            controlBean.simulateOrphanedDebit(fromAccountId, toAccountId, amount);
            // Should never reach here — the bean always throws.
            request.setAttribute("ctrlErrorMessage",
                "Unexpected: bean returned without throwing. Check NonXaControlBean.");
        } catch (NonXaOrphanException e) {
            // Expected path — the debit committed, the credit never ran.
            request.setAttribute("ctrlOrphanMessage", e.getMessage());
        }

        request.getRequestDispatcher("/XATransfer.jsp").forward(request, response);
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}