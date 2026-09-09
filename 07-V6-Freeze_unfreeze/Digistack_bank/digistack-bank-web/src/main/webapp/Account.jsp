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
    <style>
        :root {
            --db-navy:     #0b2545;
            --db-gold:     #c9a227;
            --db-gold-light:#e8c547;
            --db-light-bg: #f4f7fb;
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
        @keyframes slideInDown {
            from { opacity: 0; transform: translateY(-16px); }
            to   { opacity: 1; transform: translateY(0); }
        }
        /* ── Navbar ── */
        .dsb-navbar {
            background-color: var(--db-navy);
            box-shadow: 0 2px 8px rgba(0,0,0,0.25);
        }
        .navbar-brand-text {
            font-size: 1.3rem; font-weight: 700;
            color: var(--db-gold); letter-spacing: 1px;
            text-decoration: none;
        }
        .btn-logout {
            background-color: transparent;
            border: 1.5px solid rgba(255,255,255,0.3);
            color: rgba(255,255,255,0.85);
            font-size: 0.82rem; padding: 5px 14px;
            border-radius: 6px; text-decoration: none;
            transition: background-color 0.2s;
        }
        .btn-logout:hover {
            background-color: rgba(255,255,255,0.1); color: white;
        }
        /* ── Page Header ── */
        .page-header {
            background: linear-gradient(135deg, var(--db-navy) 0%, #1a3a6b 100%);
            color: white; padding: 36px 0 28px;
        }
        .page-header h1 {
            font-size: 1.6rem; font-weight: 700; margin-bottom: 4px;
        }
        .page-header p {
            font-size: 0.88rem; color: rgba(255,255,255,0.65); margin-bottom: 0;
        }
        .breadcrumb-nav a {
            color: var(--db-gold); text-decoration: none; font-size: 0.82rem;
        }
        .breadcrumb-nav span { color: rgba(255,255,255,0.45); font-size: 0.82rem; }
        /* ── Main Content ── */
        .main-content { flex: 1; padding: 32px 0 48px; }
        /* ── Account Summary Card ── */
        .account-summary-card {
            background: linear-gradient(135deg, var(--db-navy) 0%, #1a3a6b 100%);
            border-radius: 18px; padding: 28px 30px; color: white;
            position: relative; overflow: hidden;
            box-shadow: 0 8px 28px rgba(11,37,69,0.18);
            animation: fadeInUp 0.5s ease-out both; margin-bottom: 20px;
        }
        .account-summary-card::before {
            content: ''; position: absolute;
            top: -40px; right: -40px;
            width: 180px; height: 180px;
            background: rgba(201,162,39,0.08); border-radius: 50%;
        }
        .acc-type-label {
            font-size: 0.75rem; text-transform: uppercase;
            letter-spacing: 1.5px; color: rgba(255,255,255,0.55); margin-bottom: 4px;
        }
        .acc-number {
            font-family: monospace; font-size: 0.9rem;
            color: rgba(255,255,255,0.6); margin-bottom: 20px;
        }
        .balance-label-sm {
            font-size: 0.75rem; text-transform: uppercase;
            letter-spacing: 1px; color: rgba(255,255,255,0.5); margin-bottom: 4px;
        }
        .balance-amount { font-size: 2.2rem; font-weight: 800; color: var(--db-gold); }
        .balance-hidden-dots {
            font-size: 1.8rem; letter-spacing: 6px; color: rgba(255,255,255,0.35);
        }
        .btn-toggle-balance {
            background: rgba(255,255,255,0.1);
            border: 1px solid rgba(255,255,255,0.25);
            color: white; font-size: 0.8rem; padding: 5px 14px;
            border-radius: 6px; cursor: pointer; margin-top: 10px;
            transition: background-color 0.2s;
        }
        .btn-toggle-balance:hover { background: rgba(255,255,255,0.2); }
        .frozen-chip {
            display: inline-flex; align-items: center; gap: 6px;
            background: rgba(255,193,7,0.15);
            border: 1px solid rgba(255,193,7,0.4);
            color: #ffc107; font-size: 0.78rem; font-weight: 600;
            padding: 4px 12px; border-radius: 20px; margin-top: 12px;
        }
        /* ── Info Card ── */
        .info-card {
            background: white; border-radius: 16px; padding: 24px 26px;
            box-shadow: 0 2px 12px rgba(0,0,0,0.06);
            animation: fadeInUp 0.5s ease-out 0.1s both; margin-bottom: 20px;
        }
        .info-card-title {
            font-size: 1rem; font-weight: 700; color: var(--db-navy);
            margin-bottom: 16px; display: flex; align-items: center; gap: 8px;
        }
        /* ── Quick Action Tiles ── */
        .actions-grid {
            display: grid; grid-template-columns: 1fr 1fr;
            gap: 14px; animation: fadeInUp 0.5s ease-out 0.15s both;
        }
        .action-tile {
            background: white; border-radius: 14px; padding: 22px 20px;
            text-align: center; text-decoration: none;
            box-shadow: 0 2px 12px rgba(0,0,0,0.06);
            transition: transform 0.2s, box-shadow 0.2s;
            display: block;
        }
        .action-tile:hover {
            transform: translateY(-3px);
            box-shadow: 0 6px 20px rgba(11,37,69,0.1);
        }
        .action-tile.tile-disabled {
            opacity: 0.45; pointer-events: none; cursor: not-allowed;
        }
        .tile-icon-wrap {
            width: 48px; height: 48px; border-radius: 12px;
            display: flex; align-items: center; justify-content: center;
            margin: 0 auto 10px;
        }
        .tile-icon-wrap i { font-size: 1.4rem; }
        .tile-name {
            font-size: 0.88rem; font-weight: 700; color: var(--db-navy);
            margin-bottom: 2px;
        }
        .tile-desc { font-size: 0.75rem; color: #999; }
        /* ── Error Banner ── */
        .alert-error-custom {
            background: #fff0f0; border: 1px solid #ffcccc;
            border-radius: 12px; padding: 14px 18px; color: #c0392b;
            font-size: 0.9rem; display: flex; align-items: flex-start;
            gap: 10px; margin-bottom: 20px;
            animation: slideInDown 0.4s ease-out both;
        }
        /* ── Back Link ── */
        .back-link {
            display: inline-flex; align-items: center; gap: 6px;
            color: var(--db-navy); text-decoration: none;
            font-size: 0.88rem; font-weight: 600; margin-bottom: 20px;
            opacity: 0.7; transition: opacity 0.2s;
        }
        .back-link:hover { opacity: 1; color: var(--db-navy); }
        /* ── Footer ── */
        .dsb-footer {
            background-color: #0a1f3d; color: rgba(255,255,255,0.45);
            padding: 16px 0; font-size: 0.78rem; text-align: center;
        }
        .dsb-footer strong { color: var(--db-gold); }
    </style>
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
                <i class="bi bi-bank2 me-2"
                   style="color:var(--db-gold);"></i>DigiStack Bank
            </a>
            <div class="d-flex align-items-center gap-3">
                <span style="color:rgba(255,255,255,0.75);font-size:0.88rem;">
                    <i class="bi bi-person-circle me-1"
                       style="color:var(--db-gold);"></i>
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
            <i class="bi bi-exclamation-triangle-fill"
               style="font-size:1.2rem;flex-shrink:0;"></i>
            <div><strong>Account Error</strong><br><%= loadError %></div>
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
                    <div style="font-size:1.05rem;font-weight:600;
                                margin-bottom:6px;">
                        <%= fullName != null ? fullName : username %>
                    </div>
                    <div class="acc-number">
                        <%= account.getMaskedAccountNumber() %>
                    </div>
                    <div class="balance-label-sm">Available Balance</div>
                    <div id="balHidden" class="balance-hidden-dots">
                        ••••••
                    </div>
                    <div id="balVisible" class="balance-amount"
                         style="display:none;">
                        <%= account.getFormattedBalance() %>
                    </div>
                    <button class="btn-toggle-balance"
                            id="balBtn" onclick="toggleBal()">
                        <i class="bi bi-eye me-1"></i>View Balance
                    </button>
                    <% if (frozen) { %>
                    <div class="frozen-chip">
                        <i class="bi bi-lock-fill"></i>
                        Account Frozen — Contact Support
                    </div>
                    <% } %>
                </div>

                <div class="info-card">
                    <div class="info-card-title">
                        <i class="bi bi-info-circle"
                           style="color:var(--db-navy);"></i>
                        Account Details
                    </div>
                    <table style="width:100%;font-size:0.88rem;">
                        <tr>
                            <td style="color:#888;padding:6px 0;">
                                Account Number
                            </td>
                            <td style="font-weight:600;color:var(--db-navy);
                                       text-align:right;">
                                <%= account.getMaskedAccountNumber() %>
                            </td>
                        </tr>
                        <tr>
                            <td style="color:#888;padding:6px 0;">
                                Account Type
                            </td>
                            <td style="font-weight:600;color:var(--db-navy);
                                       text-align:right;">
                                <%= account.getAccountType() %>
                            </td>
                        </tr>
                        <tr>
                            <td style="color:#888;padding:6px 0;">
                                Status
                            </td>
                            <td style="text-align:right;">
                                <% if (frozen) { %>
                                    <span style="color:#dc3545;font-weight:600;">
                                        Frozen
                                    </span>
                                <% } else { %>
                                    <span style="color:#28a745;font-weight:600;">
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
                        <i class="bi bi-lightning-charge"
                           style="color:var(--db-gold);"></i>
                        Quick Actions
                    </div>

                    <div class="actions-grid">

                        <a href="Deposit"
                           class="action-tile <%= frozen ? "tile-disabled" : "" %>">
                            <div class="tile-icon-wrap"
                                 style="background:#e8f4fd;">
                                <i class="bi bi-arrow-down-circle"
                                   style="color:#2196f3;"></i>
                            </div>
                            <div class="tile-name">Deposit</div>
                            <div class="tile-desc">Add funds to account</div>
                        </a>

                        <a href="Withdraw"
                           class="action-tile <%= frozen ? "tile-disabled" : "" %>">
                            <div class="tile-icon-wrap"
                                 style="background:#fff3e0;">
                                <i class="bi bi-arrow-up-circle"
                                   style="color:#ff9800;"></i>
                            </div>
                            <div class="tile-name">Withdraw</div>
                            <div class="tile-desc">Withdraw funds</div>
                        </a>

                        <a href="Freeze"
                           class="action-tile <%= frozen ? "tile-disabled" : "" %>">
                            <div class="tile-icon-wrap"
                                 style="background:#fce4ec;">
                                <i class="bi bi-lock"
                                   style="color:#e91e63;"></i>
                            </div>
                            <div class="tile-name">Freeze Account</div>
                            <div class="tile-desc">Block all transactions</div>
                        </a>

                        <a href="Unfreeze"
                           class="action-tile <%= !frozen ? "tile-disabled" : "" %>">
                            <div class="tile-icon-wrap"
                                 style="background:#e8f5e9;">
                                <i class="bi bi-unlock"
                                   style="color:#4caf50;"></i>
                            </div>
                            <div class="tile-name">Unfreeze Account</div>
                            <div class="tile-desc">Restore transactions</div>
                        </a>

                    </div>

                    <% if (frozen) { %>
                    <div style="margin-top:16px;padding:12px 16px;
                                background:#fff3cd;border:1px solid #ffc107;
                                border-radius:10px;font-size:0.85rem;
                                color:#856404;">
                        <i class="bi bi-info-circle me-2"></i>
                        Account is frozen. Deposit and Withdraw are
                        disabled. Use
                        <a href="Unfreeze"
                           style="color:#856404;font-weight:700;">
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
        &copy; 2026. For educational purposes only. v6
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