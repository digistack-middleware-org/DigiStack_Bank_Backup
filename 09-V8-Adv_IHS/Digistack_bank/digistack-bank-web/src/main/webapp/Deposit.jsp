<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ page import="com.digistack.bank.model.Account" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Deposit — DigiStack Bank</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css"
          rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css"
          rel="stylesheet">
    <link rel="stylesheet" href="css/common/common.css">
    <link rel="stylesheet" href="css/pages/deposit.css">
</head>
<body>

<%
    Account account  = (Account) request.getAttribute("account");
    String loadError = (String)  request.getAttribute("loadError");
    String result    = (String)  request.getAttribute("result");
    String amount    = (String)  request.getAttribute("amount");
    String error     = (String)  request.getAttribute("error");
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
            <span> &rsaquo; </span>
            <a href="Account">My Account</a>
            <span> &rsaquo; Deposit</span>
        </div>
        <h1><i class="bi bi-arrow-down-circle me-2"></i>Deposit Funds</h1>
        <p>Add money to your account securely.</p>
    </div>
</div>

<!-- ── Main Content ── -->
<div class="main-content">
    <div class="container">

        <a href="Account" class="back-link">
            <i class="bi bi-arrow-left"></i> Back to My Account
        </a>

        <%-- ── PRG Banners ── --%>
        <% if ("success".equals(result)) { %>
        <div class="alert-success-custom">
            <i class="bi bi-check-circle-fill alert-icon"></i>
            <div>
                <strong>Deposit Successful</strong><br>
                <% if (amount != null && !amount.isEmpty()) { %>
                    ₹<%= amount %> has been deposited
                    into your account.
                <% } %>
            </div>
        </div>
        <% } %>

        <% if (error != null && !error.isEmpty()) { %>
        <div class="alert-error-custom">
            <i class="bi bi-exclamation-circle-fill alert-icon"></i>
            <div>
                <strong>Deposit Failed</strong><br>
                <%= error %>
            </div>
        </div>
        <% } %>

        <% if (loadError != null) { %>
        <div class="alert-error-custom">
            <i class="bi bi-exclamation-triangle-fill alert-icon"></i>
            <div>
                <strong>Account Error</strong><br>
                <%= loadError %>
            </div>
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
                        display:none stays inline — JavaScript toggles
                        it at runtime via element.style.display.
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
                        Account Frozen — Deposit Blocked
                    </div>
                    <% } %>
                </div>
                <% } %>
            </div>

            <!-- ── Right: Deposit Form ── -->
            <div class="col-lg-7">
                <div class="form-card">
                    <div class="form-card-title">
                        <i class="bi bi-arrow-down-circle
                                  icon-deposit-title"></i>
                        Deposit Funds
                    </div>

                    <% if (frozen) { %>
                    <div class="frozen-form-notice">
                        <i class="bi bi-lock-fill me-2"></i>
                        Your account is frozen. Deposits are blocked.
                        <a href="Unfreeze" class="frozen-form-link">
                            Unfreeze your account
                        </a>
                        to continue.
                    </div>
                    <% } %>

                    <form action="Deposit" method="post">

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
                                        onclick="setAmt(500)">
                                    ₹500
                                </button>
                                <button type="button"
                                        class="btn-quick-amount"
                                        onclick="setAmt(1000)">
                                    ₹1,000
                                </button>
                                <button type="button"
                                        class="btn-quick-amount"
                                        onclick="setAmt(5000)">
                                    ₹5,000
                                </button>
                                <button type="button"
                                        class="btn-quick-amount"
                                        onclick="setAmt(10000)">
                                    ₹10,000
                                </button>
                            </div>
                        </div>

                        <button type="submit"
                                class="btn-deposit"
                                <%= frozen ? "disabled" : "" %>>
                            <i class="bi bi-arrow-down-circle me-2"></i>
                            Confirm Deposit
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
        &copy; 2026. For educational purposes only. v8
    </div>
</footer>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script>
    var balShown = false;
    function toggleBal() {
        var h = document.getElementById('balHidden');
        var v = document.getElementById('balVisible');
        var b = document.getElementById('balBtn');
        balShown = !balShown;
        if (balShown) {
            h.style.display = 'none';
            v.style.display = 'block';
            b.innerHTML =
                '<i class="bi bi-eye-slash me-1"></i>Hide Balance';
        } else {
            h.style.display = 'block';
            v.style.display = 'none';
            b.innerHTML =
                '<i class="bi bi-eye me-1"></i>View Balance';
        }
    }
    function setAmt(v) {
        var f = document.getElementById('depositAmount');
        if (f && !f.disabled) { f.value = v; f.focus(); }
    }
</script>
</body>
</html>