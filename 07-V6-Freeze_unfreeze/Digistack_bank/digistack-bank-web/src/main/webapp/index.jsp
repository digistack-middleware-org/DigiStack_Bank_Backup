<%@ page language="java"
         contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%
    /**
     * index.jsp — DigiStack Bank P01 v6
     *
     * Welcome file declared in web.xml.
     * Immediately redirects the browser to /Home (HomeServlet).
     *
     * Why sendRedirect and not forward?
     *   forward() would make the browser think it is still at the
     *   root URL "/". If the user bookmarks or refreshes, they hit
     *   this file again — no problem since it just redirects.
     *   sendRedirect() updates the browser's address bar to /Home
     *   so the user always sees a clean URL.
     */
    response.sendRedirect(request.getContextPath() + "/Home");
%>