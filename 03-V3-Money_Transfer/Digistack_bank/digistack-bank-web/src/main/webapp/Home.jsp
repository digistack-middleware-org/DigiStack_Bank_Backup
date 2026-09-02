<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${bankName} — Your Trusted Banking Partner</title>

    <!-- Bootstrap 5 — loaded from CDN (no local install needed) -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css"
          rel="stylesheet">
    <!-- Bootstrap Icons -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css"
          rel="stylesheet">

    <style>
        /* ── DigiStack Brand Colours ── */
        :root {
            --db-navy: #0b2545;
            --db-gold: #c9a227;
            --db-gold-light: #e8c547;
            --db-light-bg: #f4f7fb;
        }

        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background-color: var(--db-light-bg);
            color: #333;
        }

        /* ── Animations ── */
        @keyframes fadeInDown {
            from { opacity: 0; transform: translateY(-30px); }
            to   { opacity: 1; transform: translateY(0); }
        }
        @keyframes fadeInUp {
            from { opacity: 0; transform: translateY(30px); }
            to   { opacity: 1; transform: translateY(0); }
        }
        @keyframes fadeIn {
            from { opacity: 0; }
            to   { opacity: 1; }
        }

        /* ── Navbar ── */
        .navbar-brand-text {
            font-size: 1.5rem;
            font-weight: 700;
            color: var(--db-gold) !important;
            letter-spacing: 1px;
        }
        .dsb-navbar {
            background-color: var(--db-navy);
            box-shadow: 0 2px 8px rgba(0,0,0,0.3);
        }
        .dsb-navbar .nav-link {
            color: rgba(255,255,255,0.85) !important;
            font-weight: 500;
            transition: color 0.2s;
        }
        .dsb-navbar .nav-link:hover { color: var(--db-gold) !important; }

        .btn-login-nav {
            background-color: var(--db-gold);
            color: var(--db-navy);
            font-weight: 700;
            border: none;
            padding: 6px 20px;
            border-radius: 6px;
            transition: background-color 0.2s, transform 0.1s;
        }
        .btn-login-nav:hover {
            background-color: var(--db-gold-light);
            transform: translateY(-1px);
        }

        /* ── Hero Section ── */
        .hero-section {
            background: linear-gradient(135deg, var(--db-navy) 0%, #1a3a6b 100%);
            color: white;
            padding: 100px 0 80px;
            position: relative;
            overflow: hidden;
        }
        .hero-section::before {
            content: '';
            position: absolute;
            top: -50%;
            right: -10%;
            width: 500px;
            height: 500px;
            background: rgba(201,162,39,0.08);
            border-radius: 50%;
        }
        .hero-title {
            font-size: 3rem;
            font-weight: 800;
            line-height: 1.2;
            animation: fadeInDown 0.8s ease-out both;
        }
        .hero-title span { color: var(--db-gold); }
        .hero-subtitle {
            font-size: 1.2rem;
            color: rgba(255,255,255,0.8);
            margin-top: 1rem;
            animation: fadeInDown 0.8s ease-out 0.2s both;
        }
        .hero-buttons {
            margin-top: 2rem;
            animation: fadeInUp 0.8s ease-out 0.4s both;
        }
        .btn-hero-primary {
            background-color: var(--db-gold);
            color: var(--db-navy);
            font-weight: 700;
            padding: 14px 36px;
            border: none;
            border-radius: 8px;
            font-size: 1rem;
            transition: background-color 0.2s, transform 0.15s;
            text-decoration: none;
        }
        .btn-hero-primary:hover {
            background-color: var(--db-gold-light);
            transform: translateY(-2px);
            color: var(--db-navy);
        }
        .btn-hero-outline {
            background-color: transparent;
            color: white;
            font-weight: 600;
            padding: 13px 36px;
            border: 2px solid rgba(255,255,255,0.5);
            border-radius: 8px;
            font-size: 1rem;
            transition: border-color 0.2s, background-color 0.2s;
            text-decoration: none;
            cursor: not-allowed;
            opacity: 0.7;
        }
        .hero-badge {
            display: inline-block;
            background-color: rgba(201,162,39,0.15);
            border: 1px solid var(--db-gold);
            color: var(--db-gold);
            padding: 4px 14px;
            border-radius: 20px;
            font-size: 0.8rem;
            font-weight: 600;
            margin-bottom: 1rem;
            animation: fadeInDown 0.8s ease-out both;
        }

        /* ── DB Status Banner (Sprint 3 wires this live) ── */
        .db-status-bar {
            background-color: rgba(255,255,255,0.07);
            border-top: 1px solid rgba(255,255,255,0.1);
            padding: 10px 0;
            animation: fadeIn 1s ease-out 0.8s both;
        }
        .db-status-pill {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            background-color: rgba(255,255,255,0.1);
            border-radius: 20px;
            padding: 5px 16px;
            font-size: 0.85rem;
            color: rgba(255,255,255,0.85);
        }
        .status-dot {
            width: 8px; height: 8px;
            background-color: #28a745;
            border-radius: 50%;
            animation: pulse 1.5s infinite;
        }
        @keyframes pulse {
            0%,100% { opacity: 1; }
            50%      { opacity: 0.4; }
        }

        /* ── Feature Cards ── */
        .features-section {
            padding: 80px 0;
            background-color: var(--db-light-bg);
        }
        .section-title {
            font-size: 2rem;
            font-weight: 700;
            color: var(--db-navy);
            text-align: center;
            margin-bottom: 0.5rem;
        }
        .section-subtitle {
            text-align: center;
            color: #666;
            margin-bottom: 3rem;
        }
        .feature-card {
            background: white;
            border-radius: 16px;
            padding: 36px 28px;
            text-align: center;
            box-shadow: 0 4px 20px rgba(0,0,0,0.06);
            transition: transform 0.25s, box-shadow 0.25s;
            height: 100%;
            animation: fadeInUp 0.7s ease-out both;
        }
        .feature-card:hover {
            transform: translateY(-6px);
            box-shadow: 0 12px 32px rgba(11,37,69,0.12);
        }
        .feature-icon {
            width: 70px; height: 70px;
            background: linear-gradient(135deg, var(--db-navy), #1a3a6b);
            border-radius: 18px;
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0 auto 1.2rem;
        }
        .feature-icon i {
            font-size: 1.8rem;
            color: var(--db-gold);
        }
        .feature-card h5 {
            font-weight: 700;
            color: var(--db-navy);
            margin-bottom: 0.6rem;
        }
        .feature-card p {
            color: #666;
            font-size: 0.92rem;
            line-height: 1.6;
        }
        .feature-link {
            color: var(--db-navy);
            font-weight: 600;
            font-size: 0.9rem;
            text-decoration: none;
            opacity: 0.5;
            cursor: not-allowed;
        }

        /* ── Stats Bar ── */
        .stats-section {
            background-color: var(--db-navy);
            padding: 50px 0;
            color: white;
        }
        .stat-item { text-align: center; }
        .stat-number {
            font-size: 2.5rem;
            font-weight: 800;
            color: var(--db-gold);
            line-height: 1;
        }
        .stat-label {
            font-size: 0.9rem;
            color: rgba(255,255,255,0.7);
            margin-top: 6px;
        }

        /* ── Footer ── */
        .dsb-footer {
            background-color: #0a1f3d;
            color: rgba(255,255,255,0.6);
            padding: 30px 0;
            font-size: 0.85rem;
            text-align: center;
        }
        .dsb-footer strong { color: var(--db-gold); }
    </style>
</head>
<body>

<!-- ═══════════════════════════════════════════
     NAVBAR
═══════════════════════════════════════════ -->
<nav class="navbar navbar-expand-lg dsb-navbar sticky-top">
    <div class="container">
        <a class="navbar-brand d-flex align-items-center gap-2" href="#">
            <i class="bi bi-bank2" style="color:var(--db-gold);font-size:1.6rem;"></i>
            <span class="navbar-brand-text">DigiStack Bank</span>
        </a>

        <button class="navbar-toggler border-0" type="button"
                data-bs-toggle="collapse" data-bs-target="#navMain">
            <i class="bi bi-list text-white fs-4"></i>
        </button>

        <div class="collapse navbar-collapse" id="navMain">
            <ul class="navbar-nav me-auto mb-2 mb-lg-0 ms-4">
                <li class="nav-item">
                    <span class="nav-link" style="opacity:0.5;cursor:not-allowed;">
                        Personal <small class="text-warning">(coming soon)</small>
                    </span>
                </li>
                <li class="nav-item">
                    <span class="nav-link" style="opacity:0.5;cursor:not-allowed;">
                        Business <small class="text-warning">(coming soon)</small>
                    </span>
                </li>
                <li class="nav-item">
                    <span class="nav-link" style="opacity:0.5;cursor:not-allowed;">
                        Support
                    </span>
                </li>
            </ul>
            <div class="d-flex gap-2 align-items-center">
                <a href="Login.jsp" class="btn btn-login-nav">
                    <i class="bi bi-person-circle me-1"></i>Login
                </a>
                <span class="btn btn-outline-secondary btn-sm disabled"
                      style="opacity:0.5;cursor:not-allowed;color:rgba(255,255,255,0.6);border-color:rgba(255,255,255,0.2);">
                    Open an Account <small>(coming soon)</small>
                </span>
            </div>
        </div>
    </div>
</nav>

<!-- ═══════════════════════════════════════════
     HERO SECTION
═══════════════════════════════════════════ -->
<section class="hero-section">
    <div class="container">
        <div class="row align-items-center">
            <div class="col-lg-7">
                <div class="hero-badge">
                    <i class="bi bi-shield-check me-1"></i>
                    Secure · Reliable · Enterprise-Grade
                </div>
                <h1 class="hero-title">
                    Banking Built for<br>
                    <span>Your Future</span>
                </h1>
                <p class="hero-subtitle">
                    Manage your accounts, transfer funds, and track transactions —
                    all from one secure, modern banking platform.
                </p>
                <div class="hero-buttons d-flex flex-wrap gap-3">
                    <a href="Login.jsp" class="btn-hero-primary">
                        <i class="bi bi-box-arrow-in-right me-2"></i>Login to NetBanking
                    </a>
                    <span class="btn-hero-outline">
                        Open an Account &nbsp;<small>(coming soon)</small>
                    </span>
                </div>
            </div>
            <div class="col-lg-5 d-none d-lg-flex justify-content-center">
                <i class="bi bi-bank2"
                   style="font-size:10rem;color:rgba(201,162,39,0.25);"></i>
            </div>
        </div>
    </div>

    <!-- DB connectivity status bar — Sprint 3 replaces placeholder text with live value -->
    <div class="db-status-bar mt-5">
        <div class="container">
            <div class="db-status-pill">
                <span class="status-dot"></span>
                System Status: &nbsp;<strong>${systemStatus}</strong>
                &nbsp;|&nbsp;
                <i class="bi bi-database me-1"></i>
                Database: <strong id="dbStatus"
                    style="color: ${dbConnStatus == 'Connected' ? '#6fff9e' : '#ff6b6b'};">
                    ${dbConnStatus}
                </strong>
            </div>
        </div>
    </div>
</section>

<!-- ═══════════════════════════════════════════
     STATS BAR
═══════════════════════════════════════════ -->
<section class="stats-section">
    <div class="container">
        <div class="row g-4 text-center">
            <div class="col-6 col-md-3 stat-item">
                <div class="stat-number">2M+</div>
                <div class="stat-label">Customers Served</div>
            </div>
            <div class="col-6 col-md-3 stat-item">
                <div class="stat-number">₹500Cr+</div>
                <div class="stat-label">Transactions Processed</div>
            </div>
            <div class="col-6 col-md-3 stat-item">
                <div class="stat-number">99.9%</div>
                <div class="stat-label">System Uptime</div>
            </div>
            <div class="col-6 col-md-3 stat-item">
                <div class="stat-number">256-bit</div>
                <div class="stat-label">SSL Encryption</div>
            </div>
        </div>
    </div>
</section>

<!-- ═══════════════════════════════════════════
     FEATURE TILES
═══════════════════════════════════════════ -->
<section class="features-section">
    <div class="container">
        <h2 class="section-title">Everything You Need</h2>
        <p class="section-subtitle">
            Powerful banking features designed around your needs
        </p>
        <div class="row g-4">

            <div class="col-md-6 col-lg-3">
                <div class="feature-card" style="animation-delay:0.1s;">
                    <div class="feature-icon">
                        <i class="bi bi-person-badge"></i>
                    </div>
                    <h5>My Accounts</h5>
                    <p>View balances, account details, and full transaction history in real time.</p>
                    <a href="Login.jsp" class="feature-link" style="opacity:1;cursor:pointer;text-decoration:underline;">
                        Login to view →
                    </a>
                </div>
            </div>

            <div class="col-md-6 col-lg-3">
                <div class="feature-card" style="animation-delay:0.2s;">
                    <div class="feature-icon">
                        <i class="bi bi-arrow-left-right"></i>
                    </div>
                    <h5>Fund Transfer</h5>
                    <p>Transfer funds instantly between accounts. NEFT, IMPS, and RTGS supported.</p>
                    <span class="feature-link">Coming soon — v15</span>
                </div>
            </div>

            <div class="col-md-6 col-lg-3">
                <div class="feature-card" style="animation-delay:0.3s;">
                    <div class="feature-icon">
                        <i class="bi bi-file-earmark-text"></i>
                    </div>
                    <h5>Statements</h5>
                    <p>Download account statements in PDF or CSV format for any date range.</p>
                    <span class="feature-link">Coming soon — v16</span>
                </div>
            </div>

            <div class="col-md-6 col-lg-3">
                <div class="feature-card" style="animation-delay:0.4s;">
                    <div class="feature-icon">
                        <i class="bi bi-credit-card-2-front"></i>
                    </div>
                    <h5>Cards</h5>
                    <p>Manage your debit and credit cards — activate, block, or set limits.</p>
                    <span class="feature-link">Coming soon — v28</span>
                </div>
            </div>

        </div>
    </div>
</section>

<!-- ═══════════════════════════════════════════
     FOOTER
═══════════════════════════════════════════ -->
<footer class="dsb-footer">
    <div class="container">
        <p class="mb-1">
            <strong>DigiStack Bank</strong> &mdash;
            A WebSphere ND Administration Learning Project
        </p>
        <p class="mb-0" style="font-size:0.78rem;">
            &copy; 2026 DigiStack Bank. For educational purposes only.
            &nbsp;|&nbsp; WebSphere ND 9.0.5.28
            &nbsp;|&nbsp; v1 — Foundation
        </p>
    </div>
</footer>

<!-- Bootstrap 5 JS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>

</body>
</html>