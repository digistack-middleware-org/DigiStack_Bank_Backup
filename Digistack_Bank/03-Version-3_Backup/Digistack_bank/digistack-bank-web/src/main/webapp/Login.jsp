<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login — DigiStack Bank</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        :root {
            --db-navy: #0b2545;
            --db-blue: #13315c;
            --db-gold: #c9a227;
        }
        body {
            background: linear-gradient(135deg, var(--db-navy) 0%, var(--db-blue) 100%);
            min-height: 100vh;
            display: flex;
            align-items: center;
            font-family: 'Segoe UI', Arial, sans-serif;
        }
        .db-login-card {
            border: none;
            border-radius: 16px;
            box-shadow: 0 12px 40px rgba(0,0,0,0.25);
            opacity: 0;
            animation: fadeInUp 0.6s ease-out forwards;
        }
        .db-brand {
            color: var(--db-navy);
            font-weight: 700;
        }
        .db-brand span {
            color: var(--db-gold);
        }
        .btn-db {
            background-color: var(--db-navy);
            border: none;
        }
        .btn-db:hover {
            background-color: var(--db-blue);
        }
        @keyframes fadeInUp {
            from { opacity: 0; transform: translateY(20px); }
            to { opacity: 1; transform: translateY(0); }
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="row justify-content-center">
            <div class="col-md-5 col-lg-4">
                <div class="card db-login-card p-4">
                    <div class="card-body">
                        <h3 class="text-center db-brand mb-1">DigiStack <span>Bank</span></h3>
                        <p class="text-center text-muted small mb-4">Secure Customer Login</p>

                        <% if (request.getAttribute("errorMessage") != null) { %>
                            <div class="alert alert-danger py-2 small" role="alert">
                                ${errorMessage}
                            </div>
                        <% } %>

                        <form action="${pageContext.request.contextPath}/login" method="post">
                            <div class="mb-3">
                                <label for="username" class="form-label small text-muted">Username</label>
                                <input type="text" class="form-control" id="username" name="username" required autofocus>
                            </div>
                            <div class="mb-4">
                                <label for="password" class="form-label small text-muted">Password</label>
                                <input type="password" class="form-control" id="password" name="password" required>
                            </div>
                            <button type="submit" class="btn btn-db w-100 text-white">Sign In</button>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</body>
</html>