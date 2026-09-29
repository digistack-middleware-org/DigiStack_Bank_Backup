<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.digistack.bank.model.Account" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard — DigiStack Bank</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css"
          rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css"
          rel="stylesheet">
    <link rel="stylesheet" href="css/common/common.css">
    <link rel="stylesheet" href="css/pages/dashboard.css">
</head>
<body>

<%
    Account dashAccount = (Account) request.getAttribute("account");
    String accountError = (String)  request.getAttribute("accountError");
    boolean isFrozen    = (dashAccount != null && dashAccount.isFrozen());
%>

<!-- ═══════════════════════════════════════════
     TOP NAVBAR
═══════════════════════════════════════════ -->
<nav class="dsb-navbar">
    <div class="container">
        <div class="navbar-inner">
            <a href="Dashboard" class="navbar-brand-text">
                <i class="bi bi-bank2 me-2"></i>DigiStack Bank
            </a>
            <div class="navbar-right">
                <span class="navbar-username">
                    <i class="bi bi-person-circle me-1 icon-gold"></i>
                    ${username}
                    <% if ("ADMINISTRATOR".equals(
                                request.getAttribute("role"))) { %>
                        <span class="role-admin-badge">[Admin]</span>
                    <% } %>
                </span>
                <a href="Logout" class="btn-logout">
                    <i class="bi bi-box-arrow-right me-1"></i>Logout
                </a>
            </div>
        </div>
    </div>
</nav>

<!-- ── Last Login Bar ── -->
<div class="last-login-bar">
    <div class="container">
        <i class="bi bi-clock-history me-1"></i>
        Last login: &nbsp;<strong>${lastLogin}</strong>
        &nbsp;&nbsp;
        <i class="bi bi-shield-check me-1 session-active-icon"></i>
        <span class="session-active-text">Session Active</span>
    </div>
</div>

<!-- ═══════════════════════════════════════════
     MAIN CONTENT
