<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Account — DigiStack Bank</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        :root {
            --db-navy: #0b2545;
            --db-blue: #13315c;
            --db-gold: #c9a227;
            --db-bg: #f4f6f9;
        }
        body {
            background-color: var(--db-bg);
            font-family: 'Segoe UI', Arial, sans-serif;
        }
        .db-navbar {
            background: linear-gradient(90deg, var(--db-navy), var(--db-blue));
        }
        .db-navbar .navbar-brand {
            font-weight: 700;
            color: #fff !important;
        }
        .db-navbar .navbar-brand span {
            color: var(--db-gold);
        }
        .db-balance-card {
            background: linear-gradient(135deg, var(--db-navy) 0%, var(--db-blue) 100%);
            color: #fff;
            border: none;
            border-radius: 16px;
            box-shadow: 0 8px 30px rgba(0,0,0,0.15);
            opacity: 0;
            animation: fadeInUp 0.6s ease-out forwards;
        }
        .db-balance-amount {
            font-size: 2.5rem;
            font-weight: 700;
            color: var(--db-gold);
        }
        .db-action-card {
            border: none;
            border-radius: 12px;
            box-shadow: 0 4px 20px rgba(0,0,0,0.08);
            opacity: 0;
            animation: fadeInUp 0.6s ease-out 0.15s forwards;
        }
        .btn-db {
            background-color: var(--db-navy);
            border: none;
            transition: background-color 0.2s ease;
        }
        .btn-db:hover {
            background-color: var(--db-blue);
        }
        .btn-withdraw {
            background-color: #7a1f2b;
            border: none;
            transition: background-color 0.2s ease;
        }
        .btn-withdraw:hover {
            background-color: #93242f;
        }
        @keyframes fadeInUp {
            from { opacity: 0; transform: translateY(16px); }
            to { opacity: 1; transform: translateY(0); }
        }
    </style>
</head>
<body>

    <nav class="navbar navbar-expand-lg db-navbar shadow-sm">
        <div class="container">
            <a class="navbar-brand" href="${pageContext.request.contextPath}/home">DigiStack <span>Bank</span></a>
            <a href="${pageContext.request.contextPath}/logout" class="btn btn-outline-light btn-sm">Logout</a>
        </div>
    </nav>

    <main class="container my-5">
        <div class="row justify-content-center">
            <div class="col-md-8 col-lg-6">

                <% if (request.getAttribute("message") != null) { %>
                    <div class="alert alert-info small">${message}</div>
                <% } %>

                <div class="card db-balance-card mb-4 text-center p-4">
                    <p class="text-uppercase small mb-1" style="letter-spacing:1px; opacity:0.8;">Available Balance</p>
                    <p class="db-balance-amount mb-0">₹${balance}</p>
                </div>

                <div class="card db-action-card p-4">
                    <h5 class="mb-3 text-muted">Deposit / Withdraw</h5>
                    <form action="${pageContext.request.contextPath}/account" method="post">
                        <div class="mb-3">
                            <label for="amount" class="form-label small text-muted">Amount</label>
                            <input type="number" step="0.01" min="0.01" class="form-control" id="amount" name="amount" required>
                        </div>
                        <div class="d-flex gap-2">
                            <button type="submit" name="action" value="deposit" class="btn btn-db text-white w-50">Deposit</button>
                            <button type="submit" name="action" value="withdraw" class="btn btn-withdraw text-white w-50">Withdraw</button>
                        </div>
                    </form>
                </div>

            </div>
        </div>
    </main>

</body>
</html>