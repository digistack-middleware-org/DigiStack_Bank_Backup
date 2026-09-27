<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>XA Transfer Lab — DigiStack Bank</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common/common.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/pages/deposit.css">
</head>
<body>

<nav class="navbar">
    <div class="navbar-brand">DigiStack Bank</div>
    <div class="navbar-user">
        <span class="navbar-user-label">XA Lab — v8.5</span>
        <a href="${pageContext.request.contextPath}/Logout" class="btn btn-secondary btn-sm">Logout</a>
    </div>
</nav>

<div class="page-container">

    <div class="page-header">
        <h1>XA Funds Transfer — WAS Lab</h1>
        <p class="page-subtitle">
            Sprint 2: Prove XA atomicity (rollback both) vs Non-XA orphaned debit.
            Not linked from Dashboard — access directly at /XATransfer.
        </p>
    </div>

    <%-- ═══ SECTION 1: XA TRANSFER FORM ═══════════════════════════════════ --%>

    <%-- XA success message --%>
    <% if (request.getAttribute("successMessage") != null) { %>
    <div class="alert alert-success">
        <span class="icon-green">&#10003;</span>
        <%= request.getAttribute("successMessage") %>
    </div>
    <% } %>

    <%-- XA error / rollback message --%>
    <% if (request.getAttribute("errorMessage") != null) { %>
    <div class="alert alert-error">
        <span class="icon-navy">&#9888;</span>
        <%= request.getAttribute("errorMessage") %>
    </div>
    <% } %>

    <div class="form-card">
        <h2>Test A — XA Transfer (Two-Phase Commit)</h2>
        <p>
            Uses two XA DataSources (jdbc/DebitDS and jdbc/CreditDS) under EJB CMT.
            WAS coordinates both via 2PC. On failure — both roll back.
        </p>

        <form method="POST" action="${pageContext.request.contextPath}/XATransfer">

            <div class="form-group">
                <label for="fromAccountId">From Account ID (Debit — via DEBIT_DS)</label>
                <input type="number" id="fromAccountId" name="fromAccountId"
                       class="form-control" required min="1" placeholder="e.g. 1001">
            </div>

            <div class="form-group">
                <label for="toAccountId">To Account ID (Credit — via CREDIT_DS)</label>
                <input type="number" id="toAccountId" name="toAccountId"
                       class="form-control" required min="1" placeholder="e.g. 1002">
            </div>

            <div class="form-group">
                <label for="amount">Amount (Rs.)</label>
                <input type="number" id="amount" name="amount"
                       class="form-control" required min="0.01" step="0.01"
                       placeholder="e.g. 500.00">
            </div>

            <%-- Sprint 2: force-fail checkbox --%>
            <div class="form-group">
                <label>
                    <input type="checkbox" name="simulateCreditFail">
                    &nbsp;Simulate credit failure (XA rollback test)
                </label>
                <small class="form-hint">
                    Tick this to throw after the debit SQL runs but before the credit SQL.
                    WAS will roll back the debit as well — both balances stay unchanged.
                    Leave unticked for a normal successful transfer.
                </small>
            </div>

            <div class="form-actions">
                <button type="submit" class="btn btn-primary">Execute XA Transfer</button>
                <a href="${pageContext.request.contextPath}/Dashboard"
                   class="btn btn-secondary">Back to Dashboard</a>
            </div>

        </form>
    </div>

    <%-- ═══ SECTION 2: NON-XA CONTROL FORM ══════════════════════════════ --%>

    <%-- Non-XA orphan result --%>
    <% if (request.getAttribute("ctrlOrphanMessage") != null) { %>
    <div class="alert alert-warning">
        <span class="icon-gold">&#9888;</span>
        <strong>ORPHANED DEBIT — NON-XA CONTROL RESULT:</strong><br>
        <%= request.getAttribute("ctrlOrphanMessage") %>
    </div>
    <% } %>

    <%-- Non-XA validation error --%>
    <% if (request.getAttribute("ctrlErrorMessage") != null) { %>
    <div class="alert alert-error">
        <span class="icon-navy">&#9888;</span>
        <%= request.getAttribute("ctrlErrorMessage") %>
    </div>
    <% } %>

    <div class="form-card">
        <h2>Test B — Non-XA Control (Negative Control — Orphaned Debit)</h2>
        <p>
            Uses the existing non-XA DataSource (jdbc/BankDS) with
            <code>autocommit=true</code> and no container transaction
            (<code>NOT_SUPPORTED</code>). The debit commits immediately on
            <code>executeUpdate()</code>. A simulated credit-side failure then
            throws. The debit is permanent — no rollback is possible.
        </p>
        <p>
            <strong>Warning:</strong> This test creates a real balance discrepancy
            in the database. After running it, use the SQL restore step below.
        </p>

        <form method="POST" action="${pageContext.request.contextPath}/XAControl">

            <div class="form-group">
                <label for="ctrlFromAccountId">From Account ID (will be debited permanently)</label>
                <input type="number" id="ctrlFromAccountId" name="ctrlFromAccountId"
                       class="form-control" required min="1" placeholder="e.g. 1001">
            </div>

            <div class="form-group">
                <label for="ctrlToAccountId">To Account ID (will NOT be credited)</label>
                <input type="number" id="ctrlToAccountId" name="ctrlToAccountId"
                       class="form-control" required min="1" placeholder="e.g. 1002">
            </div>

            <div class="form-group">
                <label for="ctrlAmount">Amount (Rs.) — will be permanently lost from source</label>
                <input type="number" id="ctrlAmount" name="ctrlAmount"
                       class="form-control" required min="0.01" step="0.01"
                       placeholder="e.g. 100.00">
                <small class="form-hint">
                    Use a small amount. You must restore it manually afterwards via psql.
                </small>
            </div>

            <div class="form-actions">
                <button type="submit" class="btn btn-primary">Run Non-XA Control Test</button>
            </div>

        </form>
    </div>

</div>

</body>
</html>