═══════════════════════════════════════════ -->
<div class="dashboard-main">
    <div class="container">
        <div class="row g-4">

            <!-- ── Left Sidebar ── -->
            <div class="col-lg-2 d-none d-lg-block">
                <div class="sidebar-nav">
                    <div class="sidebar-nav-title">Banking</div>

                    <a href="Dashboard" class="sidebar-nav-item active">
                        <i class="bi bi-grid-1x2"></i>
                        Dashboard
                    </a>

                    <span class="sidebar-nav-item disabled-nav">
                        <i class="bi bi-arrow-left-right"></i>
                        Transfer
                        <span class="coming-soon-badge">v15</span>
                    </span>

                    <span class="sidebar-nav-item disabled-nav">
                        <i class="bi bi-file-earmark-text"></i>
                        Statements
                        <span class="coming-soon-badge">v16</span>
                    </span>

                    <div class="sidebar-nav-title">Cards &amp; More</div>

                    <span class="sidebar-nav-item disabled-nav">
                        <i class="bi bi-credit-card-2-front"></i>
                        Cards
                        <span class="coming-soon-badge">v28</span>
                    </span>

                    <span class="sidebar-nav-item disabled-nav">
                        <i class="bi bi-cash-stack"></i>
                        Loans
                        <span class="coming-soon-badge">v30</span>
                    </span>
                </div>
            </div>

            <!-- ── Main Panel ── -->
            <div class="col-lg-7">

                <!-- Greeting -->
                <h1 class="greeting-title">
                    ${greeting}, ${displayName}! 👋
                </h1>
                <p class="greeting-subtitle">
                    Here is your account overview.
                </p>

                <!-- Frozen Banner -->
                <% if (isFrozen) { %>
                <div class="frozen-banner">
                    <i class="bi bi-lock-fill me-2"></i>
                    Your account is frozen.
                    <a href="Unfreeze" class="frozen-banner-link">
                        Unfreeze your account
                    </a>
                    to restore access.
                </div>
                <% } %>

                <!-- Account Error Banner -->
                <% if (accountError != null) { %>
                <div class="account-error-banner">
                    <i class="bi bi-exclamation-triangle me-2"></i>
                    <%= accountError %>
                </div>
                <% } %>

                <!-- Account Card -->
                <% if (dashAccount != null) { %>
                <div class="account-card mb-4">

                    <!-- Frozen notice inside card -->
                    <% if (isFrozen) { %>
                    <div class="account-card-frozen-notice">
                        <i class="bi bi-lock-fill me-2"></i>
                        Your account is frozen.
                        <a href="Unfreeze" class="frozen-card-link">
                            Unfreeze
                        </a>
                    </div>
                    <% } %>

                    <div class="account-type">
                        <%= dashAccount.getAccountType() %> Account
                    </div>
                    <div class="account-name">${displayName}</div>
                    <div class="account-number">
                        <%= dashAccount.getMaskedAccountNumber() %>
                    </div>
                    <div class="balance-label">Available Balance</div>

                    <div id="balanceHidden" class="balance-hidden">
                        ••••••
                    </div>
                    <div id="balanceVisible"
                         class="balance-value"
                         style="display:none;">
                        <%--
                            NOTE: display:none above CANNOT be moved to CSS.
                            It is toggled by JavaScript at runtime — the
                            initial hidden state must be set inline so JS
                            can toggle it with element.style.display.
                        --%>
                        <span id="liveBalance">Loading...</span>
                    </div>

                    <button class="btn-view-balance"
                            onclick="toggleBalance()"
                            id="balanceBtn"
                            <%= isFrozen ? "disabled" : "" %>>
                        <i class="bi bi-eye me-1"></i>View Balance
                    </button>

                </div>
                <% } %>

                <!-- Quick Actions -->
                <div class="section-title">Quick Actions</div>
                <div class="quick-actions-row mb-4">

                    <a href="Deposit" class="quick-action-tile">
                        <div class="tile-icon tile-icon-deposit">
                            <i class="bi bi-arrow-down-circle
                                      icon-deposit"></i>
                        </div>
                        <div class="tile-label">Deposit</div>
                        <div class="tile-sublabel">Add funds</div>
                    </a>

                    <a href="Withdraw" class="quick-action-tile">
                        <div class="tile-icon tile-icon-withdraw">
                            <i class="bi bi-arrow-up-circle
                                      icon-withdraw"></i>
                        </div>
                        <div class="tile-label">Withdraw</div>
                        <div class="tile-sublabel">Withdraw funds</div>
                    </a>

                    <span class="quick-action-tile disabled-tile">
                        <div class="tile-icon tile-icon-transfer">
                            <i class="bi bi-arrow-left-right
                                      icon-transfer"></i>
                        </div>
                        <div class="tile-label">Transfer</div>
                        <div class="tile-sublabel">Coming — v15</div>
                    </span>

                    <span class="quick-action-tile disabled-tile">
                        <div class="tile-icon tile-icon-statement">
                            <i class="bi bi-file-earmark-arrow-down
                                      icon-statement"></i>
                        </div>
                        <div class="tile-label">Statement</div>
                        <div class="tile-sublabel">Coming — v16</div>
                    </span>

                </div>

                <!-- Recent Transactions -->
                <div class="transactions-card">
                    <div class="section-title mb-3">
                        <i class="bi bi-clock-history me-2 icon-navy"></i>
                        Recent Transactions
                    </div>
                    <div class="transactions-placeholder">
                        <i class="bi bi-inbox"></i>
                        Transaction history available from v3 onward.
                    </div>
                </div>

            </div>

            <!-- ── Right Panel ── -->
            <div class="col-lg-3">

                <!-- Role Badge Card -->
                <div class="sidebar-card sidebar-card-1 mb-3">
                    <div class="sidebar-card-label">Account Type</div>
                    <div class="sidebar-card-title">
                        <% if ("ADMINISTRATOR".equals(
                                    request.getAttribute("role"))) { %>
                            <i class="bi bi-shield-fill-check me-2
                                      icon-gold"></i>
                            Administrator
                        <% } else { %>
                            <i class="bi bi-person-fill me-2
                                      icon-navy"></i>
                            Customer
                        <% } %>
                    </div>
                    <div class="sidebar-card-sub">
                        <i class="bi bi-envelope me-1"></i>
                        ${email}
                    </div>
                </div>

                <!-- Notifications Card -->
                <div class="sidebar-card sidebar-card-2">
                    <div class="sidebar-card-label">
                        <i class="bi bi-bell me-1"></i>Notifications
                    </div>
                    <div class="notification-placeholder">
                        <i class="bi bi-bell-slash
                                  notification-icon-large"></i>
                        Email alerts active from v13
                    </div>
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
    var balanceShown = false;
    function toggleBalance() {
        var hidden  = document.getElementById('balanceHidden');
        var visible = document.getElementById('balanceVisible');
        var btn     = document.getElementById('balanceBtn');
        var liveEl  = document.getElementById('liveBalance');
        balanceShown = !balanceShown;
        if (balanceShown) {
            hidden.style.display  = 'none';
            visible.style.display = 'block';
            btn.innerHTML =
                '<i class="bi bi-eye-slash me-1"></i>Hide Balance';
            fetch('BalanceJson')
                .then(function(r) { return r.json(); })
                .then(function(data) {
                    if (data && data.balance) {
                        liveEl.textContent = data.balance;
                    } else {
                        liveEl.innerHTML =
                            '<a href="Account" class="balance-fallback-link">' +
                            'View in Account</a>';
                    }
                })
                .catch(function() {
                    liveEl.innerHTML =
                        '<a href="Account" class="balance-fallback-link">' +
                        'View in Account</a>';
                });
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