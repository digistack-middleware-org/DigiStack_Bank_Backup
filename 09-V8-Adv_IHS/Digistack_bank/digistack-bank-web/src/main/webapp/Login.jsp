<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login — DigiStack Bank</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css"
          rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css"
          rel="stylesheet">
    <link rel="stylesheet" href="css/common/common.css">
    <link rel="stylesheet" href="css/pages/login.css">
    <link rel="stylesheet"
          href="http://192.168.10.20/static/css/ihs-brand.css">
</head>
<body>

<!-- ── IHS brand banner ── -->
<div class="ihs-brand-banner">
    <img src="http://192.168.10.20/static/images/digistack-logo.svg"
         alt="DigiStack Bank"
         class="ihs-banner-logo">
    Secured by <strong>IBM WebSphere ND 9.0.5.28</strong>
    <span class="ihs-badge">IHS v9.0.5.28</span>
</div>

<!-- ── Navbar ── -->
<nav class="navbar dsb-navbar">
    <div class="container">
        <a class="navbar-brand d-flex align-items-center gap-2"
           href="Home">
            <i class="bi bi-bank2 icon-gold icon-nav-brand"></i>
            <span class="navbar-brand-text">DigiStack Bank</span>
        </a>
        <span class="secure-login-label">
            <i class="bi bi-shield-lock me-1"></i>Secure Login
        </span>
    </div>
</nav>

<!-- ── Login Card ── -->
<div class="login-wrapper">
    <div class="login-card">

        <div class="login-logo">
            <i class="bi bi-person-circle"></i>
        </div>
        <h1 class="login-title">Welcome Back</h1>
        <p class="login-subtitle">
            Sign in to your DigiStack Bank account
        </p>

        <%-- Show error message if LoginServlet set one --%>
        <%
            String errorMsg = (String) request.getAttribute("errorMessage");
            if (errorMsg == null) {
                errorMsg = (String) session.getAttribute("loginError");
                if (errorMsg != null) {
                    session.removeAttribute("loginError");
                }
            }
        %>
        <% if (errorMsg != null && !errorMsg.isEmpty()) { %>
        <div class="alert-login-error">
            <i class="bi bi-exclamation-circle-fill"></i>
            <%= errorMsg %>
        </div>
        <% } %>

        <%-- Login Form --%>
        <form action="Login" method="post" autocomplete="off">

            <div class="mb-3">
                <label for="username" class="form-label">Username</label>
                <div class="input-group">
                    <span class="input-group-text">
                        <i class="bi bi-person"></i>
                    </span>
                    <input type="text"
                           class="form-control"
                           id="username"
                           name="username"
                           placeholder="Enter your username"
                           required
                           autofocus>
                </div>
            </div>

            <div class="mb-1">
                <label for="password" class="form-label">Password</label>
                <div class="input-group">
                    <span class="input-group-text">
                        <i class="bi bi-lock"></i>
                    </span>
                    <input type="password"
                           class="form-control"
                           id="password"
                           name="password"
                           placeholder="Enter your password"
                           required>
                </div>
            </div>

            <div class="helper-links mb-3">
                <span class="helper-link">
                    <i class="bi bi-lock-fill me-1"></i>
                    Forgot Password? <small>(coming soon)</small>
                </span>
                <span class="helper-link">
                    Unlock User <small>(coming soon — v29)</small>
                </span>
            </div>

            <button type="submit" class="btn-login">
                <i class="bi bi-box-arrow-in-right me-2"></i>
                Sign In
            </button>
        </form>

        <div class="login-divider">or</div>

        <div class="open-account-text">
            New to DigiStack Bank?
            <span class="open-account-disabled">
                Open an Account <small>(coming soon)</small>
            </span>
        </div>

        <div class="security-badge">
            <i class="bi bi-shield-check-fill icon-green"></i>
            256-bit SSL Encrypted &nbsp;·&nbsp;
            Secured by WAS ND 9.0.5.28
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