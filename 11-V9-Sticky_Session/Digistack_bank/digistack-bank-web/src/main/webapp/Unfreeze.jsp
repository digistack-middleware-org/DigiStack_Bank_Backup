<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ page import="com.digistack.bank.model.Account" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Unfreeze Account — DigiStack Bank</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css"
          rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css"
          rel="stylesheet">
    <link rel="stylesheet" href="css/common/common.css">
    <link rel="stylesheet" href="css/pages/unfreeze.css">
</head>
<body>

<%
    Account account  = (Account) request.getAttribute("account");
    String loadError = (String)  request.getAttribute("loadError");
    String result    = (String)  request.getAttribute("result");
    String error     = (String)  request.getAttribute("error");
    String username  = (String)  request.getAttribute("username");
    String fullName  = (String)  request.getAttribute("fullName");
    boolean frozen   = (account != null && account.isFrozen());
    int accountId    = (account != null) ? account.getId() : 0;
%>

<!-- ── Navbar ── -->
<nav class="navbar dsb-navbar">
    <div class="container">
        <div class="d-flex align-items-center
                    justify-content-between w-100 py-2">
            <a href="Dashboard" class="navbar-brand-text">
                <i class="bi bi-bank2 me-2 icon-gold"></i>
                DigiStack Bank
            </a>
            <div class="d-flex align-items-center gap-3">
                <span class="navbar-user-label">
                    <i class="bi bi-person-circle me-1 icon-gold"></i>
                    <%= username != null ? username : "" %>
                </span>
                <a href="Logout" class="btn-logout">
                    <i class="bi bi-box-arrow-right me-1"></i>Logout
                </a>
            </div>
        </div>
    </div>
</nav>

<!-- ── Page Header ── -->
<div class="page-header">
    <div class="container">
        <div class="breadcrumb-nav mb-2">
            <a href="Dashboard">Dashboard</a>
            <span> &rsaquo; </span>
            <a href="Account">My Account</a>
            <span> &rsaquo; Unfreeze</span>
        </div>
        <h1><i class="bi bi-unlock me-2"></i>Unfreeze Account</h1>
        <p>Restore deposits and withdrawals on this account.</p>
    </div>
</div>

<!-- ── Main Content ── -->
<div class="main-content">
    <div class="container">
        <div class="row justify-content-center">
            <div class="col-lg-7">

                <a href="Account" class="back-link">
                    <i class="bi bi-arrow-left"></i>
                    Back to My Account
                </a>

                <%-- ── PRG Banners ── --%>
                <% if ("success".equals(result)) { %>
                <div class="alert-success-custom">
                    <i class="bi bi-check-circle-fill alert-icon"></i>
                    <div>
                        <strong>Account Unfrozen Successfully</strong><br>
                        Deposits and withdrawals have been restored.
                        You can now transact normally.
                    </div>
                </div>
                <% } %>

                <% if (error != null && !error.isEmpty()) { %>
                <div class="alert-error-custom">
                    <i class="bi bi-exclamation-circle-fill
                              alert-icon"></i>
                    <div>
                        <strong>Unfreeze Failed</strong><br>
                        <%= error %>
                    </div>
                </div>
                <% } %>

                <% if (loadError != null) { %>
                <div class="alert-error-custom">
                    <i class="bi bi-exclamation-triangle-fill
                              alert-icon"></i>
                    <div>
                        <strong>Account Error</strong><br>
                        <%= loadError %>
                    </div>
                </div>
                <% } %>

                <%-- ── Current Status Card ── --%>
                <% if (account != null) { %>
                <div class="status-card">
                    <div class="status-card-title">
                        <i class="bi bi-shield-check icon-navy"></i>
                        Current Account Status
                    </div>
                    <table class="status-table">
                        <tr>
                            <td class="status-label">
                                Account Number
                            </td>
                            <td class="status-value">
                                <%= account.getMaskedAccountNumber() %>
                            </td>
                        </tr>
                        <tr>
                            <td class="status-label">
                                Account Type
                            </td>
                            <td class="status-value">
                                <%= account.getAccountType() %>
                            </td>
                        </tr>
                        <tr>
                            <td class="status-label">Status</td>
                            <td class="status-cell">
                                <% if (frozen) { %>
                                    <span class="badge-frozen">
                                        <i class="bi bi-lock-fill me-1"></i>
                                        Frozen
                                    </span>
                                <% } else { %>
                                    <span class="badge-active">
                                        <i class="bi bi-unlock me-1"></i>
                                        Active
                                    </span>
                                <% } %>
                            </td>
                        </tr>
                    </table>
                </div>

                <%-- ── Not Frozen — show info notice only ── --%>
                <% if (!frozen) { %>
                <div class="alert-info-custom">
                    <i class="bi bi-info-circle-fill alert-icon"></i>
                    <div>
                        <strong>Account is already active.</strong><br>
                        No action needed. If you want to freeze it,
                        <a href="Freeze" class="alert-action-link">
                            freeze your account
                        </a>.
                    </div>
                </div>

                <%-- ── Frozen — show confirmation form ── --%>
                <% } else { %>
                <div class="confirm-card confirm-card-unfreeze">
                    <div class="confirm-card-title">
                        <i class="bi bi-unlock
                                  icon-unfreeze-confirm"></i>
                        Confirm Unfreeze
                    </div>

                    <p class="confirm-description">
                        Unfreezing your account will
                        <strong>immediately restore</strong>
                        the ability to deposit and withdraw funds.
                    </p>

                    <form action="Unfreeze" method="post">
                        <input type="hidden"
                               name="accountId"
                               value="<%= accountId %>">

                        <div class="d-flex gap-3 flex-wrap">
                            <button type="submit"
                                    class="btn-unfreeze">
                                <i class="bi bi-unlock me-2"></i>
                                Unfreeze Account
                            </button>
                            <a href="Account" class="btn-cancel">
                                <i class="bi bi-x-circle me-2"></i>
                                Cancel
                            </a>
                        </div>
                    </form>
                </div>
                <% } %>
                <% } %>

            </div>
        </div>
    </div>
</div>

<!-- ── Footer ── -->
<footer class="dsb-footer">
    <div class="container">
        <strong>DigiStack Bank</strong> &mdash;
        &copy; 2026. For educational purposes only. v8
    </div>
</footer>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>