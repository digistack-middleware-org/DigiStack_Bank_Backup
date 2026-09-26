# ═══════════════════════════════════════════════════════════════
# freezeAccount.py — DigiStack Bank P01 v6 Sprint 4
#
# Purpose:
#   Freezes or unfreezes a bank account directly via JDBC,
#   bypassing the application UI entirely. This is the "admin
#   ops path" referenced in AccountDao.java's freeze()/unfreeze()
#   comments.
#
# Usage (from wsadmin Jython shell):
#   Run this script with two arguments: an account number and
#   an action ("freeze" or "unfreeze").
#
#   Example:
#     wsadmin.sh -lang jython -f freezeAccount.py DSB0000000001 freeze
#     wsadmin.sh -lang jython -f freezeAccount.py DSB0000000001 unfreeze
#
# What this proves for Sprint 4:
#   An administrator can freeze/unfreeze an account without the
#   application being deployed, without a customer session, and
#   without any Java code changes — pure operational control.
#
# TECHNICAL DEBT (v6, matches AccountService/DepositService/etc.):
#   Hardcoded JDBC credentials. Replaced at v7 with JNDI DataSource.
#   A wsadmin script cannot use a JNDI DataSource the same way an
#   application does, so at v7 this script will instead read
#   credentials from a WAS J2C Authentication Alias via AdminTask —
#   noted here as a forward-looking improvement, not implemented yet.
# ═══════════════════════════════════════════════════════════════

import sys
import java.sql.DriverManager as DriverManager
import java.lang.Class as Class

# ── JDBC connection details (matches AccountDao.java v6) ──
JDBC_URL      = "jdbc:postgresql://192.168.10.30:5432/digistack_bank"
JDBC_USER     = "digistack_app"
JDBC_PASSWORD = "Wasadmin@951951"

# ── SQL statements — identical logic to AccountDao.freeze()/unfreeze() ──
SQL_FREEZE = (
    "UPDATE accounts "
    "SET is_frozen = TRUE, updated_at = NOW() "
    "WHERE account_number = ?"
)

SQL_UNFREEZE = (
    "UPDATE accounts "
    "SET is_frozen = FALSE, updated_at = NOW() "
    "WHERE account_number = ?"
)

SQL_CHECK_STATUS = (
    "SELECT id, account_number, is_frozen "
    "FROM accounts "
    "WHERE account_number = ?"
)


def freezeOrUnfreezeAccount(accountNumber, action):
    """
    Connects to PostgreSQL and freezes or unfreezes the given
    account by account_number.

    accountNumber: the account_number string, e.g. "DSB0000000001"
    action:        either "freeze" or "unfreeze"

    Prints a clear success or failure message to the wsadmin console.
    """

    # Validate the action argument before touching the database
    if action != "freeze" and action != "unfreeze":
        print "ERROR: action must be 'freeze' or 'unfreeze'. Got: " + action
        return

    # Load the PostgreSQL JDBC driver — same driver class the
    # application itself uses, loaded here inside the DMgr JVM.
    try:
        Class.forName("org.postgresql.Driver")
    except Exception, e:
        print "ERROR: PostgreSQL JDBC driver not found on DMgr classpath."
        print "Copy postgresql-42.7.3.jar to " + \
              "/apps/IBM/WebSphere/AppServer/lib/ext/jdbc/ and retry."
        print "Underlying error: " + str(e)
        return

    conn = None
    try:
        conn = DriverManager.getConnection(
            JDBC_URL, JDBC_USER, JDBC_PASSWORD)

        # ── Step 1: Check the account exists and show current status ──
        checkStmt = conn.prepareStatement(SQL_CHECK_STATUS)
        checkStmt.setString(1, accountNumber)
        rs = checkStmt.executeQuery()

        if not rs.next():
            print "ERROR: No account found with account_number = " + \
                  accountNumber
            checkStmt.close()
            return

        currentlyFrozen = rs.getBoolean("is_frozen")
        print "Account found: " + accountNumber
        print "Current status: " + \
              ("FROZEN" if currentlyFrozen else "ACTIVE")
        checkStmt.close()

        # ── Step 2: Guard against redundant operations ──
        # Mirrors the same business rule in FreezeService.java —
        # freezing an already-frozen account, or unfreezing an
        # already-active one, is rejected with a clear message.
        if action == "freeze" and currentlyFrozen:
            print "NO ACTION: Account is already frozen."
            return

        if action == "unfreeze" and not currentlyFrozen:
            print "NO ACTION: Account is already active (not frozen)."
            return

        # ── Step 3: Execute the freeze or unfreeze ──
        if action == "freeze":
            sql = SQL_FREEZE
        else:
            sql = SQL_UNFREEZE

        updateStmt = conn.prepareStatement(sql)
        updateStmt.setString(1, accountNumber)
        rowsAffected = updateStmt.executeUpdate()
        updateStmt.close()

        if rowsAffected == 1:
            print "SUCCESS: Account " + accountNumber + \
                  " has been " + action.upper() + "D."
        else:
            print "WARNING: Update affected " + str(rowsAffected) + \
                  " rows (expected 1). Please verify manually."

    except Exception, e:
        print "ERROR: Database operation failed."
        print "Underlying error: " + str(e)

    finally:
        if conn is not None:
            conn.close()
            print "Database connection closed."


# ═══════════════════════════════════════════════════════════════
# SCRIPT ENTRY POINT
# ═══════════════════════════════════════════════════════════════
# sys.argv[0] is the script name itself when run with -f.
# sys.argv[1] = account number
# sys.argv[2] = action ("freeze" or "unfreeze")

if len(sys.argv) < 2:
    print "USAGE: wsadmin.sh -lang jython -f freezeAccount.py " + \
          "<account_number> <freeze|unfreeze>"
else:
    accountNumberArg = sys.argv[0]
    actionArg        = sys.argv[1]
    freezeOrUnfreezeAccount(accountNumberArg, actionArg)