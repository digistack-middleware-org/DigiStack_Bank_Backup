<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ page import="com.digistack.bank.model.Account" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Account — DigiStack Bank</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css"
          rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css"
          rel="stylesheet">
    <link rel="stylesheet" href="css/common/common.css">
    <link rel="stylesheet" href="css/pages/account-details.css">
</head>
<body>

<%
    Account account  = (Account) request.getAttribute("account");
    String loadError = (String)  request.getAttribute("loadError");
    String username  = (String)  request.getAttribute("username");
    String fullName  = (String)  request.getAttribute("fullName");
    boolean frozen   = (account != null && account.isFrozen());
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
            <span> &rsaquo; My Account</span>
        </div>
        <h1><i class="bi bi-wallet2 me-2"></i>My Account</h1>
        <p>View your account details and manage your money.</p>
    </div>
</div>

<!-- ── Main Content ── -->
<div class="main-content">
    <div class="container">

        <a href="Dashboard" class="back-link">
            <i class="bi bi-arrow-left"></i> Back to Dashboard
        </a>

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

        <div class="row g-4">

            <!-- ── Left: Account Summary + Details ── -->
            <div class="col-lg-5">

                <% if (account != null) { %>
                <div class="account-summary-card">
                    <div class="acc-type-label">
                        <%= account.getAccountType() %> Account
                    </div>
                    <div class="acct-holder-name">
                        <%= fullName != null ? fullName : username %>
                    </div>
                    <div class="acc-number">
                        <%= account.getMaskedAccountNumber() %>
                    </div>
                    <div class="balance-label-sm">
                        Available Balance
                    </div>
                    <div id="balHidden" class="balance-hidden-dots">
                        ••••••
                    </div>
                    <%--
                        display:none below CANNOT move to CSS —
                        JavaScript toggles it at runtime via
                        element.style.display = 'block'/'none'.
                    --%>
                    <div id="balVisible" class="balance-amount"
                         style="display:none;">
                        <%= account.getFormattedBalance() %>
                    </div>
                    <button class="btn-toggle-balance"
                            id="balBtn"
                            onclick="toggleBal()">
                        <i class="bi bi-eye me-1"></i>View Balance
                    </button>
                    <% if (frozen) { %>
                    <div class="frozen-chip">
                        <i class="bi bi-lock-fill"></i>
                        Account Frozen — Contact Support
                    </div>
                    <% } %>
                </div>

                <!-- Account Details Table -->
                <div class="info-card">
                    <div class="info-card-title">
                        <i class="bi bi-info-circle icon-navy"></i>
                        Account Details
                    </div>
                    <table class="details-table">
                        <tr>
                            <td class="details-label">
                                Account Number
                            </td>
                            <td class="details-value">
                                <%= account.getMaskedAccountNumber() %>
                            </td>
                        </tr>
                        <tr>
                            <td class="details-label">
                                Account Type
                            </td>
                            <td class="details-value">
                                <%= account.getAccountType() %>
                            </td>
                        </tr>
                        <tr>
                            <td class="details-label">
                                Status
                            </td>
                            <td class="details-status-cell">
                                <% if (frozen) { %>
                                    <span class="status-frozen">
                                        Frozen
                                    </span>
                                <% } else { %>
                                    <span class="status-active">
                                        Active
                                    </span>
                                <% } %>
                            </td>
                        </tr>
                    </table>
                </div>
                <% } %>

            </div>

            <!-- ── Right: Quick Actions ── -->
            <div class="col-lg-7">

                <div class="info-card">
                    <div class="info-card-title">
                        <i class="bi bi-lightning-charge
                                  icon-gold"></i>
                        Quick Actions
                    </div>

                    <div class="actions-grid">

                        <a href="Deposit"
                           class="action-tile
                           <%= frozen ? "tile-disabled" : "" %>">
                            <div class="tile-icon-wrap
                                        tile-bg-deposit">
                                <i class="bi bi-arrow-down-circle
                                          icon-deposit"></i>
                            </div>
                            <div class="tile-name">Deposit</div>
                            <div class="tile-desc">
                                Add funds to account
                            </div>
                        </a>

                        <a href="Withdraw"
                           class="action-tile
                           <%= frozen ? "tile-disabled" : "" %>">
                            <div class="tile-icon-wrap
                                        tile-bg-withdraw">
                                <i class="bi bi-arrow-up-circle
                                          icon-withdraw"></i>
                            </div>
                            <div class="tile-name">Withdraw</div>
                            <div class="tile-desc">Withdraw funds</div>
                        </a>

                        <a href="Freeze"
                           class="action-tile
                           <%= frozen ? "tile-disabled" : "" %>">
                            <div class="tile-icon-wrap
                                        tile-bg-freeze">
                                <i class="bi bi-lock
                                          icon-freeze"></i>
                            </div>
                            <div class="tile-name">
                                Freeze Account
                            </div>
                            <div class="tile-desc">
                                Block all transactions
                            </div>
                        </a>

                        <a href="Unfreeze"
                           class="action-tile
                           <%= !frozen ? "tile-disabled" : "" %>">
                            <div class="tile-icon-wrap
                                        tile-bg-unfreeze">
                                <i class="bi bi-unlock
                                          icon-unfreeze"></i>
                            </div>
                            <div class="tile-name">
                                Unfreeze Account
                            </div>
                            <div class="tile-desc">
                                Restore transactions
                            </div>
                        </a>

                    </div>

                    <% if (frozen) { %>
                    <div class="frozen-actions-notice">
                        <i class="bi bi-info-circle me-2"></i>
                        Account is frozen. Deposit and Withdraw are
                        disabled. Use
                        <a href="Unfreeze"
                           class="frozen-actions-link">
                            Unfreeze
                        </a>
                        to restore access.
                    </div>
                    <% } %>
                </div>

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
<script>
    var balShown = false;
    function toggleBal() {
        var hidden  = document.getElementById('balHidden');
        var visible = document.getElementById('balVisible');
        var btn     = document.getElementById('balBtn');
        balShown = !balShown;
        if (balShown) {
            hidden.style.display  = 'none';
            visible.style.display = 'block';
            btn.innerHTML =
                '<i class="bi bi-eye-slash me-1"></i>Hide Balance';
        } else {
            hidden.style.display  = 'block';
            visible.style.display = 'none';
            btn.innerHTML =
                '<i class="bi bi-eye me-1"></i>View Balance';
        }
    }
</script>
</body>
</html>