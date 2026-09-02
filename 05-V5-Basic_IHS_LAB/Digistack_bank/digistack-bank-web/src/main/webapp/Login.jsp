<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login — DigiStack Bank</title>

    <!-- Bootstrap 5 -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css"
          rel="stylesheet">
    <!-- Bootstrap Icons -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css"
          rel="stylesheet">

    <style>
        :root {
            --db-navy: #0b2545;
            --db-gold: #c9a227;
            --db-gold-light: #e8c547;
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
            from { opacity: 0; transform: translateY(24px); }
            to   { opacity: 1; transform: translateY(0); }
        }

        /* ── Navbar ── */
        .dsb-navbar {
            background-color: var(--db-navy);
            box-shadow: 0 2px 8px rgba(0,0,0,0.3);
        }
        .navbar-brand-text {
            font-size: 1.4rem;
            font-weight: 700;
            color: var(--db-gold) !important;
            letter-spacing: 1px;
        }

        /* ── Login Card ── */
        .login-wrapper {
            flex: 1;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 60px 16px;
        }
        .login-card {
            background: white;
            border-radius: 20px;
            box-shadow: 0 8px 40px rgba(11,37,69,0.12);
            padding: 48px 44px;
            width: 100%;
            max-width: 440px;
            animation: fadeInUp 0.6s ease-out both;
        }
        .login-logo {
            text-align: center;
            margin-bottom: 8px;
        }
        .login-logo i {
            font-size: 3rem;
            color: var(--db-navy);
        }
        .login-title {
            text-align: center;
            font-size: 1.5rem;
            font-weight: 700;
            color: var(--db-navy);
            margin-bottom: 4px;
        }
        .login-subtitle {
            text-align: center;
            font-size: 0.88rem;
            color: #888;
            margin-bottom: 32px;
        }

        /* ── Form Fields ── */
        .form-label {
            font-weight: 600;
            font-size: 0.88rem;
            color: var(--db-navy);
            margin-bottom: 6px;
        }
        .form-control {
            border: 1.5px solid #dde3ed;
            border-radius: 10px;
            padding: 12px 14px;
            font-size: 0.95rem;
            transition: border-color 0.2s, box-shadow 0.2s;
        }
        .form-control:focus {
            border-color: var(--db-navy);
            box-shadow: 0 0 0 3px rgba(11,37,69,0.08);
        }
        .input-group-text {
            background: var(--db-light-bg);
            border: 1.5px solid #dde3ed;
            border-radius: 10px 0 0 10px;
            color: #888;
        }
        .input-group .form-control {
            border-radius: 0 10px 10px 0;
        }

        /* ── Login Button ── */
        .btn-login {
            background: linear-gradient(135deg, var(--db-navy), #1a3a6b);
            color: white;
            font-weight: 700;
            font-size: 1rem;
            padding: 13px;
            border: none;
            border-radius: 10px;
            width: 100%;
            transition: opacity 0.2s, transform 0.15s;
            margin-top: 8px;
        }
        .btn-login:hover {
            opacity: 0.92;
            transform: translateY(-1px);
            color: white;
        }

        /* ── Error Alert ── */
        .alert-login-error {
            background-color: #fff0f0;
            border: 1px solid #ffcccc;
            color: #c0392b;
            border-radius: 10px;
            padding: 12px 16px;
            font-size: 0.9rem;
            display: flex;
            align-items: center;
            gap: 10px;
            margin-bottom: 20px;
            animation: fadeInUp 0.3s ease-out both;
        }

        /* ── Helper Links ── */
        .helper-links {
            display: flex;
            justify-content: space-between;
            margin-top: 6px;
        }
        .helper-link {
            font-size: 0.83rem;
            color: #999;
            text-decoration: none;
            cursor: not-allowed;
        }
        .helper-link:hover { color: #999; }

        /* ── Divider ── */
        .login-divider {
            text-align: center;
            color: #ccc;
            font-size: 0.82rem;
            margin: 24px 0 16px;
            position: relative;
        }
        .login-divider::before,
        .login-divider::after {
            content: '';
            position: absolute;
            top: 50%;
            width: 40%;
            height: 1px;
            background: #eee;
        }
        .login-divider::before { left: 0; }
        .login-divider::after  { right: 0; }

        /* ── Open Account Link ── */
        .open-account-text {
            text-align: center;
            font-size: 0.87rem;
            color: #888;
        }
        .open-account-text span {
            color: #bbb;
            cursor: not-allowed;
            font-weight: 600;
        }

        /* ── Security Badge ── */
        .security-badge {
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 6px;
            margin-top: 28px;
            font-size: 0.78rem;
            color: #aaa;
        }
        .security-badge i { color: #28a745; }

        /* ── Footer ── */
        .dsb-footer {
            background-color: #0a1f3d;
            color: rgba(255,255,255,0.5);
            padding: 18px 0;
            font-size: 0.8rem;
            text-align: center;
        }
        .dsb-footer strong { color: var(--db-gold); }
    </style>
</head>
<body>

<!-- ── Navbar ── -->
<nav class="navbar dsb-navbar">
    <div class="container">
        <a class="navbar-brand d-flex align-items-center gap-2" href="Home">
            <i class="bi bi-bank2"
               style="color:var(--db-gold);font-size:1.5rem;"></i>
            <span class="navbar-brand-text">DigiStack Bank</span>
        </a>
        <span style="color:rgba(255,255,255,0.5);font-size:0.85rem;">
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

        <%-- Login Form — POST to LoginServlet --%>
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
            <span>Open an Account <small>(coming soon)</small></span>
        </div>

        <div class="security-badge">
            <i class="bi bi-shield-check-fill"></i>
            256-bit SSL Encrypted &nbsp;·&nbsp; Secured by WAS ND 9.0.5.28
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

</body>
</html>