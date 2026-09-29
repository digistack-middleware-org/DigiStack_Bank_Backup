<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${bankName} — Your Trusted Banking Partner</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css"
          rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css"
          rel="stylesheet">
    <link rel="stylesheet" href="css/common/common.css">
    <link rel="stylesheet" href="css/pages/home.css">
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

<!-- ═══════════════════════════════════════════
     NAVBAR
═══════════════════════════════════════════ -->
<nav class="navbar navbar-expand-lg dsb-navbar sticky-top">
    <div class="container">
        <a class="navbar-brand d-flex align-items-center gap-2" href="#">
            <i class="bi bi-bank2 icon-gold icon-brand"></i>
            <span class="navbar-brand-text">DigiStack Bank</span>
        </a>

        <button class="navbar-toggler border-0" type="button"
                data-bs-toggle="collapse" data-bs-target="#navMain">
            <i class="bi bi-list text-white fs-4"></i>
        </button>

        <div class="collapse navbar-collapse" id="navMain">
            <ul class="navbar-nav me-auto mb-2 mb-lg-0 ms-4">
                <li class="nav-item">
                    <span class="nav-link nav-link-disabled">
                        Personal <small class="text-warning">(coming soon)</small>
                    </span>
                </li>
                <li class="nav-item">
                    <span class="nav-link nav-link-disabled">
                        Business <small class="text-warning">(coming soon)</small>
                    </span>
                </li>
                <li class="nav-item">
                    <span class="nav-link nav-link-disabled">
                        Support
                    </span>
                </li>
            </ul>
            <div class="d-flex gap-2 align-items-center">
                <a href="Login" class="btn btn-login-nav">
                    <i class="bi bi-person-circle me-1"></i>Login
                </a>
                <span class="btn btn-outline-secondary btn-sm btn-coming-soon">
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
                    Manage your accounts, transfer funds, and track
                    transactions — all from one secure, modern banking
                    platform.
                </p>
                <div class="hero-buttons d-flex flex-wrap gap-3">
                    <a href="Login" class="btn-hero-primary">
                        <i class="bi bi-box-arrow-in-right me-2"></i>
                        Login to NetBanking
                    </a>
                    <span class="btn-hero-outline">
                        Open an Account &nbsp;<small>(coming soon)</small>
                    </span>
                </div>
            </div>
            <div class="col-lg-5 d-none d-lg-flex justify-content-center">
                <i class="bi bi-bank2 icon-hero-graphic"></i>
            </div>
        </div>
    </div>

    <!-- DB status bar -->
    <div class="db-status-bar mt-5">
        <div class="container">
            <div class="db-status-pill">
                <span class="status-dot"></span>
                System Status: &nbsp;<strong>${systemStatus}</strong>
                &nbsp;|&nbsp;
                <i class="bi bi-database me-1"></i>
                <%--
                    NOTE: The inline style below CANNOT be moved to CSS.
                    The colour is dynamic — it depends on the live value
                    of dbConnStatus set by HomeServlet at request time.
                    Static CSS files cannot contain JSP/EL expressions.
                --%>
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
                <div class="feature-card feature-card-delay-1">
                    <div class="feature-icon">
                        <i class="bi bi-person-badge"></i>
                    </div>
                    <h5>My Accounts</h5>
                    <p>View balances, account details, and full
                       transaction history in real time.</p>
                    <a href="Login" class="feature-link feature-link-active">
                        Login to view →
                    </a>
                </div>
            </div>

            <div class="col-md-6 col-lg-3">
                <div class="feature-card feature-card-delay-2">
                    <div class="feature-icon">
                        <i class="bi bi-arrow-left-right"></i>
                    </div>
                    <h5>Fund Transfer</h5>
                    <p>Transfer funds instantly between accounts.
                       NEFT, IMPS, and RTGS supported.</p>
                    <span class="feature-link">Coming soon — v15</span>
                </div>
            </div>

            <div class="col-md-6 col-lg-3">
                <div class="feature-card feature-card-delay-3">
                    <div class="feature-icon">
                        <i class="bi bi-file-earmark-text"></i>
                    </div>
                    <h5>Statements</h5>
                    <p>Download account statements in PDF or CSV
                       format for any date range.</p>
                    <span class="feature-link">Coming soon — v16</span>
                </div>
            </div>

            <div class="col-md-6 col-lg-3">
                <div class="feature-card feature-card-delay-4">
                    <div class="feature-icon">
                        <i class="bi bi-credit-card-2-front"></i>
                    </div>
                    <h5>Cards</h5>
                    <p>Manage your debit and credit cards —
                       activate, block, or set limits.</p>
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
        <p class="mb-0 footer-sub">
            &copy; 2026 DigiStack Bank. For educational purposes only.
            &nbsp;|&nbsp; WebSphere ND 9.0.5.28
            &nbsp;|&nbsp; v8 — IHS Plugin Configuration
        </p>
    </div>
</footer>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>