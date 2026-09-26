package com.digistack.bank.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * LogoutServlet — P01 v2
 *
 * Handles GET /Logout
 *
 * Steps:
 *   1. Retrieve the existing session without creating a new one.
 *   2. If a session exists, invalidate it — destroys the server-side
 *      session object immediately. The JSESSIONID cookie in the
 *      browser becomes worthless.
 *   3. Redirect to the public Home page.
 *
 * Why GET and not POST for logout?
 *   Logout is triggered by a simple link click (no form data to
 *   submit), so GET is appropriate here. A POST-based logout is
 *   stricter (prevents CSRF logout attacks) but is a v10+
 *   security hardening topic, not v2's concern.
 */
@WebServlet(name = "LogoutServlet", urlPatterns = {"/Logout", "/logout"})
public class LogoutServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        // Retrieve the existing session.
        // false = do NOT create a new session if one doesn't exist.
        // We never want to create a session during logout.
        HttpSession session = request.getSession(false);

        String username = "unknown";

        if (session != null) {
            // Read username for the log entry before invalidating —
            // after invalidate() the session attributes are gone.
            Object usernameAttr = session.getAttribute("username");
            if (usernameAttr != null) {
                username = usernameAttr.toString();
            }

            // Invalidate destroys the session object on the server.
            // Any subsequent request using the old JSESSIONID cookie
            // will find no matching session and be treated as anonymous.
            session.invalidate();

            log("LogoutServlet: Session invalidated for user: "
                + username);
        } else {
            // No session existed — user may have already logged out
            // or navigated to /Logout directly without being logged in.
            log("LogoutServlet: Logout called with no active session.");
        }

        // Redirect to the public Home page.
        // The user sees the Home page — not a blank page or an error.
        // sendRedirect issues an HTTP 302 response telling the browser
        // to make a new GET request to the Home URL.
        response.sendRedirect(request.getContextPath() + "/Home");
    }

    /**
     * POST /Logout — delegate to doGet.
     * Included for completeness — the logout link uses GET,
     * but if a form-based logout is added later it will work too.
     */
    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}