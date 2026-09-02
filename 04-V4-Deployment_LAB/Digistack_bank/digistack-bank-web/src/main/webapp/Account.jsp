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
            font-size: 1.3rem;
            font-weight: 700;
            color: var(--db-gold);
            letter-spacing: 1px;
            text-decoration: none;
        }
        .btn-logout {
            background-color: transparent;
            border: 1.5px solid rgba(255,255,255,0.3);
            color: rgba(255,255,255,0.85);
            font-size: 0.82rem;
            padding: 5px 14px;
            border-radius: 6px;
            text-decoration: none;
            transition: background-color 0.2s;
        }
        .btn-logout:hover {
            background-color: rgba(255,255,255,0.1);
            color: white;
        }

        /* ── Page Header ── */
        .page-header {
            background: linear-gradient(
                135deg, var(--db-navy) 0%, #1a3a6b 100%);
            color: white;
            padding: 36px 0 28px;
        }
        .page-header h1 {
            font-size: 1.6rem;
            font-weight: 700;
            margin-bottom: 4px;
        }
        .page-header p {
            font-size: 0.88rem;
            color: rgba(255,255,255,0.65);
            margin-bottom: 0;
        }
        .breadcrumb-nav a {
            color: var(--db-gold);
            text-decoration: none;
            font-size: 0.82rem;
        }
        .breadcrumb-nav span {
            color: rgba(255,255,255,0.45);
            font-size: 0.82rem;
        }

        /* ── Main Content ── */
        .main-content {
            flex: 1;
            padding: 32px 0 48px;
        }

        /* ── Account Summary Card ── */
        .account-summary-card {
            background: linear-gradient(
                135deg, var(--db-navy) 0%, #1a3a6b 100%);
            border-radius: 18px;
            padding: 28px 30px;
            color: white;
            position: relative;
            overflow: hidden;
            box-shadow: 0 8px 28px rgba(11,37,69,0.18);
            animation: fadeInUp 0.5s ease-out both;
            margin-bottom: 28px;
        }
        .account-summary-card::before {
            content: '';
            position: absolute;
            top: -40px; right: -40px;
            width: 180px; height: 180px;
            background: rgba(201,162,39,0.08);
            border-radius: 50%;
        }
        .acc-type-label {
            font-size: 0.75rem;
            text-transform: uppercase;
            letter-spacing: 1.5px;
            color: rgba(255,255,255,0.55);
            margin-bottom: 4px;
        }
        .acc-number {
            font-family: monospace;
            font-size: 0.9rem;
            color: rgba(255,255,255,0.6);
            margin-bottom: 20px;
        }
        .balance-section { margin-bottom: 8px; }
        .balance-label-sm {
            font-size: 0.75rem;
            text-transform: uppercase;
            letter-spacing: 1px;
            color: rgba(255,255,255,0.5);
            margin-bottom: 4px;
        }
        .balance-amount {
            font-size: 2.2rem;
            font-weight: 800;
            color: var(--db-gold);
        }
        .balance-hidden-dots {
            font-size: 1.8rem;
            letter-spacing: 6px;
            color: rgba(255,255,255,0.35);
        }
        .btn-toggle-balance {
            background: rgba(255,255,255,0.1);
            border: 1px solid rgba(255,255,255,0.25);
            color: white;
            font-size: 0.8rem;
            padding: 5px 14px;
            border-radius: 6px;
            cursor: pointer;
            margin-top: 10px;
            transition: background-color 0.2s;
        }
        .btn-toggle-balance:hover {
            background: rgba(255,255,255,0.2);
        }
        .frozen-chip {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            background: rgba(255,193,7,0.15);
            border: 1px solid rgba(255,193,7,0.4);
            color: #ffc107;
            font-size: 0.78rem;
            font-weight: 600;
            padding: 4px 12px;
            border-radius: 20px;
            margin-top: 12px;
        }

        /* ── Transaction Form Card ── */
        .form-card {
            background: white;
            border-radius: 16px;
            padding: 28px 28px;
            box-shadow: 0 2px 12px rgba(0,0,0,0.06);
            animation: fadeInUp 0.5s ease-out 0.1s both;
            margin-bottom: 20px;
        }
        .form-card-title {
            font-size: 1rem;
            font-weight: 700;
            color: var(--db-navy);
            margin-bottom: 20px;
            display: flex;
            align-items: center;
            gap: 8px;
        }
        .form-label {
            font-weight: 600;
            font-size: 0.85rem;
            color: var(--db-navy);
            margin-bottom: 6px;
        }
        .form-control {
            border: 1.5px solid #dde3ed;
            border-radius: 10px;
            padding: 11px 14px;
            font-size: 0.95rem;
            transition: border-color 0.2s, box-shadow 0.2s;
        }
        .form-control:focus {
            border-color: var(--db-navy);
            box-shadow: 0 0 0 3px rgba(11,37,69,0.08);
        }
        .input-prefix {
            background: var(--db-light-bg);
            border: 1.5px solid #dde3ed;
            border-right: none;
            border-radius: 10px 0 0 10px;
            padding: 11px 14px;
            color: #666;
            font-weight: 600;
        }
        .input-prefix + .form-control {
            border-radius: 0 10px 10px 0;
        }

        /* ── Action Buttons ── */
        .btn-deposit {
            background: linear-gradient(135deg, #1a6b3a, #28a745);
            color: white;
            font-weight: 700;
            padding: 12px 28px;
            border: none;
            border-radius: 10px;
            font-size: 0.95rem;
            transition: opacity 0.2s, transform 0.15s;
            width: 100%;
        }
        .btn-deposit:hover {
            opacity: 0.9;
            transform: translateY(-1px);
            color: white;
        }
        .btn-withdraw {
            background: linear-gradient(135deg, #6b1a1a, #dc3545);
            color: white;
            font-weight: 700;
            padding: 12px 28px;
            border: none;
            border-radius: 10px;
            font-size: 0.95rem;
            transition: opacity 0.2s, transform 0.15s;
            width: 100%;
        }
        .btn-withdraw:hover {
            opacity: 0.9;
            transform: translateY(-1px);
            color: white;
        }

        /* ── Alert Banners ── */
        .alert-success-custom {
            background: #f0fdf4;
            border: 1px solid #86efac;
            border-radius: 12px;
            padding: 14px 18px;
            color: #166534;
            font-size: 0.9rem;
            display: flex;
            align-items: flex-start;
            gap: 10px;
            animation: slideInDown 0.4s ease-out both;
            margin-bottom: 20px;
        }
        .alert-error-custom {
            background: #fff0f0;
            border: 1px solid #ffcccc;
            border-radius: 12px;
            padding: 14px 18px;
            color: #c0392b;
            font-size: 0.9rem;
            display: flex;
            align-items: flex-start;
            gap: 10px;
            animation: slideInDown 0.4s ease-out both;
            margin-bottom: 20px;
        }

        /* ── Back Link ── */
        .back-link {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            color: var(--db-navy);
            text-decoration: none;
            font-size: 0.88rem;
            font-weight: 600;
            margin-bottom: 20px;
            opacity: 0.7;
            transition: opacity 0.2s;
        }
        .back-link:hover { opacity: 1; color: var(--db-navy); }

        /* ── Quick Amount Buttons ── */
        .quick-amounts {
            display: flex;
            gap: 8px;
            flex-wrap: wrap;
            margin-top: 8px;
        }
        .btn-quick-amount {
            background: var(--db-light-bg);
            border: 1.5px solid #dde3ed;
            border-radius: 8px;
            padding: 5px 14px;
            font-size: 0.8rem;
            font-weight: 600;
            color: var(--db-navy);
            cursor: pointer;
            transition: background-color 0.15s, border-color 0.15s;
        }
        .btn-quick-amount:hover {
            background: #e2e8f4;
            border-color: var(--db-navy);
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
    // Read servlet-set attributes
    Account account = (Account) request.getAttribute("account");
    String loadError = (String) request.getAttribute("loadError");
    String result    = (String) request.getAttribute("result");
    String action    = (String) request.getAttribute("action");
    String amount    = (String) request.getAttribute("amount");
    String error     = (String) request.getAttribute("error");
    String username  = (String) request.getAttribute("username");
    String role      = (String) request.getAttribute("role");
%>

<!-- ── Navbar ── -->
<nav class="navbar dsb-navbar">
    <div class="container">
        <div class="d-flex align-items-center
                    justify-content-between w-100 py-2">
            <a href="Dashboard" class="navbar-brand-text">
                <i class="bi bi-bank2 me-2"
                   style="color:var(--db-gold);"></i>
                DigiStack Bank
            </a>
            <div class="d-flex align-items-center gap-3">
                <span style="color:rgba(255,255,255,0.75);
                             font-size:0.88rem;">
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
        <p>View balance, deposit funds, or make a withdrawal.</p>
    </div>
</div>

<!-- ── Main Content ── -->
<div class="main-content">
    <div class="container">

        <a href="Dashboard" class="back-link">
            <i class="bi bi-arrow-left"></i> Back to Dashboard
        </a>

        <%-- ── Result / Error Banners ── --%>
        <% if ("success".equals(result) && action != null) { %>
        <div class="alert-success-custom">
            <i class="bi bi-check-circle-fill"
               style="font-size:1.2rem;flex-shrink:0;"></i>
            <div>
                <strong>
                    <%= "deposit".equals(action)
                        ? "Deposit Successful"
                        : "Withdrawal Successful" %>
                </strong><br>
                <% if (amount != null && !amount.isEmpty()) { %>
                    ₹<%= amount %> has been
                    <%= "deposit".equals(action)
                        ? "deposited into"
                        : "withdrawn from" %>
                    your account successfully.
                <% } %>
            </div>
        </div>
        <% } %>

        <% if (error != null && !error.isEmpty()) { %>
        <div class="alert-error-custom">
            <i class="bi bi-exclamation-circle-fill"
               style="font-size:1.2rem;flex-shrink:0;"></i>
            <div>
                <strong>Transaction Failed</strong><br>
                <%= error %>
            </div>
        </div>
        <% } %>

        <% if (loadError != null) { %>
        <div class="alert-error-custom">
            <i class="bi bi-exclamation-triangle-fill"
               style="font-size:1.2rem;flex-shrink:0;"></i>
            <div><strong>Account Error</strong><br><%= loadError %></div>
        </div>
        <% } %>

        <div class="row g-4">

            <!-- ── Left: Account Summary ── -->
            <div class="col-lg-5">

                <% if (account != null) { %>
                <div class="account-summary-card">
                    <div class="acc-type-label">
                        <%= account.getAccountType() %> Account
                    </div>
                    <div style="font-size:1.05rem;font-weight:600;
                                margin-bottom:6px;">
                        <%= request.getAttribute("fullName") != null
                            ? request.getAttribute("fullName")
                            : username %>
                    </div>
                    <div class="acc-number">
                        <%= account.getMaskedAccountNumber() %>
                    </div>

                    <div class="balance-section">
                        <div class="balance-label-sm">
                            Available Balance
                        </div>
                        <div id="balHidden"
                             class="balance-hidden-dots">
                            ••••••
                        </div>
                        <div id="balVisible"
                             class="balance-amount"
                             style="display:none;">
                            <%= account.getFormattedBalance() %>
                        </div>
                    </div>

                    <button class="btn-toggle-balance"
                            id="balBtn"
                            onclick="toggleBal()">
                        <i class="bi bi-eye me-1"></i>View Balance
                    </button>

                    <% if (account.isFrozen()) { %>
                    <div class="frozen-chip">
                        <i class="bi bi-lock-fill"></i>
                        Account Frozen — Contact Support
                    </div>
                    <% } %>
                </div>
                <% } %>

                <!-- Account Details Panel -->
                <% if (account != null) { %>
                <div class="form-card">
                    <div class="form-card-title">
                        <i class="bi bi-info-circle"
                           style="color:var(--db-navy);"></i>
                        Account Details
                    </div>
                    <table style="width:100%;font-size:0.88rem;">
                        <tr>
                            <td style="color:#888;padding:5px 0;">
                                Account Number
                            </td>
                            <td style="font-weight:600;
                                       color:var(--db-navy);
                                       text-align:right;">
                                <%= account.getMaskedAccountNumber() %>
                            </td>
                        </tr>
                        <tr>
                            <td style="color:#888;padding:5px 0;">
                                Account Type
                            </td>
                            <td style="font-weight:600;
                                       color:var(--db-navy);
                                       text-align:right;">
                                <%= account.getAccountType() %>
                            </td>
                        </tr>
                        <tr>
                            <td style="color:#888;padding:5px 0;">
                                Status
                            </td>
                            <td style="text-align:right;">
                                <% if (account.isFrozen()) { %>
                                    <span style="color:#dc3545;
                                                 font-weight:600;">
                                        Frozen
                                    </span>
                                <% } else { %>
                                    <span style="color:#28a745;
                                                 font-weight:600;">
                                        Active
                                    </span>
                                <% } %>
                            </td>
                        </tr>
                    </table>
                </div>
                <% } %>

            </div>

            <!-- ── Right: Deposit & Withdraw Forms ── -->
            <div class="col-lg-7">

                <%-- Deposit Form --%>
                <div class="form-card">
                    <div class="form-card-title">
                        <i class="bi bi-arrow-down-circle"
                           style="color:#28a745;"></i>
                        Deposit Funds
                    </div>

                    <% boolean frozen = (account != null &&
                                         account.isFrozen()); %>

                    <form action="Account" method="post">
                        <input type="hidden"
                               name="action"
                               value="deposit">

                        <div class="mb-3">
                            <label class="form-label">
                                Amount (₹)
                            </label>
                            <div class="input-group">
                                <span class="input-prefix">₹</span>
                                <input type="number"
                                       class="form-control"
                                       name="amount"
                                       id="depositAmount"
                                       placeholder="0.00"
                                       min="1"
                                       step="0.01"
                                       required
                                       <%= frozen ? "disabled" : "" %>>
                            </div>
                            <div class="quick-amounts">
                                <button type="button"
                                        class="btn-quick-amount"
                                        onclick="setAmount(
                                            'depositAmount', 500)">
                                    ₹500
                                </button>
                                <button type="button"
                                        class="btn-quick-amount"
                                        onclick="setAmount(
                                            'depositAmount', 1000)">
                                    ₹1,000
                                </button>
                                <button type="button"
                                        class="btn-quick-amount"
                                        onclick="setAmount(
                                            'depositAmount', 5000)">
                                    ₹5,000
                                </button>
                                <button type="button"
                                        class="btn-quick-amount"
                                        onclick="setAmount(
                                            'depositAmount', 10000)">
                                    ₹10,000
                                </button>
                            </div>
                        </div>

                        <button type="submit"
                                class="btn-deposit"
                                <%= frozen ? "disabled" : "" %>>
                            <i class="bi bi-arrow-down-circle me-2"></i>
                            Deposit
                        </button>
                    </form>
                </div>

                <%-- Withdraw Form --%>
                <div class="form-card">
                    <div class="form-card-title">
                        <i class="bi bi-arrow-up-circle"
                           style="color:#dc3545;"></i>
                        Withdraw Funds
                    </div>

                    <form action="Account" method="post">
                        <input type="hidden"
                               name="action"
                               value="withdraw">

                        <div class="mb-3">
                            <label class="form-label">
                                Amount (₹)
                            </label>
                            <div class="input-group">
                                <span class="input-prefix">₹</span>
                                <input type="number"
                                       class="form-control"
                                       name="amount"
                                       id="withdrawAmount"
                                       placeholder="0.00"
                                       min="1"
                                       step="0.01"
                                       required
                                       <%= frozen ? "disabled" : "" %>>
                            </div>
                            <div class="quick-amounts">
                                <button type="button"
                                        class="btn-quick-amount"
                                        onclick="setAmount(
                                            'withdrawAmount', 500)">
                                    ₹500
                                </button>
                                <button type="button"
                                        class="btn-quick-amount"
                                        onclick="setAmount(
                                            'withdrawAmount', 1000)">
                                    ₹1,000
                                </button>
                                <button type="button"
                                        class="btn-quick-amount"
                                        onclick="setAmount(
                                            'withdrawAmount', 5000)">
                                    ₹5,000
                                </button>
                                <button type="button"
                                        class="btn-quick-amount"
                                        onclick="setAmount(
                                            'withdrawAmount', 10000)">
                                    ₹10,000
                                </button>
                            </div>
                        </div>

                        <button type="submit"
                                class="btn-withdraw"
                                <%= frozen ? "disabled" : "" %>>
                            <i class="bi bi-arrow-up-circle me-2"></i>
                            Withdraw
                        </button>
                    </form>
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
    // Balance toggle — same pattern as Dashboard
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

    // Quick amount button — sets the input field value
    function setAmount(fieldId, value) {
        var field = document.getElementById(fieldId);
        if (field && !field.disabled) {
            field.value = value;
            field.focus();
        }
    }
</script>

</body>
</html>