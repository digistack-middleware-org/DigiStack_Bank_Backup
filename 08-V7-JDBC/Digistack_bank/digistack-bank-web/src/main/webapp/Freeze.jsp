<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ page import="com.digistack.bank.model.Account" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Freeze Account — DigiStack Bank</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css"
          rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css"
          rel="stylesheet">
    <link rel="stylesheet" href="css/common/common.css">
    <link rel="stylesheet" href="css/pages/freeze.css">
    <style>
        :root {
            --db-navy: #0b2545; --db-gold: #c9a227; --db-light-bg: #f4f7fb;
        }
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background-color: var(--db-light-bg);
            min-height: 100vh; display: flex; flex-direction: column;
        }
        @keyframes fadeInUp {
            from { opacity: 0; transform: translateY(20px); }
            to   { opacity: 1; transform: translateY(0); }
        }
        @keyframes slideInDown {
            from { opacity: 0; transform: translateY(-16px); }
            to   { opacity: 1; transform: translateY(0); }
        }
        .dsb-navbar { background-color: var(--db-navy); box-shadow: 0 2px 8px rgba(0,0,0,0.25); }
        .navbar-brand-text {
            font-size: 1.3rem; font-weight: 700;
            color: var(--db-gold); letter-spacing: 1px; text-decoration: none;
        }
        .btn-logout {
            background-color: transparent; border: 1.5px solid rgba(255,255,255,0.3);
            color: rgba(255,255,255,0.85); font-size: 0.82rem; padding: 5px 14px;
            border-radius: 6px; text-decoration: none; transition: background-color 0.2s;
        }
        .btn-logout:hover { background-color: rgba(255,255,255,0.1); color: white; }
        .page-header {
            background: linear-gradient(135deg, var(--db-navy) 0%, #1a3a6b 100%);
            color: white; padding: 36px 0 28px;
        }
        .page-header h1 { font-size: 1.6rem; font-weight: 700; margin-bottom: 4px; }
        .page-header p  { font-size: 0.88rem; color: rgba(255,255,255,0.65); margin-bottom: 0; }
        .breadcrumb-nav a   { color: var(--db-gold); text-decoration: none; font-size: 0.82rem; }
        .breadcrumb-nav span { color: rgba(255,255,255,0.45); font-size: 0.82rem; }
        .main-content { flex: 1; padding: 32px 0 48px; }
        .status-card {
            background: white; border-radius: 16px; padding: 28px;
            box-shadow: 0 2px 12px rgba(0,0,0,0.06);
            animation: fadeInUp 0.5s ease-out both; margin-bottom: 20px;
        }
        .status-card-title {
            font-size: 1rem; font-weight: 700; color: var(--db-navy);
            margin-bottom: 16px; display: flex; align-items: center; gap: 8px;
        }
        .confirm-card {
            background: white; border-radius: 16px; padding: 28px;
            box-shadow: 0 2px 12px rgba(0,0,0,0.06);
            animation: fadeInUp 0.5s ease-out 0.1s both;
            border-top: 4px solid #e91e63;
        }
        .confirm-card-title {
            font-size: 1rem; font-weight: 700; color: var(--db-navy);
            margin-bottom: 20px; display: flex; align-items: center; gap: 8px;
        }
        .btn-freeze {
            background: linear-gradient(135deg, #880e4f, #e91e63);
            color: white; font-weight: 700; padding: 12px 32px;
            border: none; border-radius: 10px; font-size: 0.95rem;
            transition: opacity 0.2s, transform 0.15s;
        }
        .btn-freeze:hover { opacity: 0.9; transform: translateY(-1px); color: white; }
        .btn-cancel {
            background: var(--db-light-bg); color: var(--db-navy);
            font-weight: 600; padding: 12px 28px;
            border: 1.5px solid #dde3ed; border-radius: 10px;
            font-size: 0.95rem; text-decoration: none;
            transition: background-color 0.2s;
        }
        .btn-cancel:hover { background: #e2e8f4; color: var(--db-navy); }
        .alert-success-custom {
            background: #f0fdf4; border: 1px solid #86efac;
            border-radius: 12px; padding: 14px 18px; color: #166534;
            font-size: 0.9rem; display: flex; align-items: flex-start;
            gap: 10px; animation: slideInDown 0.4s ease-out both; margin-bottom: 20px;
        }
        .alert-error-custom {
            background: #fff0f0; border: 1px solid #ffcccc;
            border-radius: 12px; padding: 14px 18px; color: #c0392b;
            font-size: 0.9rem; display: flex; align-items: flex-start;
            gap: 10px; animation: slideInDown 0.4s ease-out both; margin-bottom: 20px;
        }
        .alert-warning-custom {
            background: #fff3cd; border: 1px solid #ffc107;
            border-radius: 12px; padding: 14px 18px; color: #856404;
            font-size: 0.9rem; display: flex; align-items: flex-start;
            gap: 10px; margin-bottom: 20px;
        }
        .back-link {
            display: inline-flex; align-items: center; gap: 6px;
            color: var(--db-navy); text-decoration: none;
            font-size: 0.88rem; font-weight: 600; margin-bottom: 20px;
            opacity: 0.7; transition: opacity 0.2s;
        }
        .back-link:hover { opacity: 1; color: var(--db-navy); }
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
            <span> &rsaquo; </span>
            <a href="Account">My Account</a>
            <span> &rsaquo; Freeze</span>
        </div>
        <h1><i class="bi bi-lock me-2"></i>Freeze Account</h1>
        <p>Block all deposits and withdrawals on this account.</p>
    </div>
</div>

<!-- ── Main Content ── -->
<div class="main-content">
    <div class="container">
        <div class="row justify-content-center">
            <div class="col-lg-7">

                <a href="Account" class="back-link">
                    <i class="bi bi-arrow-left"></i> Back to My Account
                </a>

                <%-- ── PRG Banners ── --%>
                <% if ("success".equals(result)) { %>
                <div class="alert-success-custom">
                    <i class="bi bi-check-circle-fill"
                       style="font-size:1.2rem;flex-shrink:0;"></i>
                    <div>
                        <strong>Account Frozen Successfully</strong><br>
                        All deposits and withdrawals are now blocked.
                        <a href="Unfreeze"
                           style="color:#166534;font-weight:700;">
                            Unfreeze
                        </a>
                        to restore access.
                    </div>
                </div>
                <% } %>

                <% if (error != null && !error.isEmpty()) { %>
                <div class="alert-error-custom">
                    <i class="bi bi-exclamation-circle-fill"
                       style="font-size:1.2rem;flex-shrink:0;"></i>
                    <div><strong>Freeze Failed</strong><br><%= error %></div>
                </div>
                <% } %>

                <% if (loadError != null) { %>
                <div class="alert-error-custom">
                    <i class="bi bi-exclamation-triangle-fill"
                       style="font-size:1.2rem;flex-shrink:0;"></i>
                    <div><strong>Account Error</strong><br><%= loadError %></div>
                </div>
                <% } %>

                <%-- ── Current Status Card ── --%>
                <% if (account != null) { %>
                <div class="status-card">
                    <div class="status-card-title">
                        <i class="bi bi-shield-check"
                           style="color:var(--db-navy);"></i>
                        Current Account Status
                    </div>
                    <table style="width:100%;font-size:0.88rem;">
                        <tr>
                            <td style="color:#888;padding:6px 0;">Account Number</td>
                            <td style="font-weight:600;color:var(--db-navy);
                                       text-align:right;">
                                <%= account.getMaskedAccountNumber() %>
                            </td>
                        </tr>
                        <tr>
                            <td style="color:#888;padding:6px 0;">Account Type</td>
                            <td style="font-weight:600;color:var(--db-navy);
                                       text-align:right;">
                                <%= account.getAccountType() %>
                            </td>
                        </tr>
                        <tr>
                            <td style="color:#888;padding:6px 0;">Status</td>
                            <td style="text-align:right;">
                                <% if (frozen) { %>
                                    <span style="color:#e91e63;font-weight:700;">
                                        <i class="bi bi-lock-fill me-1"></i>Frozen
                                    </span>
                                <% } else { %>
                                    <span style="color:#28a745;font-weight:700;">
                                        <i class="bi bi-unlock me-1"></i>Active
                                    </span>
                                <% } %>
                            </td>
                        </tr>
                    </table>
                </div>

                <%-- ── Already Frozen — show message, not the form ── --%>
                <% if (frozen) { %>
                <div class="alert-warning-custom">
                    <i class="bi bi-lock-fill"
                       style="font-size:1.2rem;flex-shrink:0;"></i>
                    <div>
                        <strong>Account is already frozen.</strong><br>
                        No action needed. To restore access,
                        <a href="Unfreeze" style="color:#856404;font-weight:700;">
                            Unfreeze your account
                        </a>.
                    </div>
                </div>

                <%-- ── Not Frozen — show freeze confirmation form ── --%>
                <% } else { %>
                <div class="confirm-card">
                    <div class="confirm-card-title">
                        <i class="bi bi-exclamation-triangle"
                           style="color:#e91e63;"></i>
                        Confirm Freeze
                    </div>

                    <p style="font-size:0.9rem;color:#555;margin-bottom:20px;">
                        Freezing your account will <strong>immediately block</strong>
                        all deposits and withdrawals. You can unfreeze at any time
                        from the
                        <a href="Unfreeze" style="color:var(--db-navy);font-weight:600;">
                            Unfreeze page
                        </a>.
                    </p>

                    <%--
                        Hidden accountId — FreezeServlet.doPost() reads this
                        to know which account to freeze. The account is loaded
                        in GET and its ID is embedded here so POST does not
                        need a second DB lookup.
                    --%>
                    <form action="Freeze" method="post">
                        <input type="hidden"
                               name="accountId"
                               value="<%= accountId %>">

                        <div class="d-flex gap-3 flex-wrap">
                            <button type="submit" class="btn-freeze">
                                <i class="bi bi-lock me-2"></i>Freeze Account
                            </button>
                            <a href="Account" class="btn-cancel">
                                <i class="bi bi-x-circle me-2"></i>Cancel
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
        &copy; 2026. For educational purposes only. V7
    </div>
</footer>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>