<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.digistack.bank.model.Account" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard — DigiStack Bank</title>

    <!-- Bootstrap 5 -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css"
          rel="stylesheet">
    <!-- Bootstrap Icons -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css"
          rel="stylesheet">

    <style>
        :root {
            --db-navy:      #0b2545;
            --db-gold:      #c9a227;
            --db-gold-light:#e8c547;
            --db-light-bg:  #f4f7fb;
            --db-card-bg:   #ffffff;
        }

        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background-color: var(--db-light-bg);
            min-height: 100vh;
            display: flex;
            flex-direction: column;
        }

        @keyframes fadeInUp {
            from { opacity: 0; transform: translateY(20px); }
            to   { opacity: 1; transform: translateY(0); }
        }

        /* ── Top Navbar ── */
        .dsb-navbar {
            background-color: var(--db-navy);
            box-shadow: 0 2px 8px rgba(0,0,0,0.25);
            padding: 0 0;
        }
        .dsb-navbar .navbar-inner {
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 12px 0;
        }
        .navbar-brand-text {
            font-size: 1.3rem;
            font-weight: 700;
            color: var(--db-gold);
            letter-spacing: 1px;
            text-decoration: none;
        }
        .navbar-brand-text i {
            color: var(--db-gold);
            font-size: 1.4rem;
        }
        .navbar-right {
            display: flex;
            align-items: center;
            gap: 16px;
        }
        .navbar-username {
            color: rgba(255,255,255,0.85);
            font-size: 0.88rem;
            font-weight: 500;
        }
        .navbar-username i {
            color: var(--db-gold);
            font-size: 1.1rem;
        }
        .btn-logout {
            background-color: transparent;
            border: 1.5px solid rgba(255,255,255,0.3);
            color: rgba(255,255,255,0.85);
            font-size: 0.82rem;
            padding: 5px 14px;
            border-radius: 6px;
            text-decoration: none;
            transition: background-color 0.2s, border-color 0.2s;
        }
        .btn-logout:hover {
            background-color: rgba(255,255,255,0.1);
            border-color: rgba(255,255,255,0.6);
            color: white;
        }

        /* ── Last Login Bar ── */
        .last-login-bar {
            background-color: #eef2f9;
            border-bottom: 1px solid #dde3ed;
            padding: 7px 0;
            font-size: 0.8rem;
            color: #666;
            text-align: right;
        }
        .last-login-bar i { color: #999; }

        /* ── Main Content ── */
        .dashboard-main {
            flex: 1;
            padding: 32px 0 48px;
        }

        /* ── Greeting ── */
        .greeting-title {
            font-size: 1.6rem;
            font-weight: 700;
            color: var(--db-navy);
            margin-bottom: 2px;
            animation: fadeInUp 0.5s ease-out both;
        }
        .greeting-subtitle {
            font-size: 0.88rem;
            color: #888;
            margin-bottom: 28px;
            animation: fadeInUp 0.5s ease-out 0.1s both;
        }

        /* ── Frozen Account Banner ── */
        .frozen-banner {
            background-color: #fff3cd;
            border: 1px solid #ffc107;
            border-radius: 10px;
            padding: 12px 18px;
            margin-bottom: 20px;
            font-size: 0.9rem;
            color: #856404;
            font-weight: 600;
        }   

        /* ── Account Card ── */
        .account-card {
            background: linear-gradient(
                135deg, var(--db-navy) 0%, #1a3a6b 100%);
            border-radius: 18px;
            padding: 28px 30px;
            color: white;
            position: relative;
            overflow: hidden;
            animation: fadeInUp 0.5s ease-out 0.15s both;
            box-shadow: 0 8px 28px rgba(11,37,69,0.18);
        }
        .account-card::before {
            content: '';
            position: absolute;
            top: -40px; right: -40px;
            width: 160px; height: 160px;
            background: rgba(201,162,39,0.1);
            border-radius: 50%;
        }
        .account-type {
            font-size: 0.8rem;
            text-transform: uppercase;
            letter-spacing: 1.5px;
            color: rgba(255,255,255,0.6);
            margin-bottom: 4px;
        }
        .account-name {
            font-size: 1.1rem;
            font-weight: 600;
            margin-bottom: 20px;
        }
        .account-number {
            font-size: 0.88rem;
            color: rgba(255,255,255,0.6);
            margin-bottom: 16px;
            font-family: monospace;
        }
        .balance-label {
            font-size: 0.78rem;
            color: rgba(255,255,255,0.55);
            text-transform: uppercase;
            letter-spacing: 1px;
            margin-bottom: 4px;
        }
        .balance-value {
            font-size: 1.9rem;
            font-weight: 800;
            color: var(--db-gold);
            letter-spacing: 1px;
        }
        .balance-hidden {
            font-size: 1.6rem;
            letter-spacing: 6px;
            color: rgba(255,255,255,0.4);
        }
        .btn-view-balance {
            background-color: rgba(255,255,255,0.12);
            border: 1px solid rgba(255,255,255,0.25);
            color: white;
            font-size: 0.8rem;
            padding: 5px 14px;
            border-radius: 6px;
            cursor: pointer;
            transition: background-color 0.2s;
            margin-top: 10px;
        }
        .btn-view-balance:hover {
            background-color: rgba(255,255,255,0.2);
        }
        .account-coming-soon {
            color: rgba(255,255,255,0.45);
            font-size: 0.82rem;
            font-style: italic;
        }

        /* ── Quick Actions ── */
        .section-title {
            font-size: 1rem;
            font-weight: 700;
            color: var(--db-navy);
            margin-bottom: 14px;
        }
        .quick-actions-row {
            display: flex;
            gap: 14px;
            flex-wrap: wrap;
            animation: fadeInUp 0.5s ease-out 0.2s both;
        }
        .quick-action-tile {
            background: var(--db-card-bg);
            border-radius: 14px;
            padding: 20px 24px;
            text-align: center;
            box-shadow: 0 2px 12px rgba(0,0,0,0.06);
            flex: 1;
            min-width: 100px;
            transition: transform 0.2s, box-shadow 0.2s;
            text-decoration: none;
        }
        .quick-action-tile:hover {
            transform: translateY(-3px);
            box-shadow: 0 6px 20px rgba(11,37,69,0.1);
        }
        .quick-action-tile.disabled-tile {
            opacity: 0.5;
            cursor: not-allowed;
            pointer-events: none;
        }
        .tile-icon {
            width: 48px; height: 48px;
            border-radius: 12px;
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0 auto 10px;
        }
        .tile-icon i { font-size: 1.4rem; }
        .tile-label {
            font-size: 0.8rem;
            font-weight: 600;
            color: var(--db-navy);
        }
        .tile-sublabel {
            font-size: 0.7rem;
            color: #aaa;
            margin-top: 2px;
        }

        /* ── Recent Transactions ── */
        .transactions-card {
            background: var(--db-card-bg);
            border-radius: 16px;
            padding: 24px 26px;
            box-shadow: 0 2px 12px rgba(0,0,0,0.06);
            animation: fadeInUp 0.5s ease-out 0.25s both;
        }
        .transactions-placeholder {
            text-align: center;
            padding: 32px 0;
            color: #bbb;
        }
        .transactions-placeholder i {
            font-size: 2.5rem;
            margin-bottom: 10px;
            display: block;
        }

        /* ── Sidebar ── */
        .sidebar-nav {
            background: var(--db-card-bg);
            border-radius: 16px;
            padding: 20px 0;
            box-shadow: 0 2px 12px rgba(0,0,0,0.06);
            animation: fadeInUp 0.5s ease-out 0.2s both;
        }
        .sidebar-nav-title {
            font-size: 0.72rem;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 1.2px;
            color: #bbb;
            padding: 0 20px;
            margin-bottom: 8px;
            margin-top: 16px;
        }
        .sidebar-nav-title:first-child { margin-top: 0; }
        .sidebar-nav-item {
            display: flex;
            align-items: center;
            gap: 12px;
            padding: 11px 20px;
            font-size: 0.9rem;
            font-weight: 500;
            color: #555;
            text-decoration: none;
            transition: background-color 0.15s, color 0.15s;
            cursor: pointer;
        }
        .sidebar-nav-item:hover {
            background-color: var(--db-light-bg);
            color: var(--db-navy);
        }
        .sidebar-nav-item.active {
            background-color: #eef2fb;
            color: var(--db-navy);
            font-weight: 700;
            border-right: 3px solid var(--db-navy);
        }
        .sidebar-nav-item i {
            font-size: 1.1rem;
            width: 20px;
            text-align: center;
        }
        .sidebar-nav-item.disabled-nav {
            opacity: 0.4;
            cursor: not-allowed;
            pointer-events: none;
        }
        .coming-soon-badge {
            margin-left: auto;
            font-size: 0.65rem;
            background-color: #eee;
            color: #999;
            padding: 2px 7px;
            border-radius: 10px;
            white-space: nowrap;
        }

        /* ── Footer ── */
        .dsb-footer {
            background-color: #0a1f3d;
            color: rgba(255,255,255,0.45);
            padding: 16px 0;
            font-size: 0.78rem;
            text-align: center;
        }
        .dsb-footer strong { color: var(--db-gold); }
    </style>
</head>
<body>
<%
    Account dashAccount =
        (Account) request.getAttribute("account");
    String accountError =
        (String) request.getAttribute("accountError");
    boolean isFrozen = (dashAccount != null &&
                        dashAccount.isFrozen());
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
                    <i class="bi bi-person-circle me-1"></i>
                    ${username}
                    <% if ("ADMINISTRATOR".equals(request.getAttribute("role"))) { %>
                        <span style="color:var(--db-gold);font-size:0.75rem;
                              margin-left:4px;">[Admin]</span>
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
        <i class="bi bi-shield-check me-1" style="color:#28a745;"></i>
        <span style="color:#28a745;font-weight:600;">Session Active</span>
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

                <!-- Frozen Banner — wired at v3 via isFrozen flag.
                     Freeze/Unfreeze action introduced at v6. -->
                <% if (isFrozen) { %>
                <div class="frozen-banner"
                     style="display:block;">
                    <i class="bi bi-lock-fill me-2"></i>
                    Your account is frozen — please contact
                    support to restore access.
                </div>
                <% } %>

                <!-- Account Card -->
                <%
                    if (accountError != null) {
                %>
                <div style="background:#fff3cd;border:1px solid
                            #ffc107;border-radius:12px;padding:
                            14px 18px;margin-bottom:20px;
                            color:#856404;font-size:0.88rem;">
                    <i class="bi bi-exclamation-triangle me-2"></i>
                    <%= accountError %>
                </div>
                <%
                    }
                %>

                <% if (dashAccount != null) { %>
                <div class="account-card mb-4">

                    <%-- Frozen banner — shown when is_frozen = true.
                         Introduced at v3; freeze/unfreeze action
                         added at v6. --%>
                    <% if (isFrozen) { %>
                    <div style="background:rgba(255,193,7,0.15);
                                border:1px solid rgba(255,193,7,0.4);
                                border-radius:8px;padding:8px 14px;
                                margin-bottom:16px;font-size:0.82rem;
                                color:#ffc107;font-weight:600;">
                        <i class="bi bi-lock-fill me-2"></i>
                        Your account is frozen — please contact support.
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
                        <span id="liveBalance">Loading...</span>
                    </div>

                    <button class="btn-view-balance"
                            onclick="toggleBalance()"
                            id="balanceBtn"
                            <%= isFrozen ? "disabled style='opacity:0.5;cursor:not-allowed;'" : "" %>>
                        <i class="bi bi-eye me-1"></i>View Balance
                    </button>

                </div>
                <% } %>

                <!-- Quick Actions -->
                <div class="section-title">Quick Actions</div>
                <div class="quick-actions-row mb-4">

                    <a href="Account" class="quick-action-tile">
                        <div class="tile-icon"
                             style="background:#e8f4fd;">
                            <i class="bi bi-arrow-down-circle"
                               style="color:#2196f3;"></i>
                        </div>
                        <div class="tile-label">Deposit</div>
                        <div class="tile-sublabel">
                            Add funds
                        </div>
                    </a>

                    <a href="Account" class="quick-action-tile">
                        <div class="tile-icon"
                             style="background:#fff3e0;">
                            <i class="bi bi-arrow-up-circle"
                               style="color:#ff9800;"></i>
                        </div>
                        <div class="tile-label">Withdraw</div>
                        <div class="tile-sublabel">
                            Withdraw funds
                        </div>
                    </a>

                    <span class="quick-action-tile disabled-tile">
                        <div class="tile-icon"
                             style="background:#e8f5e9;">
                            <i class="bi bi-arrow-left-right"
                               style="color:#4caf50;"></i>
                        </div>
                        <div class="tile-label">Transfer</div>
                        <div class="tile-sublabel">Coming — v15</div>
                    </span>

                    <span class="quick-action-tile disabled-tile">
                        <div class="tile-icon"
                             style="background:#f3e5f5;">
                            <i class="bi bi-file-earmark-arrow-down"
                               style="color:#9c27b0;"></i>
                        </div>
                        <div class="tile-label">Statement</div>
                        <div class="tile-sublabel">Coming — v16</div>
                    </span>

                </div>

                <!-- Recent Transactions -->
                <div class="transactions-card">
                    <div class="section-title mb-3">
                        <i class="bi bi-clock-history me-2"
                           style="color:var(--db-navy);"></i>
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

                <!-- Role Badge -->
                <div class="mb-3 p-3"
                     style="background:white;border-radius:14px;
                            box-shadow:0 2px 12px rgba(0,0,0,0.06);
                            animation:fadeInUp 0.5s ease-out 0.3s both;">
                    <div style="font-size:0.75rem;color:#aaa;
                                text-transform:uppercase;
                                letter-spacing:1px;margin-bottom:6px;">
                        Account Type
                    </div>
                    <div style="font-weight:700;color:var(--db-navy);
                                font-size:1rem;">
                        <% if ("ADMINISTRATOR".equals(
                                    request.getAttribute("role"))) { %>
                            <i class="bi bi-shield-fill-check me-2"
                               style="color:var(--db-gold);"></i>
                            Administrator
                        <% } else { %>
                            <i class="bi bi-person-fill me-2"
                               style="color:var(--db-navy);"></i>
                            Customer
                        <% } %>
                    </div>
                    <div style="font-size:0.78rem;color:#999;
                                margin-top:6px;">
                        <i class="bi bi-envelope me-1"></i>
                        ${email}
                    </div>
                </div>

                <!-- Notification Bell — wired at v13 -->
                <div class="p-3"
                     style="background:white;border-radius:14px;
                            box-shadow:0 2px 12px rgba(0,0,0,0.06);
                            animation:fadeInUp 0.5s ease-out 0.35s both;">
                    <div style="font-size:0.75rem;color:#aaa;
                                text-transform:uppercase;
                                letter-spacing:1px;margin-bottom:10px;">
                        <i class="bi bi-bell me-1"></i>Notifications
                    </div>
                    <div style="text-align:center;color:#ccc;
                                font-size:0.82rem;padding:12px 0;">
                        <i class="bi bi-bell-slash"
                           style="font-size:1.6rem;display:block;
                                  margin-bottom:6px;"></i>
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
        &copy; 2026. For educational purposes only. v4
    </div>
</footer>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>

<script>
    // Balance toggle — fetches live balance from /Account on reveal
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
            // Fetch live balance via a lightweight AJAX call
            // to a dedicated balance endpoint added in Sprint 3.
            // Falls back to "View in Account" link if fetch fails.
            fetch('BalanceJson')
                .then(function(r) { return r.json(); })
                .then(function(data) {
                    if (data && data.balance) {
                        liveEl.textContent = data.balance;
                    } else {
                        liveEl.innerHTML =
                            '<a href="Account" ' +
                            'style="color:var(--db-gold);' +
                            'font-size:0.9rem;">View in Account</a>';
                    }
                })
                .catch(function() {
                    liveEl.innerHTML =
                        '<a href="Account" ' +
                        'style="color:var(--db-gold);' +
                        'font-size:0.9rem;">View in Account</a>';
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